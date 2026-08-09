package fr.datashare.backend.service;

import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.repository.StoredFileRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpiredFileCleanupService {

    private final StoredFileRepository storedFileRepository;
    private final StorageService storageService;

    public ExpiredFileCleanupService(
            StoredFileRepository storedFileRepository,
            StorageService storageService
    ) {
        this.storedFileRepository = storedFileRepository;
        this.storageService = storageService;
    }

    @Scheduled(fixedRateString = "${cleanup.expired-files-rate-ms}")
    @Transactional
    public void deleteExpiredFiles() {

        List<StoredFile> expiredFiles =
                storedFileRepository.findAllByExpiresAtBefore(
                        LocalDateTime.now()
                );

        expiredFiles.forEach(file -> {
            storageService.delete(file.getStorageName());
            storedFileRepository.delete(file);
        });
    }
}
