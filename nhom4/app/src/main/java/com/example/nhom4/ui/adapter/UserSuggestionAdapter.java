package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.bumptech.glide.Glide;

import java.util.List;

public class UserSuggestionAdapter extends RecyclerView.Adapter<UserSuggestionAdapter.UserViewHolder> {

    private final List<User> userList;
    private final OnAddFriendClickListener listener;

    public interface OnAddFriendClickListener {
        void onAddClick(User user);
    }

    public UserSuggestionAdapter(List<User> userList, OnAddFriendClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend_friend, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvName.setText(user.getUsername());

        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.ivAvatar);

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddClick(user);
                holder.btnAdd.setText("Đã gửi");
                holder.btnAdd.setEnabled(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        MaterialButton btnAdd;
        ShapeableImageView ivAvatar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_username);
            btnAdd = itemView.findViewById(R.id.btn_add);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}
