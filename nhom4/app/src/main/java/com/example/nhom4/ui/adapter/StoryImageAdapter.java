package com.example.nhom4.ui.adapter;

import android.graphics.Color;
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

public class StoryImageAdapter extends RecyclerView.Adapter<StoryImageAdapter.StoryImageViewHolder> {

    private List<Post> storyList = new ArrayList<>();

    public void setStoryList(List<Post> newStories) {
        if (newStories == null) newStories = new ArrayList<>();
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

    static class StoryImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgStory;

        StoryImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStory = itemView.findViewById(R.id.img_story);
        }

        void bind(Post post) {
            if (post == null) return;

            String urlToLoad;
            boolean isMoodOnly;

            // Kiểm tra: Có ảnh chụp không? Nếu không thì lấy icon mood
            if (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
                urlToLoad = post.getPhotoUrl();
                isMoodOnly = false;
            } else if (post.getMoodIconUrl() != null && !post.getMoodIconUrl().isEmpty()) {
                urlToLoad = post.getMoodIconUrl();
                isMoodOnly = true;
            } else {
                imgStory.setImageResource(R.drawable.ic_launcher_background);
                return;
            }

            // Cấu hình hiển thị Glide
            if (isMoodOnly) {
                // MOOD: Màu nền nhẹ + Icon giữ nguyên tỉ lệ (không cắt)
                imgStory.setBackgroundColor(Color.parseColor("#FFF59D")); // Màu vàng nhạt
                Glide.with(itemView.getContext())
                        .load(urlToLoad)
                        .fitCenter() // Giữ nguyên hình dáng icon
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgStory);
            } else {
                // ẢNH CHỤP: Nền đen + Crop full màn hình
                imgStory.setBackgroundColor(Color.BLACK);
                Glide.with(itemView.getContext())
                        .load(urlToLoad)
                        .centerCrop() // Cắt để lấp đầy
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imgStory);
            }
        }
    }

    private static class StoryDiffCallback extends DiffUtil.Callback {
        private final List<Post> oldList;
        private final List<Post> newList;

        StoryDiffCallback(List<Post> oldList, List<Post> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }
        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getPostId().equals(newList.get(newItemPosition).getPostId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Post oldPost = oldList.get(oldItemPosition);
            Post newPost = newList.get(newItemPosition);

            String oldUrl = oldPost.getPhotoUrl() != null ? oldPost.getPhotoUrl() : oldPost.getMoodIconUrl();
            String newUrl = newPost.getPhotoUrl() != null ? newPost.getPhotoUrl() : newPost.getMoodIconUrl();

            return (oldUrl != null && oldUrl.equals(newUrl));
        }
    }
}