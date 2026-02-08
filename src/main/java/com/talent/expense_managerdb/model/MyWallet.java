package main.java.com.talent.expense_managerdb.model;

public class MyWallet extends BaseEntity {

    private final String walletId;
    private final String accountId;

    private final double initialBalance;

    private double budgetLimit;

    public MyWallet(String walletId, String accountId,
                    double initialBalance, double budgetLimit) {
        this.walletId = walletId;
        this.accountId = accountId;
        this.initialBalance = initialBalance;
        this.budgetLimit = budgetLimit;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getAccountId() {
        return accountId;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(double budgetLimit) {
        this.budgetLimit = budgetLimit;
    }
}
