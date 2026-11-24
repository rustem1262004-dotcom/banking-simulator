
package com.bankingsimulator.testconnection;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.io.Console;
import java.util.Arrays;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        Console console = System.console();

        if (console == null) {
            System.out.println(
                    "Run this program from a system terminal."
            );
            return;
        }

        char[] password = console.readPassword(
                "Enter a new demo password: "
        );

        if (password == null || password.length == 0) {
            System.out.println("Password cannot be empty.");
            return;
        }

        try {
            BCryptPasswordEncoder encoder =
                    new BCryptPasswordEncoder();

            String hash = encoder.encode(
                    new String(password)
            );

            System.out.println("BCrypt hash:");
            System.out.println(hash);
            System.out.println("Hash length: " + hash.length());
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
