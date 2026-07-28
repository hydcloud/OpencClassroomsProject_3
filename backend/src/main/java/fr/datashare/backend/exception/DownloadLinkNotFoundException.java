package fr.datashare.backend.exception;

public class DownloadLinkNotFoundException extends RuntimeException {

    public DownloadLinkNotFoundException() {
        super("Le lien de téléchargement est invalide.");
    }
}
