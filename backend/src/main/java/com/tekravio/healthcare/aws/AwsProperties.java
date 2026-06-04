package com.tekravio.healthcare.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.aws")
public record AwsProperties(
        String region,
        String s3Bucket,
        String s3PrescriptionPrefix,
        long s3PresignedUrlMinutes,
        long maxPrescriptionUploadBytes) {
}

