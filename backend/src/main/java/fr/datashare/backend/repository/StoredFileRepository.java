package fr.datashare.backend.repository;

import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

import java.util.List;

public interface StoredFileRepository
        extends JpaRepository<StoredFile, Long> {

    List<StoredFile> findAllByOwnerOrderByUploadedAtDesc(User owner);
    List<StoredFile> findAllByExpiresAtBefore(LocalDateTime dateTime);
}
