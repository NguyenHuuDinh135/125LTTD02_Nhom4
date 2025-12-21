package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Mood;

import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    private List<Mood> moodList;
    private final OnMoodSelectedListener listener;

    // Sửa Interface để trả về cả View (để làm animation từ vị trí View này)
    public interface OnMoodSelectedListener {
        void onMoodSelected(Mood mood, View itemView);
    }

    public MoodAdapter(List<Mood> moodList, OnMoodSelectedListener listener) {
        this.moodList = moodList;
        this.listener = listener;
    }

    public void setList(List<Mood> newList) {
        this.moodList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng item_mood_icon.xml bạn đã cung cấp
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_icon, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moodList.get(position);

        holder.tvMoodName.setText(mood.getName());

        // [QUAN TRỌNG] .asBitmap() ép Glide chỉ load Frame đầu tiên (ảnh tĩnh) dù là GIF
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(mood.getIconUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgMood);

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Truyền mood và chính cái View được bấm để làm animation
                listener.onMoodSelected(mood, holder.imgMood);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moodList != null ? moodList.size() : 0;
    }

    public static class MoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMood;
        TextView tvMoodName;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMood = itemView.findViewById(R.id.mood_icon);
            tvMoodName = itemView.findViewById(R.id.mood_name);
        }
    }
}