package com.bank.bankingsimulator.controller;

import com.bank.bankingsimulator.dto.OperationRequest;
import com.bank.bankingsimulator.dto.TransactionResponse;
import com.bank.bankingsimulator.dto.TransferRequest;
import com.bank.bankingsimulator.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse deposit(@AuthenticationPrincipal Long userId,
                                       @Valid @RequestBody OperationRequest request) {
        return transactionService.deposit(userId, request);
    }

    @PostMapping("/withdraw")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse withdraw(@AuthenticationPrincipal Long userId,
                                        @Valid @RequestBody OperationRequest request) {
        return transactionService.withdraw(userId, request);
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse transfer(@AuthenticationPrincipal Long userId,
                                        @Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(userId, request);
    }

    @GetMapping
    public List<TransactionResponse> history(@AuthenticationPrincipal Long userId,
                                             @RequestParam Long accountId) {
        return transactionService.history(userId, accountId);
    }
}