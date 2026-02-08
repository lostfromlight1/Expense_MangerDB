package main.java.com.talent.expense_managerdb.util;

import main.java.com.talent.expense_managerdb.model.Expense;
import main.java.com.talent.expense_managerdb.model.Income;
import main.java.com.talent.expense_managerdb.model.Transaction;
import main.java.com.talent.expense_managerdb.model.enum_type.ExpenseType;
import main.java.com.talent.expense_managerdb.model.enum_type.IncomeType;

import java.sql.ResultSet;

public class TransactionMapper {

    public static Transaction map(ResultSet rs) throws Exception {

        String id = rs.getString("transaction_id");
        String walletId = rs.getString("wallet_id");
        double amount = rs.getDouble("amount");
        String type = rs.getString("transaction_type");
        String category = rs.getString("category");

        if ("INCOME".equals(type)) {
            return new Income(
                    id, walletId, amount,
                    IncomeType.valueOf(category)
            );
        }

        return new Expense(
                id, walletId, amount,
                ExpenseType.valueOf(category)
        );
    }
}
