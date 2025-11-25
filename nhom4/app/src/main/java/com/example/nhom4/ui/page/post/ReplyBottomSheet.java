package com.example.nhom4.ui.page.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.viewmodel.ReplyViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ReplyBottomSheet extends BottomSheetDialogFragment {

    private Post post;
    private EditText etContent;
    private ReplyViewModel viewModel;

    public static ReplyBottomSheet newInstance(Post post) {
        ReplyBottomSheet fragment = new ReplyBottomSheet();
        fragment.post = post;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_Design_BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_reply_post_bottom_sheet, container, false);
        // Tự động hiện bàn phím
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReplyViewModel.class);

        ImageView imgPreview = view.findViewById(R.id.img_post_preview);
        TextView tvCaption = view.findViewById(R.id.tv_post_caption);
        etContent = view.findViewById(R.id.et_reply_content);
        View btnSend = view.findViewById(R.id.btn_send_reply);

        // Bind Data
        if (post != null) {
            tvCaption.setText(post.getCaption());
            String img = (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty())
                    ? post.getPhotoUrl() : post.getMoodIconUrl();
            Glide.with(this).load(img).into(imgPreview);
        }

        etContent.requestFocus();

        // Event
        btnSend.setOnClickListener(v -> {
            String content = etContent.getText().toString().trim();
            viewModel.sendReply(content, post);
        });

        // Observe
        viewModel.getSendStatus().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    // Disable button, show loading...
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Đã gửi phản hồi!", Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
