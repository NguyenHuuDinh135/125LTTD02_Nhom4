package com.example.nhom4.ui.adapter;

import android.content.res.ColorStateList;
import android.content.res.Resources;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

public class StoryThumbnailAdapter extends RecyclerView.Adapter<StoryThumbnailAdapter.ThumbnailViewHolder> {

    private static final float STROKE_WIDTH_SELECTED_DP = 3f;
    private static final float STROKE_WIDTH_UNSELECTED_DP = 0f; // Không viền khi chưa chọn

    private List<Post> storyList = new ArrayList<>();
    private int selectedPosition = 0;
    private OnThumbnailClickListener listener;

    private Integer cachedPrimaryColor;
    private Integer cachedStrokeWidthSelectedPx;
    private Integer cachedStrokeWidthUnselectedPx;

    public interface OnThumbnailClickListener {
        void onThumbnailClick(int position);
    }

    public void setOnThumbnailClickListener(OnThumbnailClickListener listener) {
        this.listener = listener;
    }

    public void setStoryList(List<Post> newStories) {
        if (newStories == null) newStories = new ArrayList<>();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new ThumbnailDiffCallback(this.storyList, newStories)
        );
        this.storyList.clear();
        this.storyList.addAll(newStories);
        diffResult.dispatchUpdatesTo(this);
        clearColorCache();
    }

    public void setSelectedPosition(int position) {
        if (position == selectedPosition) return;
        int oldPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldPosition, "SELECTION_CHANGED");
        notifyItemChanged(selectedPosition, "SELECTION_CHANGED");
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story_thumbnail, parent, false);
        return new ThumbnailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position) {
        onBindViewHolder(holder, position, null);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position, List<Object> payloads) {
        if (payloads != null && !payloads.isEmpty() && payloads.contains("SELECTION_CHANGED")) {
            holder.updateSelectionState(position == selectedPosition, holder.itemView.getContext());
            return;
        }

        Post post = storyList.get(position);
        holder.bind(post, position == selectedPosition, holder.itemView.getContext());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onThumbnailClick(position);
        });
    }

    @Override
    public int getItemCount() { return storyList.size(); }

    private void clearColorCache() {
        cachedPrimaryColor = null;
        cachedStrokeWidthSelectedPx = null;
        cachedStrokeWidthUnselectedPx = null;
    }

    class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardContainer;
        private final ImageView imgThumbnail;

        ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_thumbnail_container);
            imgThumbnail = itemView.findViewById(R.id.img_story_thumbnail);
        }

        void bind(Post post, boolean isSelected, android.content.Context context) {
            loadImage(post);
            updateSelectionState(isSelected, context);
        }

        void updateSelectionState(boolean isSelected, android.content.Context context) {
            Resources resources = context.getResources();
            float density = resources.getDisplayMetrics().density;

            if (isSelected) {
                if (cachedStrokeWidthSelectedPx == null) {
                    cachedStrokeWidthSelectedPx = (int) (STROKE_WIDTH_SELECTED_DP * density);
                }
                cardContainer.setStrokeWidth(cachedStrokeWidthSelectedPx);

                // Lấy màu Primary từ theme (đã mở comment)
                if (cachedPrimaryColor == null) {
                    cachedPrimaryColor = MaterialColors.getColor(
                            context,
                            android.R.attr.colorPrimary,
                            "StoryThumbnailAdapter"
                    );
                }
                cardContainer.setStrokeColor(ColorStateList.valueOf(cachedPrimaryColor));
            } else {
                if (cachedStrokeWidthUnselectedPx == null) {
                    cachedStrokeWidthUnselectedPx = (int) (STROKE_WIDTH_UNSELECTED_DP * density);
                }
                cardContainer.setStrokeWidth(cachedStrokeWidthUnselectedPx);
            }
        }

        private void loadImage(Post post) {
            // [LOGIC MỚI] Lấy url ảnh hoặc mood
            String url = null;
            if (post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
                url = post.getPhotoUrl();
            } else if (post.getMoodIconUrl() != null && !post.getMoodIconUrl().isEmpty()) {
                url = post.getMoodIconUrl();
            }

            if (url != null) {
                Glide.with(itemView.getContext())
                        .load(url)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(0.1f)
                        .into(imgThumbnail);
            } else {
                imgThumbnail.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    private static class ThumbnailDiffCallback extends DiffUtil.Callback {
        private final List<Post> oldList;
        private final List<Post> newList;

        ThumbnailDiffCallback(List<Post> oldList, List<Post> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }
        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).getPostId().equals(newList.get(newPos).getPostId());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            Post oldPost = oldList.get(oldPos);
            Post newPost = newList.get(newPos);
            String oldUrl = oldPost.getPhotoUrl() != null ? oldPost.getPhotoUrl() : oldPost.getMoodIconUrl();
            String newUrl = newPost.getPhotoUrl() != null ? newPost.getPhotoUrl() : newPost.getMoodIconUrl();
            return (oldUrl != null && oldUrl.equals(newUrl));
        }
    }
}