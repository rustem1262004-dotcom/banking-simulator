package com.bank.bankingsimulator.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(Long id, String uuid, String type, BigDecimal amount, String status,
                                  Long fromAccountId, Long toAccountId, String description,
                                  LocalDateTime createdAt) {
}
