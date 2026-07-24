package fr.datashare.backend.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Un compte existe déjà avec l'adresse email : " + email);
    }

}