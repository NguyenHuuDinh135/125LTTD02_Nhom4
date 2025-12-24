package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.CalendarDay;
import com.example.nhom4.data.bean.Post;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private List<CalendarDay> days = new ArrayList<>();
    private final OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public DayAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setDays(List<CalendarDay> days) {
        this.days = days;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        if (day == null) {
            // Ngày padding (trống)
            holder.itemView.setVisibility(View.INVISIBLE);
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
        }

        holder.tvDayNumber.setText(String.valueOf(day.date.getDayOfMonth()));

        if (day.hasPost) {
            holder.ivImage.setVisibility(View.VISIBLE);
            holder.tvDayNumber.setVisibility(View.GONE); // Ẩn số nếu có ảnh

            if (day.thumbnailUrl != null) {
                Glide.with(holder.itemView.getContext())
                        .load(day.thumbnailUrl)
                        .centerCrop()
                        .into(holder.ivImage);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null && day.post != null) {
                    listener.onPostClick(day.post);
                }
            });
        } else {
            holder.ivImage.setVisibility(View.GONE);
            holder.tvDayNumber.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        ShapeableImageView ivImage;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            ivImage = itemView.findViewById(R.id.iv_day_image);
        }
    }
}