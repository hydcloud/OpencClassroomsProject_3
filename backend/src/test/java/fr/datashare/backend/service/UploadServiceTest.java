package fr.datashare.backend.service;

import fr.datashare.backend.dto.file.FileUploadResponse;
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

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private DownloadLinkService downloadLinkService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        uploadService = new UploadService(
                userRepository,
                storageService,
                storedFileRepository,
                downloadLinkService,
                passwordEncoder
        );
    }

    @Test
    void uploadAnonymous_should_upload_valid_file() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Contenu du fichier".getBytes()
        );

        when(storageService.store(file))
                .thenReturn("stored-test.txt");

        when(storedFileRepository.save(any(StoredFile.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        DownloadLink downloadLink = new DownloadLink();
        downloadLink.setToken("download-token");

        when(downloadLinkService.createLink(
                any(StoredFile.class),
                any()
        )).thenReturn(downloadLink);

        FileUploadResponse response =
                uploadService.uploadAnonymous(
                        file,
                        7,
                        null
                );

        assertNotNull(response);
        assertEquals("test.txt", response.originalName());
        assertEquals("text/plain", response.mimeType());
        assertEquals("download-token", response.downloadToken());
        assertEquals(
                "/api/files/download-token/file",
                response.downloadUrl()
        );
        assertFalse(response.passwordProtected());

        verify(storageService).store(file);
        verify(storedFileRepository)
                .save(any(StoredFile.class));
        verify(downloadLinkService)
                .createLink(
                        any(StoredFile.class),
                        any()
                );

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void upload_should_associate_authenticated_user() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Contenu".getBytes()
        );

        User user = new User();
        user.setEmail("test@datashare.fr");

        when(userRepository.findByEmail("test@datashare.fr"))
                .thenReturn(Optional.of(user));

        when(storageService.store(file))
                .thenReturn("stored-test.txt");

        when(storedFileRepository.save(any(StoredFile.class)))
                .thenAnswer(invocation -> {
                    StoredFile storedFile =
                            invocation.getArgument(0);

                    assertEquals(
                            user,
                            storedFile.getOwner()
                    );

                    return storedFile;
                });

        DownloadLink downloadLink = new DownloadLink();
        downloadLink.setToken("download-token");

        when(downloadLinkService.createLink(
                any(StoredFile.class),
                any()
        )).thenReturn(downloadLink);

        FileUploadResponse response =
                uploadService.upload(
                        file,
                        7,
                        "test@datashare.fr",
                        null
                );

        assertNotNull(response);

        verify(userRepository)
                .findByEmail("test@datashare.fr");
    }

    @Test
    void uploadAnonymous_should_hash_password() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "protected.txt",
                "text/plain",
                "Contenu".getBytes()
        );

        when(passwordEncoder.encode("Secret123"))
                .thenReturn("hashed-password");

        when(storageService.store(file))
                .thenReturn("stored-protected.txt");

        when(storedFileRepository.save(any(StoredFile.class)))
                .thenAnswer(invocation -> {
                    StoredFile storedFile =
                            invocation.getArgument(0);

                    assertEquals(
                            "hashed-password",
                            storedFile.getPasswordHash()
                    );

                    return storedFile;
                });

        DownloadLink downloadLink = new DownloadLink();
        downloadLink.setToken("protected-token");

        when(downloadLinkService.createLink(
                any(StoredFile.class),
                any()
        )).thenReturn(downloadLink);

        FileUploadResponse response =
                uploadService.uploadAnonymous(
                        file,
                        7,
                        "Secret123"
                );

        assertTrue(response.passwordProtected());

        verify(passwordEncoder)
                .encode("Secret123");
    }

    @Test
    void uploadAnonymous_should_reject_empty_file() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        assertThrows(
                FileUploadException.class,
                () -> uploadService.uploadAnonymous(
                        file,
                        7,
                        null
                )
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void uploadAnonymous_should_reject_forbidden_extension() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                "fake executable".getBytes()
        );

        assertThrows(
                FileUploadException.class,
                () -> uploadService.uploadAnonymous(
                        file,
                        7,
                        null
                )
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void uploadAnonymous_should_reject_invalid_expiration() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Contenu".getBytes()
        );

        assertThrows(
                FileUploadException.class,
                () -> uploadService.uploadAnonymous(
                        file,
                        8,
                        null
                )
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void uploadAnonymous_should_reject_short_password() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Contenu".getBytes()
        );

        assertThrows(
                FileUploadException.class,
                () -> uploadService.uploadAnonymous(
                        file,
                        7,
                        "12345"
                )
        );

        verifyNoInteractions(storageService);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void upload_should_throw_when_user_does_not_exist() {

        when(userRepository.findByEmail("unknown@datashare.fr"))
                .thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Contenu".getBytes()
        );

        assertThrows(
                FileUploadException.class,
                () -> uploadService.upload(
                        file,
                        7,
                        "unknown@datashare.fr",
                        null
                )
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void uploadAnonymous_should_delete_stored_file_when_database_save_fails() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Contenu".getBytes()
        );

        when(storageService.store(file))
                .thenReturn("stored-test.txt");

        when(storedFileRepository.save(any(StoredFile.class)))
                .thenThrow(
                        new RuntimeException("Database error")
                );

        assertThrows(
                FileUploadException.class,
                () -> uploadService.uploadAnonymous(
                        file,
                        7,
                        null
                )
        );

        verify(storageService)
                .delete("stored-test.txt");
    }
}