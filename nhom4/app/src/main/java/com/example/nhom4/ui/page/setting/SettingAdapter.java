// SettingAdapter.java
package com.example.nhom4.ui.page.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    private final Context context;
    private final List<SettingItem> items;
    private final UserProfileViewModel viewModel;
    private final String currentUserUid;

    public SettingAdapter(Context context, List<SettingItem> items,
                          UserProfileViewModel viewModel, String currentUserUid) {
        this.context = context;
        this.items = items;
        this.viewModel = viewModel;
        this.currentUserUid = currentUserUid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_setting_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingItem item = items.get(position);
        holder.tvLabel.setText(item.getLabel());
        holder.tvValue.setText(item.getValue() != null ? item.getValue() : "");

        holder.itemView.setOnClickListener(v -> {
            // Hiển thị dialog sửa
            EditText input = new EditText(context);
            input.setText(item.getValue());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            if (item.getField().equals("email")) {
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            }

            new AlertDialog.Builder(context)
                    .setTitle("Sửa " + item.getLabel())
                    .setView(input)
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        String newValue = input.getText().toString().trim();
                        if (newValue.isEmpty()) {
                            Toast.makeText(context, "Không được để trống", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Cập nhật Firestore
                        viewModel.getUserProfileLiveData().getValue(); // đảm bảo profile loaded
                        viewModel.getErrorLiveData().observe((SettingActivity) context, error -> {
                            if (error != null) Toast.makeText(context, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        });

                        viewModel.updateUserProfileField(currentUserUid, item.getField(), newValue);

                        // Cập nhật item local
                        item.setValue(newValue);
                        notifyItemChanged(position);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel, tvValue;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvValue = itemView.findViewById(R.id.tvValue);
        }
    }
}
