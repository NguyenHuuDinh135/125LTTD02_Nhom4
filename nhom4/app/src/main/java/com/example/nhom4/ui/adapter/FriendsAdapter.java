package com.example.nhom4.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;

import java.util.List;

/**
 * Adapter hiển thị danh sách bạn bè đơn giản (tên + avatar).
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<String> list;

    public FriendsAdapter(List<String> list) {
        this.list = list;
    }

    /**
     * Tạo ViewHolder từ layout item_friend.xml
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Bind dữ liệu mô hình vào ViewHolder.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtName.setText(list.get(position)); // Danh sách hiện chỉ lưu mỗi tên
    }
    
    /**
     * Trả về số lượng bạn bè trong danh sách.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder giữ các view avatar, tên và nút xoá.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView txtName;
        ImageView btnDel;

        /**
         * Khởi tạo ViewHolder và ánh xạ các view từ layout.
         * @param itemView
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.iv_avatar);
            txtName = itemView.findViewById(R.id.tv_username);
            btnDel = itemView.findViewById(R.id.btn_delete);
        }

    }
}
