package org.example.sudobeats.service;


import org.example.sudobeats.entity.User;
import org.example.sudobeats.repository.UserRepository;
import org.example.sudobeats.dto.AuthDtos.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        String username = request.username() == null ? "" : request.username().trim();
        String password = request.password() == null ? "" : request.password();

        if (email.isBlank() || username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username, email and password are required");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (users.existsByEmailIgnoreCase(email) || users.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("User already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        users.save(user);
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String login = request.email() == null ? "" : request.email().trim();
        String password = request.password() == null ? "" : request.password();
        if (login.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        User user = users.findByEmailIgnoreCase(normalizeEmail(login))
                .or(() -> users.findByUsernameIgnoreCase(login))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return response(user);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private AuthResponse response(User user) {
        return new AuthResponse(
                jwtService.issue(user),
                new UserView(user.getId(), user.getUsername(), user.getEmail(), user.getCurrentStreak(), user.isPro()));
    }
}

