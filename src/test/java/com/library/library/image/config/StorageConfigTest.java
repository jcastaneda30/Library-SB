package com.library.library.image.config;

import com.library.library.image.storage.ImageStore;
import com.library.library.image.storage.LocalImageStore;
import com.library.library.image.storage.S3ImageStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StorageConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(StorageConfig.class)
            .withPropertyValues("library.storage.images-dir=build/images");

    @Test
    void usesLocalStorageByDefault() {
        contextRunner.run(context -> assertThat(context.getBean(ImageStore.class)).isInstanceOf(LocalImageStore.class));
    }

    @Test
    void usesS3WhenConfigured() {
        contextRunner
                .withPropertyValues(
                        "library.storage.type=s3",
                        "library.storage.s3.bucket=library-images",
                        "library.storage.s3.region=us-east-1",
                        "library.storage.s3.presigned-url-ttl=30m")
                .run(context -> assertThat(context.getBean(ImageStore.class)).isInstanceOf(S3ImageStore.class));
    }

    @Test
    void failsFastWithoutBucket() {
        contextRunner
                .withPropertyValues("library.storage.type=s3", "library.storage.s3.region=us-east-1")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure().hasStackTraceContaining("library.storage.s3.bucket"));
    }
}
