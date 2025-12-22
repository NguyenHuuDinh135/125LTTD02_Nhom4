package com.example.nhom4.ui.page.post;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group; // [IMPORT QUAN TRỌNG]
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.main.CenterFragment;
import com.example.nhom4.ui.viewmodel.ReplyViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostFragment extends Fragment {

    // Argument Keys
    private static final String ARG_CAPTION_START = "caption_start";
    private static final String ARG_CAPTION_END = "caption_end";
    private static final String ARG_IMAGE_URL = "image_url";
    private static final String ARG_POST_ID = "arg_post_id";
    private static final String ARG_USER_ID = "arg_user_id";
    private static final String ARG_POST_TYPE = "arg_post_type";
    private static final String ARG_USERNAME = "arg_username";
    private static final String ARG_USER_AVATAR = "arg_user_avatar";
    private static final String ARG_TIMESTAMP = "arg_timestamp";

    private String captionStart, captionEnd, imageUrl;
    private String postId, userIdOfOwner, postType, userNameOfOwner, userAvatarOfOwner;
    private long timestampMillis = 0;

    // UI Controls
    private View overlayContainer;
    private View cardReplyBox;
    private EditText edtReplyReal, edtTrigger;
    private MaterialButton btnSendDirect, btnCancelReply;

    // UI Post Content
    private TextView textCaption, textPostContent;
    private Group groupContentViews; // [MỚI] Group quản lý ẩn hiện nội dung
    private View layoutEmptyPost;    // [MỚI] Layout hiển thị khi không có bài viết

    // Activity Invite UI
    private View layoutActivityInvite;
    private MaterialButton btnJoinActivity;
    private MaterialButton btnHeartOverlay;
    private ImageView imgInviterAvatar;
    private TextView tvInviteText;

    private TextView tvTimestamp;

    private ReplyViewModel replyViewModel;
    private Post currentPostObject;

    public static PostFragment newInstance(Post post) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();

        if (post != null) {
            String startText;
            String displayImageUrl;
            if ("mood".equals(post.getType())) {
                startText = post.getMoodName();
                displayImageUrl = post.getMoodIconUrl();
            } else {
                startText = post.getActivityTitle();
                displayImageUrl = post.getPhotoUrl();
            }

            args.putString(ARG_CAPTION_START, startText);
            args.putString(ARG_CAPTION_END, post.getCaption());
            args.putString(ARG_IMAGE_URL, displayImageUrl);
            args.putString(ARG_POST_ID, post.getPostId());
            args.putString(ARG_USER_ID, post.getUserId());
            args.putString(ARG_POST_TYPE, post.getType());
            args.putString(ARG_USERNAME, post.getUserName());
            args.putString(ARG_USER_AVATAR, post.getUserAvatar());

            if (post.getCreatedAt() != null) {
                args.putLong(ARG_TIMESTAMP, post.getCreatedAt().toDate().getTime());
            } else {
                args.putLong(ARG_TIMESTAMP, System.currentTimeMillis());
            }
        }

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
            userAvatarOfOwner = getArguments().getString(ARG_USER_AVATAR);
            timestampMillis = getArguments().getLong(ARG_TIMESTAMP);
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

        replyViewModel = new ViewModelProvider(this).get(ReplyViewModel.class);
        reconstructPostObject();

        initViews(view);

        // [QUAN TRỌNG] Kiểm tra xem có bài viết không trước khi setup UI
        if (checkIfEmptyState()) {
            return; // Nếu rỗng, dừng lại, không load ảnh/text lỗi
        }

        setupMainUI();
        setupPostTypeLogic();
        setupEvents();
        observeViewModel();
    }

    private void reconstructPostObject() {
        if (postId == null) {
            currentPostObject = null;
            return;
        }
        currentPostObject = new Post();
        currentPostObject.setPostId(postId);
        currentPostObject.setUserId(userIdOfOwner);
        currentPostObject.setCaption(captionEnd);
        currentPostObject.setType(postType);
        currentPostObject.setUserName(userNameOfOwner);
        currentPostObject.setUserAvatar(userAvatarOfOwner);
        currentPostObject.setCreatedAt(new Timestamp(new Date(timestampMillis)));

        if ("mood".equals(postType)) {
            currentPostObject.setMoodName(captionStart);
            currentPostObject.setMoodIconUrl(imageUrl);
        } else {
            currentPostObject.setActivityTitle(captionStart);
            currentPostObject.setPhotoUrl(imageUrl);
        }
    }

    private void initViews(View view) {
        // Main Content Views
        textCaption = view.findViewById(R.id.textCaption);
        textPostContent = view.findViewById(R.id.textPostContent);
        groupContentViews = view.findViewById(R.id.group_content_views); // Group XML
        layoutEmptyPost = view.findViewById(R.id.layout_empty_post);     // Empty Layout

        tvTimestamp = view.findViewById(R.id.tvTimestamp);
        edtTrigger = view.findViewById(R.id.edt_comment_trigger);

        layoutActivityInvite = view.findViewById(R.id.layout_activity_invite);
        btnJoinActivity = view.findViewById(R.id.btn_join_activity);
        btnHeartOverlay = view.findViewById(R.id.btn_heart_overlay);
        imgInviterAvatar = view.findViewById(R.id.img_inviter_avatar);
        tvInviteText = view.findViewById(R.id.tv_invite_text);

        overlayContainer = view.findViewById(R.id.overlay_reply_container);
        cardReplyBox = view.findViewById(R.id.card_reply_box);
        edtReplyReal = view.findViewById(R.id.edt_reply_real);
        btnSendDirect = view.findViewById(R.id.btn_send_reply_direct);
        btnCancelReply = view.findViewById(R.id.btn_cancel_reply);

        View btnShutter = view.findViewById(R.id.btn_shutter);
        btnShutter.setOnClickListener(v -> {
            Fragment p = getParentFragment();
            if (p instanceof CenterFragment) ((CenterFragment) p).navigateToCamera();
        });
    }

    // [MỚI] Hàm kiểm tra và xử lý hiển thị Empty State
    private boolean checkIfEmptyState() {
        if (currentPostObject == null || postId == null || postId.isEmpty()) {
            // Ẩn nội dung post
            groupContentViews.setVisibility(View.GONE);
            // Hiện Empty Layout
            layoutEmptyPost.setVisibility(View.VISIBLE);
            return true;
        } else {
            // Hiện nội dung post
            groupContentViews.setVisibility(View.VISIBLE);
            // Ẩn Empty Layout
            layoutEmptyPost.setVisibility(View.GONE);
            return false;
        }
    }

    private void setupMainUI() {
        // --- 1. CAPTION (TITLE/MOOD) ---
        // Không ghép chuỗi nữa, chỉ hiển thị captionStart
        if (captionStart != null && !captionStart.isEmpty()) {
            textCaption.setText(captionStart);
            textCaption.setVisibility(View.VISIBLE);

            // Set màu Primary
            TypedValue typedValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
            textCaption.setTextColor(typedValue.data);
        } else {
            // Trường hợp hy hữu không có Title
            textCaption.setVisibility(View.GONE);
        }

        // --- 2. POST CONTENT (NỘI DUNG TEXT) ---
        // Hiển thị ở TextView riêng bên dưới
        if (captionEnd != null && !captionEnd.trim().isEmpty()) {
            textPostContent.setText(captionEnd);
            textPostContent.setVisibility(View.VISIBLE);
        } else {
            // Nếu không có nội dung text, ẩn đi cho gọn UI
            textPostContent.setVisibility(View.GONE);
        }

        // --- 3. TIMESTAMP ---
        if (tvTimestamp != null) {
            if (timestampMillis > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
                String dateString = sdf.format(new Date(timestampMillis));
                tvTimestamp.setText(dateString);
            } else {
                tvTimestamp.setText("Vừa xong");
            }
        }

        // --- 4. AVATAR GROUP (USER NAME) ---
        TextView textAvatarGroup = getView().findViewById(R.id.textAvatarGroup);
        if (textAvatarGroup != null) {
            textAvatarGroup.setText((userNameOfOwner != null && !userNameOfOwner.isEmpty()) ? userNameOfOwner : "Người dùng");
        }

        // --- 5. IMAGE LOADING ---
        ImageView postImageView = getView().findViewById(R.id.postImageView);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            postImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(postImageView);
        } else {
            postImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void setupPostTypeLogic() {
        if ("activity".equals(postType)) {
            layoutActivityInvite.setVisibility(View.VISIBLE);
            tvInviteText.setText(userNameOfOwner + " rủ bạn tham gia!");

            if (userAvatarOfOwner != null && !userAvatarOfOwner.isEmpty()) {
                Glide.with(this).load(userAvatarOfOwner).into(imgInviterAvatar);
            }
            updateJoinButtonState(false);
            btnJoinActivity.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Đã tham gia Activity!", Toast.LENGTH_SHORT).show();
                updateJoinButtonState(true);
            });
        } else {
            layoutActivityInvite.setVisibility(View.GONE);
        }
        btnHeartOverlay.setVisibility(View.VISIBLE);
    }

    private void updateJoinButtonState(boolean isJoined) {
        if (isJoined) {
            btnJoinActivity.setText("Đã tham gia");
            btnJoinActivity.setEnabled(false);
            btnJoinActivity.setAlpha(0.6f);
        } else {
            btnJoinActivity.setText("Tham gia");
            btnJoinActivity.setEnabled(true);
            btnJoinActivity.setAlpha(1f);
        }
    }

    private void setupEvents() {
        // ... (Giữ nguyên logic TIM và REPLY như cũ)
        btnHeartOverlay.setOnClickListener(v -> {
            boolean isLiked = !btnHeartOverlay.isSelected();
            btnHeartOverlay.setSelected(isLiked);

            if (isLiked) {
                btnHeartOverlay.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.baseline_favorite_24));
                btnHeartOverlay.setIconTint(ColorStateList.valueOf(Color.parseColor("#E91E63")));
                btnHeartOverlay.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() ->
                        btnHeartOverlay.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                ).start();
            } else {
                btnHeartOverlay.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.outline_favorite_border_24));
                TypedValue typedValue = new TypedValue();
                requireContext().getTheme().resolveAttribute(android.R.attr.colorError, typedValue, true);
                btnHeartOverlay.setIconTint(ColorStateList.valueOf(typedValue.data));
            }
        });

        edtTrigger.setOnClickListener(v -> animateReplyOverlay(true));
        btnCancelReply.setOnClickListener(v -> animateReplyOverlay(false));
        overlayContainer.setOnClickListener(v -> animateReplyOverlay(false));

        btnSendDirect.setOnClickListener(v -> {
            String content = edtReplyReal.getText().toString().trim();
            if (!content.isEmpty()) {
                replyViewModel.sendReply(content, currentPostObject);
                hideKeyboard();
            } else {
                Toast.makeText(getContext(), "Nhập nội dung trước!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        replyViewModel.getSendStatus().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    btnSendDirect.setText("...");
                    btnSendDirect.setEnabled(false);
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Đã gửi!", Toast.LENGTH_SHORT).show();
                    btnSendDirect.setText("Gửi");
                    btnSendDirect.setEnabled(true);
                    edtReplyReal.setText("");
                    animateReplyOverlay(false);
                    break;
                case ERROR:
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    btnSendDirect.setText("Gửi");
                    btnSendDirect.setEnabled(true);
                    break;
            }
        });
    }

    private void animateReplyOverlay(boolean show) {
        if (show) {
            overlayContainer.setVisibility(View.VISIBLE);
            overlayContainer.setAlpha(0f);
            overlayContainer.animate().alpha(1f).setDuration(250).setListener(null).start();

            cardReplyBox.setScaleX(0.8f);
            cardReplyBox.setScaleY(0.8f);
            cardReplyBox.setAlpha(0f);
            cardReplyBox.animate().scaleX(1f).scaleY(1f).alpha(1f)
                    .setDuration(300).setInterpolator(new OvershootInterpolator(1.2f)).start();

            edtReplyReal.requestFocus();
            showKeyboard(edtReplyReal);
        } else {
            hideKeyboard();
            overlayContainer.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    overlayContainer.setVisibility(View.GONE);
                }
            }).start();
            cardReplyBox.animate().scaleX(0.9f).scaleY(0.9f).alpha(0f).setDuration(200).start();
        }
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}