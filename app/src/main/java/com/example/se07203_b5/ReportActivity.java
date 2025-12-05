package com.example.se07203_b5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportActivity extends AppCompatActivity {

    PieChart pieChart;
    DatabaseHelper db;
    long userId;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Ánh xạ View
        pieChart = findViewById(R.id.pieChart);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        db = new DatabaseHelper(this);

        // Lấy User ID từ SharedPreferences
        SharedPreferences sp = getSharedPreferences("AppData", MODE_PRIVATE);
        userId = sp.getLong("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy người dùng, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupBottomMenu();
        loadChartData();
    }

    // =====================================================
    //    LOAD DỮ LIỆU CHI TIÊU ĐỂ VẼ BIỂU ĐỒ TRÒN
    // =====================================================
    private void loadChartData() {

        // 1. Lấy danh sách giao dịch từ Database
        ArrayList<Transaction> list = db.getTransactionsByUserId(userId);

        if (list.isEmpty()) {
            pieChart.setNoDataText("Bạn chưa có giao dịch nào!");
            pieChart.invalidate();
            return;
        }

        // 2. Gom nhóm dữ liệu theo Danh mục (Chỉ lấy loại "EXPENSE" - Chi tiêu)
        HashMap<String, Float> map = new HashMap<>();

        for (Transaction t : list) {
            // Bỏ qua nếu là Thu nhập (INCOME)
            if (!"EXPENSE".equals(t.getType())) continue;

            float currentTotal = map.getOrDefault(t.getCategory(), 0f);
            map.put(t.getCategory(), currentTotal + (float)t.getAmount());
        }

        // 3. Chuyển đổi dữ liệu sang định dạng Entry của PieChart
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (String key : map.keySet()) {
            entries.add(new PieEntry(map.get(key), key));
        }

        if (entries.isEmpty()) {
            pieChart.setNoDataText("Chưa có dữ liệu chi tiêu để hiển thị!");
            pieChart.invalidate();
            return;
        }

        // 4. Cấu hình hiển thị Dataset (Màu sắc, kích thước...)
        PieDataSet dataSet = new PieDataSet(entries, "Danh mục chi tiêu");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Sử dụng bảng màu có sẵn đẹp mắt
        dataSet.setSliceSpace(3f); // Khoảng cách giữa các miếng bánh
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setDrawValues(true); // Hiển thị số tiền trên biểu đồ

        // 5. Đưa dữ liệu vào Biểu đồ
        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Cấu hình giao diện biểu đồ
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setCenterText("Chi tiêu");
        pieChart.setCenterTextSize(16f);
        pieChart.getDescription().setEnabled(false); // Tắt dòng chữ mô tả nhỏ ở góc
        pieChart.getLegend().setEnabled(true); // Hiển thị chú thích màu bên dưới

        pieChart.animateY(1000); // Hiệu ứng xoay khi mở
        pieChart.invalidate(); // Vẽ lại biểu đồ
    }


    // =====================================================
    //    BOTTOM NAVIGATION (Xử lý chuyển trang đơn giản)
    // =====================================================
    private void setupBottomMenu() {

        // Đặt trạng thái chọn cho nút Report
        bottomNavigationView.setSelectedItemId(R.id.nav_report);

        bottomNavigationView.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        int id = item.getItemId();

                        if (id == R.id.nav_input) {
                            // Chuyển về màn hình chính (Dashboard)
                            startActivity(new Intent(ReportActivity.this, MainActivity.class));
                            return true;

                        } else if (id == R.id.nav_calendar) {
                            Toast.makeText(ReportActivity.this, "Chức năng Lịch chưa cài đặt", Toast.LENGTH_SHORT).show();
                            return true;

                        } else if (id == R.id.nav_report) {
                            return true; // Đang ở màn hình này rồi

                        } else if (id == R.id.nav_notifications) {
                            Toast.makeText(ReportActivity.this, "Chức năng Thông báo chưa cài đặt", Toast.LENGTH_SHORT).show();
                            return true;

                        } else if (id == R.id.nav_more) {
                            Toast.makeText(ReportActivity.this, "Menu Khác chưa cài đặt", Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        return false;
                    }
                }
        );
    }
}