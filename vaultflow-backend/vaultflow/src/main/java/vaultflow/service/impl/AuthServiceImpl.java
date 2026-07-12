package vaultflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vaultflow.dto.request.LoginRequest;
import vaultflow.dto.request.RegisterRequest;
import vaultflow.dto.response.AuthResponse;
import vaultflow.entity.User;
import vaultflow.repository.UserRepository;
import vaultflow.security.JwtService;
import vaultflow.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken("")
                .userId(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .refreshToken("")
                .userId(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        // Refresh token support will be implemented later
    }
}