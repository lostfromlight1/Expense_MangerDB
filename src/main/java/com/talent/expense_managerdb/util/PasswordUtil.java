package main.java.com.talent.expense_managerdb.util;


import main.java.com.talent.expense_managerdb.exception.ValidationException;

import java.security.MessageDigest;

public class PasswordUtil {

    public static String hash(String password) {
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            throw new ValidationException("Password hashing failed");
        }
    }
}
