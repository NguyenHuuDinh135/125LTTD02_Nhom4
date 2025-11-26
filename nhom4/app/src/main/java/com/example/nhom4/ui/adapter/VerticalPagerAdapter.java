package com.example.nhom4.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.main.MainFragment; // Import MainFragment
import com.example.nhom4.ui.page.post.PostFragment;

import java.util.ArrayList;
import java.util.List;

public class VerticalPagerAdapter extends FragmentStateAdapter {

    private List<Post> postList = new ArrayList<>();

    public VerticalPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // [QUAN TRỌNG] Vị trí 0 luôn là MainFragment (Màn hình Camera)
        if (position == 0) {
            return new MainFragment();
        }

        // Các vị trí tiếp theo (1, 2, 3...) là danh sách bài đăng (Feed)
        // Ta phải trừ đi 1 để lấy đúng index trong list
        if (!postList.isEmpty() && position - 1 < postList.size()) {
            Post post = postList.get(position - 1);
            return PostFragment.newInstance(post);
        }

        // Fallback nếu có lỗi index
        return new Fragment();
    }

    @Override
    public int getItemCount() {
        // Tổng số trang = 1 (Camera) + số lượng bài post
        return 1 + postList.size();
    }
}
