package fr.datashare.backend.exception;

public class DownloadLinkExpiredException extends RuntimeException {

    public DownloadLinkExpiredException() {
        super("Le lien de téléchargement a expiré.");
    }
}
