package com.example.nhom4.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable; // Import Drawable
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Import Nullable
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource; // Import DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException; // Import GlideException
import com.bumptech.glide.request.RequestListener; // Import RequestListener
import com.bumptech.glide.request.target.Target; // Import Target
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Mood;

import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private final List<Mood> moodList;

    public MoodAdapter(List<Mood> moodList) {
        this.moodList = moodList;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_icon, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moodList.get(position);
        Context context = holder.itemView.getContext();

        holder.moodName.setText(mood.getName());

        String url = mood.getIconUrl();

        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    // --- THÊM PHẦN NÀY ĐỂ DEBUG LỖI ẢNH ---
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // Log lỗi chi tiết ra Logcat
                            Log.e("MoodAdapter", "Lỗi tải ảnh cho mood: " + mood.getName());
                            if (e != null) {
                                e.logRootCauses("MoodAdapter"); // In nguyên nhân gốc rễ
                            }
                            return false; // Để Glide tiếp tục xử lý error placeholder
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    // ---------------------------------------
                    .circleCrop()
                    .into(holder.moodIcon);
        } else {
            holder.moodIcon.setImageResource(R.drawable.ic_launcher_background);
        }

        if (mood.isPremium()) {
            holder.moodName.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            holder.moodName.setText(mood.getName() + " (VIP)");
        } else {
            // Dùng ID màu an toàn hơn
            holder.moodName.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.moodName.setText(mood.getName());
        }
    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        ImageView moodIcon;
        TextView moodName;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            moodIcon = itemView.findViewById(R.id.mood_icon);
            moodName = itemView.findViewById(R.id.mood_name);
        }
    }
}
