package com.example.nhom4.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.main.MainFragment;
import com.example.nhom4.ui.page.post.PostFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VerticalPagerAdapter extends FragmentStateAdapter {

    // Danh sách chứa bài viết thật
    private List<Post> postList = new ArrayList<>();

    public VerticalPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    // Hàm này để CenterFragment gọi khi tải xong dữ liệu từ Firebase
    public void setPostList(List<Post> posts) {
        this.postList.clear();
        this.postList.addAll(posts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            // Vị trí đầu tiên luôn là Camera (MainFragment)
            return new MainFragment();
        } else {
            // Các vị trí sau là Post
            // Vì vị trí 0 là Camera, nên bài viết thứ nhất nằm ở index 0 của list (tức là position - 1)
            int postIndex = position - 1;

            // Kiểm tra an toàn để tránh crash nếu index vượt quá size
            if (postIndex >= postList.size()) {
                return new Fragment(); // Trả về fragment rỗng hoặc xử lý lỗi
            }

            Post post = postList.get(postIndex);
            return createRealPostFragment(post);
        }
    }

    @Override
    public int getItemCount() {
        // Tổng số trang = 1 (Camera) + Số lượng bài viết thật
        return 1 + postList.size();
    }

    // Hàm map dữ liệu thật từ Model Post sang PostFragment
    private Fragment createRealPostFragment(Post post) {
        // 1. Xử lý Title (Mood hoặc Activity)
        String title = "";
        String imageUrl = "";

        if ("mood".equals(post.getType())) {
            title = post.getMoodName();
            // Nếu là mood, ưu tiên ảnh chụp, nếu không có thì lấy icon mood
            if (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
                imageUrl = post.getPhotoUrl();
            } else {
                imageUrl = post.getMoodIconUrl();
            }
        } else {
            title = post.getActivityTitle();
            imageUrl = post.getPhotoUrl();
        }

        // 2. Xử lý thời gian
        String timeStr = "Vừa xong";
        if (post.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            timeStr = sdf.format(post.getCreatedAt().toDate());
        }

        // 3. Trả về Fragment với dữ liệu thật
        return PostFragment.newInstance(
                title,                  // 1. Caption đỏ
                post.getCaption(),      // 2. Caption thường
                imageUrl,               // 3. Link ảnh
                timeStr,                // 4. Thời gian
                "Người dùng ẩn danh",   // 5. Tên người dùng
                "",                     // 6. Missing Param 1 (Check PostFragment for what this should be)
                "",                     // 7. Missing Param 2 (Check PostFragment for what this should be)
                ""                      // 8. Missing Param 3 (Check PostFragment for what this should be)
        );

    }
}
