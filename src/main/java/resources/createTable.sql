DROP DATABASE IF EXISTS expense_manager_db;
CREATE DATABASE expense_manager_db;
USE expense_manager_db;

CREATE TABLE accounts (
                          account_id VARCHAR(50) PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          email VARCHAR(100) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP NULL
);

CREATE TABLE wallets (
                         wallet_id VARCHAR(50) PRIMARY KEY,
                         account_id VARCHAR(50) NOT NULL UNIQUE,
                         initial_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
                         budget_limit DECIMAL(12,2) NOT NULL,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         deleted_at TIMESTAMP NULL,
                         CONSTRAINT fk_wallet_account FOREIGN KEY (account_id)
                             REFERENCES accounts(account_id) ON DELETE CASCADE
);
select * from wallets;

CREATE TABLE transactions (
                              transaction_id VARCHAR(50) PRIMARY KEY,
                              wallet_id VARCHAR(50) NOT NULL,
                              transaction_type ENUM('INCOME', 'EXPENSE') NOT NULL,
                              category VARCHAR(50) NOT NULL,
                              amount DECIMAL(12,2) NOT NULL,
                              is_active BOOLEAN DEFAULT TRUE,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              deleted_at TIMESTAMP NULL,
                              CONSTRAINT fk_tx_wallet FOREIGN KEY (wallet_id)
                                  REFERENCES wallets(wallet_id) ON DELETE CASCADE
);

CREATE TABLE transaction_audit (
                                   log_id VARCHAR(50) PRIMARY KEY,
                                   transaction_id VARCHAR(50) NOT NULL,
                                   action VARCHAR(50) NOT NULL,
                                   old_value DECIMAL(12,2),
                                   new_value DECIMAL(12,2),
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
drop table transaction_audit_logs;

CREATE INDEX idx_accounts_email ON accounts(email);
CREATE INDEX idx_tx_wallet ON transactions(wallet_id);
CREATE INDEX idx_audit_tx ON transaction_audit_logs(transaction_id);