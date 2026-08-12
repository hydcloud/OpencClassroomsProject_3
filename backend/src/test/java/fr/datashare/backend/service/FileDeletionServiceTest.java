package fr.datashare.backend.service;

import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.entity.User;
import fr.datashare.backend.exception.FileNotFoundException;
import fr.datashare.backend.repository.StoredFileRepository;
import fr.datashare.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDeletionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private StorageService storageService;

    private FileDeletionService fileDeletionService;

    @BeforeEach
    void setUp() {
        fileDeletionService = new FileDeletionService(
                userRepository,
                storedFileRepository,
                storageService
        );
    }

    @Test
    void deleteFile_should_delete_file_owned_by_user() {

        User owner = mock(User.class);
        StoredFile storedFile = mock(StoredFile.class);

        when(owner.getId()).thenReturn(1L);

        when(storedFile.getOwner()).thenReturn(owner);
        when(storedFile.getStorageName())
                .thenReturn("stored-file.txt");

        when(userRepository.findByEmail("test@datashare.fr"))
                .thenReturn(Optional.of(owner));

        when(storedFileRepository.findById(10L))
                .thenReturn(Optional.of(storedFile));

        fileDeletionService.deleteFile(
                10L,
                "test@datashare.fr"
        );

        verify(storageService)
                .delete("stored-file.txt");

        verify(storedFileRepository)
                .delete(storedFile);
    }

    @Test
    void deleteFile_should_throw_when_user_does_not_exist() {

        when(userRepository.findByEmail("unknown@datashare.fr"))
                .thenReturn(Optional.empty());

        assertThrows(
                FileNotFoundException.class,
                () -> fileDeletionService.deleteFile(
                        10L,
                        "unknown@datashare.fr"
                )
        );

        verifyNoInteractions(storedFileRepository);
        verifyNoInteractions(storageService);
    }

    @Test
    void deleteFile_should_throw_when_file_does_not_exist() {

        User owner = mock(User.class);

        when(userRepository.findByEmail("test@datashare.fr"))
                .thenReturn(Optional.of(owner));

        when(storedFileRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                FileNotFoundException.class,
                () -> fileDeletionService.deleteFile(
                        10L,
                        "test@datashare.fr"
                )
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void deleteFile_should_throw_when_file_belongs_to_another_user() {

        User authenticatedUser = mock(User.class);
        User fileOwner = mock(User.class);
        StoredFile storedFile = mock(StoredFile.class);

        when(authenticatedUser.getId())
                .thenReturn(1L);

        when(fileOwner.getId())
                .thenReturn(2L);

        when(storedFile.getOwner())
                .thenReturn(fileOwner);

        when(userRepository.findByEmail("test@datashare.fr"))
                .thenReturn(Optional.of(authenticatedUser));

        when(storedFileRepository.findById(10L))
                .thenReturn(Optional.of(storedFile));

        assertThrows(
                FileNotFoundException.class,
                () -> fileDeletionService.deleteFile(
                        10L,
                        "test@datashare.fr"
                )
        );

        verifyNoInteractions(storageService);

        verify(storedFileRepository, never())
                .delete(any());
    }
}
