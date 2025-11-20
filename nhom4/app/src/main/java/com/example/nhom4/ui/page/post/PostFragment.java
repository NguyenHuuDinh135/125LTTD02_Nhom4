package com.example.nhom4.ui.page.post;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;

public class PostFragment extends Fragment {

    private static final String ARG_CAPTION_START = "caption_start";
    private static final String ARG_CAPTION_END = "caption_end";
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_TIMESTAMP = "timestamp";
    private static final String ARG_AVATAR_GROUP = "avatar_group";

    private String captionStart, captionEnd, imageUrl, timestamp, avatarGroup;

    public static PostFragment newInstance(String captionStart, String captionEnd, String imageUrl, String timestamp, String avatarGroup) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAPTION_START, captionStart);
        args.putString(ARG_CAPTION_END, captionEnd);
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_TIMESTAMP, timestamp);
        args.putString(ARG_AVATAR_GROUP, avatarGroup);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            captionStart = getArguments().getString(ARG_CAPTION_START);
            captionEnd = getArguments().getString(ARG_CAPTION_END);
            imageUrl = getArguments().getString(ARG_IMAGE_URL);
            timestamp = getArguments().getString(ARG_TIMESTAMP);
            avatarGroup = getArguments().getString(ARG_AVATAR_GROUP);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.item_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- FIX LỖI Ở ĐÂY ---
        ImageView postImageView = view.findViewById(R.id.postImageView);
        TextView textCaption = view.findViewById(R.id.textCaption);

        // Đã đổi từ MaterialButton sang TextView để khớp với layout mới
        TextView chipTimestamp = view.findViewById(R.id.tvTimestamp);

        TextView textAvatarGroup = view.findViewById(R.id.textAvatarGroup);

        // Xử lý Caption (nếu null thì gán chuỗi rỗng để tránh crash)
        String start = (captionStart != null) ? captionStart : "";
        String end = (captionEnd != null) ? captionEnd : "";

        Spannable spannable = new SpannableString(start + " - " + end);
        if (!start.isEmpty()) {
            spannable.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    0,
                    start.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        textCaption.setText(spannable);

        // Gán dữ liệu
        if (chipTimestamp != null) {
            chipTimestamp.setText(timestamp);
        }
        if (textAvatarGroup != null) {
            textAvatarGroup.setText(avatarGroup);
        }

        // Load ảnh
        if (getContext() != null && imageUrl != null && postImageView != null) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .placeholder(android.R.color.darker_gray) // Sửa ID màu mặc định cho an toàn
                    .into(postImageView);
        }
    }
}
