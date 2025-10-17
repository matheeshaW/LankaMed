package com.lankamed.health.backend.controller;

import com.lankamed.health.backend.dto.AuthDtos.AuthResponse;
import com.lankamed.health.backend.dto.AuthDtos.LoginRequest;
import com.lankamed.health.backend.dto.AuthDtos.RegisterRequest;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.service.AuthService;
import com.lankamed.health.backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password(),
                request.role()
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/test-login")
    public ResponseEntity<AuthResponse> testLogin(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(new AuthResponse("Invalid credentials"));
            }

            // Try to authenticate with the provided credentials
            String token = authService.login(email, password);

            // Get user details for response
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.ok(new AuthResponse("User not found"));
            }

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            System.err.println("Test login failed: " + e.getMessage());
            return ResponseEntity.ok(new AuthResponse("Authentication failed"));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testBackend() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Backend is running successfully");
            response.put("timestamp", java.time.Instant.now());
            response.put("status", "Backend API is operational");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
