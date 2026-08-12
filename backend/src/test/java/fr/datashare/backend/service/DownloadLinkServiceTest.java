package fr.datashare.backend.service;

import fr.datashare.backend.entity.DownloadLink;
import fr.datashare.backend.entity.StoredFile;
import fr.datashare.backend.exception.DownloadLinkExpiredException;
import fr.datashare.backend.exception.DownloadLinkNotFoundException;
import fr.datashare.backend.repository.DownloadLinkRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DownloadLinkServiceTest {

    @Mock
    private DownloadLinkRepository downloadLinkRepository;

    private DownloadLinkService downloadLinkService;

    @BeforeEach
    void setUp() {
        downloadLinkService =
                new DownloadLinkService(downloadLinkRepository);
    }

    @Test
    void createLink_should_create_and_save_download_link() {

        StoredFile storedFile = new StoredFile();

        LocalDateTime expiresAt =
                LocalDateTime.now().plusDays(7);

        when(downloadLinkRepository.existsByToken(anyString()))
                .thenReturn(false);

        when(downloadLinkRepository.save(any(DownloadLink.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        DownloadLink result =
                downloadLinkService.createLink(
                        storedFile,
                        expiresAt
                );

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(expiresAt, result.getExpiresAt());
        assertEquals(storedFile, result.getStoredFile());

        verify(downloadLinkRepository)
                .existsByToken(result.getToken());

        verify(downloadLinkRepository)
                .save(any(DownloadLink.class));
    }

    @Test
    void findValidLink_should_return_valid_link() {

        DownloadLink downloadLink = new DownloadLink();

        downloadLink.setExpiresAt(
                LocalDateTime.now().plusDays(1)
        );

        when(downloadLinkRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(downloadLink));

        DownloadLink result =
                downloadLinkService.findValidLink(
                        "valid-token"
                );

        assertEquals(downloadLink, result);

        verify(downloadLinkRepository)
                .findByToken("valid-token");
    }

    @Test
    void findValidLink_should_throw_when_link_does_not_exist() {

        when(downloadLinkRepository.findByToken("unknown-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                DownloadLinkNotFoundException.class,
                () -> downloadLinkService.findValidLink(
                        "unknown-token"
                )
        );
    }

    @Test
    void findValidLink_should_throw_when_link_is_expired() {

        DownloadLink downloadLink = new DownloadLink();

        downloadLink.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        when(downloadLinkRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(downloadLink));

        assertThrows(
                DownloadLinkExpiredException.class,
                () -> downloadLinkService.findValidLink(
                        "expired-token"
                )
        );
    }
}
