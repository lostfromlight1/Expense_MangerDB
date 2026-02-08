package main.java.com.talent.expense_managerdb.model;

public class Account extends BaseEntity {
    private final String accountId;
    private String name;
    private String email;
    private String passwordHash;

    public Account(String accountId, String name, String email, String passwordHash) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
