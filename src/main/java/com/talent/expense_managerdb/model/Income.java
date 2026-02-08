package main.java.com.talent.expense_managerdb.model;

import main.java.com.talent.expense_managerdb.model.enum_type.IncomeType;
import main.java.com.talent.expense_managerdb.model.enum_type.TransactionType;

public class Income extends Transaction {

    private final IncomeType incomeType;

    public Income(String id, String walletId,
                  double amount, IncomeType type) {
        super(id, walletId, amount);
        this.incomeType = type;
    }

    public IncomeType getIncomeType() {
        return incomeType;
    }

    @Override
    public double getSignedAmount() {
        return transactionAmount;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.INCOME;
    }

    @Override
    public String getCategory() {
        return incomeType.name();
    }
}
