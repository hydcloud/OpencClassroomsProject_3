package fr.datashare.backend.service;

import fr.datashare.backend.dto.file.DownloadFileInfoResponse;
import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import fr.datashare.backend.exception.InvalidFilePasswordException;

@Service
public class DownloadService {

    private final DownloadLinkService downloadLinkService;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder;

    public DownloadService(
            DownloadLinkService downloadLinkService,
            StorageService storageService,
            PasswordEncoder passwordEncoder
    ) {
        this.downloadLinkService = downloadLinkService;
        this.storageService = storageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public DownloadResource loadDownloadFile(
            String token,
            String password
    ) {
        DownloadLink downloadLink =
                downloadLinkService.findValidLink(token);

        StoredFile storedFile =
                downloadLink.getStoredFile();

        if (storedFile.getPasswordHash() != null) {
            if (password == null ||
                    !passwordEncoder.matches(
                            password,
                            storedFile.getPasswordHash()
                    )) {

                throw new InvalidFilePasswordException();
            }
        }

        Resource resource = storageService.load(
                storedFile.getStorageName()
        );

        return new DownloadResource(
                resource,
                storedFile.getOriginalName(),
                storedFile.getMimeType()
        );
    }

    @Transactional(readOnly = true)
    public DownloadFileInfoResponse getFileInfo(String token) {

        DownloadLink downloadLink =
                downloadLinkService.findValidLink(token);

        StoredFile storedFile =
                downloadLink.getStoredFile();

        return new DownloadFileInfoResponse(
                storedFile.getOriginalName(),
                storedFile.getSize(),
                storedFile.getExpiresAt(),
                storedFile.getPasswordHash() != null
        );
    }

    public record DownloadResource(
            Resource resource,
            String originalName,
            String mimeType
    ) {
    }
}
