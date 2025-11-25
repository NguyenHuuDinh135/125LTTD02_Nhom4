package com.example.nhom4.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.post.PostFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Post post = postList.get(position);

        // 1. Xử lý Caption Title
        String title = "";
        if ("mood".equals(post.getType())) {
            title = post.getMoodName();
        } else {
            title = post.getActivityTitle();
        }

        // 2. Xử lý Hình ảnh
        // Ưu tiên ảnh chụp (photoUrl). Nếu không có ảnh chụp, dùng icon của Mood.
        String displayImageUrl = "";
        if (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
            displayImageUrl = post.getPhotoUrl();
        } else if ("mood".equals(post.getType())) {
            displayImageUrl = post.getMoodIconUrl();
        }

        // 3. Format ngày tháng
        String timeStr = "Vừa xong";
        if (post.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            timeStr = sdf.format(post.getCreatedAt().toDate());
        }

        // Truyền dữ liệu vào Fragment
        return PostFragment.newInstance(
                title,                  // captionStart
                post.getCaption(),      // captionEnd
                displayImageUrl,        // imageUrl
                timeStr,                // timestamp
                "Người dùng ẩn danh",   // avatarGroup (placeholder)
                post.getPostId(),       // postId
                post.getUserId(),       // userIdOfOwner
                post.getType()          // postType
        );
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
