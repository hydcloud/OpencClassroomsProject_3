package fr.datashare.backend.dto.file;

import java.time.LocalDateTime;

public record DownloadFileInfoResponse(
        String originalName,
        Long size,
        LocalDateTime expiresAt,
        boolean passwordProtected
) {
}
