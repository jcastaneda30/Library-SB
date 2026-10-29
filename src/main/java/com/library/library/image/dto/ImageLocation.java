package com.library.library.image.dto;

import org.springframework.core.io.Resource;

import java.net.URI;

// Local: el archivo se sirve desde la aplicación. Remote: se redirige al cliente (p. ej. URL prefirmada de S3)
public sealed interface ImageLocation {

    record Local(Resource resource, String contentType) implements ImageLocation {}

    record Remote(URI uri) implements ImageLocation {}
}
