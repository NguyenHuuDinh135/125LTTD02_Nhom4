package com.example.nhom4.ui.page.post;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PostAdapter extends FragmentStateAdapter {

    public PostAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Cung cấp dữ liệu giả (fake data)
        String imageUrl = "https://picsum.photos/seed/" + (position + 1) + "/800/1200";
        String captionStart, captionEnd, timestamp, avatarGroup;

        switch (position % 4) {
            case 0:
                captionStart = "Angry";
                captionEnd = "It's Ok";
                timestamp = "Bạn 22 thg 9";
                avatarGroup = "Quangvinh12, Tue122,...";
                break;
            case 1:
                captionStart = "Happy";
                captionEnd = "Feeling Good";
                timestamp = "Lisa 21 thg 9";
                avatarGroup = "Lisa, Jisoo,...";
                break;
            case 2:
                captionStart = "Sad";
                captionEnd = "Rainy Day";
                timestamp = "Bạn 20 thg 9";
                avatarGroup = "Jennie,...";
                break;
            default:
                captionStart = "Excited";
                captionEnd = "New Project";
                timestamp = "Rose 19 thg 9";
                avatarGroup = "Rose, Quangvinh12,...";
                break;
        }

        // Gọi hàm newInstance đã được cập nhật
        return PostFragment.newInstance(captionStart, captionEnd, imageUrl, timestamp, avatarGroup);
    }

    @Override
    public int getItemCount() {
        return 20; // Số lượng post mẫu
    }
}