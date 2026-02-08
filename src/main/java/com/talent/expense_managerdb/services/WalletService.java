package main.java.com.talent.expense_managerdb.services;

import main.java.com.talent.expense_managerdb.exception.NotFoundException;
import main.java.com.talent.expense_managerdb.exception.ValidationException;
import main.java.com.talent.expense_managerdb.model.Expense;
import main.java.com.talent.expense_managerdb.model.MyWallet;
import main.java.com.talent.expense_managerdb.model.Transaction;
import main.java.com.talent.expense_managerdb.repository.TransactionRepository;
import main.java.com.talent.expense_managerdb.repository.WalletRepository;
import main.java.com.talent.expense_managerdb.util.IdGenerator;

import java.util.List;

public class WalletService {

    private final WalletRepository walletRepository = new WalletRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    public MyWallet createWallet(String accountId,
                                 double initialBalance,
                                 double budgetLimit) {

        if (budgetLimit <= 0) {
            throw new ValidationException("Budget limit must be > 0");
        }

        if (walletRepository.findByAccountId(accountId) != null) {
            throw new ValidationException("Wallet already exists");
        }

        MyWallet wallet = new MyWallet(
                IdGenerator.walletId(),
                accountId,
                initialBalance,
                budgetLimit
        );

        walletRepository.save(wallet);
        return wallet;
    }

    public MyWallet getWallet(String accountId) {
        MyWallet wallet = walletRepository.findByAccountId(accountId);
        if (wallet == null) {
            throw new NotFoundException("Wallet not found");
        }
        return wallet;
    }

    public double recalculateBalance(MyWallet wallet) {
        List<Transaction> txs =
                transactionRepository.findByWallet(wallet.getWalletId());

        double txSum = txs.stream()
                .filter(Transaction::isActive)
                .mapToDouble(Transaction::getSignedAmount)
                .sum();

        return wallet.getInitialBalance() + txSum;
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
