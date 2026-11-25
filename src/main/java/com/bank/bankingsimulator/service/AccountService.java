package com.bank.bankingsimulator.service;

import com.bank.bankingsimulator.dto.AccountResponse;
import com.bank.bankingsimulator.dto.CreateAccountRequest;
import com.bank.bankingsimulator.entity.Account;
import com.bank.bankingsimulator.entity.Transaction;
import com.bank.bankingsimulator.entity.TransactionStatus;
import com.bank.bankingsimulator.entity.TransactionType;
import com.bank.bankingsimulator.entity.User;
import com.bank.bankingsimulator.repository.AccountRepository;
import com.bank.bankingsimulator.repository.TransactionRepository;
import com.bank.bankingsimulator.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        BigDecimal initial = request.initialDeposit() == null ? BigDecimal.ZERO : request.initialDeposit();

        Account account = new Account();
        account.setUser(user);
        account.setName(request.name().trim());
        account.setCurrency(request.currency().trim().toUpperCase());
        account.setNumber(generateNumber());
        account.setBalance(initial);
        Account saved = accountRepository.save(account);

        if (initial.compareTo(BigDecimal.ZERO) > 0) {
            Transaction deposit = new Transaction();
            deposit.setToAccount(saved);
            deposit.setType(TransactionType.DEPOSIT);
            deposit.setAmount(initial);
            deposit.setStatus(TransactionStatus.COMPLETED);
            deposit.setDescription("Initial deposit");
            transactionRepository.save(deposit);
        }
        return toResponse(saved);
    }

    public List<AccountResponse> findByUser(Long userId) {
        return accountRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    private String generateNumber() {
        String number;
        do {
            number = "ACC" + ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000);
        } while (accountRepository.findByNumber(number).isPresent());
        return number;
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(a.getId(), a.getNumber(), a.getName(), a.getCurrency(), a.getBalance());
    }
}
