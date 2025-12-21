package com.example.nhom4.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.main.MainFragment;
import com.example.nhom4.ui.page.post.PostFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter điều khiển ViewPager2 dọc.
 * Fix lỗi không cập nhật bài mới bằng cách override getItemId.
 */
public class VerticalPagerAdapter extends FragmentStateAdapter {

    private List<Post> postList = new ArrayList<>();

    public VerticalPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public void setPostList(List<Post> postList) {
        // Tạo ArrayList mới để tránh tham chiếu vùng nhớ cũ
        this.postList = new ArrayList<>(postList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Vị trí 0: Màn hình Camera (MainFragment)
        if (position == 0) {
            return new MainFragment();
        }

        // Các vị trí > 0: Màn hình Feed (PostFragment)
        // Trừ 1 để khớp với index của list (0 -> size-1)
        int realListIndex = position - 1;
        if (!postList.isEmpty() && realListIndex < postList.size()) {
            return PostFragment.newInstance(postList.get(realListIndex));
        }

        return new Fragment(); // Fallback
    }

    @Override
    public int getItemCount() {
        // 1 trang Camera + N bài viết
        return 1 + postList.size();
    }

    // [CỰC KỲ QUAN TRỌNG]
    // Giúp ViewPager2 nhận biết sự thay đổi dữ liệu để vẽ lại Fragment
    @Override
    public long getItemId(int position) {
        if (position == 0) {
            // ID cố định cho trang Camera để nó không bị load lại
            return 0;
        }

        // Với các bài viết, dùng hashCode của PostID làm ID duy nhất
        // Khi list thay đổi thứ tự, ID tại position đó thay đổi -> ViewPager tự refresh
        int realListIndex = position - 1;
        if (realListIndex < postList.size()) {
            return postList.get(realListIndex).getPostId().hashCode();
        }
        return super.getItemId(position);
    }

    @Override
    public boolean containsItem(long itemId) {
        if (itemId == 0) return true; // Trang Camera luôn tồn tại

        // Kiểm tra xem bài viết với ID này còn trong list không
        for (Post post : postList) {
            if (post.getPostId().hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }
}