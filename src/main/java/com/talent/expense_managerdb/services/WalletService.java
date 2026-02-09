package main.java.com.talent.expense_managerdb.services;

import main.java.com.talent.expense_managerdb.exception.NotFoundException;
import main.java.com.talent.expense_managerdb.exception.ValidationException;
import main.java.com.talent.expense_managerdb.model.Expense;
import main.java.com.talent.expense_managerdb.model.Income;
import main.java.com.talent.expense_managerdb.model.MyWallet;
import main.java.com.talent.expense_managerdb.model.Transaction;
import main.java.com.talent.expense_managerdb.repository.TransactionRepository;
import main.java.com.talent.expense_managerdb.repository.WalletRepository;
import main.java.com.talent.expense_managerdb.util.IdGenerator;

public class WalletService {

    private final WalletRepository walletRepository = new WalletRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();


    public void createWallet(String accountId,
                             double initialBalance,
                             double budgetLimit) {

        if (budgetLimit <= 0) {
            throw new ValidationException("Budget limit must be > 0");
        }

        if (walletRepository.findByAccountId(accountId) != null) {
            throw new ValidationException("Wallet already exists");
        }

        String walletId = IdGenerator.walletId();
        MyWallet wallet = new MyWallet(
                walletId,
                accountId,
                initialBalance,
                budgetLimit
        );

        walletRepository.save(wallet);

        if (initialBalance > 0) {
            Income initialIncome = new Income(
                    IdGenerator.incomeId(),
                    walletId,
                    initialBalance,
                    main.java.com.talent.expense_managerdb.model.enum_type.IncomeType.OTHERS
            );
            transactionRepository.save(initialIncome);
        }
    }


    public MyWallet getWallet(String accountId) {
        MyWallet wallet = walletRepository.findByAccountId(accountId);
        if (wallet == null) {
            throw new NotFoundException("Wallet not found");
        }
        return wallet;
    }


    public double getTotalIncome(String walletId) {

        return transactionRepository.findByWallet(walletId)
                .stream()
                .filter(Transaction::isActive)
                .filter(t -> t instanceof Income)
                .mapToDouble(Transaction::getTransactionAmount)
                .sum();
    }

    public double getTotalExpense(String walletId) {

        return transactionRepository.findByWallet(walletId)
                .stream()
                .filter(Transaction::isActive)
                .filter(t -> t instanceof Expense)
                .mapToDouble(Transaction::getTransactionAmount)
                .sum();
    }



    public boolean isOverBudget(MyWallet wallet) {

        double spent = transactionRepository.findByWallet(wallet.getWalletId())
                .stream()
                .filter(t -> t instanceof Expense && t.isActive())
                .mapToDouble(Transaction::getTransactionAmount)
                .sum();

        return spent > wallet.getBudgetLimit();
    }
}
