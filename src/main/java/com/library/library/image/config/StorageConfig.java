package com.library.library.image.config;

import com.library.library.image.storage.ImageStore;
import com.library.library.image.storage.LocalImageStore;
import com.library.library.image.storage.S3ImageStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "library.storage.type", havingValue = "local", matchIfMissing = true)
    public ImageStore localImageStore(StorageProperties properties) {
        return new LocalImageStore(Path.of(properties.imagesDir()));
    }

    @Configuration
    @ConditionalOnProperty(name = "library.storage.type", havingValue = "s3")
    static class S3StorageConfig {

        // Sin credenciales explícitas: el SDK usa su cadena por defecto (variables de entorno, ~/.aws, rol IAM)
        @Bean
        public S3Client s3Client(StorageProperties properties) {
            StorageProperties.S3 s3 = properties.s3();
            var builder = S3Client.builder()
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(s3.pathStyle()).build());
            if (hasText(s3.region())) {
                builder.region(Region.of(s3.region()));
            }
            if (hasText(s3.endpoint())) {
                builder.endpointOverride(URI.create(s3.endpoint()));
            }
            return builder.build();
        }

        @Bean
        public S3Presigner s3Presigner(StorageProperties properties) {
            StorageProperties.S3 s3 = properties.s3();
            var builder = S3Presigner.builder()
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(s3.pathStyle()).build());
            if (hasText(s3.region())) {
                builder.region(Region.of(s3.region()));
            }
            if (hasText(s3.endpoint())) {
                builder.endpointOverride(URI.create(s3.endpoint()));
            }
            return builder.build();
        }

        @Bean
        public ImageStore s3ImageStore(S3Client s3Client, S3Presigner s3Presigner, StorageProperties properties) {
            StorageProperties.S3 s3 = properties.s3();
            if (!hasText(s3.bucket())) {
                throw new IllegalStateException("library.storage.type=s3 requiere library.storage.s3.bucket (S3_BUCKET)");
            }
            Duration ttl = s3.presignedUrlTtl() == null ? Duration.ofHours(1) : s3.presignedUrlTtl();
            return new S3ImageStore(s3Client, s3Presigner, s3.bucket(), s3.prefix(), ttl);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
