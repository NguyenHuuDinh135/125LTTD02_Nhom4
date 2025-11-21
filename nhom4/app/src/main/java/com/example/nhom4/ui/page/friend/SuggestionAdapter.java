package com.example.nhom4.ui.page.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    private List<String> list;
    private List<String> sentRequests = new ArrayList<>(); // danh sách đã gửi
    private OnAddClickListener addClickListener;

    public SuggestionAdapter(List<String> list) {
        this.list = list;
    }

    public void setList(List<String> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public void setOnAddClickListener(OnAddClickListener listener) {
        this.addClickListener = listener;
    }

    public void clearSentRequests() {
        sentRequests.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item friend request (có nút btn_add)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String username = list.get(position);
        holder.txtName.setText(username);

        if (sentRequests.contains(username)) {
            holder.btnAdd.setText("Đã gửi");
            holder.btnAdd.setEnabled(false);
        } else {
            holder.btnAdd.setText("Thêm");
            holder.btnAdd.setEnabled(true);
            holder.btnAdd.setOnClickListener(v -> {
                if (addClickListener != null) {
                    addClickListener.onAddClick(username); // callback cho fragment/activity
                }
                sentRequests.add(username); // đánh dấu đã gửi
                notifyItemChanged(position);
            });
        }

        // TODO: load avatar nếu có URL
        // Glide.with(holder.avatar.getContext()).load(url).into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView txtName;
        Button btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_avatar);
            txtName = itemView.findViewById(R.id.tv_username);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }

    // Interface callback để Fragment/Activity xử lý gửi yêu cầu
    public interface OnAddClickListener {
        void onAddClick(String userId);
    }
}
