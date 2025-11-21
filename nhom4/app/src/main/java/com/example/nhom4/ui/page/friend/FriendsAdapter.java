package com.example.nhom4.ui.page.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.model.Relationship;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private List<Relationship> list;
    private String currentUserId;

    public FriendsAdapter(List<Relationship> list, String currentUserId) {
        this.list = list;
        this.currentUserId = currentUserId;
    }

    public void setList(List<Relationship> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Relationship r = list.get(position);

        // Lấy tên bạn bè (không phải user hiện tại)
        String friendName = "";
        for (String member : r.getMembers()) {
            if (!member.equals(currentUserId)) {
                friendName = member;
                break;
            }
        }

        holder.txtName.setText(friendName);

        // TODO: load avatar nếu có URL
        // Glide.with(holder.avatar.getContext()).load(url).into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar, btnDel;
        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_avatar);
            txtName = itemView.findViewById(R.id.tv_username);
            btnDel = itemView.findViewById(R.id.btn_delete);
        }
    }
}
