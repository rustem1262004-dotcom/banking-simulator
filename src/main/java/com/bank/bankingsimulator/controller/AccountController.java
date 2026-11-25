package com.bank.bankingsimulator.controller;

import com.bank.bankingsimulator.dto.AccountResponse;
import com.bank.bankingsimulator.dto.CreateAccountRequest;
import com.bank.bankingsimulator.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @GetMapping
    public List<AccountResponse> list(@RequestParam Long userId) {
        return accountService.findByUser(userId);
    }
}
