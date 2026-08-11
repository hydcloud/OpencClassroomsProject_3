package fr.datashare.backend.service;

import fr.datashare.backend.dto.file.FileUploadResponse;
import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.entity.User;
import fr.datashare.backend.exception.FileUploadException;
import fr.datashare.backend.repository.StoredFileRepository;
import fr.datashare.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

@Service
public class UploadService {

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
    private final StorageService storageService;
    private final StoredFileRepository storedFileRepository;
    private final DownloadLinkService downloadLinkService;
    private final PasswordEncoder passwordEncoder;

    public UploadService(
            UserRepository userRepository,
            StorageService storageService,
            StoredFileRepository storedFileRepository,
            DownloadLinkService downloadLinkService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.storedFileRepository = storedFileRepository;
        this.downloadLinkService = downloadLinkService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public FileUploadResponse upload(
            MultipartFile file,
            int expirationDays,
            String authenticatedEmail,
            String password
    ) {
        User owner = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                        new FileUploadException(
                                "L'utilisateur authentifié est introuvable."
                        )
                );

        return processUpload(
                file,
                expirationDays,
                owner,
                password
        );
    }

    @Transactional
    public FileUploadResponse uploadAnonymous(
            MultipartFile file,
            int expirationDays,
            String password
    ) {
        return processUpload(
                file,
                expirationDays,
                null,
                password
        );
    }

    private FileUploadResponse processUpload(
            MultipartFile file,
            int expirationDays,
            User owner,
            String password
    ) {
        validateFile(file);
        validateExpiration(expirationDays);

        String passwordHash = resolvePasswordHash(password);

        LocalDateTime expiresAt =
                LocalDateTime.now().plusDays(expirationDays);

        String storageName =
                storageService.store(file);

        try {
            StoredFile storedFile = new StoredFile();

            storedFile.setOriginalName(resolveOriginalName(file));
            storedFile.setStorageName(storageName);
            storedFile.setMimeType(resolveMimeType(file));
            storedFile.setSize(file.getSize());
            storedFile.setExpiresAt(expiresAt);
            storedFile.setOwner(owner);
            storedFile.setPasswordHash(passwordHash);

            StoredFile savedFile =
                    storedFileRepository.save(storedFile);

            DownloadLink savedLink =
                    downloadLinkService.createLink(
                            savedFile,
                            expiresAt
                    );

            return new FileUploadResponse(
                    savedFile.getId(),
                    savedFile.getOriginalName(),
                    savedFile.getMimeType(),
                    savedFile.getSize(),
                    savedFile.getUploadedAt(),
                    savedFile.getExpiresAt(),
                    savedLink.getToken(),
                    "/api/files/" + savedLink.getToken() + "/file",
                    savedFile.getPasswordHash() != null
            );

        } catch (RuntimeException exception) {
            storageService.delete(storageName);

            throw new FileUploadException(
                    "L'enregistrement du fichier a échoué.",
                    exception
            );
        }
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

    private String resolvePasswordHash(String password) {
        if (password == null || password.isBlank()) {
            return null;
        }

        if (password.length() < 6) {
            throw new FileUploadException(
                    "Le mot de passe doit contenir au moins 6 caractères."
            );
        }

        return passwordEncoder.encode(password);
    }
}
