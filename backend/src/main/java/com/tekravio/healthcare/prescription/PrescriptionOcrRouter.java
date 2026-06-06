package com.tekravio.healthcare.prescription;

import java.util.List;

import com.tekravio.healthcare.common.ApiException;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(PrescriptionOcrProperties.class)
public class PrescriptionOcrRouter {

    private final List<PrescriptionOcrProvider> providers;
    private final PrescriptionOcrProperties properties;

    public PrescriptionOcrRouter(List<PrescriptionOcrProvider> providers, PrescriptionOcrProperties properties) {
        this.providers = providers;
        this.properties = properties;
    }

    public OcrExtractionResult extract(String bucket, String key, String contentType) {
        return providers.stream()
                .filter(provider -> provider.providerName().equalsIgnoreCase(properties.provider()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_GATEWAY,
                        "Configured OCR provider is not available: " + properties.provider()))
                .extract(bucket, key, contentType);
    }
}
