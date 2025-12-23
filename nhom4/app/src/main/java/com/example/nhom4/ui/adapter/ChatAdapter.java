package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

/**
 * Adapter chính cho màn hình chat, hỗ trợ 4 kiểu view:
 * - Tin nhắn text gửi/nhận
 * - Widget trả lời bài post gửi/nhận
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private static final int TYPE_WIDGET_SENT = 3;
    private static final int TYPE_WIDGET_RECEIVED = 4;

    private List<Message> messages = new ArrayList<>();
    private final String currentUserId;
    private String partnerAvatarUrl;  // Avatar của partner

    public ChatAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setPartnerAvatarUrl(String url) {
        this.partnerAvatarUrl = url;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId() == null) return TYPE_RECEIVED;

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

        if (holder instanceof SentHolder) {
            ((SentHolder) holder).bind(msg);
        } else if (holder instanceof ReceivedHolder) {
            ((ReceivedHolder) holder).bind(msg, partnerAvatarUrl);
        } else if (holder instanceof WidgetSenderHolder) {
            ((WidgetSenderHolder) holder).bind(msg);
        } else if (holder instanceof WidgetReceiverHolder) {
            ((WidgetReceiverHolder) holder).bind(msg, partnerAvatarUrl);
        }
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
            tvMsg = v.findViewById(R.id.tv_message_content); // Adjust ID if different in sender layout
            tvTime = v.findViewById(R.id.tv_timestamp);
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

        void bind(Message m, String partnerAvatarUrl) {
            tvMsg.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));

            if (avatar != null) {
                loadAvatar(avatar, partnerAvatarUrl);
            }
        }
    }

    static class WidgetSenderHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvBody;
        ImageView imgPostPreview;

        WidgetSenderHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_post_title);
            tvTime = v.findViewById(R.id.tv_post_time);
            tvBody = v.findViewById(R.id.tv_message_body);
            imgPostPreview = v.findViewById(R.id.img_post_preview);
        }

        void bind(Message m) {
            tvTitle.setText(m.getReplyPostTitle() != null ? m.getReplyPostTitle() : "Bài viết");
            tvBody.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));

            if (m.getReplyPostImage() != null && !m.getReplyPostImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(m.getReplyPostImage())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(imgPostPreview);
                imgPostPreview.setVisibility(View.VISIBLE);
            } else {
                imgPostPreview.setVisibility(View.GONE);
            }
        }
    }

    static class WidgetReceiverHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvBody;
        ImageView imgPostPreview;
        ShapeableImageView avatar;

        WidgetReceiverHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_post_title);
            tvTime = v.findViewById(R.id.tv_post_time);
            tvBody = v.findViewById(R.id.tv_message_body);
            imgPostPreview = v.findViewById(R.id.img_post_preview);
            avatar = v.findViewById(R.id.iv_avatar);  // ID in widget receiver layout
        }

        void bind(Message m, String partnerAvatarUrl) {
            tvTitle.setText(m.getReplyPostTitle() != null ? m.getReplyPostTitle() : "Bài viết");
            tvBody.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));

            if (m.getReplyPostImage() != null && !m.getReplyPostImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(m.getReplyPostImage())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(imgPostPreview);
                imgPostPreview.setVisibility(View.VISIBLE);
            } else {
                imgPostPreview.setVisibility(View.GONE);
            }

            if (avatar != null) {
                loadAvatar(avatar, partnerAvatarUrl);
            }
        }
    }

    private static void loadAvatar(ShapeableImageView avatarView, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(avatarView.getContext())
                    .load(url)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .circleCrop()
                    .into(avatarView);
        } else {
            avatarView.setImageResource(R.drawable.avatar_placeholder);
        }
    }
}