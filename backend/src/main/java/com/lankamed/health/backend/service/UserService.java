package com.lankamed.health.backend.service;

import com.lankamed.health.backend.model.User;
import com.lankamed.health.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User updateProfile(String email, String firstName, String lastName) {
        User user = getByEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = getByEmail(email);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void resetPasswordByUserId(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long userId) {
        userRepository.deleteById(userId);
    }
}
