package fr.datashare.backend.service;

import fr.datashare.backend.dto.download.DownloadMetadataResponse;
import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.exception.DownloadLinkExpiredException;
import fr.datashare.backend.exception.DownloadLinkNotFoundException;
import fr.datashare.backend.repository.DownloadLinkRepository;
import fr.datashare.backend.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DownloadService {

    private final DownloadLinkRepository downloadLinkRepository;
    private final StorageService storageService;

    public DownloadService(
            DownloadLinkRepository downloadLinkRepository,
            StorageService storageService
    ) {
        this.downloadLinkRepository = downloadLinkRepository;
        this.storageService = storageService;
    }

    public DownloadMetadataResponse getMetadata(String token) {
        DownloadLink downloadLink = findValidLink(token);
        StoredFile storedFile = downloadLink.getStoredFile();

        return new DownloadMetadataResponse(
                storedFile.getOriginalName(),
                storedFile.getMimeType(),
                storedFile.getSize(),
                downloadLink.getExpiresAt()
        );
    }

    public DownloadResource loadFile(String token) {
        DownloadLink downloadLink = findValidLink(token);
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

    private DownloadLink findValidLink(String token) {
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
