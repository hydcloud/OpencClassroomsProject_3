package fr.datashare.backend.service;

import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.entity.User;
import fr.datashare.backend.exception.FileNotFoundException;
import fr.datashare.backend.repository.StoredFileRepository;
import fr.datashare.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileDeletionService {

    private final UserRepository userRepository;
    private final StoredFileRepository storedFileRepository;
    private final StorageService storageService;

    public FileDeletionService(
            UserRepository userRepository,
            StoredFileRepository storedFileRepository,
            StorageService storageService
    ) {
        this.userRepository = userRepository;
        this.storedFileRepository = storedFileRepository;
        this.storageService = storageService;
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
