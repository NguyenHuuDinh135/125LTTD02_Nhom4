package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Message;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private static final int TYPE_WIDGET_SENT = 3;
    private static final int TYPE_WIDGET_RECEIVED = 4;

    private List<Message> messages = new ArrayList<>();
    private final String currentUserId;

    public ChatAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isMe = message.getSenderId().equals(currentUserId);

        if ("post_reply".equals(message.getType())) {
            return isMe ? TYPE_WIDGET_SENT : TYPE_WIDGET_RECEIVED;
        }
        return isMe ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_WIDGET_SENT:
                return new WidgetSenderHolder(inflater.inflate(R.layout.item_message_widget_sender, parent, false));
            case TYPE_WIDGET_RECEIVED:
                return new WidgetReceiverHolder(inflater.inflate(R.layout.item_message_widget_receiver, parent, false));
            case TYPE_SENT:
                return new SentHolder(inflater.inflate(R.layout.item_message_sender, parent, false));
            default:
                return new ReceivedHolder(inflater.inflate(R.layout.item_message_receiver, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof SentHolder) ((SentHolder) holder).bind(msg);
        else if (holder instanceof ReceivedHolder) ((ReceivedHolder) holder).bind(msg);
        else if (holder instanceof WidgetSenderHolder) ((WidgetSenderHolder) holder).bind(msg);
        else if (holder instanceof WidgetReceiverHolder) ((WidgetReceiverHolder) holder).bind(msg);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private static String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate());
    }

    // --- VIEW HOLDERS ---

    static class SentHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        SentHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMessageSent);
            tvTime = v.findViewById(R.id.tvTimeSent);
        }
        void bind(Message m) {
            tvMsg.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));
        }
    }

    static class ReceivedHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        ShapeableImageView avatar;
        ReceivedHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tv_message_content);
            tvTime = v.findViewById(R.id.tv_timestamp);
            avatar = v.findViewById(R.id.img_avatar);
        }
        void bind(Message m) {
            tvMsg.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));
            // Load avatar logic here if needed
        }
    }

    static class WidgetSenderHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvBody;
        WidgetSenderHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_post_title);
            tvTime = v.findViewById(R.id.tv_post_time);
            tvBody = v.findViewById(R.id.tv_message_body);
        }
        void bind(Message m) {
            tvTitle.setText(m.getReplyPostTitle());
            tvBody.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));
        }
    }

    static class WidgetReceiverHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvBody;
        WidgetReceiverHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_post_title);
            tvTime = v.findViewById(R.id.tv_post_time);
            tvBody = v.findViewById(R.id.tv_message_body);
        }
        void bind(Message m) {
            tvTitle.setText(m.getReplyPostTitle());
            tvBody.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));
        }
    }
}
