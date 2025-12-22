package com.example.nhom4.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.post.PostFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho ViewPager2 dọc của feed, mỗi trang là một {@link PostFragment}.
 */
public class PostAdapter extends FragmentStateAdapter {

    private final List<Post> postList = new ArrayList<>();

    public PostAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    // Hàm cập nhật dữ liệu từ ViewModel
    public void setPostList(List<Post> posts) {
        this.postList.clear();
        if (posts != null) {
            this.postList.addAll(posts);
        }
        notifyDataSetChanged();
    }

    /**
     * Tạo Fragment cho một bài đăng tại vị trí nhất định.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Post post = postList.get(position);
        return PostFragment.newInstance(post);
    }

    /**
     * Trả về số lượng bài đăng trong danh sách.
     */
    @Override
    public int getItemCount() {
        return postList.size();
    }

    // =========================================================================
    // [FIX CRASH] QUAN TRỌNG: Cần override 2 hàm này để ViewPager2 định danh được Fragment
    // Nguyên nhân lỗi "Page can only be offset...": Do ID mặc định là position,
    // khi list thay đổi (load thêm/xóa), position lệch làm ViewPager2 bị crash.
    // =========================================================================

    @Override
    public long getItemId(int position) {
        // Trả về một ID duy nhất cho Post (sử dụng hashCode của String PostID)
        // Thay vì trả về position (0,1,2...) mặc định
        if (position >= 0 && position < postList.size()) {
            Post post = postList.get(position);
            if (post.getPostId() != null) {
                return post.getPostId().hashCode();
            }
        }
        return position; // Fallback nếu không có ID
    }

    @Override
    public boolean containsItem(long itemId) {
        // Kiểm tra xem ID này còn tồn tại trong list mới không
        // Nếu không có hàm này, ViewPager2 sẽ không biết fragment nào cần giữ lại/xóa đi
        for (Post post : postList) {
            if (post.getPostId() != null && post.getPostId().hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }
}