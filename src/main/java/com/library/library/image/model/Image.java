package com.library.library.image.model;

import com.library.common.model.Timer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "image")
public class Image extends Timer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourceUrl;
    // Ruta relativa al directorio de imágenes configurado
    @Column(nullable = false)
    private String storagePath;
    @Column(nullable = false)
    private String contentType;
    @Column(nullable = false)
    private long sizeBytes;
    // Evita guardar dos veces la misma imagen
    @Column(nullable = false, unique = true)
    private String sha256;
}
