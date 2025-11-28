package com.example.nhom4.ui.adapter;

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

/**
 * Adapter hiển thị danh sách hoạt động/habit trong màn hình streak.
 * Chịu trách nhiệm bind dữ liệu mô hình {@link Activity} vào layout item_habit_card.
 */
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> list;
    private final OnItemClickListener listener;

    /**
     * Interface thông báo ra ngoài khi người dùng bấm vào một habit.
     */
    public interface OnItemClickListener {
        void onItemClick(Activity activity);
    }

    /**
     * @param list        dữ liệu ban đầu, có thể rỗng
     * @param listener    callback xử lý sự kiện click
     */
    public ActivityAdapter(List<Activity> list, OnItemClickListener listener) {
        this.list = (list != null) ? list : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Thay toàn bộ danh sách và refresh RecyclerView.
     */
    public void setList(List<Activity> list) {
        // Ghi đè danh sách cũ bằng dữ liệu mới từ ViewModel
        this.list = list;
        notifyDataSetChanged();
    }

    /**
     * Tạo ViewHolder cho mỗi item.
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate mỗi item card habit
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit_card, parent, false);
        return new ActivityViewHolder(view);
    }

    /**
     * Bind dữ liệu mô hình vào ViewHolder.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = list.get(position); // Lấy activity tương ứng vị trí hiện tại

        holder.tvTitle.setText(activity.getTitle());
        holder.tvDesc.setText(activity.getDescription() != null ? activity.getDescription() : "");

        // Load ảnh hoạt động bằng Glide
        if (activity.getImageUrl() != null && !activity.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(activity.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(holder.imgIcon);
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Xử lý Progress Bar
        int target = activity.getTarget() > 0 ? activity.getTarget() : 10; // Tránh chia cho 0
        int progress = activity.getProgress();
        int percent = (progress * 100) / target; // Quy đổi sang %
        if (percent > 100) percent = 100;

        if (holder.progressBar != null) {
            holder.progressBar.setProgress(percent);
        }

        if (holder.tvProgressText != null) {
            holder.tvProgressText.setText(progress + "/" + target);
        }

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(activity); // Bắn callback ra Fragment
        });
    }

    /**
     * Trả về số lượng item trong danh sách.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder quản lý toàn bộ view trong một thẻ habit.
     */
    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgIcon; // iv_habit_icon
        TextView tvTitle;           // tv_habit_title
        TextView tvDesc;            // tv_habit_desc
        LinearProgressIndicator progressBar;
        TextView tvProgressText;    // tv_habit_status (hoặc tv_progress_text tùy XML)

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID theo item_habit_card.xml
            imgIcon = itemView.findViewById(R.id.iv_habit_icon);
            tvTitle = itemView.findViewById(R.id.tv_habit_title);
            tvDesc = itemView.findViewById(R.id.tv_habit_desc);

            // Bạn cần đảm bảo file XML có các ID này, hoặc sửa ở đây cho khớp
            progressBar = itemView.findViewById(R.id.progressBar);
            // Nếu chưa có id progressBar trong XML thì thêm vào, hoặc dùng tạm 1 view khác

            // Tạm thời map vào tv_habit_status_label nếu XML của bạn dùng tên đó
            tvProgressText = itemView.findViewById(R.id.tv_habit_status_label);
        }
    }
}
