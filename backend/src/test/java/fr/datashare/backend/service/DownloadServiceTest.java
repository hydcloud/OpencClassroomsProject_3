package fr.datashare.backend.service;

import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.exception.InvalidFilePasswordException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadServiceTest {

    @Mock
    private DownloadLinkService downloadLinkService;

    @Mock
    private StorageService storageService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Resource resource;

    private DownloadService downloadService;

    private StoredFile storedFile;

    private DownloadLink downloadLink;

    @BeforeEach
    void setUp() {

        downloadService = new DownloadService(
                downloadLinkService,
                storageService,
                passwordEncoder
        );

        storedFile = new StoredFile();
        storedFile.setOriginalName("test.txt");
        storedFile.setStorageName("stored-test.txt");
        storedFile.setMimeType("text/plain");

        downloadLink = new DownloadLink();
        downloadLink.setStoredFile(storedFile);
    }

    @Test
    void loadDownloadFile_should_download_unprotected_file() {

        storedFile.setPasswordHash(null);

        when(downloadLinkService.findValidLink("token"))
                .thenReturn(downloadLink);

        when(storageService.load("stored-test.txt"))
                .thenReturn(resource);

        DownloadService.DownloadResource result =
                downloadService.loadDownloadFile(
                        "token",
                        null
                );

        assertNotNull(result);
        assertEquals(resource, result.resource());
        assertEquals("test.txt", result.originalName());
        assertEquals("text/plain", result.mimeType());

        verify(storageService)
                .load("stored-test.txt");

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void loadDownloadFile_should_download_when_password_is_correct() {

        storedFile.setPasswordHash("hashed-password");

        when(downloadLinkService.findValidLink("token"))
                .thenReturn(downloadLink);

        when(passwordEncoder.matches(
                "Secret123",
                "hashed-password"
        )).thenReturn(true);

        when(storageService.load("stored-test.txt"))
                .thenReturn(resource);

        DownloadService.DownloadResource result =
                downloadService.loadDownloadFile(
                        "token",
                        "Secret123"
                );

        assertNotNull(result);
        assertEquals(resource, result.resource());
        assertEquals("test.txt", result.originalName());
        assertEquals("text/plain", result.mimeType());

        verify(passwordEncoder)
                .matches(
                        "Secret123",
                        "hashed-password"
                );

        verify(storageService)
                .load("stored-test.txt");
    }

    @Test
    void loadDownloadFile_should_throw_when_password_is_missing() {

        storedFile.setPasswordHash("hashed-password");

        when(downloadLinkService.findValidLink("token"))
                .thenReturn(downloadLink);

        assertThrows(
                InvalidFilePasswordException.class,
                () -> downloadService.loadDownloadFile(
                        "token",
                        null
                )
        );

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(storageService);
    }

    @Test
    void loadDownloadFile_should_throw_when_password_is_incorrect() {

        storedFile.setPasswordHash("hashed-password");

        when(downloadLinkService.findValidLink("token"))
                .thenReturn(downloadLink);

        when(passwordEncoder.matches(
                "WrongPassword",
                "hashed-password"
        )).thenReturn(false);

        assertThrows(
                InvalidFilePasswordException.class,
                () -> downloadService.loadDownloadFile(
                        "token",
                        "WrongPassword"
                )
        );

        verify(passwordEncoder)
                .matches(
                        "WrongPassword",
                        "hashed-password"
                );

        verifyNoInteractions(storageService);
    }
}
