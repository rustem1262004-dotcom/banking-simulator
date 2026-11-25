package com.bank.bankingsimulator.service;

import com.bank.bankingsimulator.dto.RegisterRequest;
import com.bank.bankingsimulator.dto.UserResponse;
import com.bank.bankingsimulator.entity.Role;
import com.bank.bankingsimulator.entity.User;
import com.bank.bankingsimulator.repository.UserRepository;
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
        User saved = userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getEmail(), saved.getFullName(),
                saved.getRole().name(), saved.getLanguage());
    }
}