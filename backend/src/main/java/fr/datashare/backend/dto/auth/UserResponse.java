package fr.datashare.backend.dto.auth;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        LocalDateTime createdAt
) {
}
