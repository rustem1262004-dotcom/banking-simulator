package com.bank.bankingsimulator.service;

import com.bank.bankingsimulator.dto.LoginRequest;
import com.bank.bankingsimulator.dto.RegisterRequest;
import com.bank.bankingsimulator.dto.UserResponse;
import com.bank.bankingsimulator.entity.Role;
import com.bank.bankingsimulator.entity.User;
import com.bank.bankingsimulator.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("This email is already registered");
        }
        String language = request.language() != null && Set.of("kk", "ru", "en").contains(request.language())
                ? request.language() : "en";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(Role.CUSTOMER);
        user.setLanguage(language);
        return toResponse(userRepository.save(user));
    }

    public UserResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return toResponse(user);
    }

    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole().name(), u.getLanguage());
    }
}