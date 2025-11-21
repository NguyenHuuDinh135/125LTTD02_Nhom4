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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    private List<FriendRequest> requestList;
    private OnRequestActionListener listener;
    private FirebaseFirestore db;

    public interface OnRequestActionListener {
        void onAccept(FriendRequest request);
        void onDecline(FriendRequest request);
    }

    public FriendRequestAdapter(List<FriendRequest> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
        db = FirebaseFirestore.getInstance();
    }

    public void setRequests(List<FriendRequest> requests) {
        this.requestList = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FriendRequest request = requestList.get(position);

        // Load thông tin User theo requesterId
        db.collection("users").document(request.getRequesterId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            holder.tvName.setText(user.getUsername());
                            Glide.with(holder.itemView.getContext())
                                    .load(user.getProfilePhotoUrl())
                                    .placeholder(R.drawable.avatar_placeholder)
                                    .error(R.drawable.avatar_placeholder)
                                    .into(holder.ivAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(holder.itemView.getContext(),
                        "Lỗi load user: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(request);
            holder.btnAccept.setText("Đã chấp nhận");
            holder.btnAccept.setEnabled(false);
            holder.btnDecline.setEnabled(false);
        });

        holder.btnDecline.setOnClickListener(v -> {
            if (listener != null) listener.onDecline(request);
            holder.btnDecline.setText("Đã từ chối");
            holder.btnDecline.setEnabled(false);
            holder.btnAccept.setEnabled(false);
        });
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        MaterialButton btnAccept, btnDecline;
        ShapeableImageView ivAvatar;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_username);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnReject);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }
    }
}
