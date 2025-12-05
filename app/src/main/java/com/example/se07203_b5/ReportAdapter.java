package com.example.se07203_b5;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<ReportItem> reportList;

    public ReportAdapter(List<ReportItem> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo tên file layout đúng là item_report_detail.xml trong thư mục res/layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_detail, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportItem item = reportList.get(position);

        // Nếu dòng này báo đỏ, hãy làm Bước 2 bên dưới
        holder.tvName.setText(item.categoryName);
        holder.tvIcon.setText(getEmojiForCategory(item.categoryName));

        holder.tvAmount.setText(String.format(Locale.getDefault(), "%,.0f đ", item.amount));
        holder.tvPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", item.percentage));

        holder.tvAmount.setTextColor(Color.parseColor("#F44336"));
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    private String getEmojiForCategory(String categoryName) {
        switch (categoryName) {
            case "Ăn uống": return "🍜";
            case "Di chuyển": return "🛵";
            case "Nhà ở": return "🏠";
            case "Hóa đơn": return "🧾";
            case "Mỹ phẩm": return "💄";
            case "Phí giao lưu": return "🍻";
            case "Y tế": return "💊";
            case "Giáo dục": return "📚";
            case "Tiền điện": return "⚡";
            case "Đi lại": return "🚆";
            case "Quần áo": return "👕";
            case "Lương": return "💰";
            case "Thưởng": return "🎁";
            case "Đầu tư": return "📈";
            case "Phụ cấp": return "💎";
            case "Thu nhập phụ": return "💸";
            default: return "📦";
        }
    }

    // --- ViewHolder ---
    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        // Các biến này PHẢI là public
        public TextView tvName;
        public TextView tvIcon;
        public TextView tvAmount;
        public TextView tvPercentage;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvIcon = itemView.findViewById(R.id.tvCategoryIcon);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
        }
    }

    // --- Data Model ---
    public static class ReportItem {
        public String categoryName;
        public double amount;
        public float percentage;

        public ReportItem(String categoryName, double amount, float percentage) {
            this.categoryName = categoryName;
            this.amount = amount;
            this.percentage = percentage;
        }
    }
}