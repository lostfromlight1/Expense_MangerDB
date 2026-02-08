package main.java.com.talent.expense_managerdb.util;

import main.java.com.talent.expense_managerdb.exception.ValidationException;

import java.util.UUID;

public class IdGenerator {

    private static String generate(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new ValidationException("ID prefix cannot be empty");
        }
        return prefix +  UUID.randomUUID().toString().substring(0, 5);
    }

    public static String accountId() {
        return generate("ACC-");
    }

    public static String walletId() {
        return generate("WAL-");
    }

    public static String incomeId() {
        return generate("INC-");
    }

    public static String expenseId() {
        return generate("EXP-");
    }

    public static String logId() {
        return generate("LOG-");
    }
}
