package com.example.nhom4.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.ui.page.main.MainFragment;
import com.example.nhom4.ui.page.post.PostFragment;

public class VerticalPagerAdapter extends FragmentStateAdapter {

    // Giả sử mình muốn hiển thị 20 bài post mẫu
    private static final int NUM_POSTS = 20;

    public VerticalPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            //
            // Vị trí đầu tiên luôn là Màn hình chính (Camera/Check-in)
            return new MainFragment();
        } else {
            // Các vị trí còn lại (1, 2, 3...) là Post
            // Vì position 0 là MainFragment, nên index của Post sẽ là (position - 1)
            int postIndex = position - 1;

            return createFakePostFragment(postIndex);
        }
    }

    @Override
    public int getItemCount() {
        // Tổng số trang = 1 (MainFragment) + Số lượng bài Post
        return 1 + NUM_POSTS;
    }

    // Hàm tạo dữ liệu giả (Logic lấy từ PostAdapter cũ của bạn)
    private Fragment createFakePostFragment(int index) {
        String imageUrl = "https://picsum.photos/seed/" + (index + 100) + "/800/1200"; // +100 để đổi seed khác
        String captionStart, captionEnd, timestamp, avatarGroup;

        // Logic switch case để tạo dữ liệu đa dạng
        switch (index % 4) {
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

        return PostFragment.newInstance(captionStart, captionEnd, imageUrl, timestamp, avatarGroup);
    }
}