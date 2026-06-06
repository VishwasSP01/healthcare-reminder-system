# OCR Evaluation Plan

The goal is not to blindly replace Textract. The goal is to find the safest OCR flow for prescriptions.

## Current Decision

The backend currently uses AWS Textract because the assignment already includes AWS S3. The code is now provider-ready through:

```text
PrescriptionOcrProvider
PrescriptionOcrRouter
TextractPrescriptionExtractor
OCR_PROVIDER=textract
```

Very simple meaning: Textract is the current OCR engine, but the backend is structured so Gemini OCR or Azure Document Intelligence can be added later without rewriting prescription upload.

## Safety Rule

OCR output must not directly create medicine reminders.

Very simple meaning:

```text
OCR suggests medicines.
User/admin confirms medicines.
Only confirmed medicines become reminders.
```

## Why

During sample testing, Gemini also misread at least one handwritten medicine. That means even a strong OCR/AI model can be wrong on messy prescriptions.

For medical data, one wrong medicine can create the wrong reminder. So the safe design is:

```text
Upload prescription
Run OCR
Store raw extracted text
Show detected medicines
Manual review/correction
Create reminders only from verified medicines
```

## Providers To Compare

- AWS Textract
- Gemini OCR / Gemini Vision
- Azure Document Intelligence

## Test Samples

Use the same files for all providers:

```text
Sample 1 - printed prescription
Sample 2 - table handwritten
Sample 3 - messy handwritten
Sample 4 - clear typed medicine
Sample 5 - very messy handwritten
Sample 6 - handwritten simple
Sample 7 - PDF scan
```

Before uploading, crop or blur patient-identifying details if these are not demo-safe samples.

## Prompt For Gemini

```text
Extract only the medicines from this prescription.

Return in this format:
Medicine name:
Dosage:
Timing/frequency:
Duration:
Confidence: High/Medium/Low

If anything is unclear, write "unclear" instead of guessing.
```

## Evaluation Table

| Sample | Provider | Medicine Names Correct | Dosage Correct | Timing Correct | Wrong/Hallucinated Text | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| Sample 1 | Textract |  |  |  |  |  |
| Sample 1 | Gemini |  |  |  |  |  |
| Sample 1 | Azure DI |  |  |  |  |  |
| Sample 2 | Textract |  |  |  |  |  |
| Sample 2 | Gemini |  |  |  |  |  |
| Sample 2 | Azure DI |  |  |  |  |  |

## Scoring

Use this simple score:

```text
Medicine name correct = 3 points
Dosage correct = 2 points
Timing/frequency correct = 2 points
Duration correct = 1 point
Wrong medicine hallucinated = -5 points
```

Very simple meaning: wrong medicine names are more dangerous than missing a field, so they get a heavy penalty.

## Final Recommendation

Do not depend completely on any OCR provider for prescriptions.

Best production-style approach:

```text
Pluggable OCR provider
Raw OCR text stored
Medicine confidence scoring
Medicine dictionary matching
Manual verification
Only verified medicines used for reminders
```
