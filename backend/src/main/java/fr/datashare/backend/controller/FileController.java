package fr.datashare.backend.controller;

import fr.datashare.backend.dto.file.FileUploadResponse;
import fr.datashare.backend.service.FileService;
import fr.datashare.backend.dto.file.FileHistoryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import fr.datashare.backend.dto.download.DownloadMetadataResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(
                    name = "expirationDays",
                    defaultValue = "7"
            ) int expirationDays,
            Authentication authentication
    ) {
        FileUploadResponse response = fileService.upload(
                file,
                expirationDays,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public List<FileHistoryResponse> history(
            Authentication authentication
    ) {
        return fileService.getHistory(
                authentication.getName()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        fileService.deleteFile(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

        @GetMapping("/{token}")
        public ResponseEntity<DownloadMetadataResponse> getMetadata(
                @PathVariable String token
        ) {
            return ResponseEntity.ok(
                    fileService.getDownloadMetadata(token)
            );
        }

        @GetMapping("/{token}/file")
        public ResponseEntity<Resource> download(
                @PathVariable String token
        ) {
            FileService.DownloadResource download =
                    fileService.loadDownloadFile(token);

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
