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

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho danh sách người dùng được gợi ý kết bạn.
 */
public class UserSuggestionAdapter extends RecyclerView.Adapter<UserSuggestionAdapter.UserViewHolder> {

    private final List<User> userList;           // Danh sách đầy đủ
    private final OnAddFriendClickListener listener;

    private boolean isLimitedMode = true;        // Mặc định giới hạn hiển thị
    private static final int LIMIT_COUNT = 5;    // Số lượng item tối đa khi thu gọn

    public interface OnAddFriendClickListener {
        void onAddClick(User user);
    }

    public UserSuggestionAdapter(List<User> userList, OnAddFriendClickListener listener) {
        this.userList = userList == null ? new ArrayList<>() : userList;
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
        // Lấy item từ danh sách ảo (đã cắt giảm nếu đang ở chế độ limit)
        User user = getCurrentList().get(position);

        holder.tvName.setText(user.getUsername());

        // Load ảnh
        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.ivAvatar);

        holder.btnAdd.setText("Thêm");
        holder.btnAdd.setEnabled(true);

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
        // Nếu không limit -> trả về size thật
        if (!isLimitedMode) {
            return userList.size();
        }
        // Nếu limit -> trả về max là 5 hoặc size thật nếu nhỏ hơn 5
        return Math.min(userList.size(), LIMIT_COUNT);
    }

    /**
     * Bật/tắt chế độ giới hạn
     */
    public void setLimitedMode(boolean limited) {
        if (this.isLimitedMode != limited) {
            this.isLimitedMode = limited;
            notifyDataSetChanged();
        }
    }

    public void setUsers(List<User> users) {
        this.userList.clear();
        if (users != null) {
            this.userList.addAll(users);
        }
        notifyDataSetChanged();
    }

    public boolean isLimitedMode() {
        return isLimitedMode;
    }

    public int getFullItemCount() {
        return userList.size();
    }

    /**
     * Helper để lấy sublist an toàn cho onBindViewHolder
     */
    private List<User> getCurrentList() {
        if (!isLimitedMode) {
            return userList;
        }
        if (userList.size() > LIMIT_COUNT) {
            return userList.subList(0, LIMIT_COUNT);
        }
        return userList;
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