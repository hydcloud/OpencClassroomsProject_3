package fr.datashare.backend.service;

import fr.datashare.backend.dto.file.FileUploadResponse;
import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.entity.User;
import fr.datashare.backend.exception.FileUploadException;
import fr.datashare.backend.exception.FileNotFoundException;
import fr.datashare.backend.repository.DownloadLinkRepository;
import fr.datashare.backend.repository.StoredFileRepository;
import fr.datashare.backend.repository.UserRepository;
import fr.datashare.backend.storage.StorageService;
import fr.datashare.backend.dto.file.FileHistoryResponse;
import fr.datashare.backend.dto.download.DownloadMetadataResponse;
import fr.datashare.backend.exception.DownloadLinkExpiredException;
import fr.datashare.backend.exception.DownloadLinkNotFoundException;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.List;

@Service
public class FileService {

    private static final long MAX_FILE_SIZE = 1_073_741_824L;

    private static final Set<String> FORBIDDEN_EXTENSIONS = Set.of(
            "exe",
            "bat",
            "cmd",
            "com",
            "msi",
            "ps1",
            "sh"
    );

    private final UserRepository userRepository;
    private final StoredFileRepository storedFileRepository;
    private final DownloadLinkRepository downloadLinkRepository;
    private final StorageService storageService;

    public FileService(
            UserRepository userRepository,
            StoredFileRepository storedFileRepository,
            DownloadLinkRepository downloadLinkRepository,
            StorageService storageService
    ) {
        this.userRepository = userRepository;
        this.storedFileRepository = storedFileRepository;
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

    @Transactional
    public FileUploadResponse upload(
            MultipartFile file,
            int expirationDays,
            String authenticatedEmail
    ) {
        validateFile(file);
        validateExpiration(expirationDays);

        User owner = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new FileUploadException(
                                "L'utilisateur authentifié est introuvable."
                        )
                );

        LocalDateTime expiresAt =
                LocalDateTime.now().plusDays(expirationDays);

        String storageName = storageService.store(file);

        try {
            StoredFile storedFile = new StoredFile();
            storedFile.setOriginalName(resolveOriginalName(file));
            storedFile.setStorageName(storageName);
            storedFile.setMimeType(resolveMimeType(file));
            storedFile.setSize(file.getSize());
            storedFile.setExpiresAt(expiresAt);
            storedFile.setOwner(owner);

            StoredFile savedFile =
                    storedFileRepository.save(storedFile);

            DownloadLink downloadLink = new DownloadLink();
            downloadLink.setToken(generateUniqueToken());
            downloadLink.setExpiresAt(expiresAt);
            downloadLink.setStoredFile(savedFile);

            DownloadLink savedLink =
                    downloadLinkRepository.save(downloadLink);

            return new FileUploadResponse(
                    savedFile.getId(),
                    savedFile.getOriginalName(),
                    savedFile.getMimeType(),
                    savedFile.getSize(),
                    savedFile.getUploadedAt(),
                    savedFile.getExpiresAt(),
                    savedLink.getToken(),
                    "/api/downloads/" + savedLink.getToken()
            );

        } catch (RuntimeException exception) {
            storageService.delete(storageName);

            throw new FileUploadException(
                    "L'enregistrement du fichier a échoué.",
                    exception
            );
        }
    }

    @Transactional(readOnly = true)
    public List<FileHistoryResponse> getHistory(String email) {

        User owner = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new FileUploadException(
                                "Utilisateur introuvable."
                        )
                );

        List<StoredFile> files =
                storedFileRepository
                        .findAllByOwnerOrderByUploadedAtDesc(owner);

        return files.stream()
                .map(file -> new FileHistoryResponse(
                        file.getId(),
                        file.getOriginalName(),
                        file.getMimeType(),
                        file.getSize(),
                        file.getUploadedAt(),
                        file.getExpiresAt(),
                        file.getDownloadLink().getToken()
                ))
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException(
                    "Le fichier est obligatoire et ne doit pas être vide."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException(
                    "La taille maximale autorisée est de 1 Go."
            );
        }

        String extension = extractExtension(file.getOriginalFilename());

        if (FORBIDDEN_EXTENSIONS.contains(extension)) {
            throw new FileUploadException(
                    "Ce type de fichier n'est pas autorisé."
            );
        }
    }

    private void validateExpiration(int expirationDays) {
        if (expirationDays < 1 || expirationDays > 7) {
            throw new FileUploadException(
                    "La durée d'expiration doit être comprise entre 1 et 7 jours."
            );
        }
    }

    private String generateUniqueToken() {
        String token;

        do {
            token = UUID.randomUUID().toString();
        } while (downloadLinkRepository.existsByToken(token));

        return token;
    }

    private String resolveOriginalName(MultipartFile file) {
        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            return "fichier";
        }

        return originalName;
    }

    private String resolveMimeType(MultipartFile file) {
        String contentType = file.getContentType();

        return contentType == null
                ? "application/octet-stream"
                : contentType;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        return filename
                .substring(filename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);
    }

    @Transactional
    public void deleteFile(Long fileId, String authenticatedEmail) {

        User owner = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(FileNotFoundException::new);

        StoredFile storedFile = storedFileRepository
                .findById(fileId)
                .orElseThrow(FileNotFoundException::new);

        if (!storedFile.getOwner().getId().equals(owner.getId())) {
            throw new FileNotFoundException();
        }

        storageService.delete(storedFile.getStorageName());

        storedFileRepository.delete(storedFile);
    }
}
