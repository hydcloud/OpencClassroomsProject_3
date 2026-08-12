package fr.datashare.backend.dto.auth;

public record LoginResponse(
        String token,
        String email
) {
}
