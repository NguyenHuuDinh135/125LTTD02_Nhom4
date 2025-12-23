package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.FriendRequest;
import com.example.nhom4.data.bean.User;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView; // QUAN TRỌNG
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách lời mời kết bạn, cho phép chấp nhận hoặc từ chối.
 */
public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<FriendRequest> requestList;
    private OnRequestActionListener listener;
    private FirebaseFirestore db;

    // Thêm để hỗ trợ giới hạn 5 item (chỉ dùng trong FriendsBottomSheet)
    private boolean isLimitedMode = true;        // Mặc định bật giới hạn 5 item
    private static final int LIMIT_COUNT = 5;

    /**
     * Callback để màn hình bên ngoài xử lý hành động Accept/Decline.
     */
    public interface OnRequestActionListener {
        void onAccept(FriendRequest request);
        void onDecline(FriendRequest request);
    }

    public FriendRequestAdapter(List<FriendRequest> requestList, OnRequestActionListener listener) {
        this.requestList = requestList == null ? new ArrayList<>() : requestList;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Thay danh sách lời mời mới và refresh lại RecyclerView.
     */
    public void setRequests(List<FriendRequest> requests) {
        this.requestList = requests == null ? new ArrayList<>() : requests;
        notifyDataSetChanged();
    }

    /**
     * Tạo ViewHolder cho mỗi item lời mời kết bạn.
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    /**
     * Bind dữ liệu mô hình vào ViewHolder.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FriendRequest request = getCurrentList().get(position);

        // Sử dụng sender đã load sẵn từ repository (không cần load lại)
        User sender = request.getSender();
        if (sender != null) {
            holder.tvName.setText(sender.getUsername() != null ? sender.getUsername() : "User name");  // Fallback nếu null
            Glide.with(holder.itemView.getContext())
                    .load(sender.getProfilePhotoUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.ivAvatar);
        } else {
            // Nếu sender chưa load (hiếm xảy ra), fallback load từ Firestore với senderId đúng
            db.collection("users").document(request.getSenderId())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                holder.tvName.setText(user.getUsername() != null ? user.getUsername() : "User name");
                                Glide.with(holder.itemView.getContext())
                                        .load(user.getProfilePhotoUrl())
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .error(R.drawable.ic_launcher_foreground)
                                        .into(holder.ivAvatar);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        holder.tvName.setText("User name");  // Fallback lỗi
                    });
        }

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(request);
        });

        holder.btnDecline.setOnClickListener(v -> {
            if (listener != null) listener.onDecline(request);
        });
    }

    /**
     * Trả về số lượng lời mời trong danh sách.
     */
    @Override
    public int getItemCount() {
        if (!isLimitedMode || requestList == null) {
            return requestList == null ? 0 : requestList.size();
        }
        return Math.min(requestList.size(), LIMIT_COUNT);
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
     * Trả về trạng thái hiện tại có đang giới hạn không
     */
    public boolean isLimitedMode() {
        return isLimitedMode;
    }

    /**
     * Trả về tổng số lời mời thực tế (để kiểm tra có cần hiện nút "Xem tất cả" không)
     */
    public int getFullItemCount() {
        return requestList == null ? 0 : requestList.size();
    }

    /**
     * Lấy danh sách đang hiển thị hiện tại (dùng trong onBind để tránh lỗi index)
     */
    private List<FriendRequest> getCurrentList() {
        if (requestList == null) {
            return new ArrayList<>();
        }
        if (!isLimitedMode) {
            return requestList;
        }
        return requestList.size() > LIMIT_COUNT ? requestList.subList(0, LIMIT_COUNT) : requestList;
    }

    /**
     * ViewHolder giữ view của một lời mời kết bạn.
     */
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        MaterialButton btnAccept, btnDecline;
        ShapeableImageView ivAvatar; // Đã khớp với XML

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_username);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}