package com.example.nhom4.ui.page.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.model.FriendRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<FriendRequest> requestList;
    private OnRequestActionListener listener;

    // Constructor
    public FriendRequestAdapter(List<FriendRequest> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest request = requestList.get(position);

        // Lấy username từ Firestore dựa trên requesterId
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(request.getRequesterId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        holder.txtName.setText(username != null ? username : request.getRequesterId());
                    } else {
                        holder.txtName.setText(request.getRequesterId());
                    }
                });

        // Hiển thị/ẩn nút theo status
        if ("pending".equals(request.getStatus())) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnDecline.setVisibility(View.VISIBLE);
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);
        }

        // Click Accept / Decline
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(request, position);
        });
        holder.btnDecline.setOnClickListener(v -> {
            if (listener != null) listener.onDecline(request, position);
        });
    }


    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    public void setRequests(List<FriendRequest> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView txtName;
        Button btnAccept, btnDecline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_avatar);
            txtName = itemView.findViewById(R.id.tv_username);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
        }
    }

    // Interface callback để Activity/Fragment xử lý Accept/Decline
    public interface OnRequestActionListener {
        void onAccept(FriendRequest request, int position);
        void onDecline(FriendRequest request, int position);
    }
}
