package com.tekravio.healthcare.prescription;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tekravio.healthcare.prescription.dto.MedicineCandidate;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.GetDocumentTextDetectionRequest;
import software.amazon.awssdk.services.textract.model.GetDocumentTextDetectionResponse;
import software.amazon.awssdk.services.textract.model.JobStatus;
import software.amazon.awssdk.services.textract.model.S3Object;
import software.amazon.awssdk.services.textract.model.StartDocumentTextDetectionRequest;

@Component
class TextractPrescriptionExtractor implements PrescriptionOcrProvider {

    private static final Pattern DOSAGE_PATTERN = Pattern.compile("(?i)\\b\\d+(?:\\.\\d+)?\\s?(?:mg|mcg|g|ml|iu)\\b");
    private static final Pattern DURATION_PATTERN = Pattern.compile("(?i)\\b(?:for\\s*)?\\d+\\s?(?:day|days|week|weeks|month|months)\\b");

    private final TextractClient textractClient;

    TextractPrescriptionExtractor(TextractClient textractClient) {
        this.textractClient = textractClient;
    }

    @Override
    public String providerName() {
        return "textract";
    }

    @Override
    public OcrExtractionResult extract(String bucket, String key, String contentType) {
        if ("application/pdf".equals(contentType)) {
            return extractPdf(bucket, key);
        }
        return extractImage(bucket, key);
    }

    private OcrExtractionResult extractImage(String bucket, String key) {
        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                .document(Document.builder()
                        .s3Object(S3Object.builder().bucket(bucket).name(key).build())
                        .build())
                .build();

        var response = textractClient.detectDocumentText(request);
        return fromBlocks(response.blocks());
    }

    private OcrExtractionResult extractPdf(String bucket, String key) {
        StartDocumentTextDetectionRequest startRequest = StartDocumentTextDetectionRequest.builder()
                .documentLocation(location -> location.s3Object(S3Object.builder().bucket(bucket).name(key).build()))
                .build();

        String jobId = textractClient.startDocumentTextDetection(startRequest).jobId();
        List<software.amazon.awssdk.services.textract.model.Block> allBlocks = new ArrayList<>();
        String nextToken = null;

        for (int attempt = 0; attempt < 15; attempt++) {
            sleep();
            GetDocumentTextDetectionResponse response = textractClient.getDocumentTextDetection(
                    GetDocumentTextDetectionRequest.builder().jobId(jobId).nextToken(nextToken).build());

            if (response.jobStatus() == JobStatus.SUCCEEDED) {
                allBlocks.addAll(response.blocks());
                nextToken = response.nextToken();
                while (nextToken != null) {
                    response = textractClient.getDocumentTextDetection(
                            GetDocumentTextDetectionRequest.builder().jobId(jobId).nextToken(nextToken).build());
                    allBlocks.addAll(response.blocks());
                    nextToken = response.nextToken();
                }
                return fromBlocks(allBlocks);
            }

            if (response.jobStatus() == JobStatus.FAILED || response.jobStatus() == JobStatus.PARTIAL_SUCCESS) {
                throw new IllegalStateException("Textract PDF OCR did not complete successfully");
            }
        }

        throw new IllegalStateException("Textract PDF OCR timed out");
    }

    private OcrExtractionResult fromBlocks(List<software.amazon.awssdk.services.textract.model.Block> blocks) {
        List<String> lines = blocks.stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .sorted(Comparator.comparing(block -> block.geometry().boundingBox().top()))
                .map(block -> block.text() == null ? "" : block.text().trim())
                .filter(text -> !text.isBlank())
                .toList();

        double minConfidence = blocks.stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .map(block -> block.confidence() == null ? 0.0f : block.confidence())
                .min(Float::compare)
                .orElse(0.0f);

        return new OcrExtractionResult(providerName(), String.join("\n", lines), parseMedicines(lines), minConfidence < 70.0);
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Textract PDF OCR", exception);
        }
    }

    private List<MedicineCandidate> parseMedicines(List<String> lines) {
        List<MedicineCandidate> candidates = new ArrayList<>();
        for (String line : lines) {
            String normalized = line.trim();
            if (normalized.length() < 3 || looksLikeInstructionOnly(normalized)) {
                continue;
            }

            Matcher dosageMatcher = DOSAGE_PATTERN.matcher(normalized);
            String dosage = dosageMatcher.find() ? dosageMatcher.group() : null;
            String duration = find(DURATION_PATTERN, normalized);
            String frequency = inferFrequency(normalized);
            String name = cleanMedicineName(normalized, dosage, duration, frequency);

            if (name.length() >= 3 && (dosage != null || frequency != null || duration != null)) {
                candidates.add(new MedicineCandidate(name, dosage, frequency, duration, BigDecimal.valueOf(75)));
            }
        }
        return candidates.stream().limit(20).toList();
    }

    private boolean looksLikeInstructionOnly(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return lower.contains("patient") || lower.contains("doctor") || lower.contains("hospital") || lower.contains("date");
    }

    private String find(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group() : null;
    }

    private String inferFrequency(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.contains("once") || lower.contains("od") || lower.contains("1-0-0")) {
            return "ONCE_DAILY";
        }
        if (lower.contains("twice") || lower.contains("bd") || lower.contains("1-0-1")) {
            return "TWICE_DAILY";
        }
        if (lower.contains("thrice") || lower.contains("tds") || lower.contains("1-1-1")) {
            return "THRICE_DAILY";
        }
        return null;
    }

    private String cleanMedicineName(String line, String dosage, String duration, String frequency) {
        String cleaned = line;
        if (dosage != null) {
            cleaned = cleaned.replace(dosage, "");
        }
        if (duration != null) {
            cleaned = cleaned.replace(duration, "");
        }
        if (frequency != null) {
            cleaned = cleaned.replaceAll("(?i)once|twice|thrice|od|bd|tds|1-0-0|1-0-1|1-1-1", "");
        }
        return cleaned.replaceAll("(?i)tablet|tab\\.?|capsule|cap\\.?|syrup", "")
                .replaceAll("[^A-Za-z0-9 .-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
