package com.example.se07203_b5;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SE07203Expense";
    private static final int DATABASE_VERSION = 5; 

    //    Table User
    private static final String TABLE_USER = "users";
    private static final String TABLE_USER_COLUMN_ID = "id";
    private static final String TABLE_USER_COLUMN_USERNAME = "username";
    private static final String TABLE_USER_COLUMN_PASSWORD = "password";
    private static final String TABLE_USER_COLUMN_FULLNAME = "fullname";

    // Table Product
    private static final String TABLE_PRODUCT = "products";
    private static final String TABLE_PRODUCT_COLUMN_ID = "id";
    private static final String TABLE_PRODUCT_COLUMN_NAME = "name";
    private static final String TABLE_PRODUCT_COLUMN_PRICE = "price";
    private static final String TABLE_PRODUCT_COLUMN_QUANTITY = "quantity";
    private static final String TABLE_PRODUCT_COLUMN_USER_ID = "user_id";
    private static final String TABLE_PRODUCT_COLUMN_TYPE = "type"; 
    private static final String TABLE_PRODUCT_COLUMN_DATE = "date"; 

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "("
                + TABLE_USER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TABLE_USER_COLUMN_USERNAME + " TEXT, "
                + TABLE_USER_COLUMN_FULLNAME + " TEXT, "
                + TABLE_USER_COLUMN_PASSWORD + " TEXT);";
        db.execSQL(CREATE_TABLE_USERS);

        String CREATE_TABLE_PRODUCTS = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCT + "("
                + TABLE_PRODUCT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TABLE_PRODUCT_COLUMN_NAME + " TEXT, "
                + TABLE_PRODUCT_COLUMN_PRICE + " INTEGER, "
                + TABLE_PRODUCT_COLUMN_QUANTITY + " INTEGER, "
                + TABLE_PRODUCT_COLUMN_TYPE + " INTEGER DEFAULT -1, "
                + TABLE_PRODUCT_COLUMN_DATE + " TEXT, "
                + TABLE_PRODUCT_COLUMN_USER_ID + " INTEGER);";
        db.execSQL(CREATE_TABLE_PRODUCTS);
    }

    public long addProduct(Item product, long UserId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TABLE_PRODUCT_COLUMN_NAME, product.getName());
        values.put(TABLE_PRODUCT_COLUMN_QUANTITY, product.getQuantity());
        values.put(TABLE_PRODUCT_COLUMN_PRICE, product.getUnitPrice());
        values.put(TABLE_PRODUCT_COLUMN_TYPE, product.getType());
        values.put(TABLE_PRODUCT_COLUMN_DATE, product.getDate()); 
        values.put(TABLE_PRODUCT_COLUMN_USER_ID, UserId);
        long id = db.insert(TABLE_PRODUCT, null, values);
        db.close();
        return id;
    }

    public ArrayList<Item> getProducts(long UserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCT,
                new String[]{TABLE_PRODUCT_COLUMN_ID, TABLE_PRODUCT_COLUMN_NAME, TABLE_PRODUCT_COLUMN_QUANTITY, TABLE_PRODUCT_COLUMN_PRICE, TABLE_PRODUCT_COLUMN_TYPE, TABLE_PRODUCT_COLUMN_DATE},
                TABLE_PRODUCT_COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(UserId)},
                null, null, null);
        ArrayList<Item> items = new ArrayList<Item>();
        if (cursor.moveToFirst()) {
            do {
                int typeIndex = cursor.getColumnIndex(TABLE_PRODUCT_COLUMN_TYPE);
                int type = (typeIndex != -1) ? cursor.getInt(typeIndex) : -1;
                
                int dateIndex = cursor.getColumnIndex(TABLE_PRODUCT_COLUMN_DATE);
                String date = (dateIndex != -1) ? cursor.getString(dateIndex) : "";

                Item item = new Item(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        type,
                        date
                );
                items.add(item);
            } while (cursor.moveToNext());
        }
        return items;
    }

    public ArrayList<Item> getProductsByType(long UserId, int type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCT,
                new String[]{TABLE_PRODUCT_COLUMN_ID, TABLE_PRODUCT_COLUMN_NAME, TABLE_PRODUCT_COLUMN_QUANTITY, TABLE_PRODUCT_COLUMN_PRICE, TABLE_PRODUCT_COLUMN_TYPE, TABLE_PRODUCT_COLUMN_DATE},
                TABLE_PRODUCT_COLUMN_USER_ID + "=? AND " + TABLE_PRODUCT_COLUMN_TYPE + "=?",
                new String[]{String.valueOf(UserId), String.valueOf(type)},
                null, null, null);
        ArrayList<Item> items = new ArrayList<Item>();
        if (cursor.moveToFirst()) {
            do {
                int dateIndex = cursor.getColumnIndex(TABLE_PRODUCT_COLUMN_DATE);
                String date = (dateIndex != -1) ? cursor.getString(dateIndex) : "";

                Item item = new Item(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        date
                );
                items.add(item);
            } while (cursor.moveToNext());
        }
        return items;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa sạch cả 2 bảng để tạo lại từ đầu cho an toàn, tránh xung đột schema
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
        onCreate(db);
    }
    
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase(); 
        ContentValues values = new ContentValues(); 
        values.put(TABLE_USER_COLUMN_USERNAME, user.getUsername());
        values.put(TABLE_USER_COLUMN_PASSWORD, user.getPassword());
        values.put(TABLE_USER_COLUMN_FULLNAME, user.getFullname());
        long id = db.insert(TABLE_USER, null, values); 
        db.close();
        return id;
    }

    public User getUserByUsernameAndPassword(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase(); 
        Cursor cursor = db.query(TABLE_USER, 
                new String[]{TABLE_USER_COLUMN_ID, TABLE_USER_COLUMN_USERNAME, TABLE_USER_COLUMN_FULLNAME, TABLE_USER_COLUMN_PASSWORD},
                TABLE_USER_COLUMN_USERNAME + "=? AND " + TABLE_USER_COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
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

    public boolean removeProductById(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PRODUCT,
                TABLE_PRODUCT_COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );
        db.close();
        return result > 0;

    }

    public boolean updateProduct(Item product) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TABLE_PRODUCT_COLUMN_NAME, product.getName());
        values.put(TABLE_PRODUCT_COLUMN_PRICE, product.getUnitPrice());
        values.put(TABLE_PRODUCT_COLUMN_QUANTITY, product.getQuantity());
        values.put(TABLE_PRODUCT_COLUMN_TYPE, product.getType());
        values.put(TABLE_PRODUCT_COLUMN_DATE, product.getDate());
        int result = db.update(TABLE_PRODUCT,
                values,
                TABLE_PRODUCT_COLUMN_ID + "=?",
                new String[]{String.valueOf(product.getId())}
        );
        db.close();
        return result > 0;
    }

}
