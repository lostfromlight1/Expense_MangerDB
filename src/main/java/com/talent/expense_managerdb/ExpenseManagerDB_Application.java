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
        System.out.println("Welcome to Expense Manager");

        while (true) {
            try {
                System.out.println("""
                        \n=====================
                        1. Register
                        2. Login
                        3. Exit
                        =====================
                        """);
                System.out.print("Choose an option: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> handleRegister();
                    case "2" -> handleLogin();
                    case "3" -> {
                        System.out.println("Bye");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println(STR."Critical Error: \{e.getMessage()}");
                System.out.println("Restarting menu...");
            }
        }
    }

    // ================= AUTHENTICATION =================

    private static void handleRegister() {
        System.out.println("\n--- REGISTRATION ---");
        String name = readRequiredString("Enter Name: ");
        String email = readRequiredString("Enter Email: ");
        String password = readRequiredString("Enter Password: ");

        try {
            Account acc = accountService.register(name, email, password);
            System.out.println(STR."Registered successfully! Account ID: \{acc.getAccountId()}");
        } catch (Exception e) {
            System.out.println(STR."Registration Failed: \{e.getMessage()}");
        }
    }

    private static void handleLogin() {
        System.out.println("\n--- LOGIN ---");
        String email = readRequiredString("Enter Email: ");
        String password = readRequiredString("Enter Password: ");

        try {
            Account acc = accountService.login(email, password);
            System.out.println(STR."Login Successful! Welcome, \{acc.getName()}");

            MyWallet wallet = walletRepository.findByAccountId(acc.getAccountId());
            if (wallet == null) {
                wallet = createWalletFlow(acc);
            }

            walletMenu(acc, wallet);

        } catch (Exception e) {
            System.out.println(STR."Login Failed: \{e.getMessage()}");
        }
    }

    // ================= WALLET SETUP =================

    private static MyWallet createWalletFlow(Account acc) {
        System.out.println("\n⚠ No wallet found for this account.");
        System.out.println("Let's set one up.");

        double initial = readDouble("Enter Initial Balance: ");
        double budget = readDouble("Enter Monthly Budget Limit: ");

        try {
            walletService.createWallet(acc.getAccountId(), initial, budget);
            System.out.println("Wallet created successfully!");
            return walletRepository.findByAccountId(acc.getAccountId());
        } catch (Exception e) {
            System.out.println(STR."Error creating wallet: \{e.getMessage()}");
            return null;
        }
    }

    // ================= MAIN DASHBOARD =================

    private static void walletMenu(Account acc, MyWallet wallet) {
        boolean loggedIn = true;
        while (loggedIn) {
            try {
                wallet = walletRepository.findByAccountId(acc.getAccountId());
                showWalletHeader(wallet);

                System.out.println("""
                        1. Manage Income
                        2. Manage Expense
                        3. Show Transactions (By Type)
                        4. Budget Calculator
                        5. Transactions by Date Range
                        6. Monthly Summary
                        7. Audit Logs
                        8. Logout
                        """);
                System.out.print("Select action: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> incomeMenu(wallet);
                    case "2" -> expenseMenu(wallet);
                    case "3" -> showByType(wallet);
                    case "4" -> showBudget(wallet);
                    case "5" -> showByDateRange(wallet);
                    case "6" -> showMonthlySummary(wallet);
                    case "7" -> showAuditLogs();
                    case "8" -> loggedIn = false;
                    default -> System.out.println("Invalid choice.");
                }

                if (loggedIn) promptEnterKey();

            } catch (Exception e) {
                System.out.println(STR."Operation Error: \{e.getMessage()}");
                promptEnterKey();
            }
        }
    }

    private static void showWalletHeader(MyWallet wallet) {
        double currentBalance = walletRepository.getFinalBalance(wallet.getWalletId());

        boolean overBudget = false;

        System.out.println(STR."""
            ===============================
            Wallet ID : \{wallet.getWalletId()}
            Balance   : \{String.format("%.2f", currentBalance)}
            Budget    : \{String.format("%.2f", wallet.getBudgetLimit())}
            ===============================
            """);
    }

    // ================= INCOME MANAGEMENT =================

    private static void incomeMenu(MyWallet wallet) {
        System.out.println("\n--- INCOME MENU ---");
        System.out.println("1. Add New Income");
        System.out.println("2. Edit Existing Income");
        System.out.println("3. Delete Income");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();

        try {
            switch (c) {
                case "1" -> {
                    double amt = readDouble("Amount: ");
                    IncomeType type = readEnum("Source", IncomeType.class);

                    Income inc = new Income(IdGenerator.incomeId(), wallet.getWalletId(), amt, type);
                    transactionService.add(inc);
                    System.out.println("Income added!");
                }
                case "2" -> {
                    printTransactionsByType(wallet, "INCOME");
                    String id = readRequiredString("Enter Income ID to Edit: ");
                    double newAmt = readDouble("Enter New Amount: ");
                    transactionService.edit(id, newAmt); // Requires your service to handle 'touch'
                    System.out.println("Income updated!");
                }
                case "3" -> {
                    printTransactionsByType(wallet, "INCOME");
                    String id = readRequiredString("Enter Income ID to Delete: ");
                    transactionService.delete(id);
                    System.out.println("🗑 Income deleted!");
                }
                default -> System.out.println("Invalid option.");
            }
        } catch (Exception e) {
            System.out.println(STR."Failed: \{e.getMessage()}");
        }
    }

    // ================= EXPENSE MANAGEMENT =================

    private static void expenseMenu(MyWallet wallet) {
        System.out.println("\n--- EXPENSE MENU ---");
        System.out.println("1. Add New Expense");
        System.out.println("2. Edit Existing Expense");
        System.out.println("3. Delete Expense");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();

        try {
            switch (c) {
                case "1" -> {
                    double amt = readDouble("Amount: ");
                    ExpenseType type = readEnum("Category", ExpenseType.class);

                    Expense exp = new Expense(IdGenerator.expenseId(), wallet.getWalletId(), amt, type);
                    transactionService.add(exp);
                    System.out.println("Expense added!");
                }
                case "2" -> {
                    printTransactionsByType(wallet, "EXPENSE");
                    String id = readRequiredString("Enter Expense ID to Edit: ");
                    double newAmt = readDouble("Enter New Amount: ");
                    transactionService.edit(id, newAmt);
                    System.out.println("Expense updated!");
                }
                case "3" -> {
                    printTransactionsByType(wallet, "EXPENSE");
                    String id = readRequiredString("Enter Expense ID to Delete: ");
                    transactionService.delete(id);
                    System.out.println("🗑 Expense deleted!");
                }
                default -> System.out.println("Invalid option.");
            }
        } catch (Exception e) {
            System.out.println(STR."Failed: \{e.getMessage()}");
        }
    }

    // ================= REPORTS & HELPERS =================

    private static void printTransactionsByType(MyWallet wallet, String typeStr) {
        System.out.println(STR."\n--- Your \{typeStr}s ---");
        List<Transaction> list = transactionRepository.findByWallet(wallet.getWalletId());

        boolean found = false;
        for (Transaction t : list) {
            if (t.getTransactionType().name().equals(typeStr)) {
                System.out.println(STR."ID: \{t.getTransactionId()} | Category \{t.getCategory()} | $ \{t.getTransactionAmount()}");
                found = true;
            }
        }
        if (!found) System.out.println("(No records found)");
        System.out.println("-----------------------");
    }

    private static void showByType(MyWallet wallet) {
        System.out.println("\n--- Filter Transactions ---");
        System.out.println("1. INCOME");
        System.out.println("2. EXPENSE");
        String typeChoice = sc.nextLine().trim();

        String filter = typeChoice.equals("1") ? "INCOME" : (typeChoice.equals("2") ? "EXPENSE" : null);

        if (filter != null) {
            printTransactionsByType(wallet, filter);
        } else {
            System.out.println("Invalid Type");
        }
    }

    private static void showBudget(MyWallet wallet) {
        double totalExpense = transactionRepository.findByWallet(wallet.getWalletId())
                .stream()
                .filter(t -> t instanceof Expense)
                .mapToDouble(Transaction::getTransactionAmount)
                .sum();

        double remaining = wallet.getBudgetLimit() - totalExpense;
        String status = remaining < 0 ? "OVER BUDGET" : "Within Budget";

        System.out.println("\n--- Budget Overview ---");
        System.out.println(STR."Budget Limit : \{wallet.getBudgetLimit()}");
        System.out.println(STR."Total Spent  : \{totalExpense}");
        System.out.println(STR."Remaining    : \{remaining} (\{status})");
    }

    private static void showByDateRange(MyWallet wallet) {
        System.out.println("\n--- Date Range Filter ---");
        LocalDate from = readDate("From (yyyy-mm-dd): ");
        LocalDate to = readDate("To (yyyy-mm-dd): ");

        List<Transaction> list = transactionRepository.findByDateRange(wallet.getWalletId(), from, to);

        if (list.isEmpty()) {
            System.out.println("No transactions found in this range.");
        } else {
            System.out.println("ID | Type | Amount | Date");
            list.forEach(t -> System.out.println(
                    STR."\{t.getTransactionId()} | \{t.getTransactionType()} | \{t.getTransactionAmount()} | \{t.getCreatedAt().toLocalDate()}"
            ));
        }
    }

    private static void showMonthlySummary(MyWallet wallet) {
        System.out.println("\n--- Monthly Summary ---");
        int year = (int) readDouble("Enter Year (e.g. 2025): ");
        int month = (int) readDouble("Enter Month (1-12): ");

        double income = 0;
        double expense = 0;

        for (Transaction tx : transactionRepository.findByWallet(wallet.getWalletId())) {
            LocalDate date = tx.getCreatedAt().toLocalDate();
            if (date.getYear() == year && date.getMonthValue() == month) {
                if (tx instanceof Income) income += tx.getTransactionAmount();
                else expense += tx.getTransactionAmount();
            }
        }

        System.out.println(STR."\nSummary for \{month}/\{year}");
        System.out.println(STR."Total Income : \{income}");
        System.out.println(STR."Total Expense: \{expense}");
        System.out.println(STR."Net Savings  : \{income - expense}");
    }

    private static void showAuditLogs() {
        System.out.println("\n--- Audit Logs ---");
        auditRepository.findAll().forEach(log ->
                System.out.println(STR."\{log.getCreatedAt()} | \{log.getAction()} | ID: \{log.getTransactionId()} | Old: \{log.getOldValue()} -> New: \{log.getNewValue()}")
        );
    }

    // ================= ROBUST INPUT HELPERS =================

    private static String readRequiredString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty.");
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a value like 100.50");
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
                System.out.println("Invalid date format. Use yyyy-mm-dd");
            }
        }
    }

    private static <T extends Enum<T>> T readEnum(String label, Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        while (true) {
            System.out.println(STR."Select \{label}:");
            for (int i = 0; i < constants.length; i++) {
                System.out.println(STR."  \{i + 1}. \{constants[i]}");
            }
            System.out.print("Enter number or name: ");
            String input = sc.nextLine().trim().toUpperCase();

            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < constants.length) {
                    return constants[index];
                }
            } catch (NumberFormatException ignored) {
                try {
                    return Enum.valueOf(enumClass, input);
                } catch (IllegalArgumentException e) {
                }
            }
            System.out.println("Invalid selection. Please try again.");
        }
    }

    private static void promptEnterKey() {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
}