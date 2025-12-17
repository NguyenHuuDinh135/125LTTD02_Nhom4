package com.example.nhom4.ui.adapter;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

/**
 * Adapter cho RecyclerView hiển thị thumbnails của các story.
 * Tối ưu với DiffUtil, cache màu sắc và stroke width.
 */
public class StoryThumbnailAdapter extends RecyclerView.Adapter<StoryThumbnailAdapter.ThumbnailViewHolder> {

    // Constants để dễ maintain
    private static final float STROKE_WIDTH_SELECTED_DP = 3f;
    private static final float STROKE_WIDTH_UNSELECTED_DP = 2f;

    private List<Post> storyList = new ArrayList<>();
    private int selectedPosition = 0;
    private OnThumbnailClickListener listener;

    // Cache để tránh tính toán lại nhiều lần
    private Integer cachedPrimaryColor;
    private Integer cachedOutlineColor;
    private Integer cachedStrokeWidthSelectedPx;
    private Integer cachedStrokeWidthUnselectedPx;

    public interface OnThumbnailClickListener {
        void onThumbnailClick(int position);
    }

    public void setOnThumbnailClickListener(OnThumbnailClickListener listener) {
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách story với DiffUtil.
     */
    public void setStoryList(List<Post> newStories) {
        if (newStories == null) {
            newStories = new ArrayList<>();
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new ThumbnailDiffCallback(this.storyList, newStories)
        );
        this.storyList.clear();
        this.storyList.addAll(newStories);
        diffResult.dispatchUpdatesTo(this);
        
        // Clear cache khi danh sách thay đổi
        clearColorCache();
    }

    /**
     * Cập nhật vị trí được chọn với payload để chỉ update phần cần thiết.
     */
    public void setSelectedPosition(int position) {
        if (position == selectedPosition) return;
        
        int oldPosition = selectedPosition;
        selectedPosition = position;
        
        // Sử dụng payload để chỉ update stroke, không reload ảnh
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
    public void onBindViewHolder(@NonNull ThumbnailViewHolder holder, int position, @NonNull List<Object> payloads) {
        Post post = storyList.get(position);
        
        // Nếu chỉ có payload (selection changed), chỉ update stroke
        if (payloads != null && !payloads.isEmpty() && payloads.contains("SELECTION_CHANGED")) {
            holder.updateSelectionState(position == selectedPosition, holder.itemView.getContext());
            return;
        }
        
        // Full bind: load ảnh và update selection
        holder.bind(post, position == selectedPosition, holder.itemView.getContext());
        
        // Click listener - chỉ set một lần
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onThumbnailClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    /**
     * Clear cache màu sắc khi context thay đổi (ví dụ theme change).
     */
    private void clearColorCache() {
        cachedPrimaryColor = null;
        cachedOutlineColor = null;
        cachedStrokeWidthSelectedPx = null;
        cachedStrokeWidthUnselectedPx = null;
    }

    /**
     * ViewHolder với logic bind tách riêng.
     */
    class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardContainer;
        private final android.widget.ImageView imgThumbnail;

        ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_thumbnail_container);
            imgThumbnail = itemView.findViewById(R.id.img_story_thumbnail);
        }

        /**
         * Bind đầy đủ: ảnh + selection state.
         */
        void bind(Post post, boolean isSelected, android.content.Context context) {
            loadImage(post);
            updateSelectionState(isSelected, context);
        }

        /**
         * Chỉ update selection state (khi có payload).
         */
        void updateSelectionState(boolean isSelected, android.content.Context context) {
            Resources resources = context.getResources();
            float density = resources.getDisplayMetrics().density;

            if (isSelected) {
                // Cache stroke width selected
                if (cachedStrokeWidthSelectedPx == null) {
                    cachedStrokeWidthSelectedPx = (int) (STROKE_WIDTH_SELECTED_DP * density);
                }
                cardContainer.setStrokeWidth(cachedStrokeWidthSelectedPx);

                // Cache primary color
                if (cachedPrimaryColor == null) {
                    cachedPrimaryColor = MaterialColors.getColor(
                            context,
                            com.google.android.material.R.attr.colorPrimary,
                            "StoryThumbnailAdapter"
                    );
                }
                cardContainer.setStrokeColor(ColorStateList.valueOf(cachedPrimaryColor));
            } else {
                // Cache stroke width unselected
                if (cachedStrokeWidthUnselectedPx == null) {
                    cachedStrokeWidthUnselectedPx = (int) (STROKE_WIDTH_UNSELECTED_DP * density);
                }
                cardContainer.setStrokeWidth(cachedStrokeWidthUnselectedPx);

                // Cache outline color
                if (cachedOutlineColor == null) {
                    cachedOutlineColor = MaterialColors.getColor(
                            context,
                            com.google.android.material.R.attr.colorOutline,
                            "StoryThumbnailAdapter"
                    );
                }
                cardContainer.setStrokeColor(ColorStateList.valueOf(cachedOutlineColor));
            }
        }

        /**
         * Load ảnh thumbnail với Glide caching.
         */
        private void loadImage(Post post) {
            if (post != null && post.getPhotoUrl() != null && !post.getPhotoUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(post.getPhotoUrl())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache để tối ưu
                        .thumbnail(0.1f) // Load thumbnail nhỏ trước để UX mượt hơn
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imgThumbnail);
            } else {
                imgThumbnail.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    /**
     * DiffUtil.Callback để so sánh và cập nhật hiệu quả.
     */
    private static class ThumbnailDiffCallback extends DiffUtil.Callback {
        private final List<Post> oldList;
        private final List<Post> newList;

        ThumbnailDiffCallback(List<Post> oldList, List<Post> newList) {
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
            return oldPost.getPhotoUrl() != null && 
                   oldPost.getPhotoUrl().equals(newPost.getPhotoUrl());
        }
    }
}
