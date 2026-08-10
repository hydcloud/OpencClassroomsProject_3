package fr.datashare.backend.service;

import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DownloadService {

    private final DownloadLinkService downloadLinkService;
    private final StorageService storageService;

    public DownloadService(
            DownloadLinkService downloadLinkService,
            StorageService storageService
    ) {
        this.downloadLinkService = downloadLinkService;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public DownloadResource loadDownloadFile(String token) {
        DownloadLink downloadLink = downloadLinkService.findValidLink(token);
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

    public record DownloadResource(
            Resource resource,
            String originalName,
            String mimeType
    ) {
    }
}
