package main.java.com.talent.expense_managerdb;

import main.java.com.talent.expense_managerdb.model.*;
import main.java.com.talent.expense_managerdb.model.enum_type.*;
import main.java.com.talent.expense_managerdb.repository.*;
import main.java.com.talent.expense_managerdb.services.*;
import main.java.com.talent.expense_managerdb.util.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ExpenseManagerDB_Application {

    private static final Scanner sc = new Scanner(System.in);

    private static final AccountService accountService = new AccountService();
    private static final WalletService walletService = new WalletService();
    private static final TransactionService transactionService = new TransactionService();

    private static final WalletRepository walletRepository = new WalletRepository();
    private static final TransactionRepository transactionRepository = new TransactionRepository();
    private static final TransactionAuditRepository auditRepository = new TransactionAuditRepository();

    public static void main(String[] args) {
        System.out.println("--------------------------------------");
        System.out.println("      EXPENSE MANAGER DATABASE        ");
        System.out.println("--------------------------------------");

        while (true) {
            try {
                System.out.println("\nMAIN MENU");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Selection: ");

                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> handleRegister();
                    case "2" -> handleLogin();
                    case "3" -> {
                        System.out.println("Application terminated.");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid selection. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ================= AUTHENTICATION =================

    private static void handleRegister() {
        System.out.println("\n[ REGISTRATION ]");
        String name = readRequiredString("Name: ");
        String email = readRequiredString("Email: ");
        String password = readRequiredString("Password: ");

        try {
            Account acc = accountService.register(name, email, password);
            System.out.println("Registration successful. Account ID: " + acc.getAccountId());
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void handleLogin() {
        System.out.println("\n[ LOGIN ]");
        String email = readRequiredString("Email: ");
        String password = readRequiredString("Password: ");

        try {
            Account acc = accountService.login(email, password);
            System.out.println("Access granted. Welcome, " + acc.getName());

            MyWallet wallet = walletRepository.findByAccountId(acc.getAccountId());
            if (wallet == null) {
                wallet = createWalletFlow(acc);
            }

            if (wallet != null) {
                walletMenu(acc, wallet);
            }
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    // ================= WALLET SETUP =================

    private static MyWallet createWalletFlow(Account acc) {
        System.out.println("\nNotice: No wallet found for this account.");
        System.out.println("Initializing wallet setup...");

        double initial = readDouble("Initial Balance: ");
        double budget = readDouble("Monthly Budget Limit: ");

        try {
            walletService.createWallet(acc.getAccountId(), initial, budget);
            System.out.println("Wallet initialized successfully.");
            return walletRepository.findByAccountId(acc.getAccountId());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    // ================= MAIN DASHBOARD =================

    private static void walletMenu(Account acc, MyWallet wallet) {
        boolean sessionActive = true;
        while (sessionActive) {
            try {
                wallet = walletRepository.findByAccountId(acc.getAccountId());
                showWalletHeader(wallet);

                System.out.println("1. Manage Income");
                System.out.println("2. Manage Expense");
                System.out.println("3. View Transactions by Type");
                System.out.println("4. View Budget Status");
                System.out.println("5. View Transactions by Date");
                System.out.println("6. View Monthly Summary");
                System.out.println("7. View Audit Logs");
                System.out.println("8. Logout");
                System.out.print("Selection: ");

                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> incomeMenu(wallet);
                    case "2" -> expenseMenu(wallet);
                    case "3" -> showByType(wallet);
                    case "4" -> showBudget(wallet);
                    case "5" -> showByDateRange(wallet);
                    case "6" -> showMonthlySummary(wallet);
                    case "7" -> showAuditLogs();
                    case "8" -> sessionActive = false;
                    default -> System.out.println("Invalid selection.");
                }

                if (sessionActive) promptEnterKey();

            } catch (Exception e) {
                System.out.println("Runtime Error: " + e.getMessage());
                promptEnterKey();
            }
        }
    }

    private static void showWalletHeader(MyWallet wallet) {
        double currentBalance = walletRepository.getFinalBalance(wallet.getWalletId());
        double balance = walletService.getTotalIncome(wallet.getWalletId());
        double expense = walletService.getTotalExpense(wallet.getWalletId());

        System.out.println("===============================");
        System.out.println("Wallet ID : " + wallet.getWalletId());
        System.out.println("Balance   : " + balance);
        System.out.println("Expense   : " + expense);
        System.out.println("Budget    : " + wallet.getBudgetLimit());
        System.out.println("===============================");

    }

    // ================= TRANSACTION MANAGEMENT =================

    private static void incomeMenu(MyWallet wallet) {
        System.out.println("\n[ INCOME MANAGEMENT ]");
        System.out.println("1. Add Income\n2. Edit Income\n3. Delete Income");
        System.out.print("Selection: ");
        String c = sc.nextLine().trim();

        try {
            switch (c) {
                case "1" -> {
                    double amt = readDouble("Amount: ");
                    IncomeType type = readEnum("Source", IncomeType.class);
                    Income inc = new Income(IdGenerator.incomeId(), wallet.getWalletId(), amt, type);
                    transactionService.add(inc);
                    System.out.println("Income recorded.");
                }
                case "2" -> {
                    printTransactionsByType(wallet, "INCOME");
                    String id = readRequiredString("Target Income ID: ");
                    double newAmt = readDouble("New Amount: ");
                    transactionService.edit(id, newAmt);
                    System.out.println("Income updated.");
                }
                case "3" -> {
                    printTransactionsByType(wallet, "INCOME");
                    String id = readRequiredString("Target Income ID: ");
                    transactionService.delete(id);
                    System.out.println("Income record removed.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void expenseMenu(MyWallet wallet) {
        System.out.println("\n[ EXPENSE MANAGEMENT ]");
        System.out.println("1. Add Expense\n2. Edit Expense\n3. Delete Expense");
        System.out.print("Selection: ");
        String c = sc.nextLine().trim();

        try {
            switch (c) {
                case "1" -> {
                    double amt = readDouble("Amount: ");
                    ExpenseType type = readEnum("Category", ExpenseType.class);
                    Expense exp = new Expense(IdGenerator.expenseId(), wallet.getWalletId(), amt, type);
                    transactionService.add(exp);
                    System.out.println("Expense recorded.");
                }
                case "2" -> {
                    printTransactionsByType(wallet, "EXPENSE");
                    String id = readRequiredString("Target Expense ID: ");
                    double newAmt = readDouble("New Amount: ");
                    transactionService.edit(id, newAmt);
                    System.out.println("Expense updated.");
                }
                case "3" -> {
                    printTransactionsByType(wallet, "EXPENSE");
                    String id = readRequiredString("Target Expense ID: ");
                    transactionService.delete(id);
                    System.out.println("Expense record removed.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================= DATA VISUALIZATION =================

    private static void showBudget(MyWallet wallet) {
        double totalExpense = transactionRepository.findByWallet(wallet.getWalletId())
                .stream()
                .filter(t -> t.getTransactionType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getTransactionAmount)
                .sum();

        double remaining = wallet.getBudgetLimit() - totalExpense;
        String status = (remaining < 0) ? "OVER BUDGET" : "UNDER BUDGET";

        System.out.println("\n[ BUDGET STATUS ]");
        System.out.printf("Limit     : %.2f%n", wallet.getBudgetLimit());
        System.out.printf("Spent     : %.2f%n", totalExpense);
        System.out.printf("Remaining : %.2f (%s)%n", remaining, status);
    }

    private static void showAuditLogs() {
        System.out.println("\n[ SYSTEM AUDIT LOGS ]");
        auditRepository.findAll().forEach(log ->
                System.out.printf("%s | %-10s | ID: %s | %.2f -> %.2f%n",
                        log.getCreatedAt(), log.getAction(), log.getTransactionId(), log.getOldValue(), log.getNewValue())
        );
    }

    // ================= INPUT HELPERS =================

    private static String readRequiredString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Field cannot be empty.");
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format (YYYY-MM-DD).");
            }
        }
    }

    private static <T extends Enum<T>> T readEnum(String label, Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        while (true) {
            System.out.println("\nAvailable " + label + " options:");
            for (int i = 0; i < constants.length; i++) {
                System.out.println((i + 1) + ". " + constants[i]);
            }
            System.out.print("Select index or name: ");
            String input = sc.nextLine().trim().toUpperCase();

            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < constants.length) return constants[index];
            } catch (NumberFormatException ignored) {}

            try {
                return Enum.valueOf(enumClass, input);
            } catch (IllegalArgumentException ignored) {}

            System.out.println("Invalid option.");
        }
    }

    private static void promptEnterKey() {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }

    private static void printTransactionsByType(MyWallet wallet, String typeStr) {
        System.out.println("\n--- " + typeStr + " LIST ---");
        transactionRepository.findByWallet(wallet.getWalletId()).stream()
                .filter(t -> t.getTransactionType().name().equalsIgnoreCase(typeStr))
                .forEach(t -> System.out.printf("ID: %s | Category: %s | Amount: %.2f%n",
                        t.getTransactionId(), t.getCategory(), t.getTransactionAmount()));
    }

    private static void showByType(MyWallet wallet) {
        System.out.println("\n1. INCOME\n2. EXPENSE");
        System.out.print("Selection: ");
        String type = sc.nextLine().trim();
        if (type.equals("1")) printTransactionsByType(wallet, "INCOME");
        else if (type.equals("2")) printTransactionsByType(wallet, "EXPENSE");
    }

    private static void showByDateRange(MyWallet wallet) {
        LocalDate from = readDate("Start Date (yyyy-mm-dd): ");
        LocalDate to = readDate("End Date (yyyy-mm-dd): ");
        List<Transaction> list = transactionRepository.findByDateRange(wallet.getWalletId(), from, to);
        if (list.isEmpty()) System.out.println("No records found.");
        else list.forEach(t -> System.out.printf("%s | %s | %.2f | %s%n",
                t.getTransactionId(), t.getTransactionType(), t.getTransactionAmount(), t.getCreatedAt().toLocalDate()));
    }

    private static void showMonthlySummary(MyWallet wallet) {
        int year = (int) readDouble("Year: ");
        int month = (int) readDouble("Month (1-12): ");
        double incTotal = 0, expTotal = 0;
        for (Transaction tx : transactionRepository.findByWallet(wallet.getWalletId())) {
            LocalDate date = tx.getCreatedAt().toLocalDate();
            if (date.getYear() == year && date.getMonthValue() == month) {
                if (tx.getTransactionType() == TransactionType.INCOME) incTotal += tx.getTransactionAmount();
                else expTotal += tx.getTransactionAmount();
            }
        }
        System.out.printf("Summary: Income: %.2f | Expense: %.2f | Net: %.2f%n",
                incTotal, expTotal, (incTotal - expTotal));
    }
}