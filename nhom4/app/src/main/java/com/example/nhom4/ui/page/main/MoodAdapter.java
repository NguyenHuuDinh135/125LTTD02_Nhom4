package com.example.nhom4.ui.page.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.moodName.setText(mood.getName());
        holder.moodIcon.setImageResource(mood.getIconDrawableId());
        // (Bạn có thể dùng Glide/Coil để tải ảnh thật sau này)
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
