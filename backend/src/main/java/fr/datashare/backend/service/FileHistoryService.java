package fr.datashare.backend.service;

import fr.datashare.backend.dto.file.FileHistoryResponse;
import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.entity.User;
import fr.datashare.backend.exception.FileUploadException;
import fr.datashare.backend.repository.StoredFileRepository;
import fr.datashare.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FileHistoryService {

    private final UserRepository userRepository;
    private final StoredFileRepository storedFileRepository;

    public FileHistoryService(
            UserRepository userRepository,
            StoredFileRepository storedFileRepository
    ) {
        this.userRepository = userRepository;
        this.storedFileRepository = storedFileRepository;
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
                        file.getDownloadLink().getToken(),
                        file.getPasswordHash() != null
                ))
                .toList();
    }

}
