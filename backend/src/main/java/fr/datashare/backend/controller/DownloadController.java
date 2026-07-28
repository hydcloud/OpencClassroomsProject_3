package fr.datashare.backend.controller;

import fr.datashare.backend.dto.download.DownloadMetadataResponse;
import fr.datashare.backend.service.DownloadService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/downloads")
public class DownloadController {

    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<DownloadMetadataResponse> getMetadata(
            @PathVariable String token
    ) {
        return ResponseEntity.ok(
                downloadService.getMetadata(token)
        );
    }

    @GetMapping("/{token}/file")
    public ResponseEntity<Resource> download(
            @PathVariable String token
    ) {
        DownloadService.DownloadResource download =
                downloadService.loadFile(token);

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(
                    download.mimeType()
            );
        } catch (IllegalArgumentException exception) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        ContentDisposition contentDisposition =
                ContentDisposition.attachment()
                        .filename(
                                download.originalName(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(download.resource());
    }
}
