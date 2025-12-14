package com.example.nhom4.ui.adapter;

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

import java.util.ArrayList;
import java.util.List;

public class OperatorAdapter extends RecyclerView.Adapter<OperatorAdapter.OperatorViewHolder> {

    private final Context context;
    private List<Activity> list = new ArrayList<>();

    public OperatorAdapter(Context context) {
        this.context = context;
    }

    public void setList(List<Activity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OperatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo tên file layout đúng là file XML bạn vừa sửa (ví dụ: item_habit_card.xml hoặc item_activity.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new OperatorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OperatorViewHolder holder, int position) {
        Activity activity = list.get(position);

        // 1. Gán Tên Hoạt động (Mapping vào tv_activity_name)
        holder.tvName.setText(activity.getTitle());

        // 2. Gán Mô tả thời gian (Mapping vào tv_activity_time)
        // Nếu trong model Activity có trường time, hãy dùng nó. Nếu không, tạm dùng description.
        if (activity.getDescription() != null && !activity.getDescription().isEmpty()) {
            holder.tvTime.setText(activity.getDescription());
        } else {
            // Text mặc định nếu không có mô tả
            holder.tvTime.setText("Chưa thiết lập thời gian");
        }

        // 3. Load Icon
        // (Dùng logic IconMapper hoặc Glide/DiceBear mà chúng ta đã bàn trước đó)
        if (activity.getImageUrl() != null && !activity.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(activity.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.imgIcon);
        } else {
            holder.imgIcon.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            // Todo: Navigate to Detail Activity
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder cập nhật ID mới
     */
    public static class OperatorViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgIcon;
        TextView tvName;  // Đổi tên biến cho dễ hiểu (cũ là tvTitle)
        TextView tvTime;  // Đổi tên biến cho dễ hiểu (cũ là tvDetail)
        // Bỏ tvCategory vì layout mới không còn view này

        public OperatorViewHolder(@NonNull View itemView) {
            super(itemView);

            // --- ÁNH XẠ ID MỚI ---
            imgIcon = itemView.findViewById(R.id.iv_activity_icon); // ID mới trong XML
            tvName = itemView.findViewById(R.id.tv_activity_name);  // ID mới
            tvTime = itemView.findViewById(R.id.tv_activity_time);  // ID mới

            // Không cần ánh xạ iv_arrow_details nếu không cần bắt sự kiện click riêng vào mũi tên
        }
    }
}