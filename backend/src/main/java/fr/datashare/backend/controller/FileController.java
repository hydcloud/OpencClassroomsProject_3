package fr.datashare.backend.controller;

import fr.datashare.backend.dto.file.DownloadFileInfoResponse;
import fr.datashare.backend.dto.file.FileUploadResponse;
import fr.datashare.backend.service.FileDeletionService;
import fr.datashare.backend.service.FileHistoryService;
import fr.datashare.backend.service.DownloadService;
import fr.datashare.backend.dto.file.FileHistoryResponse;
import fr.datashare.backend.service.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import fr.datashare.backend.dto.file.DownloadFileRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final UploadService uploadService;
    private final FileHistoryService fileHistoryService;
    private final FileDeletionService fileDeletionService;
    private final DownloadService downloadService;

    public FileController(UploadService uploadService,
                          FileHistoryService fileHistoryService,
                          FileDeletionService fileDeletionService,
                          DownloadService downloadService)
    {
        this.uploadService = uploadService;
        this.fileHistoryService = fileHistoryService;
        this.fileDeletionService = fileDeletionService;
        this.downloadService = downloadService;
    }

    @PostMapping
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(
                    name = "expirationDays",
                    defaultValue = "7"
            ) int expirationDays,
            @RequestParam(
                    name = "password",
                    required = false
            ) String password,
            Authentication authentication
    ) {
        FileUploadResponse response = uploadService.upload(
                file,
                expirationDays,
                authentication.getName(),
                password
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/anonymous")
    public ResponseEntity<FileUploadResponse> uploadAnonymous(
            @RequestParam("file") MultipartFile file,
            @RequestParam(
                    name = "expirationDays",
                    defaultValue = "7"
            ) int expirationDays,
            @RequestParam(
                    name = "password",
                    required = false
            ) String password
    ) {
        FileUploadResponse response =
                uploadService.uploadAnonymous(
                        file,
                        expirationDays,
                        password
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public List<FileHistoryResponse> history(
            Authentication authentication
    ) {
        return fileHistoryService.getHistory(
                authentication.getName()
        );
    }

    @GetMapping("/{token}")
    public ResponseEntity<DownloadFileInfoResponse> getFileInfo(
            @PathVariable String token
    ) {
        DownloadFileInfoResponse response =
                downloadService.getFileInfo(token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{token}/file")
    public ResponseEntity<Resource> download(
            @PathVariable String token,
            @RequestBody(required = false) DownloadFileRequest request
    ) {
        String password =
                request == null ? null : request.password();

        DownloadService.DownloadResource download =
                downloadService.loadDownloadFile(
                        token,
                        password
                );

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        fileDeletionService.deleteFile(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}
