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

import java.util.List;

/**
 * Adapter hiển thị danh sách lời mời kết bạn, cho phép chấp nhận hoặc từ chối.
 */
public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<FriendRequest> requestList;
    private OnRequestActionListener listener;
    private FirebaseFirestore db;

    /**
     * Callback để màn hình bên ngoài xử lý hành động Accept/Decline.
     */
    public interface OnRequestActionListener {
        void onAccept(FriendRequest request);
        void onDecline(FriendRequest request);
    }

    public FriendRequestAdapter(List<FriendRequest> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Thay danh sách lời mời mới và refresh lại RecyclerView.
     */
    public void setRequests(List<FriendRequest> requests) {
        this.requestList = requests;
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
        FriendRequest request = requestList.get(position);

        // Load thông tin User
        db.collection("users").document(request.getRequesterId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            holder.tvName.setText(user.getUsername());
                            Glide.with(holder.itemView.getContext())
                                    .load(user.getProfilePhotoUrl())
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(holder.ivAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> {}); // Bỏ qua lỗi nhỏ, UI vẫn hiển thị tên request

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(request); // Báo ra ViewModel xử lý
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
        return requestList != null ? requestList.size() : 0;
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
