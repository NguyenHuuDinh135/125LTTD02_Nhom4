package com.example.nhom4.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.Activity;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private final List<Activity> activityList;
    private final OnActivitySelectedListener listener; // Biến lưu listener
    private int selectedPosition = -1; // Vị trí item đang được chọn

    // 1. Định nghĩa Interface để Fragment có thể lắng nghe
    public interface OnActivitySelectedListener {
        void onActivitySelected(Activity activity);
    }

    // 2. Cập nhật Constructor để nhận thêm Listener
    public ActivityAdapter(List<Activity> activityList, OnActivitySelectedListener listener) {
        this.activityList = activityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lưu ý: Đảm bảo layout item_activity_text của bạn phù hợp (ví dụ dùng MaterialCardView hoặc TextView có background selector)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_text, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        holder.activityTitle.setText(activity.getTitle());

        // 3. Xử lý hiệu ứng Visual khi được chọn
        if (selectedPosition == position) {
            // Item đang chọn: Đổi màu hoặc style (Ví dụ: Đậm, màu primary container)
            holder.itemView.setBackgroundColor(Color.LTGRAY); // Hoặc setBackgroundResource(...)
            holder.activityTitle.getPaint().setFakeBoldText(true);
        } else {
            // Item bình thường
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.activityTitle.getPaint().setFakeBoldText(false);
        }

        // 4. Bắt sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            // Cập nhật vị trí chọn để redraw lại giao diện
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(previousPosition); // Reset item cũ
            notifyItemChanged(selectedPosition); // Highlight item mới

            // Gọi callback về Fragment
            if (listener != null) {
                listener.onActivitySelected(activity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView activityTitle;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            activityTitle = itemView.findViewById(R.id.activity_title);
        }
    }
}
