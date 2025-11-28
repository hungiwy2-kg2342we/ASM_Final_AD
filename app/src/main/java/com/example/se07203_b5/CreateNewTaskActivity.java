package com.example.se07203_b5;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CreateNewTaskActivity extends AppCompatActivity {

    Button btnSubmitCreate, btnBackToMain;
    TextView titlePageCreateEdit;
    Boolean isEditMode = false;
    int position = -1;

    SharedPreferences sharedPreferences;

    EditText edtItemName, edtQuantity, edtUnitPrice, edtDate;
    RadioGroup rgType; 
    RadioButton rbIncome, rbExpense; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); 
        setContentView(R.layout.activity_create_new_task);

        sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);

        btnSubmitCreate = findViewById(R.id.btnSubmitCreate);
        btnBackToMain = findViewById(R.id.btnBackToMain);
        edtItemName = findViewById(R.id.edtItemName);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtUnitPrice = findViewById(R.id.edtUnitPrice);
        edtDate = findViewById(R.id.edtDate); 
        titlePageCreateEdit = findViewById(R.id.titlePageCreateEdit);

        rgType = findViewById(R.id.rgType);
        rbIncome = findViewById(R.id.rbIncome);
        rbExpense = findViewById(R.id.rbExpense);
        
        // --- CODE XỬ LÝ NGÀY ĐƠN GIẢN ---
        // 1. Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 2. Gán ngày hiện tại làm mặc định (Lưu ý: tháng bắt đầu từ 0 nên phải +1)
        edtDate.setText(day + "/" + (month + 1) + "/" + year);

        // 3. Sự kiện bấm vào để chọn ngày khác
        edtDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
                // Cập nhật ô nhập liệu sau khi chọn
                edtDate.setText(d + "/" + (m + 1) + "/" + y);
            }, year, month, day);
            dialog.show();
        });
        // --------------------------------

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            position = intent.getIntExtra("position", -1);
            if (position > -1) {
                isEditMode = true;
                titlePageCreateEdit.setText("Sửa thông tin");
                Item currentItem = AppData.ListItem.get(position);
                
                edtItemName.setText(currentItem.getName());
                edtQuantity.setText(String.valueOf(currentItem.getQuantity()));
                edtUnitPrice.setText(String.valueOf(currentItem.getUnitPrice()));
                
                // Nếu có ngày cũ thì hiển thị lại
                if(currentItem.getDate() != null && !currentItem.getDate().isEmpty()){
                    edtDate.setText(currentItem.getDate());
                }

                if (currentItem.getType() == 1) {
                    rbIncome.setChecked(true);
                } else {
                    rbExpense.setChecked(true);
                }
            } else {
                isEditMode = false;
            }
        } else {
            isEditMode = false;
        }

        btnSubmitCreate.setOnClickListener(v -> {
            if (isEditMode){
                editAnItem();
            } else {
                createNewItem();
            }
        });

        btnBackToMain.setOnClickListener(v -> {
            backToMain();
        });
    }

    private void backToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void editAnItem(){
        String itemName = edtItemName.getText().toString();
        int quantity;
        double unitPrice;
        if (itemName.trim().isEmpty()) {
            edtItemName.setError("Tên sản phẩm không được để trống");
            return;
        }
        try {
            quantity = Integer.parseInt(edtQuantity.getText().toString());
            unitPrice = Double.parseDouble(edtUnitPrice.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (quantity < 1) {
            edtQuantity.setError("Số lượng phải lớn hơn 0");
            return;
        }
        
        int type = (rbIncome.isChecked()) ? 1 : -1;
        String date = edtDate.getText().toString(); // Lấy chuỗi ngày

        Item itemToUpdate = AppData.ListItem.get(position);
        itemToUpdate.setName(itemName);
        itemToUpdate.setQuantity(quantity);
        itemToUpdate.setUnitPrice((int) unitPrice);
        itemToUpdate.setType(type);
        itemToUpdate.setDate(date); 

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        boolean isUpdated = dbHelper.updateProduct(itemToUpdate); 
        if (isUpdated) {
            setResult(RESULT_OK);
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            finish(); 
        } else {
            Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
        }
    }


    private void createNewItem(){
        String itemName = edtItemName.getText().toString();
        int quantity = 0, unitPrice = 0;
        try {
            quantity = Integer.parseInt(edtQuantity.getText().toString());
            unitPrice = Integer.parseInt(edtUnitPrice.getText().toString());
        } catch (NumberFormatException e){
            edtQuantity.setError("Số lượng phải lớn hơn 0");
            return;
        }

        if (quantity < 1){
            edtQuantity.setError("Số lượng phải lớn hơn 0");
            return;
        } else {
            int type = (rbIncome.isChecked()) ? 1 : -1;
            String date = edtDate.getText().toString(); // Lấy chuỗi ngày
            
            Item item = new Item(itemName, quantity, unitPrice, type, date);
            
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            long userId = sharedPreferences.getLong("user_id", -1);
            if (userId > 0){
                long resultId = databaseHelper.addProduct(item, userId);
                if (resultId <= 0){
                    Toast.makeText(this, "Lỗi thêm vào CSDL", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Lỗi xác thực người dùng", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
