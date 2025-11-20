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
    private OnMoodSelectedListener listener; // Interface lắng nghe
    private int selectedPosition = -1; // Để highlight item đang chọn

    // Interface callback
    public interface OnMoodSelectedListener {
        void onMoodSelected(Mood mood);
    }

    // Constructor nhận thêm listener
    public MoodAdapter(List<Mood> moodList, OnMoodSelectedListener listener) {
        this.moodList = moodList;
        this.listener = listener;
    }

    // Constructor cũ (để tương thích nếu code khác gọi, nhưng nên tránh dùng)
    public MoodAdapter(List<Mood> moodList) {
        this.moodList = moodList;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_icon, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moodList.get(position);

        // Load ảnh bằng Glide (vì iconUrl là String URL)
        Glide.with(holder.itemView.getContext())
                .load(mood.getIconUrl())
                .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ
                .into(holder.imgMood);

        holder.tvMoodName.setText(mood.getName());

        // Hiệu ứng chọn (ví dụ: làm mờ các item không chọn hoặc phóng to item chọn)
        if (selectedPosition == position) {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setScaleX(1.2f);
            holder.itemView.setScaleY(1.2f);
        } else {
            holder.itemView.setAlpha(selectedPosition == -1 ? 1.0f : 0.5f);
            holder.itemView.setScaleX(1.0f);
            holder.itemView.setScaleY(1.0f);
        }

        // Sự kiện Click
        holder.itemView.setOnClickListener(v -> {
            // Cập nhật vị trí chọn
            int previousItem = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Refresh lại danh sách để hiện hiệu ứng highlight
            notifyItemChanged(previousItem);
            notifyItemChanged(selectedPosition);

            // Gửi sự kiện về Fragment
            if (listener != null) {
                listener.onMoodSelected(mood);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moodList.size();
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
