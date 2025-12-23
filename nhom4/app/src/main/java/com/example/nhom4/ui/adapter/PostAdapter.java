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
 * Adapter cho ViewPager2 dọc của feed.
 */
public class PostAdapter extends FragmentStateAdapter {

    private final List<Post> postList = new ArrayList<>();

    public PostAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

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
        // Truyền Post vào Fragment
        Post post = postList.get(position);
        return PostFragment.newInstance(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < postList.size()) {
            Post post = postList.get(position);
            // Dùng hashCode của ID để tránh bug crash khi refresh list
            if (post.getPostId() != null) {
                return post.getPostId().hashCode();
            }
        }
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        for (Post post : postList) {
            if (post.getPostId() != null && post.getPostId().hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }
}