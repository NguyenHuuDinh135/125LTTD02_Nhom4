package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Reaction;
import java.util.ArrayList;
import java.util.List;

public class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.ViewHolder> {
    private List<Reaction> list = new ArrayList<>();

    public void submitList(List<Reaction> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tận dụng layout item_friend hoặc tạo layout đơn giản tương tự
        // Ở đây giả sử bạn dùng item_contact hoặc layout đơn giản có avatar + tên
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reaction r = list.get(position);
        holder.tvName.setText(r.getUserName());
        // Hiển thị Emoji ngay cạnh tên
        holder.tvDesc.setText("Đã thả " + r.getEmoji());

        if (r.getUserAvatar() != null) {
            Glide.with(holder.itemView.getContext()).load(r.getUserAvatar()).into(holder.imgAvatar);
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvDesc;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID tùy theo layout bạn chọn (ví dụ item_contact)
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDesc = itemView.findViewById(R.id.tv_desc); // Hoặc TextView phụ
        }
    }
}