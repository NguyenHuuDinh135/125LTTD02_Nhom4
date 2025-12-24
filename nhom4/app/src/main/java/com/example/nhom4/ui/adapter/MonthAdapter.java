package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.MonthData;
import com.example.nhom4.data.bean.Post;
import java.util.ArrayList;
import java.util.List;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.MonthViewHolder> {

    private List<MonthData> monthList = new ArrayList<>();
    private final DayAdapter.OnPostClickListener postClickListener;

    public MonthAdapter(DayAdapter.OnPostClickListener listener) {
        this.postClickListener = listener;
    }

    public void setMonthList(List<MonthData> list) {
        this.monthList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month_card, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        MonthData data = monthList.get(position);

        // Set Title (Ví dụ: "Tháng 3 2025")
        String title = "Tháng " + data.yearMonth.getMonthValue() + " " + data.yearMonth.getYear();
        holder.tvMonthTitle.setText(title);

        // Setup Inner RecyclerView (Days)
        DayAdapter dayAdapter = new DayAdapter(postClickListener);
        holder.rvDays.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 7));
        holder.rvDays.setAdapter(dayAdapter);
        dayAdapter.setDays(data.days);
    }

    @Override
    public int getItemCount() {
        return monthList.size();
    }

    static class MonthViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthTitle;
        RecyclerView rvDays;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonthTitle = itemView.findViewById(R.id.tv_month_title);
            rvDays = itemView.findViewById(R.id.rv_days);
            // Tối ưu nested scrolling
            rvDays.setNestedScrollingEnabled(false);
        }
    }
}