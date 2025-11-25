package com.example.nhom4.ui.page.post;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.main.CenterFragment;

public class PostFragment extends Fragment {

    private static final String ARG_CAPTION_START = "caption_start";
    private static final String ARG_CAPTION_END = "caption_end";
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_TIMESTAMP = "timestamp";
    private static final String ARG_AVATAR_GROUP = "avatar_group";
    private static final String ARG_POST_ID = "arg_post_id";
    private static final String ARG_USER_ID = "arg_user_id";
    private static final String ARG_POST_TYPE = "arg_post_type";

    private String captionStart, captionEnd, imageUrl, timestamp, avatarGroup;
    private String postId, userIdOfOwner, postType;

    public static PostFragment newInstance(String captionStart, String captionEnd, String imageUrl,
                                           String timestamp, String avatarGroup,
                                           String postId, String userIdOfOwner, String type) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAPTION_START, captionStart);
        args.putString(ARG_CAPTION_END, captionEnd);
        args.putString(ARG_IMAGE_URL, imageUrl);
        args.putString(ARG_TIMESTAMP, timestamp);
        args.putString(ARG_AVATAR_GROUP, avatarGroup);
        args.putString(ARG_POST_ID, postId);
        args.putString(ARG_USER_ID, userIdOfOwner);
        args.putString(ARG_POST_TYPE, type);
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
            postId = getArguments().getString(ARG_POST_ID);
            userIdOfOwner = getArguments().getString(ARG_USER_ID);
            postType = getArguments().getString(ARG_POST_TYPE);
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
        EditText edtComment = view.findViewById(R.id.edt_comment);
        View btnShutter = view.findViewById(R.id.btn_shutter);

        // 1. Caption Styling
        String start = (captionStart != null) ? captionStart : "";
        String end = (captionEnd != null) ? captionEnd : "";
        String fullText = start + " - " + end;

        Spannable spannable = new SpannableString(fullText);
        if (!start.isEmpty()) {
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, start.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textCaption.setText(spannable);

        // 2. Bind Text
        tvTimestamp.setText(timestamp);
        textAvatarGroup.setText(avatarGroup);

        // 3. Load Image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(postImageView);
        } else {
            postImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // 4. Open Reply BottomSheet
        edtComment.setOnClickListener(v -> {
            Post currentPost = new Post();
            currentPost.setPostId(postId);
            currentPost.setUserId(userIdOfOwner);
            currentPost.setCaption(captionEnd);
            currentPost.setPhotoUrl(imageUrl);
            currentPost.setType(postType);

            if ("mood".equals(postType)) {
                currentPost.setMoodName(captionStart);
                if (imageUrl != null && imageUrl.startsWith("http")) {
                    currentPost.setMoodIconUrl(imageUrl);
                }
            } else {
                currentPost.setActivityTitle(captionStart);
            }

            ReplyBottomSheet sheet = ReplyBottomSheet.newInstance(currentPost);
            sheet.show(getParentFragmentManager(), "ReplyBottomSheet");
        });

        // 5. Back to Camera
        btnShutter.setOnClickListener(v -> {
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof CenterFragment) {
                ((CenterFragment) parentFragment).navigateToCamera();
            }
        });
    }
}
