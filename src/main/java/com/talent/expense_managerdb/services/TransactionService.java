package main.java.com.talent.expense_managerdb.services;

import main.java.com.talent.expense_managerdb.exception.NotFoundException;
import main.java.com.talent.expense_managerdb.model.*;
import main.java.com.talent.expense_managerdb.repository.*;
import main.java.com.talent.expense_managerdb.util.IdGenerator;

public class TransactionService {

    private final TransactionRepository txRepo = new TransactionRepository();
    private final TransactionAuditRepository auditRepo = new TransactionAuditRepository();

    public void add(Transaction tx) {
        txRepo.save(tx);
        audit("CREATE", tx.getTransactionId(), 0, tx.getTransactionAmount());
    }

    public void edit(String id, double newAmount) {
        Transaction tx = txRepo.findById(id);
        if (tx == null) throw new NotFoundException("Transaction not found");

        double old = tx.getTransactionAmount();
        tx.updateAmount(newAmount);
        txRepo.update(tx);

        audit("UPDATE", id, old, newAmount);
    }

    public void delete(String id) {
        Transaction tx = txRepo.findById(id);
        if (tx == null) throw new NotFoundException("Transaction not found");

        txRepo.delete(id);
        audit("DELETE", id, tx.getTransactionAmount(), 0);
    }

    private void audit(String action,
                       String txId,
                       double oldVal,
                       double newVal) {

        auditRepo.save(new TransactionAuditLog(
                IdGenerator.logId(),
                txId,
                action,
                oldVal,
                newVal
        ));
    }
}

