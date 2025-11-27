package com.example.nhom4.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.Activity;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter liệt kê hoạt động nổi bật (operator panel) trong màn Discovery/Main.
 */
public class OperatorAdapter extends RecyclerView.Adapter<OperatorAdapter.OperatorViewHolder> {

    private final Context context;
    private List<Activity> list = new ArrayList<>();

    public OperatorAdapter(Context context) {
        this.context = context;
    }

    /**
     * Cập nhật danh sách hoạt động và notify để vẽ lại.
     */
    public void setList(List<Activity> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    /**
     * Tạo ViewHolder cho một item hoạt động.
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public OperatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng item_activity.xml bạn đã cung cấp
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new OperatorViewHolder(view);
    }

    /**
     * Bind dữ liệu mô hình vào ViewHolder.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull OperatorViewHolder holder, int position) {
        Activity activity = list.get(position);

        // Gán dữ liệu Activity vào View
        holder.tvCategory.setText("Hoạt động"); // Hoặc lấy từ type activity nếu có
        holder.tvTitle.setText(activity.getTitle());

        // Mô tả hoặc tiến độ
        String desc = "Tiến độ: " + activity.getProgress() + "/" + activity.getTarget();
        holder.tvDetail.setText(desc);

        // Load Icon (Giả sử iconRes lưu tên resource hoặc url)
        // Ở đây demo icon mặc định nếu chưa có logic icon động
        holder.imgIcon.setImageResource(R.drawable.element);

        // Sự kiện click (nếu muốn mở chi tiết)
        holder.itemView.setOnClickListener(v -> {
            // Todo: Navigate to Detail Activity
        });
    }

    /**
     * Trả về số lượng hoạt động trong danh sách.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder giữ thông tin icon, category, title, detail của một hoạt động.
     */
    public static class OperatorViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgIcon;
        TextView tvCategory, tvTitle, tvDetail;

        public OperatorViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ theo item_activity.xml
            imgIcon = itemView.findViewById(R.id.myImageView);
            tvCategory = itemView.findViewById(R.id.textView2);
            tvTitle = itemView.findViewById(R.id.textView3);
            tvDetail = itemView.findViewById(R.id.textView4);
        }
    }
}
