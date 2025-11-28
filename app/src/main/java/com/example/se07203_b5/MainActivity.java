package com.example.se07203_b5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnCreate, btnLogout;

    Button btnAll, btnIncome, btnExpense;
    
    ListView lvListItem;
    TextView tvListTitle, tvReport; 
    ArrayAdapter<Item> adapter; 

    DatabaseHelper dbHelper;
    SharedPreferences sharedPreferences;
    
    long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("isLogin", false)){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); 
            return; 
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); 

        btnCreate = findViewById(R.id.btnCreate); 
        btnLogout = findViewById(R.id.btnLogout); 
        lvListItem = findViewById(R.id.lvItem); 
        tvListTitle = findViewById(R.id.tvListTitle); 
        tvReport = findViewById(R.id.tvReport);
        
        // Ánh xạ biến Java với ID XML (giờ tên đã giống nhau)
        btnAll = findViewById(R.id.btnAll);
        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        
        dbHelper = new DatabaseHelper(this); 

        userId = sharedPreferences.getLong("user_id", 0);

        loadData(0); 

        //  Sử dụng layout có sẵn
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, AppData.ListItem);
        lvListItem.setAdapter(adapter);
        
        showReport();

        // Sự kiện click
        btnAll.setOnClickListener(v -> {
            loadData(0);
            tvListTitle.setText("Tất cả giao dịch");
        });
        
        btnIncome.setOnClickListener(v -> {
            loadData(1);
            tvListTitle.setText("Danh sách Thu nhập");
        });
        
        btnExpense.setOnClickListener(v -> {
            loadData(-1);
            tvListTitle.setText("Danh sách Chi tiêu");
        });


        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateNewTaskActivity.class);
            startActivity(intent);
        });

        lvListItem.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(this, "Bạn chọn item thứ " + (position + 1) + ", món đồ " + AppData.ListItem.get(position).getName(), Toast.LENGTH_LONG).show();
            showOptionsDialog(position);
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putBoolean("isLogin", false);
            sharedPreferencesEditor.apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void loadData(int type) {
        ArrayList<Item> _items;
        if (type == 0) {
            _items = dbHelper.getProducts(userId); 
        } else {
            _items = dbHelper.getProductsByType(userId, type); 
        }
        
        AppData.ListItem.clear();
        if (_items != null) {
            AppData.ListItem.addAll(_items);
        }
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        showReport();
    }

    private void showOptionsDialog(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lựa chọn hành động!");
        String[] options = {"Sửa", "Xóa"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0){ 
                Intent intent = new Intent(MainActivity.this, CreateNewTaskActivity.class);
                intent.putExtra("position", position);
                startActivityForResult(intent, AppData.EDIT_TASK);
            }else{
                Item _item = AppData.ListItem.get(position);
                long itemId = _item.getId();
                boolean result = dbHelper.removeProductById(itemId);
                if (result) {
                    AppData.ListItem.remove(position);
                    showReport();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Xóa Thành Công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Xóa Thất Bại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppData.EDIT_TASK && resultCode == RESULT_OK){
            loadData(0); 
        }
    }
    
    private void showReport(){
        ArrayList<Item> allItems = dbHelper.getProducts(userId);
        
        long totalIncome = 0;
        long totalExpense = 0;
        
        for (Item item : allItems) {
            long amount = (long) item.getQuantity() * item.getUnitPrice();
            if (item.getType() == 1) {
                totalIncome += amount;
            } else {
                totalExpense += amount;
            }
        }
        
        long balance = totalIncome - totalExpense;
        tvReport.setText("Số dư: " + balance + " VND \n(Thu: " + totalIncome + " - Chi: " + totalExpense + ")");
    }
}
