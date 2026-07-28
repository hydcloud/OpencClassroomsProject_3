package fr.datashare.backend.dto.file;

import java.time.LocalDateTime;

public record FileHistoryResponse(

        Long id,
        String originalName,
        String mimeType,
        long size,
        LocalDateTime uploadedAt,
        LocalDateTime expiresAt,
        String downloadToken

) {
}
