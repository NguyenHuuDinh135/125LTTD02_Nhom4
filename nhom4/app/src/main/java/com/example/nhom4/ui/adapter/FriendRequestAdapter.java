package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<User> requestList;
    private OnRequestActionListener listener;

    // Interface để Fragment xử lý sự kiện
    public interface OnRequestActionListener {
        void onAccept(User user);
        // void onDecline(User user); // Có thể thêm từ chối sau này
    }

    public FriendRequestAdapter(List<User> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout item_friend (giả sử bạn đã có layout này giống item_recommend_friend)
        // Nếu chưa có, bạn có thể dùng tạm item_recommend_friend
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend_friend, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        User user = requestList.get(position);
        holder.tvName.setText(user.getUsername());

        // Đổi text nút thành "Chấp nhận"
        holder.btnAdd.setText("Chấp nhận");

        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .into(holder.ivAvatar);

        holder.btnAdd.setOnClickListener(v -> {
            listener.onAccept(user);
            holder.btnAdd.setText("Bạn bè");
            holder.btnAdd.setEnabled(false);
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        MaterialButton btnAdd;
        ShapeableImageView ivAvatar;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_username);
            btnAdd = itemView.findViewById(R.id.btn_add);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}
