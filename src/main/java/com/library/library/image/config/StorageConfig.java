package com.library.library.image.config;

import com.library.library.image.storage.ImageStore;
import com.library.library.image.storage.LocalImageStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    public ImageStore localImageStore(StorageProperties properties) {
        return new LocalImageStore(Path.of(properties.imagesDir()));
    }
}
