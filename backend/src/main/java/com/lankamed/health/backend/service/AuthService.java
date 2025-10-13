package com.lankamed.health.backend.service;

import com.lankamed.health.backend.model.Role;
import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.UserRepository;
import com.lankamed.health.backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public User register(String firstName, String lastName, String email, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(role)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();
        return userRepository.save(user);
    }

    public String login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        String role = authentication.getAuthorities().stream().findFirst().orElseThrow().getAuthority().replace("ROLE_", "");
        return jwtUtil.generateToken(email, role);
    }
}
