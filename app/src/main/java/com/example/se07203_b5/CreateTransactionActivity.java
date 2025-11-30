package com.example.se07203_b5;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View; // Import View
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout; // Import LinearLayout
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
import java.util.Locale;

public class CreateTransactionActivity extends AppCompatActivity {

    // --- Biến UI ---
    private EditText edtTransactionAmount, edtTransactionDescription, edtTransactionDate;
    private Spinner spnTransactionCategory;
    private RadioGroup rgTransactionType;
    private Button btnSubmitCreate, btnBackToMain;
    private TextView titlePageCreateEdit;
    private LinearLayout layoutCategoryGroup; // Khai báo Layout nhóm Danh mục

    // --- Biến trạng thái và dữ liệu ---
    private boolean isEditMode = false;
    private long transactionId = -1;
    private long currentUserId;

    // --- Hằng số và Helper ---
    private DatabaseHelper dbHelper;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final String[] categories = {"Ăn uống", "Di chuyển", "Nhà ở", "Hóa đơn", "Lương", "Đầu tư", "Khác"};

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
        layoutCategoryGroup = findViewById(R.id.layoutCategoryGroup); // Ánh xạ layout nhóm

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spnTransactionCategory.setAdapter(adapter);

        rgTransactionType.check(R.id.rbExpense);
    }

    private void setupListeners() {
        btnSubmitCreate.setOnClickListener(v -> handleSubmit());
        btnBackToMain.setOnClickListener(v -> backToMain());
        setupDatePicker();

        // Logic ẩn Danh mục khi chọn Thu nhập
        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbIncome) {
                layoutCategoryGroup.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbExpense) {
                layoutCategoryGroup.setVisibility(View.VISIBLE);
            }
        });

        // Cập nhật trạng thái khởi tạo (cho chế độ chỉnh sửa)
        if (rgTransactionType.getCheckedRadioButtonId() == R.id.rbIncome) {
            layoutCategoryGroup.setVisibility(View.GONE);
        }
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
        Transaction transaction = dbHelper.getTransactionById(id);

        if (transaction != null) {
            edtTransactionAmount.setText(String.valueOf(transaction.getAmount()));
            edtTransactionDescription.setText(transaction.getDescription());
            edtTransactionDate.setText(transaction.getDate());

            if ("INCOME".equals(transaction.getType())) {
                rgTransactionType.check(R.id.rbIncome);
            } else {
                rgTransactionType.check(R.id.rbExpense);
            }

            int categoryIndex = Arrays.asList(categories).indexOf(transaction.getCategory());
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

        // Xử lý Category: Nếu là Thu nhập, gán giá trị mặc định, ngược lại lấy từ Spinner
        String category = type.equals("INCOME") ? "Lương/Thu khác" : spnTransactionCategory.getSelectedItem().toString();

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