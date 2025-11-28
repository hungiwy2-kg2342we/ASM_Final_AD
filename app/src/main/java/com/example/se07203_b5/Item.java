package com.example.se07203_b5;

import androidx.annotation.NonNull;

public class Item {
    private long id; 
    private String ItemName; 
    private int Quantity; 
    private int unitPrice = 0; 
    private int type = -1; 
    //  Thêm biến date
    private String date = "";

    // --- Constructors---
    public Item(String itemName, int quantity) {
        ItemName = itemName;
        Quantity = quantity;
    }
    
    public Item(String _itemName, int _quantity, int _unitPrice){
        ItemName = _itemName;
        Quantity = _quantity;
        unitPrice = _unitPrice;
    }

    public Item (long id, String name, int quantity, int unitPrice){
        this.id = id;
        this.ItemName = name;
        this.Quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    public Item(String _itemName, int _quantity, int _unitPrice, int _type){
        ItemName = _itemName;
        Quantity = _quantity;
        unitPrice = _unitPrice;
        this.type = _type;
    }
    
    // Constructor mới có thêm date
    public Item(String _itemName, int _quantity, int _unitPrice, int _type, String _date){
        ItemName = _itemName;
        Quantity = _quantity;
        unitPrice = _unitPrice;
        this.type = _type;
        this.date = _date;
    }

    // Constructor mới có thêm date
    public Item (long id, String name, int quantity, int unitPrice, int type, String date){
        this.id = id;
        this.ItemName = name;
        this.Quantity = quantity;
        this.unitPrice = unitPrice;
        this.type = type;
        this.date = date;
    }

    public Item (long id, String name, int quantity, int unitPrice, int type){
        this.id = id;
        this.ItemName = name;
        this.Quantity = quantity;
        this.unitPrice = unitPrice;
        this.type = type;
    }

    @NonNull
    @Override
    public String toString(){
        String prefix = (type == 1) ? "(+) " : "(-) ";
        String dateStr = (date != null && !date.isEmpty()) ? " [" + date + "]" : "";
        return prefix + ItemName + " - Số lượng: " + Quantity + " - Đơn giá: " + unitPrice + dateStr;
    }
    
    public String getName(){
        return ItemName;
    }

    public int getQuantity(){
        return Quantity;
    }

    public int getUnitPrice(){
        return unitPrice;
    }
    public void setUnitPrice(int _price){
        unitPrice = _price;
    }

    public void setName(String name){
        ItemName = name;
    }

    public void setQuantity(int quantity){
        Quantity = quantity;
    }

    public long getId(){
        return id;
    }
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
