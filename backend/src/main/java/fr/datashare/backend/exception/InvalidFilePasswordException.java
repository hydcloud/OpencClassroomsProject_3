package fr.datashare.backend.exception;

public class InvalidFilePasswordException extends RuntimeException {

    public InvalidFilePasswordException() {
        super("Mot de passe du fichier invalide.");
    }
}
