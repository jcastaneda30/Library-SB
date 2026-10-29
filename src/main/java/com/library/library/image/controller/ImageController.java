package com.library.library.image.controller;

import com.library.library.image.dto.ImageLocation;
import com.library.library.image.service.ImageStorageService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("api/images")
public class ImageController {
    private final ImageStorageService imageStorageService;
    public ImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getImage(@PathVariable Long id) {
        return imageStorageService.locate(id)
                .<ResponseEntity<?>>map(ImageController::toResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static ResponseEntity<?> toResponse(ImageLocation location) {
        if (location instanceof ImageLocation.Remote remote) {
            // La URL prefirmada caduca, así que la redirección solo se cachea un rato
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(remote.uri())
                    .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePrivate())
                    .build();
        }
        ImageLocation.Local local = (ImageLocation.Local) location;
        // Las imágenes se guardan por hash, así que su contenido nunca cambia
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(local.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
                .body(local.resource());
    }
}
