package fr.datashare.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "L'adresse email est obligatoire.")
        @Email(message = "L'adresse email doit être valide.")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire.")
        @Size(
                min = 8,
                message = "Le mot de passe doit contenir au moins 8 caractères."
        )
        String password
) {
}