package com.tekravio.healthcare.prescription;

public interface PrescriptionOcrProvider {

    String providerName();

    OcrExtractionResult extract(String bucket, String key, String contentType);
}
