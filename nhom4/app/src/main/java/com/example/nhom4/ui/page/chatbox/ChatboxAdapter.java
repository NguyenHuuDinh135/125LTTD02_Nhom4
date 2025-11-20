package com.example.nhom4.ui.page.chatbox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.model.Message;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatboxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messages = new ArrayList<>();
    private String currentUserId;

    public ChatboxAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public ChatboxAdapter(String currentUserId, boolean isGroupChat) {
        this.currentUserId = currentUserId;
    }

    // ... getItemViewType(), onCreateViewHolder(), onBindViewHolder(), getItemCount(), setMessages() giữ nguyên ...
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message == null || message.getSenderId() == null) {
            return VIEW_TYPE_RECEIVED; // Hoặc một kiểu mặc định khác để tránh crash
        }
        return message.getSenderId().equals(currentUserId)
                ? VIEW_TYPE_SENT
                : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sender, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_receiver, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }


    // ViewHolder cho tin nhắn gửi
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessageSent);
            tvTime = itemView.findViewById(R.id.tvTimeSent);
        }

        void bind(Message message) {
            tvMessage.setText(message.getContent());
            // Gọi hàm formatTime đã được cập nhật
            if (message.getCreatedAt() != null) {
                tvTime.setText(formatTime(message.getCreatedAt()));
            }
        }
    }

    // ViewHolder cho tin nhắn nhận
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvMessage, tvTime;
        // Bỏ tvSenderName vì nó không có trong model Message
        // TextView tvSenderName;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.imageAvatarReceiver);
            tvMessage = itemView.findViewById(R.id.textMessageReceiver);
            tvTime = itemView.findViewById(R.id.textTimeReceiver);
        }

        void bind(Message message) {
            // tvSenderName.setText(message.getSenderId()); // Dòng này sẽ gây lỗi nếu tvSenderName bị comment
            tvMessage.setText(message.getContent());
            // Gọi hàm formatTime đã được cập nhật
            if (message.getCreatedAt() != null) {
                tvTime.setText(formatTime(message.getCreatedAt()));
            }
            // Load avatar với Glide hoặc Picasso
        }
    }

    private static String formatTime(Timestamp timestamp) {
        if (timestamp == null) {
            return ""; // Trả về chuỗi rỗng nếu timestamp là null
        }
        // Chuyển đổi com.google.firebase.Timestamp thành java.util.Date
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
