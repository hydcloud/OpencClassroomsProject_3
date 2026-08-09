package fr.datashare.backend.service;

import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.repository.DownloadLinkRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DownloadLinkService {

    private final DownloadLinkRepository downloadLinkRepository;

    public DownloadLinkService(
            DownloadLinkRepository downloadLinkRepository
    ) {
        this.downloadLinkRepository = downloadLinkRepository;
    }

    public DownloadLink createLink(
            StoredFile storedFile,
            LocalDateTime expiresAt
    ) {
        DownloadLink downloadLink = new DownloadLink();
        downloadLink.setToken(generateUniqueToken());
        downloadLink.setExpiresAt(expiresAt);
        downloadLink.setStoredFile(storedFile);

        return downloadLinkRepository.save(downloadLink);
    }

    private String generateUniqueToken() {
        String token;

        do {
            token = UUID.randomUUID().toString();
        } while (downloadLinkRepository.existsByToken(token));

        return token;
    }
}

