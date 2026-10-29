package com.library.library.image.storage;

import com.library.library.image.dto.ImageLocation;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class LocalImageStore implements ImageStore {

    private final Path rootDir;

    public LocalImageStore(Path rootDir) {
        this.rootDir = rootDir.toAbsolutePath().normalize();
    }

    @Override
    public void save(String key, byte[] bytes, String contentType) {
        Path path = rootDir.resolve(key);
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.write(path, bytes);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen en " + path, e);
        }
    }

    @Override
    public Optional<ImageLocation> locate(String key, String contentType) {
        FileSystemResource resource = new FileSystemResource(rootDir.resolve(key));
        return resource.exists() ? Optional.of(new ImageLocation.Local(resource, contentType)) : Optional.empty();
    }
}
