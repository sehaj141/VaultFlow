package vaultflow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vaultflow.dto.request.LoginRequest;
import vaultflow.dto.request.RegisterRequest;
import vaultflow.dto.response.AuthResponse;
import vaultflow.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {
                System.out.println("LOGIN ENDPOINT HIT");
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout(
            @RequestBody Map<String, String> request) {

        authService.logout(request.get("refreshToken"));
    }
}