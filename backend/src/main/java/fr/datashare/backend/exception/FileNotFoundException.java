package fr.datashare.backend.exception;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
        super("Le fichier est introuvable.");
    }
}

