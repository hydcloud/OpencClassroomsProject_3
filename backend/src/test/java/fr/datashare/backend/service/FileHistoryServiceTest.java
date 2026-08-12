package fr.datashare.backend.service;

import fr.datashare.backend.dto.file.FileHistoryResponse;
import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.entity.User;
import fr.datashare.backend.exception.FileUploadException;
import fr.datashare.backend.repository.StoredFileRepository;
import fr.datashare.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileHistoryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoredFileRepository storedFileRepository;

    private FileHistoryService fileHistoryService;

    @BeforeEach
    void setUp() {
        fileHistoryService = new FileHistoryService(
                userRepository,
                storedFileRepository
        );
    }

    @Test
    void getHistory_should_return_user_files() {

        User user = new User();
        user.setEmail("test@datashare.fr");

        DownloadLink downloadLink = new DownloadLink();
        downloadLink.setToken("download-token");

        StoredFile storedFile = new StoredFile();
        storedFile.setOriginalName("document.txt");
        storedFile.setMimeType("text/plain");
        storedFile.setSize(100L);
        storedFile.setExpiresAt(
                LocalDateTime.of(2026, 8, 19, 10, 0)
        );
        storedFile.setDownloadLink(downloadLink);
        storedFile.setPasswordHash("hashed-password");

        when(userRepository.findByEmail("test@datashare.fr"))
                .thenReturn(Optional.of(user));

        when(storedFileRepository
                .findAllByOwnerOrderByUploadedAtDesc(user))
                .thenReturn(List.of(storedFile));

        List<FileHistoryResponse> result =
                fileHistoryService.getHistory(
                        "test@datashare.fr"
                );

        assertEquals(1, result.size());

        FileHistoryResponse response = result.get(0);

        assertEquals(
                "document.txt",
                response.originalName()
        );
        assertEquals(
                "text/plain",
                response.mimeType()
        );
        assertEquals(100L, response.size());
        assertEquals(
                "download-token",
                response.downloadToken()
        );
        assertTrue(response.passwordProtected());

        verify(userRepository)
                .findByEmail("test@datashare.fr");

        verify(storedFileRepository)
                .findAllByOwnerOrderByUploadedAtDesc(user);
    }

    @Test
    void getHistory_should_return_empty_list_when_user_has_no_files() {

        User user = new User();
        user.setEmail("test@datashare.fr");

        when(userRepository.findByEmail("test@datashare.fr"))
                .thenReturn(Optional.of(user));

        when(storedFileRepository
                .findAllByOwnerOrderByUploadedAtDesc(user))
                .thenReturn(List.of());

        List<FileHistoryResponse> result =
                fileHistoryService.getHistory(
                        "test@datashare.fr"
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getHistory_should_throw_when_user_does_not_exist() {

        when(userRepository.findByEmail("unknown@datashare.fr"))
                .thenReturn(Optional.empty());

        assertThrows(
                FileUploadException.class,
                () -> fileHistoryService.getHistory(
                        "unknown@datashare.fr"
                )
        );

        verifyNoInteractions(storedFileRepository);
    }
}