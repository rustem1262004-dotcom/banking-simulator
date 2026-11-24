
package com.bankingsimulator.testconnection;

import com.bankingsimulator.repository.UserRepository;
import com.bankingsimulator.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public DatabaseConnectionTest(
            UserRepository userRepository,
            AccountRepository accountRepository) {

        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) {

        System.out.println("=== DATABASE CONNECTION TEST ===");

        userRepository.findByEmail("demo.customer@example.com")
                .ifPresentOrElse(
                        user -> {
                            System.out.println("DATABASE TEST SUCCESS!");
                            System.out.println("User ID: " + user.getId());
                            System.out.println("Full name: " + user.getFullName());
                            System.out.println("Role: " + user.getRole());

                            System.out.println("=== BANK ACCOUNTS ===");

                            var accounts = accountRepository.findByUserId(user.getId());

                            if (accounts.isEmpty()) {
                                System.out.println("No accounts found.");
                            } else {
                                accounts.forEach(account -> {
                                    System.out.println("Account: " + account.getName());
                                    System.out.println("Number: " + account.getNumber());
                                    System.out.println("Balance: " + account.getBalance());
                                    System.out.println("Currency: " + account.getCurrency());
                                    System.out.println("--------------------");
                                });
                            }
                        },
                        () -> System.out.println("User not found in database.")
                );

        System.out.println("=== TEST FINISHED ===");
    }
}
