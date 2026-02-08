package main.java.com.talent.expense_managerdb.model;

import java.time.LocalDateTime;

public abstract class BaseEntity {
    protected boolean isActive = true;
    protected LocalDateTime createdAt = LocalDateTime.now();
    protected LocalDateTime updatedAt = LocalDateTime.now(); // Initialized to avoid null
    protected LocalDateTime deletedAt;

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void softDelete() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
        touch();
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}