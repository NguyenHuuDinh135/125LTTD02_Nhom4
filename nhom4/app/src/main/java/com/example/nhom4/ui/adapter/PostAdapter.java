package com.example.nhom4.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.page.post.PostFragment;

import java.util.ArrayList;
import java.util.List;

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
        // [FIX] Sử dụng phương thức newInstance mới nhận vào object Post
        return PostFragment.newInstance(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}
