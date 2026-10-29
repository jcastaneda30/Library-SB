package com.library.library.image.storage;

import com.library.library.image.dto.ImageLocation;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;

/**
 * Guarda las imágenes en un bucket privado y las entrega con URLs prefirmadas de corta duración,
 * así el bucket no tiene que ser público y la aplicación no hace de proxy de los bytes.
 */
public class S3ImageStore implements ImageStore {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String prefix;
    private final Duration presignedUrlTtl;

    public S3ImageStore(S3Client s3Client, S3Presigner s3Presigner, String bucket, String prefix, Duration presignedUrlTtl) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.prefix = prefix == null ? "" : prefix;
        this.presignedUrlTtl = presignedUrlTtl;
    }

    @Override
    public void save(String key, byte[] bytes, String contentType) {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(prefix + key)
                        .contentType(contentType)
                        // El contenido de una clave nunca cambia porque la clave es su hash
                        .cacheControl("public, max-age=31536000, immutable")
                        .build(),
                RequestBody.fromBytes(bytes));
    }

    @Override
    public Optional<ImageLocation> locate(String key, String contentType) {
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(presignedUrlTtl)
                .getObjectRequest(get -> get.bucket(bucket).key(prefix + key))
                .build();
        try {
            return Optional.of(new ImageLocation.Remote(s3Presigner.presignGetObject(request).url().toURI()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
