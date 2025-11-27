package com.example.nhom4.ui.page.post;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.main.CenterFragment;
import com.example.nhom4.ui.viewmodel.ReplyViewModel;
import com.google.android.material.button.MaterialButton;

/**
 * Fragment hiển thị một bài post trong feed (ảnh + caption) và cho phép gửi phản hồi nhanh.
 */
public class PostFragment extends Fragment {

    // Các key argument
    private static final String ARG_CAPTION_START = "caption_start";
    private static final String ARG_CAPTION_END = "caption_end";
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_POST_ID = "arg_post_id";
    private static final String ARG_USER_ID = "arg_user_id";
    private static final String ARG_POST_TYPE = "arg_post_type";
    private static final String ARG_USERNAME = "arg_username";

    private String captionStart, captionEnd, imageUrl;
    private String postId, userIdOfOwner, postType, userNameOfOwner;

    // UI Overlay Components
    private View overlayContainer;
    private EditText edtReplyReal;
    private MaterialButton btnSendDirect, btnCancelReply;

    // ViewModel
    private ReplyViewModel replyViewModel;
    private Post currentPostObject; // Lưu lại object post hiện tại để gửi

    /**
     * Factory method truyền toàn bộ dữ liệu bài viết qua Bundle.
     */
    public static PostFragment newInstance(Post post) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();

        String startText = "mood".equals(post.getType()) ? post.getMoodName() : post.getActivityTitle();
        args.putString(ARG_CAPTION_START, startText);
        args.putString(ARG_CAPTION_END, post.getCaption());
        args.putString(ARG_IMAGE_URL, post.getPhotoUrl());
        args.putString(ARG_POST_ID, post.getPostId());
        args.putString(ARG_USER_ID, post.getUserId());
        args.putString(ARG_POST_TYPE, post.getType());
        args.putString(ARG_USERNAME, post.getUserName());

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
            postId = getArguments().getString(ARG_POST_ID);
            userIdOfOwner = getArguments().getString(ARG_USER_ID);
            postType = getArguments().getString(ARG_POST_TYPE);
            userNameOfOwner = getArguments().getString(ARG_USERNAME);
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

        // Init ViewModel
        replyViewModel = new ViewModelProvider(this).get(ReplyViewModel.class);

        // Reconstruct Post Object for sending logic
        currentPostObject = new Post();
        currentPostObject.setPostId(postId);
        currentPostObject.setUserId(userIdOfOwner);
        currentPostObject.setCaption(captionEnd);
        currentPostObject.setPhotoUrl(imageUrl);
        currentPostObject.setType(postType);
        currentPostObject.setUserName(userNameOfOwner);
        if ("mood".equals(postType)) {
            currentPostObject.setMoodName(captionStart);
            if (imageUrl != null && imageUrl.startsWith("http")) currentPostObject.setMoodIconUrl(imageUrl);
        } else {
            currentPostObject.setActivityTitle(captionStart);
        }


        // Ánh xạ Views cơ bản
        ImageView postImageView = view.findViewById(R.id.postImageView);
        TextView textCaption = view.findViewById(R.id.textCaption);
        TextView textAvatarGroup = view.findViewById(R.id.textAvatarGroup);
        EditText edtTrigger = view.findViewById(R.id.edt_comment_trigger); // Nút giả ở dưới
        View btnShutter = view.findViewById(R.id.btn_shutter);

        // Ánh xạ Views Overlay
        overlayContainer = view.findViewById(R.id.overlay_reply_container);
        edtReplyReal = view.findViewById(R.id.edt_reply_real);
        btnSendDirect = view.findViewById(R.id.btn_send_reply_direct);
        btnCancelReply = view.findViewById(R.id.btn_cancel_reply);

        // 1. Styling
        String start = (captionStart != null) ? captionStart : "";
        String end = (captionEnd != null) ? captionEnd : "";
        Spannable spannable = new SpannableString(start + " - " + end);
        if (!start.isEmpty()) {
            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, start.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textCaption.setText(spannable);

        if (textAvatarGroup != null) {
            textAvatarGroup.setText((userNameOfOwner != null && !userNameOfOwner.isEmpty()) ? userNameOfOwner : "Người dùng");
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_foreground).into(postImageView);
        } else {
            postImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // 2. Xử lý sự kiện CLICK VÀO Ô COMMENT GIẢ -> Hiện Overlay
        edtTrigger.setOnClickListener(v -> showReplyOverlay());

        // 3. Xử lý sự kiện TRONG OVERLAY
        btnCancelReply.setOnClickListener(v -> hideReplyOverlay());

        // Đóng overlay khi bấm ra ngoài vùng trắng
        overlayContainer.setOnClickListener(v -> hideReplyOverlay());

        btnSendDirect.setOnClickListener(v -> {
            String content = edtReplyReal.getText().toString().trim();
            if (!content.isEmpty()) {
                // Gửi qua ViewModel
                replyViewModel.sendReply(content, currentPostObject);
                // Ẩn bàn phím ngay lập tức
                hideKeyboard();
            } else {
                Toast.makeText(getContext(), "Nhập nội dung trước!", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Lắng nghe kết quả gửi từ ViewModel
        replyViewModel.getSendStatus().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    btnSendDirect.setText("Đang gửi...");
                    btnSendDirect.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Đã gửi phản hồi!", Toast.LENGTH_SHORT).show();
                    btnSendDirect.setText("Gửi");
                    btnSendDirect.setEnabled(true);
                    edtReplyReal.setText(""); // Clear text
                    hideReplyOverlay(); // Đóng overlay
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    btnSendDirect.setText("Gửi");
                    btnSendDirect.setEnabled(true);
                    break;
            }
        });

        // 5. Back to Camera
        btnShutter.setOnClickListener(v -> {
            Fragment parentFragment = getParentFragment();
            if (parentFragment instanceof CenterFragment) {
                ((CenterFragment) parentFragment).navigateToCamera();
            }
        });
    }

    // Hàm hiện Overlay
    /**
     * Mở overlay nhập phản hồi và hiện bàn phím.
     */
    private void showReplyOverlay() {
        overlayContainer.setVisibility(View.VISIBLE);
        edtReplyReal.requestFocus();
        showKeyboard(edtReplyReal);
    }

    // Hàm ẩn Overlay
    private void hideReplyOverlay() {
        hideKeyboard();
        overlayContainer.setVisibility(View.GONE);
    }

    // Helper Show Keyboard
    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // Helper Hide Keyboard
    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
