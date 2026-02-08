package main.java.com.talent.expense_managerdb.services;

import main.java.com.talent.expense_managerdb.exception.AuthenticationException;
import main.java.com.talent.expense_managerdb.exception.ValidationException;
import main.java.com.talent.expense_managerdb.model.Account;
import main.java.com.talent.expense_managerdb.repository.AccountRepository;
import main.java.com.talent.expense_managerdb.util.IdGenerator;
import main.java.com.talent.expense_managerdb.util.PasswordUtil;

public class AccountService {

    private final AccountRepository accountRepository = new AccountRepository();

    public Account register(String name, String email, String password) {

        if (name == null || name.isBlank()) {
            throw new ValidationException("Name cannot be empty");
        }

        if (email == null || email.isBlank()) {
            throw new ValidationException("Email cannot be empty");
        }

        if (accountRepository.findByEmail(email) != null) {
            throw new ValidationException("Email already registered");
        }

        String accountId = IdGenerator.accountId();
        String passwordHash = PasswordUtil.hash(password);

        Account account = new Account(accountId, name, email, passwordHash);
        accountRepository.save(account);

        return account;
    }

    public Account login(String email, String password) {

        Account account = accountRepository.findByEmail(email);

        if (account == null) {
            throw new AuthenticationException("Account not found");
        }

        String hashed = PasswordUtil.hash(password);

        if (!hashed.equals(account.getPasswordHash())) {
            throw new AuthenticationException("Invalid password");
        }

        return account;
    }
}
