package com.example.nhom4.ui.page.calendar;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nhom4.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Calendar;
import java.util.Locale;

public class AddHabitBottomSheet extends BottomSheetDialogFragment {

    private ImageView imgPreview;
    private EditText edtName, edtDesc, edtStartTime, edtEndTime;
    private MaterialSwitch switchRepeat;
    private Button btnCreate;
    private Uri selectedImageUri = null;

    // Interface để gửi dữ liệu về Activity/Fragment cha
    public interface OnHabitCreatedListener {
        void onHabitCreated(String name, String desc, String timeRange, boolean isRepeat, Uri imageUri);
    }

    private OnHabitCreatedListener listener;

    public void setListener(OnHabitCreatedListener listener) {
        this.listener = listener;
    }

    // Launcher chọn ảnh từ Gallery
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgPreview.setImageURI(uri);
                }
            }
    );

    // Launcher chụp ảnh (cần cấu hình FileProvider phức tạp hơn, ở đây demo mở Gallery trước)
    // Nếu muốn chụp ảnh, bạn cần thêm logic CameraIntent

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_add_habit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        imgPreview = view.findViewById(R.id.img_habit_preview);
        View btnPickImage = view.findViewById(R.id.btn_pick_image);
        edtName = view.findViewById(R.id.edt_habit_name);
        edtDesc = view.findViewById(R.id.edt_habit_desc);
        edtStartTime = view.findViewById(R.id.edt_time_start);
        edtEndTime = view.findViewById(R.id.edt_time_end);
        switchRepeat = view.findViewById(R.id.switch_repeat);
        btnCreate = view.findViewById(R.id.btn_create_activity);

        // Sự kiện chọn ảnh
        btnPickImage.setOnClickListener(v -> showImageSourceDialog());

        // Sự kiện chọn giờ
        edtStartTime.setOnClickListener(v -> showTimePicker(edtStartTime));
        edtEndTime.setOnClickListener(v -> showTimePicker(edtEndTime));

        // Sự kiện nút Tạo
        btnCreate.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String desc = edtDesc.getText().toString();
            String timeRange = edtStartTime.getText().toString() + " - " + edtEndTime.getText().toString();
            boolean isRepeat = switchRepeat.isChecked();

            if (name.isEmpty()) {
                edtName.setError("Vui lòng nhập tên");
                return;
            }

            if (listener != null) {
                listener.onHabitCreated(name, desc, timeRange, isRepeat, selectedImageUri);
            }
            dismiss(); // Đóng BottomSheet
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Chọn từ thư viện", "Chụp ảnh mới"};
        new AlertDialog.Builder(getContext())
                .setTitle("Thêm ảnh minh họa")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Mở thư viện
                        pickImageLauncher.launch("image/*");
                    } else {
                        // Mở Camera
                        Toast.makeText(getContext(), "Tính năng chụp ảnh đang phát triển", Toast.LENGTH_SHORT).show();
                        // Todo: Implement Camera Intent here
                    }
                })
                .show();
    }

    private void showTimePicker(EditText targetView) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    targetView.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }
}
