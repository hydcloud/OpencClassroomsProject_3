package fr.datashare.backend.controller;

import fr.datashare.backend.dto.file.FileHistoryResponse;
import fr.datashare.backend.dto.file.FileUploadResponse;
import fr.datashare.backend.security.CustomUserDetailsService;
import fr.datashare.backend.security.JwtAuthenticationFilter;
import fr.datashare.backend.security.JwtService;
import fr.datashare.backend.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UploadService uploadService;

    @MockitoBean
    private FileHistoryService fileHistoryService;

    @MockitoBean
    private FileDeletionService fileDeletionService;

    @MockitoBean
    private DownloadService downloadService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void upload_should_return_201_created() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "contenu du fichier".getBytes()
        );

        FileUploadResponse response = new FileUploadResponse(
                1L,
                "test.pdf",
                "application/pdf",
                file.getSize(),
                LocalDateTime.of(2026, 7, 29, 10, 0),
                LocalDateTime.of(2026, 8, 1, 10, 0),
                "download-token",
                "/api/downloads/download-token",
                false
        );

        when(uploadService.upload(
                any(),
                eq(3),
                eq("stephane@example.com"),
                isNull()
        )).thenReturn(response);

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "stephane@example.com",
                        null,
                        List.of()
                );

        mockMvc.perform(
                        multipart("/api/files")
                                .file(file)
                                .param("expirationDays", "3")
                                .principal(authentication)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.originalName").value("test.pdf"))
                .andExpect(jsonPath("$.downloadToken")
                        .value("download-token"));

        verify(uploadService).upload(
                any(),
                eq(3),
                eq("stephane@example.com"),
                isNull()
        );
    }

    @Test
    void history_should_return_200_and_user_files() throws Exception {

        FileHistoryResponse fileResponse = new FileHistoryResponse(
                1L,
                "test.pdf",
                "application/pdf",
                1234L,
                LocalDateTime.of(2026, 7, 29, 10, 0),
                LocalDateTime.of(2026, 8, 1, 10, 0),
                "download-token",
                false
        );

        when(fileHistoryService.getHistory("stephane@example.com"))
                .thenReturn(List.of(fileResponse));

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "stephane@example.com",
                        null,
                        List.of()
                );

        mockMvc.perform(
                        get("/api/files")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].originalName")
                        .value("test.pdf"))
                .andExpect(jsonPath("$[0].downloadToken")
                        .value("download-token"));

        verify(fileHistoryService)
                .getHistory("stephane@example.com");
    }

    @Test
    void delete_should_return_204_no_content() throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "stephane@example.com",
                        null,
                        List.of()
                );

        mockMvc.perform(
                        delete("/api/files/{id}", 1L)
                                .principal(authentication)
                )
                .andExpect(status().isNoContent());

        verify(fileDeletionService)
                .deleteFile(1L, "stephane@example.com");
    }
}