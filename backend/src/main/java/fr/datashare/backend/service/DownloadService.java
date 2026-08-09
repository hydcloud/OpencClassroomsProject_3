package fr.datashare.backend.service;

import fr.datashare.backend.dto.download.DownloadMetadataResponse;
import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.exception.DownloadLinkExpiredException;
import fr.datashare.backend.exception.DownloadLinkNotFoundException;
import fr.datashare.backend.repository.DownloadLinkRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DownloadService {

    private final DownloadLinkRepository downloadLinkRepository;
    private final StorageService storageService;

    public DownloadService(
            DownloadLinkRepository downloadLinkRepository,
            StorageService storageService) {
        this.downloadLinkRepository = downloadLinkRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public DownloadMetadataResponse getDownloadMetadata(String token) {
        DownloadLink downloadLink = findValidDownloadLink(token);
        StoredFile storedFile = downloadLink.getStoredFile();

        return new DownloadMetadataResponse(
                storedFile.getOriginalName(),
                storedFile.getMimeType(),
                storedFile.getSize(),
                downloadLink.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public DownloadResource loadDownloadFile(String token) {
        DownloadLink downloadLink = findValidDownloadLink(token);
        StoredFile storedFile = downloadLink.getStoredFile();

        Resource resource = storageService.load(
                storedFile.getStorageName()
        );

        return new DownloadResource(
                resource,
                storedFile.getOriginalName(),
                storedFile.getMimeType()
        );
    }

    private DownloadLink findValidDownloadLink(String token) {

        DownloadLink downloadLink = downloadLinkRepository
                .findByToken(token)
                .orElseThrow(DownloadLinkNotFoundException::new);

        if (downloadLink.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new DownloadLinkExpiredException();
        }

        return downloadLink;
    }

    public record DownloadResource(
            Resource resource,
            String originalName,
            String mimeType
    ) {
    }
}
