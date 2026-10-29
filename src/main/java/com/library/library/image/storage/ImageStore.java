package com.library.library.image.storage;

import com.library.library.image.dto.ImageLocation;

import java.util.Optional;

// Dónde viven los bytes de las imágenes; la BD solo guarda la clave
public interface ImageStore {

    // La clave es el hash del contenido, así que guardar dos veces la misma clave es inofensivo
    void save(String key, byte[] bytes, String contentType);

    Optional<ImageLocation> locate(String key, String contentType);
}
