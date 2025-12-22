package com.example.nhom4.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.CalendarDay;
import com.example.nhom4.data.bean.Post;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StreakAdapter extends RecyclerView.Adapter<StreakAdapter.DayViewHolder> {

    private List<CalendarDay> days = new ArrayList<>();
    private final LocalDate today = LocalDate.now();

    // [MỚI] Interface lắng nghe sự kiện click
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public void setOnPostClickListener(OnPostClickListener listener) {
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

        // Reset state
        holder.ivDayImage.setVisibility(View.GONE);
        holder.viewOverlay.setVisibility(View.GONE);
        holder.tvDayNumber.setVisibility(View.VISIBLE);
        holder.tvDayNumber.setTextColor(Color.parseColor("#000000"));

        if (day.date == null) {
            holder.tvDayNumber.setText("");
            return;
        }
        holder.tvDayNumber.setText(String.valueOf(day.date.getDayOfMonth()));

        // Opacity cho ngày không thuộc tháng
        holder.itemView.setAlpha(day.isCurrentMonth ? 1.0f : 0.3f);

        // Xử lý hiển thị ảnh
        if (day.hasPost && day.thumbnailUrl != null && !day.thumbnailUrl.isEmpty()) {
            holder.ivDayImage.setVisibility(View.VISIBLE);
            holder.viewOverlay.setVisibility(View.VISIBLE);
            holder.tvDayNumber.setVisibility(View.GONE); // Ẩn số nếu có ảnh

            Glide.with(holder.itemView.getContext())
                    .load(day.thumbnailUrl)
                    .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(12)))
                    .into(holder.ivDayImage);
        }

        // Highlight hôm nay
        if (day.date.equals(today) && !day.hasPost) {
            holder.tvDayNumber.setTextColor(Color.BLUE);
        }

        // [MỚI] Xử lý sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            // Chỉ bắt sự kiện nếu ngày đó có bài viết và listener đã được set
            if (day.hasPost && day.post != null && listener != null) {
                listener.onPostClick(day.post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        ImageView ivDayImage;
        View viewOverlay;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            ivDayImage = itemView.findViewById(R.id.iv_day_image);
            viewOverlay = itemView.findViewById(R.id.view_overlay);
        }
    }
}