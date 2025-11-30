package com.example.se07203_b5;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import static com.example.se07203_b5.CreateTransactionActivity.Transaction;

public class TransactionAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Transaction> transactionList;
    private final LayoutInflater inflater;

    public TransactionAdapter(Context context, ArrayList<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return transactionList.size();
    }

    @Override
    public Object getItem(int position) {
        return transactionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return transactionList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_transaction_adapter, parent, false);

            holder = new ViewHolder();
            holder.ivIcon = convertView.findViewById(R.id.ivTransactionIcon);
            holder.tvCategory = convertView.findViewById(R.id.tvTransactionCategory);
            holder.tvDescription = convertView.findViewById(R.id.tvTransactionDescription);
            holder.tvAmount = convertView.findViewById(R.id.tvTransactionAmount);
            holder.tvDate = convertView.findViewById(R.id.tvTransactionDate);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Transaction transaction = transactionList.get(position);

        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDescription.setText(transaction.getDescription());
        holder.tvDate.setText(transaction.getDate());

        String amountText;
        int color;

        if ("EXPENSE".equals(transaction.getType())) {
            amountText = String.format(Locale.getDefault(), "- %,.0f VNĐ", transaction.getAmount());
            color = Color.parseColor("#F44336"); // Đỏ
            // holder.ivIcon.setImageResource(R.drawable.ic_expense_default); // Cần tạo drawable
            holder.ivIcon.setColorFilter(color);
        } else {
            amountText = String.format(Locale.getDefault(), "+ %,.0f VNĐ", transaction.getAmount());
            color = Color.parseColor("#4CAF50"); // Xanh lá
            // holder.ivIcon.setImageResource(R.drawable.ic_income_default); // Cần tạo drawable
            holder.ivIcon.setColorFilter(color);
        }

        holder.tvAmount.setText(amountText);
        holder.tvAmount.setTextColor(color);

        return convertView;
    }

    static class ViewHolder {
        ImageView ivIcon;
        TextView tvCategory;
        TextView tvDescription;
        TextView tvAmount;
        TextView tvDate;
    }
}