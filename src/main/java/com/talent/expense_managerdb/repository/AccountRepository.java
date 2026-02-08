package main.java.com.talent.expense_managerdb.repository;

import main.java.com.talent.expense_managerdb.config.DBConnection;
import main.java.com.talent.expense_managerdb.exception.*;
import main.java.com.talent.expense_managerdb.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AccountRepository {
    public void save(Account account){
        String sql = """
                INSERT INTO accounts
                (account_id, name, email, password_hash, is_active, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, account.getAccountId());
            ps.setString(2, account.getName());
            ps.setString(3, account.getEmail());
            ps.setString(4, account.getPasswordHash());
            ps.setBoolean(5, account.isActive());
            ps.setObject(6, account.getCreatedAt());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new DatabaseException("Failed to save account", e);
        }
    }

    public Account findByEmail(String email){
        String sql = """
                SELECT * FROM accounts WHERE email = ? AND is_active = true
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1,email);
                ResultSet result = ps.executeQuery();

                if(result.next()){
                    return new Account(
                            result.getString("account_id"),
                            result.getString("name"),
                            result.getString("email"),
                            result.getString("password_hash")
                    );
                }
                return null;

        }catch (Exception e){
            throw new DatabaseException("Failed to fetch account", e);
        }

    }
}
