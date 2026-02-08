package main.java.com.talent.expense_managerdb.repository;

import main.java. com.talent.expense_managerdb.config.DBConnection;
import main.java.com.talent.expense_managerdb.model.TransactionAuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionAuditRepository {

    public void save(TransactionAuditLog log) {
        String sql = """
            INSERT INTO transaction_audit
            (log_id, transaction_id, action,
             old_value, new_value, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, log.getLogId());
            ps.setString(2, log.getTransactionId());
            ps.setString(3, log.getAction());
            ps.setDouble(4, log.getOldValue());
            ps.setDouble(5, log.getNewValue());
            ps.setObject(6, log.getCreatedAt());
            ps.executeUpdate();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<TransactionAuditLog> findAll() {
        String sql = "SELECT * FROM transaction_audit ORDER BY created_at DESC";
        List<TransactionAuditLog> logs = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                logs.add(mapRow(rs));
            }
            return logs;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch audit logs", e);
        }
    }

    private TransactionAuditLog mapRow(ResultSet rs) throws SQLException {
        return new TransactionAuditLog(
                rs.getString("log_id"),
                rs.getString("transaction_id"),
                rs.getString("action"),
                rs.getDouble("old_value"),
                rs.getDouble("new_value"),
                rs.getObject("created_at", LocalDateTime.class)
        );
    }
}
