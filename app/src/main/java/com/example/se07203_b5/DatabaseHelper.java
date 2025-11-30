package com.example.se07203_b5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import static com.example.se07203_b5.CreateTransactionActivity.Transaction;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SE07203Expense";
    private static final int DATABASE_VERSION = 4;

    // BẢNG USER - HẰNG SỐ CHÍNH
    private static final String TABLE_USER = "users";
    private static final String USER_ID = "id";
    private static final String USER_USERNAME = "username";
    private static final String USER_PASSWORD = "password";
    private static final String USER_FULLNAME = "fullname";

    // Bảng Transaction
    private static final String TABLE_TRANSACTION = "transactions";
    private static final String TRANS_ID = "id";
    public static final String TRANS_USER_ID = "user_id";
    public static final String TRANS_AMOUNT = "amount";
    public static final String TRANS_CATEGORY = "category";
    public static final String TRANS_DESCRIPTION = "description";
    public static final String TRANS_DATE = "date";
    public static final String TRANS_TYPE = "type";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng USER
        db.execSQL(
                "CREATE TABLE " + TABLE_USER + "(" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USER_USERNAME + " TEXT, " +
                        USER_FULLNAME + " TEXT, " +
                        USER_PASSWORD + " TEXT)"
        );
        // Tạo bảng TRANSACTION
        db.execSQL(
                "CREATE TABLE " + TABLE_TRANSACTION + "(" +
                        TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        TRANS_USER_ID + " INTEGER, " +
                        TRANS_AMOUNT + " REAL, " +
                        TRANS_CATEGORY + " TEXT, " +
                        TRANS_DESCRIPTION + " TEXT, " +
                        TRANS_DATE + " TEXT, " +
                        TRANS_TYPE + " TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // --- USER FUNCTIONS ---

    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_USERNAME, user.getUsername());
        values.put(USER_PASSWORD, user.getPassword());
        values.put(USER_FULLNAME, user.getFullname());

        long id = db.insert(TABLE_USER, null, values);
        db.close();
        return id;
    }

    public User getUserByUsernameAndPassword(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {USER_ID, USER_USERNAME, USER_FULLNAME, USER_PASSWORD};
        String selection = USER_USERNAME + "=? AND " + USER_PASSWORD + "=?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USER, columns, selection, selectionArgs, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            // Giả định User constructor nhận (id, username, fullname, password)
            user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
        }
        cursor.close();
        db.close();
        return user;
    }


    // --- TRANSACTION CRUD/READ ---

    public long addTransaction(Transaction t) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(TRANS_USER_ID, t.getUserId());
        v.put(TRANS_AMOUNT, t.getAmount());
        v.put(TRANS_CATEGORY, t.getCategory());
        v.put(TRANS_DESCRIPTION, t.getDescription());
        v.put(TRANS_DATE, t.getDate());
        v.put(TRANS_TYPE, t.getType());

        long id = db.insert(TABLE_TRANSACTION, null, v);
        db.close();
        return id;
    }

    public boolean updateTransaction(Transaction t) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(TRANS_AMOUNT, t.getAmount());
        v.put(TRANS_CATEGORY, t.getCategory());
        v.put(TRANS_DESCRIPTION, t.getDescription());
        v.put(TRANS_DATE, t.getDate());
        v.put(TRANS_TYPE, t.getType());

        int result = db.update(TABLE_TRANSACTION, v,
                TRANS_ID + "=?", new String[]{String.valueOf(t.getId())});
        db.close();
        return result > 0;
    }

    public Transaction getTransactionById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TRANSACTION, null, TRANS_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

        Transaction t = null;
        if (c.moveToFirst()) {
            t = new Transaction(c.getLong(0), c.getLong(1), c.getDouble(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6));
        }
        c.close();
        db.close();
        return t;
    }

    // ⭐ HÀM XÓA TRANSACTION ⭐
    public boolean removeTransactionById(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_TRANSACTION, TRANS_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public ArrayList<Transaction> getTransactionsByUserId(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Transaction> transactions = new ArrayList<>();

        Cursor c = db.query(TABLE_TRANSACTION, null, TRANS_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, TRANS_ID + " DESC");

        if (c.moveToFirst()) {
            do {
                Transaction t = new Transaction(
                        c.getLong(0), c.getLong(1), c.getDouble(2), c.getString(3),
                        c.getString(4), c.getString(5), c.getString(6)
                );
                transactions.add(t);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return transactions;
    }


    // --- AGGREGATE FUNCTIONS ---

    public double getTotalAmountByType(long userId, String type) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;
        String query = "SELECT SUM(" + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION +
                " WHERE " + TRANS_USER_ID + "=? AND " + TRANS_TYPE + "=?";
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(userId), type});

        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        db.close();
        return total;
    }

    public double getTotalAmountByTypeAndFilter(long userId, String type, String dateFilter) {
        SQLiteDatabase db = getReadableDatabase();
        double total = 0;
        String query = "SELECT SUM(" + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION +
                " WHERE " + TRANS_USER_ID + "=? AND " + TRANS_TYPE + "=? AND " + dateFilter;
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(userId), type});

        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        db.close();
        return total;
    }

    public Cursor getExpenseBreakdown(long userId, String dateFilter) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Chỉ lấy giao dịch loại EXPENSE
        String query = "SELECT " + TRANS_CATEGORY + ", SUM(" + TRANS_AMOUNT + ") AS total " +
                "FROM " + TABLE_TRANSACTION +
                " WHERE " + TRANS_USER_ID + "=? AND " + TRANS_TYPE + "='EXPENSE' AND " + dateFilter +
                " GROUP BY " + TRANS_CATEGORY;

        // Hàm này sẽ trả về một Cursor chứa các cặp (Category, Total)
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }
}