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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StreakAdapter extends RecyclerView.Adapter<StreakAdapter.DayViewHolder> {

    private List<CalendarDay> days = new ArrayList<>();
    private final LocalDate today = LocalDate.now();

    public void setDays(List<CalendarDay> days) {
        this.days = days;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng đúng layout bạn đã cung cấp
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        // 1. Reset View
        holder.ivDayImage.setVisibility(View.GONE);
        holder.viewOverlay.setVisibility(View.GONE);
        holder.tvDayNumber.setTextColor(Color.parseColor("#000000")); // Mặc định màu đen (hoặc lấy từ attr)

        // 2. Hiển thị số ngày
        if (day.date == null) {
            holder.tvDayNumber.setText("");
            return;
        }
        holder.tvDayNumber.setText(String.valueOf(day.date.getDayOfMonth()));

        // 3. Xử lý mờ cho ngày không thuộc tháng hiện tại
        if (!day.isCurrentMonth) {
            holder.itemView.setAlpha(0.3f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        // 4. Xử lý hiển thị ẢNH (Streak)
        if (day.hasPost && day.thumbnailUrl != null && !day.thumbnailUrl.isEmpty()) {
            // Có bài viết + có ảnh -> Hiện ảnh
            holder.ivDayImage.setVisibility(View.VISIBLE);
            holder.viewOverlay.setVisibility(View.VISIBLE);

            // Load ảnh bằng Glide
            Glide.with(holder.itemView.getContext())
                    .load(day.thumbnailUrl)
                    // Sửa ở đây: Dùng CenterCrop là chủ đạo, bo góc nhẹ hơn (ví dụ 12)
                    .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(12)))
                    .into(holder.ivDayImage);


            // Đổi màu chữ sang trắng để nổi trên nền ảnh
            holder.tvDayNumber.setTextColor(Color.WHITE);
        }
        // Nếu có bài viết nhưng không có ảnh (chỉ là mood/text) -> Có thể hiện nền màu
        else if (day.hasPost) {
            // Tùy chọn: Set background màu cam nếu chỉ có text
            // holder.ivDayImage.setBackgroundColor(Color.parseColor("#FF5722"));
            // holder.ivDayImage.setVisibility(View.VISIBLE);
        }

        // 5. Highlight ngày hôm nay (nếu chưa có ảnh)
        if (day.date.equals(today) && !day.hasPost) {
            holder.tvDayNumber.setTextColor(Color.BLUE);
        }
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
            // Ánh xạ đúng ID trong item_calendar_day.xml của bạn
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            ivDayImage = itemView.findViewById(R.id.iv_day_image);
            viewOverlay = itemView.findViewById(R.id.view_overlay);
        }
    }
}
