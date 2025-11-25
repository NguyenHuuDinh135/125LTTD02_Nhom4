package com.example.nhom4.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Conversation;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.ui.page.chat.ChatActivity;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<Conversation> conversationList;
    private final Context context;
    private final String currentUserId;
    // Vẫn giữ Firestore ở đây để load thông tin User phụ trợ (Avatar/Name)
    // Trong dự án lớn, việc này nên thực hiện ở ViewModel và trả về một List<ChatUiModel>
    private final FirebaseFirestore db;

    // Cache: Lưu trữ user đã tải để không phải tải lại khi scroll
    private final Map<String, User> userCache = new HashMap<>();

    public ChatListAdapter(Context context) {
        this.context = context;
        this.conversationList = new ArrayList<>();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        this.db = FirebaseFirestore.getInstance();
    }

    public void setList(List<Conversation> list) {
        this.conversationList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        // 1. Hiển thị tin nhắn cuối
        String msg = conversation.getLastMessage() != null ? conversation.getLastMessage() : "Bắt đầu trò chuyện";
        holder.tvLastMessage.setText(msg);

        // 2. Tìm ID người chat cùng
        String partnerId = null;
        if (conversation.getMembers() != null) {
            for (String memberId : conversation.getMembers()) {
                if (!memberId.equals(currentUserId)) {
                    partnerId = memberId;
                    break;
                }
            }
        }

        // 3. Load thông tin User (Có sử dụng Cache để tối ưu)
        if (partnerId != null) {
            loadPartnerInfo(holder, partnerId);

            String finalPartnerId = partnerId;
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
                intent.putExtra("PARTNER_ID", finalPartnerId);
                context.startActivity(intent);
            });
        }
    }

    private void loadPartnerInfo(ChatViewHolder holder, String partnerId) {
        // Kiểm tra Cache trước
        if (userCache.containsKey(partnerId)) {
            bindUserToView(holder, userCache.get(partnerId));
            return;
        }

        // Nếu chưa có, gọi Firestore (Chỉ gọi 1 lần duy nhất cho mỗi User)
        db.collection("users").document(partnerId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                User user = snapshot.toObject(User.class);
                if (user != null) {
                    // Lưu vào cache
                    userCache.put(partnerId, user);
                    bindUserToView(holder, user);
                }
            }
        }).addOnFailureListener(e -> {
            holder.tvName.setText("Người dùng");
        });
    }

    private void bindUserToView(ChatViewHolder holder, User user) {
        holder.tvName.setText(user.getUsername() != null ? user.getUsername() : "Người dùng");
        if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfilePhotoUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgAvatar;
        TextView tvName;
        TextView tvLastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.my_image_view);
            tvName = itemView.findViewById(R.id.textView3);
            tvLastMessage = itemView.findViewById(R.id.textView4);

            // Fix màu chữ nếu nền tối/sáng
            tvName.setTextColor(itemView.getResources().getColor(android.R.color.black));
            tvLastMessage.setTextColor(itemView.getResources().getColor(android.R.color.darker_gray));
        }
    }
}
