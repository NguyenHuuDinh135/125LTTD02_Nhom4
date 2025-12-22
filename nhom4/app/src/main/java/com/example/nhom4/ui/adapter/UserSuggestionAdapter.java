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

    private final List<User> userList;           // Danh sách đầy đủ (giữ nguyên như cũ)
    private final OnAddFriendClickListener listener;

    // Thêm để hỗ trợ giới hạn 5 item (chỉ dùng trong FriendsBottomSheet)
    private boolean isLimitedMode = true;        // Mặc định giới hạn 5 item
    private static final int LIMIT_COUNT = 5;

    public interface OnAddFriendClickListener {
        void onAddClick(User user);
    }

    public UserSuggestionAdapter(List<User> userList, OnAddFriendClickListener listener) {
        this.userList = userList == null ? new ArrayList<>() : userList;
        this.listener = listener;
    }

    /**
     * Tạo ViewHolder cho một item người dùng.
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend_friend, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Bind dữ liệu người dùng vào ViewHolder, xử lý sự kiện nút thêm bạn.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = getCurrentList().get(position); // Lấy từ danh sách đang hiển thị

        holder.tvName.setText(user.getUsername());

        // Sử dụng Glide để tải ảnh avatar
        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.ivAvatar);

        // Reset trạng thái nút khi view được tái sử dụng (tránh lỗi recycle)
        holder.btnAdd.setText("Thêm");
        holder.btnAdd.setEnabled(true);

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddClick(user);
                holder.btnAdd.setText("Đã gửi"); // Đổi trạng thái nút để báo cho người dùng
                holder.btnAdd.setEnabled(false);
            }
        });
    }

    /**
     * Trả về số lượng người dùng trong danh sách.
     */
    @Override
    public int getItemCount() {
        if (!isLimitedMode) {
            return userList.size();
        }
        return Math.min(userList.size(), LIMIT_COUNT);
    }

    /**
     * Bật/tắt chế độ giới hạn 5 item (chỉ dùng trong FriendsBottomSheet)
     */
    public void setLimitedMode(boolean limited) {
        if (this.isLimitedMode != limited) {
            this.isLimitedMode = limited;
            notifyDataSetChanged();
        }
    }

    /**
     * Cập nhật danh sách user mới mà không tạo mới adapter
     */
    public void setUsers(List<User> users) {
        this.userList.clear();
        if (users != null) {
            this.userList.addAll(users);
        }
        notifyDataSetChanged();
    }

    /**
     * Trả về trạng thái hiện tại có đang giới hạn không
     */
    public boolean isLimitedMode() {
        return isLimitedMode;
    }

    /**
     * Trả về tổng số item thực tế (để kiểm tra có cần hiện nút "Xem tất cả" không)
     */
    public int getFullItemCount() {
        return userList.size();
    }

    /**
     * Lấy danh sách đang hiển thị hiện tại (dùng trong onBind để tránh lỗi index)
     */
    private List<User> getCurrentList() {
        if (!isLimitedMode) {
            return userList;
        }
        return userList.size() > LIMIT_COUNT ? userList.subList(0, LIMIT_COUNT) : userList;
    }

    /**
     * ViewHolder giữ thông tin hiển thị avatar + nút kết bạn.
     */
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