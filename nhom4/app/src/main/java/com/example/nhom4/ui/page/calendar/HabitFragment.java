package com.example.nhom4.ui.page.calendar;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.ui.adapter.ActivityAdapter;
import com.example.nhom4.ui.viewmodel.ActivityViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class HabitFragment extends Fragment {

    private ActivityViewModel viewModel;
    private ActivityAdapter adapter;
    private RecyclerView recyclerView;
    private MaterialButton btnAddHabit;

    // Biến xử lý ảnh trong Dialog
    private Uri selectedImageUri = null;
    private ImageView imgPreviewInDialog;

    // Launcher chọn ảnh
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (imgPreviewInDialog != null) {
                        imgPreviewInDialog.setImageURI(uri);
                        imgPreviewInDialog.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ActivityViewModel.class);

        recyclerView = view.findViewById(R.id.rv_habits);
        btnAddHabit = view.findViewById(R.id.btn_add_habit);

        setupRecyclerView();
        setupEvents();
        observeViewModel();

        // Tải dữ liệu
        viewModel.loadMyActivities();
    }

    private void setupRecyclerView() {
        adapter = new ActivityAdapter(new ArrayList<>(), activity -> {
            Toast.makeText(getContext(), "Đã chọn: " + activity.getTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupEvents() {
        btnAddHabit.setOnClickListener(v -> showAddActivityDialog());
    }

    private void showAddActivityDialog() {
        selectedImageUri = null; // Reset ảnh

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Tạo thói quen mới");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // --- UI Chọn Ảnh ---
        MaterialButton btnPickImage = new MaterialButton(getContext());
        btnPickImage.setText("Chọn ảnh minh họa");
        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        layout.addView(btnPickImage);

        imgPreviewInDialog = new ImageView(getContext());
        imgPreviewInDialog.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
        imgPreviewInDialog.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgPreviewInDialog.setVisibility(View.GONE); // Ẩn khi chưa chọn
        layout.addView(imgPreviewInDialog);
        // -------------------

        final EditText titleBox = new EditText(getContext());
        titleBox.setHint("Tên hoạt động (vd: Chạy bộ)");
        layout.addView(titleBox);

        final EditText descBox = new EditText(getContext());
        descBox.setHint("Mô tả (vd: 15 phút mỗi ngày)");
        layout.addView(descBox);

        builder.setView(layout);

        builder.setPositiveButton("Tạo", (dialog, which) -> {
            String title = titleBox.getText().toString().trim();
            String desc = descBox.getText().toString().trim();

            if (!title.isEmpty()) {
                Toast.makeText(getContext(), "Đang tạo hoạt động...", Toast.LENGTH_SHORT).show();
                // Gọi ViewModel tạo hoạt động kèm ảnh
                viewModel.createActivity(title, desc, selectedImageUri);
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập tên hoạt động", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void observeViewModel() {
        viewModel.getMyActivities().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                adapter.setList(resource.data);
                if (resource.data.isEmpty()) {
                    // Có thể hiện thông báo "Chưa có hoạt động"
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getUnlockedMood().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("🎉 CHÚC MỪNG! 🎉")
                        .setMessage("Bạn đã mở khóa Mood Premium: " + resource.data.getName())
                        .setPositiveButton("Tuyệt vời", null)
                        .show();
            }
        });
    }
}
