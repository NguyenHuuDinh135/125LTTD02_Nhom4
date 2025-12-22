package com.example.nhom4.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Activity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> originalList = new ArrayList<>(); // Danh sách gốc từ ViewModel
    private List<Activity> filteredList = new ArrayList<>(); // Danh sách đã lọc (chỉ hiển thị chưa hoàn thành)
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Activity activity);
    }

    public ActivityAdapter(List<Activity> list, OnItemClickListener listener) {
        this.listener = listener;
        setList(list); // Khởi tạo với lọc ngay từ đầu
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<Activity> newList) {
        this.originalList.clear();
        if (newList != null) {
            this.originalList.addAll(newList);
        }

        // LỌC: Chỉ giữ lại các activity chưa hoàn thành (progress < target)
        filteredList.clear();
        for (Activity activity : originalList) {
            if (activity.getProgress() < activity.getTarget()) {
                filteredList.add(activity);
            }
        }

        notifyDataSetChanged(); // Cập nhật toàn bộ UI
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit_card, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = filteredList.get(position);

        holder.tvTitle.setText(activity.getTitle());

        // Mô tả: thời gian + loại
        long durationMin = activity.getDurationSeconds() / 60;
        String desc = String.format(Locale.getDefault(), "%d phút • %s",
                durationMin,
                activity.isDaily() ? "Hằng ngày" : "Mục tiêu: " + activity.getTarget());
        holder.tvDesc.setText(desc);

        // Load ảnh bìa
        if (activity.getImageUrl() != null && !activity.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(activity.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(holder.imgIcon);
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Progress bar & text
        int target = activity.getTarget() > 0 ? activity.getTarget() : 1;
        int progress = activity.getProgress();
        int percent = (int) (((float) progress / target) * 100);
        if (percent > 100) percent = 100;

        if (holder.progressBar != null) {
            holder.progressBar.setProgress(percent);
        }

        if (holder.tvProgressText != null) {
            holder.tvProgressText.setText(String.format(Locale.getDefault(), "%d/%d", progress, target));
        }

        // Click để chọn activity
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(activity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgIcon;
        TextView tvTitle;
        TextView tvDesc;
        LinearProgressIndicator progressBar;
        TextView tvProgressText;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.iv_habit_icon);
            tvTitle = itemView.findViewById(R.id.tv_habit_title);
            tvDesc = itemView.findViewById(R.id.tv_habit_desc);
            progressBar = itemView.findViewById(R.id.progressBar);
            tvProgressText = itemView.findViewById(R.id.tv_habit_status_label);
        }
    }
}