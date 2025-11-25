package com.example.nhom4.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.Activity;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> activityList;
    private final OnActivitySelectedListener listener;
    private int selectedPosition = -1;

    public interface OnActivitySelectedListener {
        void onActivitySelected(Activity activity);
    }

    public ActivityAdapter(List<Activity> activityList, OnActivitySelectedListener listener) {
        this.activityList = activityList;
        this.listener = listener;
    }

    // [MVVM] Thêm hàm này để ViewModel hoặc Fragment có thể cập nhật danh sách mới
    public void setList(List<Activity> newList) {
        this.activityList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_text, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        holder.activityTitle.setText(activity.getTitle());

        // Visual effect: Highlight item được chọn
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            holder.activityTitle.getPaint().setFakeBoldText(true);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.activityTitle.getPaint().setFakeBoldText(false);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onActivitySelected(activity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityList != null ? activityList.size() : 0;
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView activityTitle;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            activityTitle = itemView.findViewById(R.id.activity_title);
        }
    }
}
