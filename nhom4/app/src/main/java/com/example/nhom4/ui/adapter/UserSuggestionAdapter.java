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

import java.util.List;

public class UserSuggestionAdapter extends RecyclerView.Adapter<UserSuggestionAdapter.UserViewHolder> {

    private List<User> userList;
    private OnAddFriendClickListener listener;

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
        // Đảm bảo bạn có layout item_recommend_friend.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend_friend, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.getUsername());

        // Load avatar using Glide
        // Make sure to add a placeholder drawable if you haven't already
        // or remove the .placeholder() line if you don't have one.
        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.avatar_placeholder) // Optional: default image
                .error(R.drawable.avatar_placeholder)       // Optional: error image
                .into(holder.ivAvatar);

        holder.btnAdd.setOnClickListener(v -> {
            listener.onAddClick(user);
            holder.btnAdd.setText("Đã gửi");
            holder.btnAdd.setEnabled(false);
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        MaterialButton btnAdd;
        // Add the ImageView for the avatar
        ShapeableImageView ivAvatar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // CORRECTED IDs:
            tvName = itemView.findViewById(R.id.tv_username); // Changed from tv_friend_name
            btnAdd = itemView.findViewById(R.id.btn_add);     // Changed from btn_add_friend
            ivAvatar = itemView.findViewById(R.id.iv_avatar); // Bind the avatar too
        }
    }

}
