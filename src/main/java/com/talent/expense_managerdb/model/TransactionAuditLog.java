package main.java.com.talent.expense_managerdb.model;

import java.time.LocalDateTime;

public class TransactionAuditLog extends BaseEntity {

    private final String logId;
    private final String transactionId;
    private final String action;
    private final double oldValue;
    private final double newValue;

    public TransactionAuditLog(String logId, String transactionId, String action, double oldValue, double newValue) {
        this.logId = logId;
        this.transactionId = transactionId;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    public TransactionAuditLog(String logId, String transactionId, String action,
                               double oldValue, double newValue, LocalDateTime createdAt) {
        this.logId = logId;
        this.transactionId = transactionId;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = createdAt;
    }

    public String getLogId() {
        return logId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAction() {
        return action;
    }

    public double getOldValue() {
        return oldValue;
    }

    public double getNewValue() {
        return newValue;
    }
}
