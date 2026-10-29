package com.library.library.image.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("library.storage")
public record StorageProperties(String imagesDir) {}
