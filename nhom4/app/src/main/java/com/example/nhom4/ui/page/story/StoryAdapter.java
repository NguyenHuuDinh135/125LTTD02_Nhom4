package com.example.nhom4.ui.page.story;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    // 1. Khai báo mảng chứa ID ảnh (được truyền từ MainActivity)
    private int[] imageList;

    // 2. Constructor nhận dữ liệu đầu vào
    public StoryAdapter(int[] imageList) {
        this.imageList = imageList;
    }

    @Override
    public int getItemCount() {
        // Trả về đúng số lượng ảnh trong mảng (ví dụ: 5)
        if (imageList != null) {
            return imageList.length;
        }
        return 0;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lưu ý: Tên file xml phải khớp với file layout bạn đang có
        // (Trong đoạn code bạn gửi là R.layout.fragment_item_story)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        // Lấy ID ảnh tương ứng với vị trí hiện tại và hiển thị
        holder.imgStory.setImageResource(imageList[position]);

        holder.tvYear.setText("2025");
        // Tự động tăng ngày để test: 12, 13, 14...
        holder.tvDate.setText("Ngày " + (12 + position) + " tháng 8");

        // --- GÁN TEXT TIÊU ĐỀ ---
        // Tau thêm (position + 1) vào text để biết đang ở trang số mấy
        String part1 = "Angry";
        String part2 = " - It's Ok (" + (position + 1) + ")";

        // Tạo chuỗi HTML: Angry màu Đỏ, phần sau màu Trắng
        String textHTML = "<font color='#FF453A'>" + part1 + "</font> <font color='#FFFFFF'>" + part2 + "</font>";

        // Render HTML lên TextView
        holder.tvTitle.setText(Html.fromHtml(textHTML, Html.FROM_HTML_MODE_LEGACY));


    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvYear, tvDate, tvTitle;
        ImageView imgStory;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo các ID này khớp với file xml fragment_item_story.xml
            tvYear = itemView.findViewById(R.id.tvYear);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            imgStory = itemView.findViewById(R.id.imgStory);
        }
    }
}