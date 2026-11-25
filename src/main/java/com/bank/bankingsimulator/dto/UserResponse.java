package com.bank.bankingsimulator.dto;

public record UserResponse(Long id, String email, String fullName, String role, String language) {
}