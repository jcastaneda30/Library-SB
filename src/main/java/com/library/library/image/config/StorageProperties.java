package com.library.library.image.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("library.storage")
public record StorageProperties(StorageType type, String imagesDir, S3 s3) {

    public enum StorageType {
        LOCAL,
        S3
    }

    public record S3(
            String bucket,
            String region,
            String prefix,
            // Para MinIO/LocalStack; en AWS se deja vacío
            String endpoint,
            boolean pathStyle,
            Duration presignedUrlTtl
    ) {}
}
