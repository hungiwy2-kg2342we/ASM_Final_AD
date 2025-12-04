package com.example.se07203_b5;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

// Giả định Transaction và TransactionAdapter đã được định nghĩa
import static com.example.se07203_b5.CreateTransactionActivity.Transaction;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_LAST_ACTIVE_TIME = "last_active_time";
    private static final long SESSION_TIMEOUT_MS = 60000; // 1 phút

    // Hằng số cho Notification
    private static final String CHANNEL_ID = "BUDGET_ALERTS";
    private static final int NOTIFICATION_ID = 1001;

    // View
    private TextView tvCurrentBalance;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvBudgetNotification;
    private ListView lvRecentTransactions;
    // ĐÃ XÓA: private Button btnAddTransaction; // Không còn tồn tại trong XML mới
    private View budgetReportCard;
    private BottomNavigationView bottomNavigationView;

    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("AppData", MODE_PRIVATE);
        dbHelper = new DatabaseHelper(this);

        createNotificationChannel();
        currentUserId = sharedPreferences.getLong("user_id", -1);

        initViews();

        if (currentUserId > 0) {
            checkSessionValidity();
        } else {
            redirectToLogin();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentUserId > 0) {
            updateLastActiveTime();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        currentUserId = sharedPreferences.getLong("user_id", -1);

        if (currentUserId > 0) {
            long lastActive = sharedPreferences.getLong(PREF_LAST_ACTIVE_TIME, 0);
            long now = System.currentTimeMillis();

            if (lastActive > 0 && (now - lastActive) > SESSION_TIMEOUT_MS) {
                Toast.makeText(this, "Phiên đăng nhập đã hết hạn.", Toast.LENGTH_LONG).show();
                handleSessionExpired();
                return;
            }

            updateLastActiveTime();
            loadDashboardData();
        } else {
            redirectToLogin();
        }
    }

    // --- Session & Navigation ---

    private void checkSessionValidity() {
        long lastActiveTime = sharedPreferences.getLong(PREF_LAST_ACTIVE_TIME, 0);
        long currentTime = System.currentTimeMillis();

        if (lastActiveTime > 0 && (currentTime - lastActiveTime) > SESSION_TIMEOUT_MS) {
            handleSessionExpired();
        }
    }

    private void updateLastActiveTime() {
        sharedPreferences.edit()
                .putLong(PREF_LAST_ACTIVE_TIME, System.currentTimeMillis())
                .apply();
    }

    private void handleSessionExpired() {
        sharedPreferences.edit()
                .remove("user_id")
                .remove(PREF_LAST_ACTIVE_TIME)
                .apply();
        redirectToLogin();
    }

    private void handleLogout() {
        sharedPreferences.edit()
                .remove("user_id")
                .remove(PREF_LAST_ACTIVE_TIME)
                .apply();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // --- View Initialization ---

    private void initViews() {
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        lvRecentTransactions = findViewById(R.id.lvItem);
        // ĐÃ XÓA: btnAddTransaction = findViewById(R.id.btnAddTransaction);
        // ĐÃ XÓA: Logic click của btnAddTransaction

        budgetReportCard = findViewById(R.id.layoutBudgetReport);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // ⭐ ĐÃ SỬA: Thiết lập sự kiện cho Bottom Navigation ⭐
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                // DÙNG CÁC ID TỪ MENU XML BẠN CUNG CẤP TRƯỚC ĐÓ
                if (itemId == R.id.nav_input) {
                    Intent inputIntent = new Intent(MainActivity.this, CreateTransactionActivity.class);
                    startActivity(inputIntent);
                    return true;
                } else if (itemId == R.id.nav_calendar) {
                    Toast.makeText(MainActivity.this, "Chức năng Lịch chưa được cài đặt", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_report) {
                    Toast.makeText(MainActivity.this, "Chức năng Báo cáo chưa được cài đặt", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    Toast.makeText(MainActivity.this, "Chức năng Thông báo chưa được cài đặt", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_more) {
                    Toast.makeText(MainActivity.this, "Chức năng Khác chưa được cài đặt", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }

    // --- Data Loading & UI Logic ---

    private void loadDashboardData() {
        if (currentUserId <= 0) return;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        String currentMonthYear = monthYearFormat.format(calendar.getTime());
        String monthFilterCondition = DatabaseHelper.TRANS_DATE + " LIKE '%/" + currentMonthYear + "'";

        double totalIncomeMonthly = dbHelper.getTotalAmountByTypeAndFilter(currentUserId, "INCOME", monthFilterCondition);
        double totalExpenseMonthly = dbHelper.getTotalAmountByTypeAndFilter(currentUserId, "EXPENSE", monthFilterCondition);
        double totalOverallIncome = dbHelper.getTotalAmountByType(currentUserId, "INCOME");
        double totalOverallExpense = dbHelper.getTotalAmountByType(currentUserId, "EXPENSE");
        double currentBalance = totalOverallIncome - totalOverallExpense;

        tvCurrentBalance.setText(formatCurrency(currentBalance));
        tvTotalIncome.setText(formatCurrency(totalIncomeMonthly));
        tvTotalExpense.setText(formatCurrency(totalExpenseMonthly));

        loadRecentTransactions();
        updateBudgetNotification(totalExpenseMonthly);
    }

    private void loadRecentTransactions() {
        ArrayList<Transaction> transactions = dbHelper.getTransactionsByUserId(currentUserId);
        TransactionAdapter adapter = new TransactionAdapter(this, transactions);
        lvRecentTransactions.setAdapter(adapter);

        lvRecentTransactions.setOnItemClickListener((parent, view, position, id) -> {
            Transaction selectedTransaction = transactions.get(position);
            showEditDeleteDialog(selectedTransaction);
        });
    }

    private void showEditDeleteDialog(Transaction transaction) {
        String[] options = {"Sửa giao dịch", "Xóa giao dịch"};

        new AlertDialog.Builder(this)
                .setTitle("Tùy chọn cho giao dịch")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent editIntent = new Intent(MainActivity.this, CreateTransactionActivity.class);
                        editIntent.putExtra("transactionId", transaction.getId());
                        startActivity(editIntent);
                    } else if (which == 1) {
                        confirmAndDelete(transaction);
                    }
                })
                .show();
    }

    private void confirmAndDelete(Transaction transaction) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch: " + transaction.getDescription() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (dbHelper.removeTransactionById(transaction.getId())) {
                        Toast.makeText(this, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show();
                        loadDashboardData();
                    } else {
                        Toast.makeText(this, "Lỗi xóa giao dịch.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- Notification Logic ---

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Cảnh báo Chi tiêu", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void updateBudgetNotification(double totalExpense) {
        final double BUDGET_LIMIT = 10000000.0;
        double percentage = (totalExpense / BUDGET_LIMIT) * 100;
        String notificationMessage = null;

        if (percentage >= 90) {
            notificationMessage = "⚠ NGUY HIỂM! Đã chi tiêu " + String.format(Locale.getDefault(), "%.0f", percentage) + "% tổng ngân sách tháng này.";
            budgetReportCard.setVisibility(View.VISIBLE);
        } else if (percentage >= 70) {
            notificationMessage = "Cảnh báo: Đã chi tiêu " + String.format(Locale.getDefault(), "%.0f", percentage) + "% ngân sách.";
            budgetReportCard.setVisibility(View.VISIBLE);
        } else {
            budgetReportCard.setVisibility(View.GONE);
        }

        if (notificationMessage != null && percentage >= 70) {
            sendBudgetNotification("CẢNH BÁO NGÂN SÁCH", notificationMessage);
        }
    }

    private void sendBudgetNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // --- Utility ---

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "%,.0f VNĐ", amount);
    }
}