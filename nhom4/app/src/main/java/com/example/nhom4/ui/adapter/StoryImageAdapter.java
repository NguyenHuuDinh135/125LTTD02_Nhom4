package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho ViewPager2 hiển thị từng story image.
 * Tối ưu với DiffUtil và Glide caching.
 */
public class StoryImageAdapter extends RecyclerView.Adapter<StoryImageAdapter.StoryImageViewHolder> {

    private List<Post> storyList = new ArrayList<>();

    /**
     * Cập nhật danh sách story với DiffUtil để tối ưu performance.
     */
    public void setStoryList(List<Post> newStories) {
        if (newStories == null) {
            newStories = new ArrayList<>();
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new StoryDiffCallback(this.storyList, newStories)
        );
        this.storyList.clear();
        this.storyList.addAll(newStories);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public StoryImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story_image, parent, false);
        return new StoryImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryImageViewHolder holder, int position) {
        Post post = storyList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public Post getItem(int position) {
        if (position >= 0 && position < storyList.size()) {
            return storyList.get(position);
        }
        return null;
    }

    /**
     * ViewHolder với logic bind tách riêng để dễ maintain.
     */
    static class StoryImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgStory;

        StoryImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStory = itemView.findViewById(R.id.img_story);
        }

        /**
         * Bind dữ liệu Post vào view. Tách riêng để dễ test và maintain.
         */
        void bind(Post post) {
            if (post != null && post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(post.getPhotoUrl())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache để tối ưu
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imgStory);
            } else {
                imgStory.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    /**
     * DiffUtil.Callback để so sánh và cập nhật hiệu quả.
     */
    private static class StoryDiffCallback extends DiffUtil.Callback {
        private final List<Post> oldList;
        private final List<Post> newList;

        StoryDiffCallback(List<Post> oldList, List<Post> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Post oldPost = oldList.get(oldItemPosition);
            Post newPost = newList.get(newItemPosition);
            // So sánh bằng postId nếu có, hoặc photoUrl
            if (oldPost.getPostId() != null && newPost.getPostId() != null) {
                return oldPost.getPostId().equals(newPost.getPostId());
            }
            return oldPost.getPhotoUrl() != null && 
                   oldPost.getPhotoUrl().equals(newPost.getPhotoUrl());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Post oldPost = oldList.get(oldItemPosition);
            Post newPost = newList.get(newItemPosition);
            // So sánh photoUrl vì đây là dữ liệu chính để hiển thị
            return oldPost.getPhotoUrl() != null && 
                   oldPost.getPhotoUrl().equals(newPost.getPhotoUrl());
        }
    }
}
