package com.bank.bankingsimulator.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank(message = "Account name is required") String name,
        @NotBlank(message = "Currency is required") String currency,
        @DecimalMin(value = "0.00", message = "Initial deposit must be 0 or more") BigDecimal initialDeposit) {
}