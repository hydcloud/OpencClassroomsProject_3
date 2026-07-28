package fr.datashare.backend.controller;

import fr.datashare.backend.dto.file.FileUploadResponse;
import fr.datashare.backend.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
