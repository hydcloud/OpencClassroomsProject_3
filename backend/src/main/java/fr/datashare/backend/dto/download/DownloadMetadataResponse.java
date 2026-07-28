package fr.datashare.backend.dto.download;

import java.time.LocalDateTime;

public record DownloadMetadataResponse(
        String originalName,
        String mimeType,
        long size,
        LocalDateTime expiresAt
) {
}
