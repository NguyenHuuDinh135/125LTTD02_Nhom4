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
import com.example.nhom4.ui.page.chat.ChatActivity; // [QUAN TRỌNG] Import đúng Activity mới

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private final Context context;
    private List<Conversation> list = new ArrayList<>();

    public ChatListAdapter(Context context) {
        this.context = context;
    }

    public void setList(List<Conversation> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo tên layout item đúng là chat_item.xml
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Conversation conversation = list.get(position);

        // 1. Hiển thị thông tin
        holder.tvName.setText(conversation.getFriendName());

        if (conversation.getLastMessage() != null) {
            holder.tvLastMessage.setText(conversation.getLastMessage());
        } else {
            holder.tvLastMessage.setText("Bắt đầu trò chuyện");
        }

        if (conversation.getFriendAvatar() != null) {
            Glide.with(context)
                    .load(conversation.getFriendAvatar())
                    .circleCrop()
                    .placeholder(R.drawable.avatar_placeholder) // Ảnh mặc định nếu lỗi
                    .into(holder.imgAvatar);
        }

        // 2. [QUAN TRỌNG] Sự kiện click mở màn hình Chat
        holder.itemView.setOnClickListener(v -> {
            // Mở ChatActivity mới
            Intent intent = new Intent(context, ChatActivity.class);

            // Truyền đúng KEY mà ChatActivity đang chờ nhận
            intent.putExtra("CONVERSATION_ID", conversation.getConversationId());
            intent.putExtra("PARTNER_ID", conversation.getFriendId());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName;
        TextView tvLastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID theo file chat_item.xml của bạn
            imgAvatar = itemView.findViewById(R.id.my_image_view);
            tvName = itemView.findViewById(R.id.textView3);
            tvLastMessage = itemView.findViewById(R.id.textView4);
        }
    }
}
