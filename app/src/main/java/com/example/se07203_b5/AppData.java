package com.example.se07203_b5;

import java.util.ArrayList;

public class AppData {
    static boolean isLogin = false;
    static ArrayList<String> ListTask = new ArrayList<String>();

    static ArrayList<Item> ListItem = new ArrayList<Item>();

    static final int EDIT_TASK = 1;

    static int getTotalBill(){
        int totalBill = 0;
        for (int i = 0; i < ListItem.size(); i++) {
            totalBill += ListItem.get(i).getQuantity() * ListItem.get(i).getUnitPrice();
        }
        return totalBill;
    }

}
