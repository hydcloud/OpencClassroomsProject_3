package fr.datashare.backend.controller;

import fr.datashare.backend.dto.auth.RegisterRequest;
import fr.datashare.backend.dto.auth.UserResponse;
import fr.datashare.backend.service.AuthService;
import fr.datashare.backend.dto.auth.LoginRequest;
import fr.datashare.backend.dto.auth.LoginResponse;
import fr.datashare.backend.service.UserRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRegistrationService userRegistrationService;
    private final AuthService authService;

    public UserController(
            UserRegistrationService userRegistrationService,
            AuthService authService
    ) {
        this.userRegistrationService = userRegistrationService;
        this.authService = authService;
    }

    @PostMapping("/user")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse response =
                userRegistrationService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }
}