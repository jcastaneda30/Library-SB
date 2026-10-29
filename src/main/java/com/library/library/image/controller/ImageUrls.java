package com.library.library.image.controller;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// URL pública de una imagen servida por ImageController
public final class ImageUrls {

    private static final String PATH = "/api/images/{id}";

    private ImageUrls() {
    }

    /**
     * Absoluta con el host de la petición en curso (http://localhost:8080/api/images/1), para que el
     * cliente no tenga que saber a qué servidor pegarla. Fuera de una petición HTTP queda relativa.
     */
    public static String of(Long imageId) {
        if (imageId == null) {
            return null;
        }
        if (RequestContextHolder.getRequestAttributes() == null) {
            return PATH.replace("{id}", imageId.toString());
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(PATH)
                .buildAndExpand(imageId)
                .toUriString();
    }
}
