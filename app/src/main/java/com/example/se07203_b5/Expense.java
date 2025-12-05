package com.example.se07203_b5;

public class Expense {
    private int id;
    private int amount;
    private String category;
    private String description;
    private String date;

    public Expense(int id, int amount, String category, String description, String date) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    public Expense(int amount, String category, String description, String date) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    public int getId() { return id; }
    public int getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getDate() { return date; }

    public void setAmount(int amount) { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
}
