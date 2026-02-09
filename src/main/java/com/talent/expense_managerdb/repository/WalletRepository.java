package main.java. com.talent.expense_managerdb.repository;

import main.java. com.talent.expense_managerdb.config.DBConnection;
import main.java.com.talent.expense_managerdb.exception.DatabaseException;
import main.java.com.talent.expense_managerdb.model.MyWallet;

import java.sql.*;

public class WalletRepository {

    public void save(MyWallet wallet) {
        String sql = """
            INSERT INTO wallets
            (wallet_id, account_id, initial_balance, budget_limit,
             is_active, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, wallet.getWalletId());
            ps.setString(2, wallet.getAccountId());
            ps.setDouble(3, wallet.getInitialBalance());
            ps.setDouble(4, wallet.getBudgetLimit());
            ps.setBoolean(5, wallet.isActive());
            ps.setObject(6, wallet.getCreatedAt());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new DatabaseException("Failed to save wallet", e);
        }
    }

    public MyWallet findByAccountId(String accountId) {
        String sql = """
            SELECT * FROM wallets
            WHERE account_id = ? AND is_active = true
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new MyWallet(
                        rs.getString("wallet_id"),
                        rs.getString("account_id"),
                        rs.getDouble("initial_balance"),
                        rs.getDouble("budget_limit")
                );
            }
            return null;

        } catch (Exception e) {
            throw new DatabaseException("Failed to fetch wallet", e);
        }
    }
    public double getFinalBalance(String walletId) {

        String sql = """
                           SELECT\s
                               w.initial_balance
                               + COALESCE(SUM(
                                   CASE\s
                                       WHEN t.transaction_type = 'INCOME' AND t.is_active = true\s
                                           THEN t.amount
                                       ELSE 0
                                   END
                               ), 0) AS final_balance
                           FROM wallets w
                           LEFT JOIN transactions t ON w.wallet_id = t.wallet_id
                           WHERE w.wallet_id = ?
                           GROUP BY w.wallet_id
    """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, walletId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("final_balance");
            }
            return 0.0;

        } catch (Exception e) {
            throw new DatabaseException("Failed to calculate balance", e);
        }
    }

    public void updateInitialBalance(String walletId, double delta) {
        String sql = """
            UPDATE wallets
            SET initial_balance = initial_balance + ?
            WHERE wallet_id = ?
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, delta);
            ps.setString(2, walletId);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new DatabaseException("Failed to update wallet balance", e);
        }
    }

}
