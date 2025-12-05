package com.example.se07203_b5;

import androidx.annotation.NonNull;
import java.util.Locale;

public class Transaction {
    private long id;
    private long userId;
    private double amount;
    private String category;
    private String description;
    private String date;
    private String type; // "INCOME" hoặc "EXPENSE"

    // Constructor đầy đủ (khi lấy từ DB)
    public Transaction(long id, long userId, double amount, String category, String description, String date, String type) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.type = type;
    }

    // Constructor rút gọn (khi tạo mới)
    public Transaction(long userId, double amount, String category, String description, String date, String type) {
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.type = type;
    }

    // Getters
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getType() { return type; }

    @NonNull
    @Override
    public String toString() {
        return description + " - " + amount;
    }
}