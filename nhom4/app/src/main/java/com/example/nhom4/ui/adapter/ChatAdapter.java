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

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    private static final int TYPE_WIDGET_SENT = 3;
    private static final int TYPE_WIDGET_RECEIVED = 4;

    private List<Message> messages = new ArrayList<>();
    private final String currentUserId;
    private String partnerAvatarUrl;

    public ChatAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setPartnerAvatarUrl(String url) {
        this.partnerAvatarUrl = url;
        notifyDataSetChanged(); // Refresh lại để cập nhật avatar
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        // Bảo vệ null cho senderId
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
        return messages == null ? 0 : messages.size();
    }

    private static String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate());
    }

    // --- HELPER LOAD IMAGE AN TOÀN ---
    private static void loadImageSafe(ImageView imageView, String url) {
        if (url != null && !url.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            try {
                Glide.with(imageView.getContext())
                        .load(url)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground) // Fallback nếu URL lỗi
                        .centerCrop()
                        .into(imageView);
            } catch (Exception e) {
                // Bắt lỗi nếu Context bị hủy hoặc Glide lỗi
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    private static void loadAvatarSafe(ShapeableImageView avatarView, String url) {
        if (avatarView == null) return;

        if (url != null && !url.isEmpty()) {
            try {
                Glide.with(avatarView.getContext())
                        .load(url)
                        .placeholder(R.drawable.ic_launcher_background) // Dùng background làm placeholder avatar
                        .error(R.drawable.ic_launcher_background)
                        .circleCrop()
                        .into(avatarView);
            } catch (Exception e) {
                avatarView.setImageResource(R.drawable.ic_launcher_background);
            }
        } else {
            avatarView.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    // --- VIEW HOLDERS ---

    static class SentHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        SentHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tv_message_content);
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
        void bind(Message m, String url) {
            tvMsg.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));
            loadAvatarSafe(avatar, url);
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
            loadImageSafe(imgPostPreview, m.getReplyPostImage());
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
            avatar = v.findViewById(R.id.iv_avatar); // <--- ID này phải khớp item_message_widget_receiver.xml
        }

        void bind(Message m, String url) {
            tvTitle.setText(m.getReplyPostTitle() != null ? m.getReplyPostTitle() : "Bài viết");
            tvBody.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));

            // Load ảnh bài viết (An toàn)
            loadImageSafe(imgPostPreview, m.getReplyPostImage());

            // Load avatar người gửi (An toàn)
            loadAvatarSafe(avatar, url);
        }
    }
}