package com.bank.bankingsimulator.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Source account is required") Long fromAccountId,
        @NotNull(message = "Destination account is required") Long toAccountId,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 17, fraction = 2, message = "Amount must have at most 2 decimal places") BigDecimal amount,
        Long categoryId,
        String description) {
}
