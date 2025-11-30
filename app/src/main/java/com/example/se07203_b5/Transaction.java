package com.example.se07203_b5;

public class Transaction {

    private long id;
    private long userId;
    private double amount;
    private String category;
    private String description;
    private String date;
    private String type; // INCOME / EXPENSE

    // Constructor tạo mới
    public Transaction(long userId, double amount, String category,
                       String description, String date, String type) {
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.type = type;
    }

    // Constructor dùng khi UPDATE
    public Transaction(long id, long userId, double amount, String category,
                       String description, String date, String type) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.type = type;
    }

    // GETTER – SETTER
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getType() { return type; }

    public void setId(long id) { this.id = id; }
}
