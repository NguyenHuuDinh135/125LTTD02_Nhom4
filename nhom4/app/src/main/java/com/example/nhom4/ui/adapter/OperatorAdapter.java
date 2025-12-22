package com.example.nhom4.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OperatorAdapter extends RecyclerView.Adapter<OperatorAdapter.OperatorViewHolder> {

    private final Context context;
    private List<Activity> list = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Activity activity);
    }

    public OperatorAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<Activity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OperatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new OperatorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OperatorViewHolder holder, int position) {
        Activity activity = list.get(position);

        holder.tvName.setText(activity.getTitle());

        // --- Hiển thị khung giờ (VD: 08:00 - 08:30) ---
        if (activity.getScheduledTime() != null) {
            Date startDate = activity.getScheduledTime().toDate();
            // Tính giờ kết thúc dựa trên durationSeconds
            long endTimeMillis = startDate.getTime() + (activity.getDurationSeconds() * 1000);
            Date endDate = new Date(endTimeMillis);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String timeString = String.format("%s - %s (%d phút)",
                    timeFormat.format(startDate),
                    timeFormat.format(endDate),
                    activity.getDurationSeconds() / 60);

            holder.tvTime.setText(timeString);
        } else {
            holder.tvTime.setText("Chưa thiết lập thời gian");
        }

        // --- Load Ảnh ---
        if (activity.getImageUrl() != null && !activity.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(activity.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(holder.imgIcon);
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(activity);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class OperatorViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgIcon;
        TextView tvName;
        TextView tvTime;

        public OperatorViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvName = itemView.findViewById(R.id.tv_activity_name);
            tvTime = itemView.findViewById(R.id.tv_activity_time);
        }
    }
}