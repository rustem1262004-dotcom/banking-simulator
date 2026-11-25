package com.bank.bankingsimulator.dto;

import java.math.BigDecimal;

public record AccountResponse(Long id, String number, String name, String currency, BigDecimal balance) {
}
