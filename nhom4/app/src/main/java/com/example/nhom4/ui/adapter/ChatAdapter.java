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

    /**
     * @param currentUserId id người dùng hiện tại để xác định tin nhắn gửi hay nhận
     */
    public ChatAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    /**
     * Cập nhật toàn bộ danh sách tin nhắn.
     */
    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    /**
     * Xác định kiểu view cho từng tin nhắn.
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        // Kiểm tra null để tránh crash
        if (message.getSenderId() == null) return TYPE_RECEIVED;

        boolean isMe = message.getSenderId().equals(currentUserId);

        if ("post_reply".equals(message.getType())) {
            return isMe ? TYPE_WIDGET_SENT : TYPE_WIDGET_RECEIVED; // Widget trả lời bài post
        }
        return isMe ? TYPE_SENT : TYPE_RECEIVED; // Tin nhắn text
    }

    /**
     * Tạo ViewHolder tương ứng với từng kiểu view.
     * @param parent
     * @param viewType
     * @return
     */
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

    /**
     * Bind dữ liệu mô hình vào ViewHolder. 
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);

        if (holder instanceof SentHolder) {
            ((SentHolder) holder).bind(msg); // Bong bóng tự gửi
        } else if (holder instanceof ReceivedHolder) {
            ((ReceivedHolder) holder).bind(msg); // Bong bóng nhận
        }
        // [QUAN TRỌNG] Bổ sung logic bind cho Widget
        else if (holder instanceof WidgetSenderHolder) {
            ((WidgetSenderHolder) holder).bind(msg); // Widget do mình gửi
        } else if (holder instanceof WidgetReceiverHolder) {
            ((WidgetReceiverHolder) holder).bind(msg); // Widget nhận
        }
    }

    /**
     * Trả về số lượng tin nhắn.
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Định dạng timestamp Firebase thành chuỗi HH:mm.
     */
    private static String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate());
    }

    // --- VIEW HOLDERS ---

    /**
     * ViewHolder cho bong bóng tin nhắn do người dùng hiện tại gửi.
     */
    static class SentHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        SentHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMessageSent); // Đảm bảo ID đúng trong item_message_sender.xml
            tvTime = v.findViewById(R.id.tvTimeSent);
        }
        void bind(Message m) {
            tvMsg.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));
        }
    }

    /**
     * ViewHolder cho tin nhắn nhận, kèm avatar người gửi.
     */
    static class ReceivedHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        ShapeableImageView avatar;
        ReceivedHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tv_message_content); // Đảm bảo ID đúng trong item_message_receiver.xml
            tvTime = v.findViewById(R.id.tv_timestamp);
            avatar = v.findViewById(R.id.img_avatar);
        }
        void bind(Message m) {
            tvMsg.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));
            // TODO: Load avatar người gửi ở đây nếu cần
        }
    }

    /**
     * ViewHolder cho widget trả lời bài viết do chính mình gửi đi.
     */
    static class WidgetSenderHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvBody;
        // Nếu bạn muốn hiện ảnh bài post nhỏ trong widget, khai báo thêm ImageView ở đây
        // ImageView imgPostThumb;

        WidgetSenderHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_post_title);
            tvTime = v.findViewById(R.id.tv_post_time);
            tvBody = v.findViewById(R.id.tv_message_body);
            // imgPostThumb = v.findViewById(R.id.img_post_thumb); // Ví dụ
        }
        void bind(Message m) {
            tvTitle.setText(m.getReplyPostTitle() != null ? m.getReplyPostTitle() : "Bài viết");
            tvBody.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));

            // Nếu layout có ImageView để hiện ảnh post:
            // if (m.getReplyPostImage() != null) {
            //    Glide.with(itemView.getContext()).load(m.getReplyPostImage()).into(imgPostThumb);
            // }
        }
    }

    /**
     * ViewHolder cho widget trả lời bài viết nhận từ người khác.
     */
    static class WidgetReceiverHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvBody;

        WidgetReceiverHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_post_title);
            tvTime = v.findViewById(R.id.tv_post_time);
            tvBody = v.findViewById(R.id.tv_message_body);
        }
        void bind(Message m) {
            tvTitle.setText(m.getReplyPostTitle() != null ? m.getReplyPostTitle() : "Bài viết");
            tvBody.setText(m.getContent());
            tvTime.setText(formatTime(m.getCreatedAt()));
        }
    }
}
