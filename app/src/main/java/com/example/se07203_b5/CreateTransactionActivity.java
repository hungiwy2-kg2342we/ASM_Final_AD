package com.example.se07203_b5;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List; // Import List
import java.util.Locale;

public class CreateTransactionActivity extends AppCompatActivity {

    // --- Biến UI ---
    private EditText edtTransactionAmount, edtTransactionDescription, edtTransactionDate;
    private Spinner spnTransactionCategory;
    private RadioGroup rgTransactionType;
    private Button btnSubmitCreate, btnBackToMain;
    private TextView titlePageCreateEdit;
    private LinearLayout layoutCategoryGroup;

    // --- Biến trạng thái và dữ liệu ---
    private boolean isEditMode = false;
    private long transactionId = -1;
    private long currentUserId;

    // ⭐ ĐÃ SỬA: Danh mục được phân loại thành 2 mảng riêng biệt ⭐
    private final String[] expenseCategories = {"Ăn uống", "Di chuyển", "Nhà ở", "Hóa đơn", "Khác Chi tiêu"};
    private final String[] incomeCategories = {"Lương", "Thưởng", "Đầu tư", "Thu khác"};

    // --- Hằng số và Helper ---
    private DatabaseHelper dbHelper;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Đã xóa mảng categories cũ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_transaction);

        initHelpers();
        initViews();
        setupListeners();

        if (!validateUser()) return;
        checkMode();
    }

    private void initHelpers() {
        dbHelper = new DatabaseHelper(this);
        SharedPreferences sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("user_id", -1);
    }

    private void initViews() {
        btnSubmitCreate = findViewById(R.id.btnSubmitCreate);
        btnBackToMain = findViewById(R.id.btnBackToMain);
        titlePageCreateEdit = findViewById(R.id.titlePageCreateEdit);
        edtTransactionAmount = findViewById(R.id.edtTransactionAmount);
        edtTransactionDescription = findViewById(R.id.edtTransactionDescription);
        edtTransactionDate = findViewById(R.id.edtTransactionDate);
        spnTransactionCategory = findViewById(R.id.spnTransactionCategory);
        rgTransactionType = findViewById(R.id.rgTransactionType);
        layoutCategoryGroup = findViewById(R.id.layoutCategoryGroup);

        // ⭐ ĐÃ SỬA: Khởi tạo Spinner với Danh mục Chi tiêu mặc định ⭐
        // Chúng ta sẽ luôn hiển thị Spinner, chỉ thay đổi nội dung bên trong.
        loadCategories("EXPENSE");

        rgTransactionType.check(R.id.rbExpense);
    }

    private void setupListeners() {
        btnSubmitCreate.setOnClickListener(v -> handleSubmit());
        btnBackToMain.setOnClickListener(v -> backToMain());
        setupDatePicker();

        // ⭐ ĐÃ SỬA: Logic xử lý chuyển đổi loại giao dịch và tải danh mục ⭐
        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbIncome) {
                loadCategories("INCOME");
            } else if (checkedId == R.id.rbExpense) {
                loadCategories("EXPENSE");
            }
        });

        // Đã loại bỏ logic ẩn/hiện Layout Danh mục (vì giờ đây cả Thu và Chi đều có danh mục)
    }

    // ⭐ PHƯƠNG THỨC MỚI: Tải danh mục vào Spinner ⭐
    private void loadCategories(String type) {
        String[] currentCategories;
        if (type.equals("INCOME")) {
            currentCategories = incomeCategories;
        } else {
            currentCategories = expenseCategories;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, currentCategories);
        spnTransactionCategory.setAdapter(adapter);
    }

    private boolean validateUser() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Lỗi: Không thể xác thực người dùng.", Toast.LENGTH_LONG).show();
            disableInputs();
            return false;
        }
        return true;
    }

    private void checkMode() {
        Intent intent = getIntent();
        transactionId = intent.getLongExtra("transactionId", -1);

        if (transactionId > -1) {
            isEditMode = true;
            titlePageCreateEdit.setText("Sửa Giao Dịch");
            btnSubmitCreate.setText("Cập Nhật");
            loadTransactionData(transactionId);
        } else {
            isEditMode = false;
            titlePageCreateEdit.setText("Thêm Giao Dịch");
            btnSubmitCreate.setText("Lưu");
            edtTransactionDate.setText(sdf.format(calendar.getTime()));
        }
    }

    private void loadTransactionData(long id) {
        // Tên lớp Transaction phải có sẵn (đã được cung cấp ở cuối code này)
        Transaction transaction = dbHelper.getTransactionById(id);

        if (transaction != null) {
            edtTransactionAmount.setText(String.valueOf(transaction.getAmount()));
            edtTransactionDescription.setText(transaction.getDescription());
            edtTransactionDate.setText(transaction.getDate());

            String type = transaction.getType();
            List<String> currentCategoryList;

            if ("INCOME".equals(type)) {
                rgTransactionType.check(R.id.rbIncome);
                loadCategories("INCOME"); // Tải danh mục Thu nhập
                currentCategoryList = Arrays.asList(incomeCategories);
            } else {
                rgTransactionType.check(R.id.rbExpense);
                loadCategories("EXPENSE"); // Tải danh mục Chi tiêu
                currentCategoryList = Arrays.asList(expenseCategories);
            }

            // ⭐ ĐÃ SỬA: Tìm category index trong mảng danh mục đúng loại ⭐
            int categoryIndex = currentCategoryList.indexOf(transaction.getCategory());
            if (categoryIndex >= 0) {
                spnTransactionCategory.setSelection(categoryIndex);
            }
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy giao dịch.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void handleSubmit() {
        String amountStr = edtTransactionAmount.getText().toString().trim();
        String description = edtTransactionDescription.getText().toString().trim();
        String date = edtTransactionDate.getText().toString();

        int selectedTypeId = rgTransactionType.getCheckedRadioButtonId();
        String type = (selectedTypeId == R.id.rbIncome) ? "INCOME" : "EXPENSE";

        // ⭐ ĐÃ SỬA: Category luôn được lấy từ Spinner ⭐
        String category = spnTransactionCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(amountStr)) {
            edtTransactionAmount.setError("Số tiền là bắt buộc");
            edtTransactionAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                edtTransactionAmount.setError("Số tiền phải lớn hơn 0");
                return;
            }
        } catch (NumberFormatException e) {
            edtTransactionAmount.setError("Số tiền không hợp lệ");
            return;
        }

        Transaction transaction;
        if (isEditMode) {
            transaction = new Transaction(transactionId, currentUserId, amount, category, description, date, type);
            editATransaction(transaction);
        } else {
            transaction = new Transaction(currentUserId, amount, category, description, date, type);
            createNewTransaction(transaction);
        }
    }

    private void createNewTransaction(Transaction transaction) {
        long result = dbHelper.addTransaction(transaction);
        if (result != -1) {
            Toast.makeText(this, "Thêm giao dịch thành công!", Toast.LENGTH_SHORT).show();
            backToMain();
        } else {
            Toast.makeText(this, "Thêm giao dịch thất bại.", Toast.LENGTH_SHORT).show();
        }
    }

    private void editATransaction(Transaction transaction) {
        boolean isUpdated = dbHelper.updateTransaction(transaction);
        if (isUpdated) {
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            backToMain();
        } else {
            Toast.makeText(this, "Cập nhật thất bại hoặc không có gì thay đổi.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDatePicker() {
        edtTransactionDate.setOnClickListener(v -> {
            DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                edtTransactionDate.setText(sdf.format(calendar.getTime()));
            };

            new DatePickerDialog(this,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void disableInputs() {
        // ... (Logic disable inputs)
    }

    private void backToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    // ⭐ LỚP TRANSACTION MODEL ⭐
    public static class Transaction {
        private long id;
        private long userId;
        private double amount;
        private String category;
        private String description;
        private String date;
        private String type; // "INCOME" or "EXPENSE"

        public Transaction(long userId, double amount, String category, String description, String date, String type) {
            this.userId = userId;
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
            this.type = type;
        }

        public Transaction(long id, long userId, double amount, String category, String description, String date, String type) {
            this.id = id;
            this.userId = userId;
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
            this.type = type;
        }

        @NonNull
        @Override
        public String toString() {
            String sign = type.equals("EXPENSE") ? "-" : "+";
            return category + ": " + description + " (" + date + ") | " + sign + String.format(Locale.getDefault(), "%,.0f VNĐ", amount);
        }

        public long getId() { return id; }
        public long getUserId() { return userId; }
        public double getAmount() { return amount; }
        public String getCategory() { return category; }
        public String getDescription() { return description; }
        public String getDate() { return date; }
        public String getType() { return type; }
    }
}