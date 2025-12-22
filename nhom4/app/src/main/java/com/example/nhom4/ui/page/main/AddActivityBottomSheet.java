package com.example.nhom4.ui.page.main;

import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhom4.R;
import com.example.nhom4.ui.viewmodel.MainViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Locale;

public class AddActivityBottomSheet extends BottomSheetDialogFragment {

    private MainViewModel viewModel;
    private Uri selectedImageUri = null;

    // UI Components
    private ImageView imgPreview;
    private View layoutPlaceholder;
    private TextInputEditText edtTitle, edtTargetCount;
    private TextInputEditText edtStart, edtEnd;
    private TextInputLayout layoutTargetInput;
    private RadioGroup radioGroupTarget;

    // Time Variables
    private int startHour = 8, startMinute = 0;
    private int endHour = 8, endMinute = 30;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgPreview.setImageURI(uri);
                    imgPreview.setVisibility(View.VISIBLE);
                    layoutPlaceholder.setVisibility(View.GONE);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bottom_sheet_add_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        initViews(view);
        setupEvents();
    }

    private void initViews(View view) {
        MaterialCardView cardImagePicker = view.findViewById(R.id.card_image_picker);
        imgPreview = view.findViewById(R.id.img_preview);
        layoutPlaceholder = view.findViewById(R.id.layout_placeholder);

        edtTitle = view.findViewById(R.id.edt_title);
        edtTargetCount = view.findViewById(R.id.edt_target_count);
        layoutTargetInput = view.findViewById(R.id.layout_target_input);
        radioGroupTarget = view.findViewById(R.id.radio_group_target);

        edtStart = view.findViewById(R.id.edt_start_time);
        edtEnd = view.findViewById(R.id.edt_end_time);

        MaterialButton btnCreate = view.findViewById(R.id.btn_create);

        cardImagePicker.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnCreate.setOnClickListener(v -> createActivity());

        // Set giá trị mặc định hiển thị
        updateTimeText();
    }

    private void setupEvents() {
        // 1. Xử lý ẩn hiện ô nhập số lần mục tiêu
        radioGroupTarget.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_custom) {
                layoutTargetInput.setVisibility(View.VISIBLE);
            } else {
                layoutTargetInput.setVisibility(View.GONE);
            }
        });

        // 2. Click chọn giờ Bắt đầu
        edtStart.setOnClickListener(v -> showTimePicker(true));

        // 3. Click chọn giờ Kết thúc
        edtEnd.setOnClickListener(v -> showTimePicker(false));
    }

    private void showTimePicker(boolean isStart) {
        int hour = isStart ? startHour : endHour;
        int minute = isStart ? startMinute : endMinute;

        TimePickerDialog dialog = new TimePickerDialog(getContext(), (view, h, m) -> {
            if (isStart) {
                startHour = h;
                startMinute = m;
            } else {
                endHour = h;
                endMinute = m;
            }
            updateTimeText();
        }, hour, minute, true); // true = 24h format
        dialog.show();
    }

    private void updateTimeText() {
        edtStart.setText(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute));
        edtEnd.setText(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
    }

    private void createActivity() {
        String title = edtTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên hoạt động", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Xử lý Mục tiêu ---
        boolean isDaily = radioGroupTarget.getCheckedRadioButtonId() == R.id.rb_daily;
        int target = 1; // Mặc định 1 lần/ngày
        if (!isDaily) {
            String countStr = edtTargetCount.getText().toString().trim();
            if (countStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập số lần mục tiêu", Toast.LENGTH_SHORT).show();
                return;
            }
            target = Integer.parseInt(countStr);
        }

        // --- Xử lý Thời gian ---
        // 1. Tính Scheduled Time (Timestamp) cho Bắt đầu (Lấy ngày hôm nay + giờ đã chọn)
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.HOUR_OF_DAY, startHour);
        calStart.set(Calendar.MINUTE, startMinute);
        calStart.set(Calendar.SECOND, 0);
        Timestamp scheduledTime = new Timestamp(calStart.getTime());

        // 2. Tính Duration (End - Start)
        int startMinutesTotal = startHour * 60 + startMinute;
        int endMinutesTotal = endHour * 60 + endMinute;

        if (endMinutesTotal <= startMinutesTotal) {
            Toast.makeText(getContext(), "Giờ kết thúc phải sau giờ bắt đầu", Toast.LENGTH_SHORT).show();
            return;
        }

        long durationSeconds = (endMinutesTotal - startMinutesTotal) * 60L; // Đổi ra giây

        // Gửi sang ViewModel
        Toast.makeText(getContext(), "Đang tạo...", Toast.LENGTH_SHORT).show();
        viewModel.createActivity(title, isDaily, target, durationSeconds, scheduledTime, selectedImageUri);

        dismiss();
    }
}