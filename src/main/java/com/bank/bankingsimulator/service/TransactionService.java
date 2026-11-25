package com.bank.bankingsimulator.service;

import com.bank.bankingsimulator.dto.OperationRequest;
import com.bank.bankingsimulator.dto.TransactionResponse;
import com.bank.bankingsimulator.dto.TransferRequest;
import com.bank.bankingsimulator.entity.Account;
import com.bank.bankingsimulator.entity.Transaction;
import com.bank.bankingsimulator.entity.TransactionStatus;
import com.bank.bankingsimulator.entity.TransactionType;
import com.bank.bankingsimulator.repository.AccountRepository;
import com.bank.bankingsimulator.repository.CategoryRepository;
import com.bank.bankingsimulator.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private static final String NO_FUNDS = "Insufficient funds: the amount exceeds the available balance";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public TransactionResponse deposit(OperationRequest r) {
        Account account = lock(r.accountId());
        account.setBalance(account.getBalance().add(r.amount()));
        accountRepository.save(account);
        return record(TransactionType.DEPOSIT, null, account, r.amount(), r.categoryId(), r.description());
    }

    @Transactional
    public TransactionResponse withdraw(OperationRequest r) {
        Account account = lock(r.accountId());
        if (account.getBalance().compareTo(r.amount()) < 0) {
            throw new IllegalArgumentException(NO_FUNDS);
        }
        account.setBalance(account.getBalance().subtract(r.amount()));
        accountRepository.save(account);
        return record(TransactionType.WITHDRAWAL, account, null, r.amount(), r.categoryId(), r.description());
    }

    // Atomic transfer: if anything fails, the whole transaction is rolled back.
    @Transactional
    public TransactionResponse transfer(TransferRequest r) {
        if (r.fromAccountId().equals(r.toAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        // Lock accounts in a fixed order (by id) to avoid deadlocks.
        Account first = lock(Math.min(r.fromAccountId(), r.toAccountId()));
        Account second = lock(Math.max(r.fromAccountId(), r.toAccountId()));
        Account from = first.getId().equals(r.fromAccountId()) ? first : second;
        Account to = from == first ? second : first;

        if (!from.getCurrency().equals(to.getCurrency())) {
            throw new IllegalArgumentException("Accounts must have the same currency");
        }
        if (from.getBalance().compareTo(r.amount()) < 0) {
            throw new IllegalArgumentException(NO_FUNDS);
        }
        from.setBalance(from.getBalance().subtract(r.amount()));
        to.setBalance(to.getBalance().add(r.amount()));
        accountRepository.save(from);
        accountRepository.save(to);
        return record(TransactionType.TRANSFER, from, to, r.amount(), r.categoryId(), r.description());
    }

    public List<TransactionResponse> history(Long accountId) {
        return transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(accountId, accountId)
                .stream().map(this::toResponse).toList();
    }

    private Account lock(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    private TransactionResponse record(TransactionType type, Account from, Account to, BigDecimal amount,
                                       Long categoryId, String description) {
        Transaction t = new Transaction();
        t.setType(type);
        t.setFromAccount(from);
        t.setToAccount(to);
        t.setAmount(amount);
        t.setStatus(TransactionStatus.COMPLETED);
        t.setDescription(description);
        if (categoryId != null) {
            t.setCategory(categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found")));
        }
        return toResponse(transactionRepository.save(t));
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(t.getId(), t.getUuid(), t.getType().name(), t.getAmount(),
                t.getStatus().name(),
                t.getFromAccount() == null ? null : t.getFromAccount().getId(),
                t.getToAccount() == null ? null : t.getToAccount().getId(),
                t.getDescription(), t.getCreatedAt());
    }
}
