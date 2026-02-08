package main.java.com.talent.expense_managerdb.model;

import main.java.com.talent.expense_managerdb.model.enum_type.TransactionType;

import java.time.LocalDateTime;

public abstract class Transaction extends BaseEntity {

    protected final String transactionId;
    protected final String walletId;
    protected double transactionAmount;

    protected Transaction(String transactionId,
                          String walletId,
                          double transactionAmount) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getWalletId() {
        return walletId;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void updateAmount(double newAmount) {
        this.transactionAmount = newAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public abstract double getSignedAmount();
    public abstract TransactionType getTransactionType();
    public abstract String getCategory();
}
