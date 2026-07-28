package fr.datashare.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path storageDirectory;

    public LocalStorageService(
            @Value("${storage.location}") String storageLocation
    ) {
        this.storageDirectory = Path.of(storageLocation)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Impossible d'initialiser le stockage local.",
                    exception
            );
        }
    }

    @Override
    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename();

        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(
                    originalName.lastIndexOf(".")
            );
        }

        String storageName = UUID.randomUUID() + extension;
        Path target = storageDirectory.resolve(storageName);

        try {
            Files.copy(
                    file.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Impossible de stocker le fichier.",
                    exception
            );
        }

        return storageName;
    }

    @Override
    public Resource load(String storageName) {
        try {
            Resource resource = new UrlResource(
                    storageDirectory.resolve(storageName).toUri()
            );

            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException(
                        "Le fichier est introuvable."
                );
            }

            return resource;

        } catch (MalformedURLException exception) {
            throw new IllegalStateException(
                    "Chemin de fichier invalide.",
                    exception
            );
        }
    }

    @Override
    public void delete(String storageName) {
        try {
            Files.deleteIfExists(
                    storageDirectory.resolve(storageName)
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Impossible de supprimer le fichier.",
                    exception
            );
        }
    }
}
