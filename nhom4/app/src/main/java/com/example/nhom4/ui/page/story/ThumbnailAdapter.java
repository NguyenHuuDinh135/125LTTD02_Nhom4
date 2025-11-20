package com.example.nhom4.ui.page.story;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbViewHolder> {
    private int[] imageList; // Mảng ảnh
    private int selectedPosition = 0; // Mặc định chọn 0

    // Constructor nhận ảnh
    public ThumbnailAdapter(int[] imageList) {
        this.imageList = imageList;
    }

    @Override
    public int getItemCount() {
        return imageList.length;
    }

    @NonNull
    @Override
    public ThumbViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thumbnail, parent, false);
        return new ThumbViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbViewHolder holder, int position) {
        // Gán ảnh thật vào thumbnail
        holder.imgThumb.setImageResource(imageList[position]);

        // Logic làm sáng/tối giữ nguyên
        if (position == selectedPosition) {
            holder.imgThumb.setAlpha(1.0f);
            holder.cardThumb.setCardBackgroundColor(0x40FFFFFF);
            holder.itemView.setScaleX(1.2f);
            holder.itemView.setScaleY(1.2f);
        } else {
            holder.imgThumb.setAlpha(0.5f);
            holder.cardThumb.setCardBackgroundColor(0x00000000);
            holder.itemView.setScaleX(1.0f);
            holder.itemView.setScaleY(1.0f);
        }
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    static class ThumbViewHolder extends RecyclerView.ViewHolder {
        CardView cardThumb;
        ImageView imgThumb;
        public ThumbViewHolder(@NonNull View itemView) {
            super(itemView);
            cardThumb = itemView.findViewById(R.id.cardThumb);
            imgThumb = itemView.findViewById(R.id.imgThumb);
        }
    }
}
