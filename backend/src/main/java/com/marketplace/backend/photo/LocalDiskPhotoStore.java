package com.marketplace.backend.photo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalDiskPhotoStore implements PhotoStore {

    private final Path storageDir;

    public LocalDiskPhotoStore(@Value("${app.photo.storage-dir}") String storageDir) {
        this.storageDir = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String store(MultipartFile photo) {
        String reference = UUID.randomUUID() + extensionOf(photo.getOriginalFilename());
        try {
            photo.transferTo(storageDir.resolve(reference));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return reference;
    }

    @Override
    public Resource retrieve(String reference) {
        Path path = storageDir.resolve(reference).normalize();
        if (!path.startsWith(storageDir)) {
            throw new PhotoNotFoundException(reference);
        }

        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new PhotoNotFoundException(reference);
        }
        return resource;
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        return dot >= 0 ? originalFilename.substring(dot) : "";
    }
}
