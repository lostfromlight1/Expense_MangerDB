package main.java.com.talent.expense_managerdb.model;

import main.java.com.talent.expense_managerdb.model.enum_type.ExpenseType;
import main.java.com.talent.expense_managerdb.model.enum_type.TransactionType;

public class Expense extends Transaction {

    private final ExpenseType expenseType;

    public Expense(String id, String walletId,
                   double amount, ExpenseType type) {
        super(id, walletId, amount);
        this.expenseType = type;
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    @Override
    public double getSignedAmount() {
        return -transactionAmount;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionType.EXPENSE;
    }

    @Override
    public String getCategory() {
        return expenseType.name();
    }
}
