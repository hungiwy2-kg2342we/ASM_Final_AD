package com.example.se07203_b5;

import java.util.ArrayList;

public class AppData {

    // Trạng thái đăng nhập
    public static boolean isLogin = false;

    // User đang đăng nhập
    public static User currentUser = null;

    // Danh sách Transaction hiện tại
    public static ArrayList<Transaction> ListItem = new ArrayList<>();

    // Request code dùng cho Edit Activity
    public static final int EDIT_TASK = 1;

    // Giới hạn chi tiêu tháng (tùy ý)
    public static double monthlyLimit = 0;

    // Tính tổng chi tiêu từ danh sách Transaction
    public static double getTotalTransaction() {
        double total = 0;
        for (Transaction t : ListItem) {
            total += t.getAmount();
        }
        return total;
    }

    // Kiểm tra có vượt giới hạn không
    public static boolean isOverLimit() {
        return monthlyLimit > 0 && getTotalTransaction() > monthlyLimit;
    }
}
