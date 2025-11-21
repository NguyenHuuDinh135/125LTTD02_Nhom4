package com.example.nhom4.ui.page.feed;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.R;
import com.example.nhom4.data.model.Message;
import com.example.nhom4.ui.page.chatbox.ChatboxViewModel;
import com.example.nhom4.ui.page.post.PostAdapter;
import com.google.android.material.button.MaterialButton;

public class FeedContainerFragment extends Fragment {

    private ViewPager2 viewPagerPosts;
    private EditText etMessage;
    private LinearLayout reactionButtonContainer;
    private  MaterialButton btnSend;

    private ChatboxViewModel viewModel;

    private String currentUserId = "njf1b9ZQfRRrYc9SeEXDUp9ZFez1"; // ID của người dùng hiện tại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerPosts = view.findViewById(R.id.viewPagerPosts);
        etMessage = view.findViewById(R.id.etMessage);
        reactionButtonContainer = view.findViewById(R.id.emojiContainer);
        btnSend = view.findViewById(R.id.btnSend);

        // --- SỬA ĐỔI TẠI ĐÂY ---
        // Vì PostAdapter hiện tại của bạn đang dùng dữ liệu giả (fake data)
        // và chỉ có constructor nhận FragmentActivity, nên ta chỉ truyền requireActivity().
        PostAdapter adapter = new PostAdapter(requireActivity());
        viewPagerPosts.setAdapter(adapter);

        // Xử lý nút bấm Calendar để chuyển tab của ViewPager Cha (MainPagerAdapter)
        View btnCalendar = view.findViewById(R.id.btnOpenCalendar);
        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v -> {
                // Gọi về Activity để chuyển sang trang Calendar
                if (getActivity() instanceof com.example.nhom4.MainActivity) {
                    ((com.example.nhom4.MainActivity) getActivity()).navigateToCalendar();
                }
            });
        }

        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
           if(hasFocus) {
               // Hiện nút send tin nhắn lên nhé broo
               reactionButtonContainer.setVisibility(View.GONE);
               btnSend.setVisibility(View.VISIBLE);
           } else {
               // Ngược lại thì chỉ cần ẩn đi thôi broo
               reactionButtonContainer.setVisibility(View.VISIBLE);
               btnSend.setVisibility(View.GONE);
           }
        });

        btnSend.setOnClickListener(v -> {sendMessage();});

    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString();
        if (TextUtils.isEmpty(messageText)) return;

        Message message = new Message(
                currentUserId,
                messageText,
                "widget"
        );

        String conversationId = "OpGMIA2nZ8cjCMfow41Z"; // test
        viewModel.init(conversationId);

        viewModel.sendMessage(message);

        etMessage.setText("");
    }
}
