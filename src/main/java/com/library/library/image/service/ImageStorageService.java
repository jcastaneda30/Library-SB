package com.library.library.image.service;

import com.library.library.image.dto.ImageLocation;
import com.library.library.image.model.Image;
import com.library.library.image.repository.ImageRepository;
import com.library.library.image.storage.ImageStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

@Service
public class ImageStorageService {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp",
            "image/avif", ".avif",
            "image/svg+xml", ".svg"
    );

    private final ImageRepository imageRepository;
    private final ImageStore imageStore;

    public ImageStorageService(ImageRepository imageRepository, ImageStore imageStore) {
        this.imageRepository = imageRepository;
        this.imageStore = imageStore;
    }

    @Transactional
    public Image store(byte[] bytes, String contentType, String sourceUrl) {
        String type = resolveContentType(bytes, contentType);
        String sha256 = sha256(bytes);
        return imageRepository.findBySha256(sha256).orElseGet(() -> {
            // Se reparte en subcarpetas por prefijo para no tener miles de archivos en un solo directorio
            String key = sha256.substring(0, 2) + "/" + sha256 + EXTENSIONS.getOrDefault(type, ".bin");
            imageStore.save(key, bytes, type);

            Image image = new Image();
            image.setSourceUrl(sourceUrl);
            image.setStoragePath(key);
            image.setContentType(type);
            image.setSizeBytes(bytes.length);
            image.setSha256(sha256);
            return imageRepository.save(image);
        });
    }

    @Transactional(readOnly = true)
    public Optional<ImageLocation> locate(Long id) {
        return imageRepository.findById(id)
                .flatMap(image -> imageStore.locate(image.getStoragePath(), image.getContentType()));
    }

    private String resolveContentType(byte[] bytes, String declared) {
        String type = declared == null ? "" : declared.split(";")[0].strip().toLowerCase();
        if (!type.startsWith("image/")) {
            // Muchos CDN responden application/octet-stream; se adivina por los bytes
            try {
                type = Optional.ofNullable(URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes)))
                        .orElse("");
            } catch (IOException e) {
                type = "";
            }
        }
        if (!type.startsWith("image/")) {
            throw new IllegalArgumentException("El recurso descargado no es una imagen");
        }
        return type;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
