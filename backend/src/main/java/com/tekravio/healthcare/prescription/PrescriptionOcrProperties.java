package com.tekravio.healthcare.prescription;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ocr")
public record PrescriptionOcrProperties(String provider) {
}
