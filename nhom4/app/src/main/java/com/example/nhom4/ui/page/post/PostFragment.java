package com.example.nhom4.ui.page.post;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.main.CenterFragment;
import com.example.nhom4.ui.viewmodel.ReplyViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostFragment extends Fragment {
    private final List<String> reactionEmojis = List.of(
            "❤️", "😂", "😍", "😢", "😢", "😡", "👍", "👎",
            "🎉", "🔥", "💯", "🙌", "👏", "🤔", "😮", "😴"
    );
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

    private String currentPhotoUrl; // URL ảnh hiện tại (photo hoặc mood icon)

    // UI Controls
    private View overlayContainer;
    private View cardReplyBox;
    private EditText edtReplyReal, edtTrigger;
    private MaterialButton btnSendDirect, btnCancelReply;

    // UI Post Content
    private TextView textCaption, textPostContent;
    private Group groupContentViews;
    private View layoutEmptyPost;

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

        if (checkIfEmptyState()) {
            return;
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
        textCaption = view.findViewById(R.id.textCaption);
        textPostContent = view.findViewById(R.id.textPostContent);
        groupContentViews = view.findViewById(R.id.group_content_views);
        layoutEmptyPost = view.findViewById(R.id.layout_empty_post);

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

        // Nút Download và Share
        MaterialButton btnGridView = view.findViewById(R.id.btn_grid_view);
        MaterialButton btnShare = view.findViewById(R.id.btn_share);

        btnGridView.setOnClickListener(v -> downloadPostImage());
        btnShare.setOnClickListener(v -> sharePostImage());
    }

    private boolean checkIfEmptyState() {
        if (currentPostObject == null || postId == null || postId.isEmpty()) {
            groupContentViews.setVisibility(View.GONE);
            layoutEmptyPost.setVisibility(View.VISIBLE);
            return true;
        } else {
            groupContentViews.setVisibility(View.VISIBLE);
            layoutEmptyPost.setVisibility(View.GONE);
            return false;
        }
    }

    private void setupMainUI() {
        currentPhotoUrl = imageUrl;

        if (captionStart != null && !captionStart.isEmpty()) {
            textCaption.setText(captionStart);
            textCaption.setVisibility(View.VISIBLE);
            TypedValue typedValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
            textCaption.setTextColor(typedValue.data);
        } else {
            textCaption.setVisibility(View.GONE);
        }

        if (captionEnd != null && !captionEnd.trim().isEmpty()) {
            textPostContent.setText(captionEnd);
            textPostContent.setVisibility(View.VISIBLE);
        } else {
            textPostContent.setVisibility(View.GONE);
        }

        if (tvTimestamp != null) {
            if (timestampMillis > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
                tvTimestamp.setText(sdf.format(new Date(timestampMillis)));
            } else {
                tvTimestamp.setText("Vừa xong");
            }
        }

        TextView textAvatarGroup = getView().findViewById(R.id.textAvatarGroup);
        if (textAvatarGroup != null) {
            textAvatarGroup.setText(userNameOfOwner != null && !userNameOfOwner.isEmpty() ? userNameOfOwner : "Người dùng");
        }

        ImageView postImageView = getView().findViewById(R.id.postImageView);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            postImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this)
                    .load(imageUrl)
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

    // ==================== DOWNLOAD & SHARE ====================

    private void downloadPostImage() {
        if (currentPhotoUrl == null || currentPhotoUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Không có ảnh để tải", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadImageAndroid10Plus();
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1002);
                return;
            }
            downloadImageLegacy();
        }
    }

    private void downloadImageAndroid10Plus() {
        Glide.with(this)
                .asBitmap()
                .load(currentPhotoUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        saveImageToGallery(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void downloadImageLegacy() {
        Toast.makeText(requireContext(), "Tải ảnh thành công!", Toast.LENGTH_SHORT).show();
    }

    private void saveImageToGallery(Bitmap bitmap) {
        ContentResolver resolver = requireContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "MyApp_Post_" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp");

        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = resolver.openOutputStream(uri)) {
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    Toast.makeText(requireContext(), "Đã lưu ảnh vào thư viện!", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(requireContext(), "Lỗi lưu ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sharePostImage() {
        if (currentPhotoUrl == null || currentPhotoUrl.isEmpty()) {
            shareTextOnly();
            return;
        }

        Glide.with(this)
                .asFile()
                .load(currentPhotoUrl)
                .into(new CustomTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                        Uri uri = FileProvider.getUriForFile(requireContext(),
                                requireContext().getPackageName() + ".provider", resource);

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareCaption());
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void shareTextOnly() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getShareCaption());
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"));
    }

    private String getShareCaption() {
        String title = captionStart != null && !captionStart.isEmpty() ? captionStart : "Bài viết của tôi";
        String caption = captionEnd != null && !captionEnd.isEmpty() ? " - " + captionEnd : "";
        return title + caption + "\n\nChia sẻ từ MyApp";
    }

    // ==================== CÁC HÀM CŨ ====================

    private void setupEvents() {
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