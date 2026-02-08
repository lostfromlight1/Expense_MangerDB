package main.java.com.talent.expense_managerdb.repository;

import main.java.com.talent.expense_managerdb.config.DBConnection;
import main.java.com.talent.expense_managerdb.exception.DatabaseException;
import main.java.com.talent.expense_managerdb.model.Transaction;
import main.java.com.talent.expense_managerdb.util.TransactionMapper;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    public void save(Transaction tx) {
        String sql = """
            INSERT INTO transactions
            (transaction_id, wallet_id, amount,
             transaction_type, category,
             is_active, created_at)
            VALUES (?, ?, ?, ?, ?, true, ?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tx.getTransactionId());
            ps.setString(2, tx.getWalletId());
            ps.setDouble(3, tx.getTransactionAmount());
            ps.setString(4, tx.getTransactionType().name());
            ps.setString(5, tx.getCategory());
            ps.setObject(6, tx.getCreatedAt());
            ps.executeUpdate();
        }
        catch (Exception e) {
            throw new DatabaseException("Save transaction failed", e);
        }
    }

    public Transaction findById(String id) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? TransactionMapper.map(rs) : null;
        }
        catch (Exception e) {
            throw new DatabaseException("Find transaction failed", e);
        }
    }

    public List<Transaction> findByWallet(String walletId) {
        String sql = """
            SELECT * FROM transactions
            WHERE wallet_id = ? AND is_active = true
        """;

        List<Transaction> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, walletId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(TransactionMapper.map(rs));
            }
            return list;
        }
        catch (Exception e) {
            throw new DatabaseException("Fetch transactions failed", e);
        }
    }

    public List<Transaction> findByDateRange(
            String walletId, LocalDate from, LocalDate to) {

        String sql = """
            SELECT * FROM transactions
            WHERE wallet_id = ?
            AND DATE(created_at) BETWEEN ? AND ?
            AND is_active = true
        """;

        List<Transaction> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, walletId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(TransactionMapper.map(rs));
            }
            return list;
        }
        catch (Exception e) {
            throw new DatabaseException("Date range fetch failed", e);
        }
    }

    public void update(Transaction tx) {
        String sql = """
            UPDATE transactions
            SET amount = ?, updated_at = NOW()
            WHERE transaction_id = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, tx.getTransactionAmount());
            ps.setString(2, tx.getTransactionId());
            ps.executeUpdate();
        }
        catch (Exception e) {
            throw new DatabaseException("Update transaction failed", e);
        }
    }

    public void delete(String id) {
        String sql = """
            UPDATE transactions
            SET is_active = false, deleted_at = NOW()
            WHERE transaction_id = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();
        }
        catch (Exception e) {
            throw new DatabaseException("Delete transaction failed", e);
        }
    }
}
