package fr.datashare.backend.dto.file;

import java.time.LocalDateTime;

public record FileUploadResponse(
        Long id,
        String originalName,
        String mimeType,
        long size,
        LocalDateTime uploadedAt,
        LocalDateTime expiresAt,
        String downloadToken,
        String downloadUrl
) {
}
