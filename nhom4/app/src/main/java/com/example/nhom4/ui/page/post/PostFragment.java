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
import com.google.android.material.button.MaterialButton; // Import MaterialButton
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;

public class PostFragment extends Fragment {

    // Cập nhật các key
    private static final String ARG_CAPTION_START = "caption_start";
    private static final String ARG_CAPTION_END = "caption_end";
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_TIMESTAMP = "timestamp";
    private static final String ARG_AVATAR_GROUP = "avatar_group";

    private String captionStart, captionEnd, imageUrl, timestamp, avatarGroup;

    // Cập nhật hàm newInstance
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
        // Dùng layout item_post mới
        return inflater.inflate(R.layout.item_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các View mới
        ImageView postImageView = view.findViewById(R.id.postImageView);
        TextView textCaption = view.findViewById(R.id.textCaption);
        MaterialButton chipTimestamp = view.findViewById(R.id.chipTimestamp);
        TextView textAvatarGroup = view.findViewById(R.id.textAvatarGroup);

        // Xử lý caption 2 màu (ví dụ: "Angry" màu đỏ)
        Spannable spannable = new SpannableString(captionStart + " - " + captionEnd);
        spannable.setSpan(
                new ForegroundColorSpan(Color.RED), // Màu đỏ cho phần đầu
                0, // Bắt đầu
                captionStart.length(), // Kết thúc
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        textCaption.setText(spannable);

        // Gán dữ liệu
        chipTimestamp.setText(timestamp);
        textAvatarGroup.setText(avatarGroup);

        // Dùng Glide để tải ảnh
        if (getContext() != null && imageUrl != null) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .placeholder(R.color.md_theme_surface)
                    .into(postImageView);
        }
    }
}