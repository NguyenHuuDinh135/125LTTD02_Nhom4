package com.example.nhom4.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Conversation;
import com.example.nhom4.ui.page.chat.ChatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho danh sách cuộc trò chuyện tại màn hình Chat List.
 * Hiển thị tên bạn bè, tin nhắn gần nhất và avatar.
 * Click vào item → mở ChatActivity với thông tin đúng.
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private final Context context;
    private List<Conversation> list = new ArrayList<>();

    public ChatListAdapter(Context context) {
        this.context = context;
    }

    /**
     * Cập nhật danh sách hội thoại (gọi từ ViewModel khi có dữ liệu mới).
     */
    public void setList(List<Conversation> list) {
        this.list = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Conversation conversation = list.get(position);

        // Hiển thị tên bạn bè
        String friendName = conversation.getFriendName();
        holder.tvFriendName.setText(friendName != null && !friendName.isEmpty() ? friendName : "Người dùng");

        // Hiển thị tin nhắn gần nhất
        String lastMessage = conversation.getLastMessage();
        if (lastMessage != null && !lastMessage.isEmpty()) {
            holder.tvLastMessage.setText(lastMessage);
        } else {
            holder.tvLastMessage.setText("Bắt đầu trò chuyện ngay");
        }

        // Load avatar bằng Glide (bo tròn)
        String avatarUrl = conversation.getFriendAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.avatar_placeholder);
        }

        // Click vào item → mở ChatActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
            intent.putExtra("PARTNER_ID", conversation.getFriendId());
            intent.putExtra("PARTNER_NAME", conversation.getFriendName());
            intent.putExtra("PARTNER_AVATAR", conversation.getFriendAvatar());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder ánh xạ các view trong chat_item.xml mới.
     */
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvFriendName;
        TextView tvLastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvFriendName = itemView.findViewById(R.id.tv_friend_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
        }
    }
}