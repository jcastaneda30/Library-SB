package com.library.library.image.storage;

import com.library.library.image.dto.ImageLocation;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class S3ImageStoreTest {

    private final S3Client s3Client = mock(S3Client.class);
    // Prefirmar es un cálculo local, no hace falta red ni credenciales reales
    private final S3Presigner presigner = S3Presigner.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
            .build();
    private final S3ImageStore store = new S3ImageStore(s3Client, presigner, "library-images", "images/", Duration.ofMinutes(10));

    @Test
    void savesUnderPrefixWithContentType() {
        store.save("ab/abc.png", new byte[]{1, 2, 3}, "image/png");

        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertThat(request.getValue().bucket()).isEqualTo("library-images");
        assertThat(request.getValue().key()).isEqualTo("images/ab/abc.png");
        assertThat(request.getValue().contentType()).isEqualTo("image/png");
    }

    @Test
    void locatesImageWithPresignedUrl() {
        ImageLocation location = store.locate("ab/abc.png", "image/png").orElseThrow();

        assertThat(location).isInstanceOf(ImageLocation.Remote.class);
        String url = ((ImageLocation.Remote) location).uri().toString();
        assertThat(url).contains("library-images").contains("images/ab/abc.png")
                .contains("X-Amz-Signature=").contains("X-Amz-Expires=600");
    }
}
