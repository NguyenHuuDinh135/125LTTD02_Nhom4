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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nhom4.R;
import com.example.nhom4.ui.page.main.CenterFragment;

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
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView postImageView = view.findViewById(R.id.postImageView);
        TextView textCaption = view.findViewById(R.id.textCaption);
        TextView tvTimestamp = view.findViewById(R.id.tvTimestamp);
        TextView textAvatarGroup = view.findViewById(R.id.textAvatarGroup);

        // --- 1. Xử lý Caption ---
        String start = (captionStart != null) ? captionStart : "";
        String end = (captionEnd != null) ? captionEnd : "";
        String fullText = start + " - " + end;

        Spannable spannable = new SpannableString(fullText);
        if (!start.isEmpty()) {
            // Tô đỏ phần Mood/Title
            spannable.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    0,
                    start.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        textCaption.setText(spannable);

        // --- 2. Gán dữ liệu khác ---
        if (tvTimestamp != null) tvTimestamp.setText(timestamp);
        if (textAvatarGroup != null) textAvatarGroup.setText(avatarGroup);

        // --- 3. LOAD ẢNH BẰNG GLIDE (QUAN TRỌNG) ---
        if (postImageView != null) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Có URL -> Load ảnh
                Glide.with(this)
                        .load(imageUrl)
                        // Dùng DiskCacheStrategy.ALL để cache tốt hơn
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_launcher_foreground) // Ảnh hiển thị khi đang tải
                        .error(android.R.color.darker_gray) // Ảnh hiển thị khi lỗi
                        .into(postImageView);
            } else {
                // Không có URL -> Hiển thị ảnh mặc định hoặc ẩn đi
                postImageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        // --- 4. XỬ LÝ NÚT SHUTTER (QUAY VỀ CAMERA) ---
        View btnShutter = view.findViewById(R.id.btn_shutter);
        if (btnShutter != null) {
            btnShutter.setOnClickListener(v -> {
                Fragment parentFragment = getParentFragment();
                if (parentFragment instanceof CenterFragment) {
                    ((CenterFragment) parentFragment).navigateToCamera();
                }
            });
        }
    }
}
