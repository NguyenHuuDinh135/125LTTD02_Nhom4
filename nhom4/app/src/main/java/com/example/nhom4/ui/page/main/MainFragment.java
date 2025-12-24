package com.example.nhom4.ui.page.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.ui.adapter.ActivityAdapter;
import com.example.nhom4.ui.adapter.MoodAdapter;
import com.example.nhom4.ui.page.main.AddActivityBottomSheet; // Chú ý package này
import com.example.nhom4.ui.page.friend.FriendsBottomSheet;
import com.example.nhom4.ui.viewmodel.MainViewModel;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.common.util.concurrent.ListenableFuture;
import android.view.Menu;
import android.widget.PopupMenu;
import com.example.nhom4.data.bean.PostFilterType;
import com.example.nhom4.data.bean.User;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainFragment extends Fragment {
    // --- UI Components ---
    private MaterialButtonToggleGroup toggleGroupContentType;
    private RecyclerView moodRecyclerView, activityRecyclerView;
    private PreviewView cameraPreviewView;
    private ImageView imgHeaderIcon, imgMainDisplay, imgAnimationFloat, btnFlash;
    private View btnAddActivity, inputLayoutContent;
    private EditText edtContent;
    private TextView textViewGreeting;
    private View btnNavLeft, btnNavRight, containerShutter;
    private ImageView imgSendIcon, iconNavLeft, iconNavRight;

    // --- State Variables ---
    private MoodAdapter moodAdapter;
    private ActivityAdapter activityAdapter;
    private Mood selectedMood = null;
    private Activity selectedActivity = null;
    private boolean isMoodTabSelected = true;
    private boolean isEditingMode = false;

    // --- Camera & ViewModel ---
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private CameraSelector currentCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;
    private File currentPhotoFile = null;
    private MainViewModel viewModel;
    // === FILTER UI (từ top bar) ===
    // [MỚI] Launcher để hứng kết quả từ FocusActivity
    private ActivityResultLauncher<Intent> focusActivityLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // [MỚI] Khởi tạo Launcher
        // Nhận kết quả từ FocusActivity. Nếu OK -> Mở Camera để Check-in
        focusActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        // Người dùng đã hoàn thành Focus hoặc bấm Finish sớm
                        if (selectedActivity != null) {
                            enterCameraModeActivity(selectedActivity);
                        }
                    } else {
                        // Người dùng hủy (bấm nút X) -> Reset selection
                        selectedActivity = null;
                        Toast.makeText(getContext(), "Đã hủy focus", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Dùng requireActivity() để share ViewModel với BottomSheet
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        initViews(view);
        setupRecyclers();
        setupEventHandlers();
        setupThemeToggle();
        observeViewModel();



    }

    private void setupThemeToggle() {
//        btnNavLeft.setOnClickListener(v -> toggleTheme());

        // Cập nhật icon đúng với theme hiện tại khi fragment được tạo/resume
        updateThemeIcon();
    }
    @Override
    public void onResume() {
        super.onResume();
        updateThemeIcon(); // Đảm bảo icon đúng với theme hiện tại
    }
    private void toggleTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        int newNightMode = (currentNightMode == Configuration.UI_MODE_NIGHT_YES)
                ? AppCompatDelegate.MODE_NIGHT_NO   // Từ Dark → Light
                : AppCompatDelegate.MODE_NIGHT_YES; // Từ Light → Dark

        AppCompatDelegate.setDefaultNightMode(newNightMode);

        // Cập nhật icon ngay lập tức
        updateThemeIcon();
    }

    private void updateThemeIcon() {
        if (iconNavLeft == null) return;

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // Đang Dark mode → hiện icon mặt trời (chuyển sang Light)
            iconNavLeft.setImageResource(R.drawable.outline_light_mode_24);
        } else {
            // Đang Light mode → hiện icon mặt trăng (chuyển sang Dark)
            iconNavLeft.setImageResource(R.drawable.outline_dark_mode_24);
        }
    }
    private void initViews(View view) {
        textViewGreeting = view.findViewById(R.id.textViewGreeting);
        imgHeaderIcon = view.findViewById(R.id.imgHeaderIcon);
        imgAnimationFloat = view.findViewById(R.id.imgAnimationFloat);
        inputLayoutContent = view.findViewById(R.id.inputLayoutContent);
        edtContent = view.findViewById(R.id.edtContent);
        toggleGroupContentType = view.findViewById(R.id.toggleGroupContentType);
        moodRecyclerView = view.findViewById(R.id.mood_recycler_view);
        activityRecyclerView = view.findViewById(R.id.activity_recycler_view);
        cameraPreviewView = view.findViewById(R.id.cameraPreviewView);
        imgMainDisplay = view.findViewById(R.id.imgMainDisplay);
        btnFlash = view.findViewById(R.id.btnFlash);
        btnAddActivity = view.findViewById(R.id.btnAddActivity);
        imgAnimationFloat = view.findViewById(R.id.imgAnimationFloat);

        View bottomBar = view.findViewById(R.id.bottom_bar);
        btnNavLeft = bottomBar.findViewById(R.id.btn_nav_left);
        iconNavLeft = (ImageView) btnNavLeft;
        btnNavRight = bottomBar.findViewById(R.id.btn_nav_right);
        iconNavRight = (ImageView) btnNavRight;
        containerShutter = bottomBar.findViewById(R.id.container_shutter);
        imgSendIcon = bottomBar.findViewById(R.id.img_send_icon);
    }

    private void setupRecyclers() {
        // Mood Adapter (Giữ nguyên)
        moodAdapter = new MoodAdapter(new ArrayList<>(), (mood, itemView) -> performMoodAnimation(mood, itemView));
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        moodRecyclerView.setAdapter(moodAdapter);

        // Activity Adapter - Thêm Logic kiểm tra điều kiện
        activityAdapter = new ActivityAdapter(new ArrayList<>(), activity -> {
            // [LOGIC MỚI] Kiểm tra điều kiện trước khi bắt đầu
            if (isActivityAvailable(activity)) {
                this.selectedActivity = activity;
                openFocusScreen(activity);
            }
        });
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityRecyclerView.setAdapter(activityAdapter);
    }
    // [MỚI] Hàm kiểm tra logic thời gian và số lần thực hiện
    private boolean isActivityAvailable(Activity activity) {
        // 1. Kiểm tra tiến độ (Progress)
        // Nếu đã hoàn thành đủ số lần mục tiêu
        if (activity.getProgress() >= activity.getTarget()) {
            Toast.makeText(getContext(), "Bạn đã hoàn thành mục tiêu hôm nay rồi!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2. Kiểm tra thời gian (Scheduled Time)
        if (activity.getScheduledTime() != null) {
            Calendar currentCal = Calendar.getInstance();
            Calendar scheduledCal = Calendar.getInstance();
            scheduledCal.setTime(activity.getScheduledTime().toDate());

            if (activity.isDaily()) {
                // Nếu là Daily: Chỉ so sánh Giờ và Phút
                int currentMinutes = currentCal.get(Calendar.HOUR_OF_DAY) * 60 + currentCal.get(Calendar.MINUTE);
                int scheduledMinutes = scheduledCal.get(Calendar.HOUR_OF_DAY) * 60 + scheduledCal.get(Calendar.MINUTE);

                if (currentMinutes < scheduledMinutes) {
                    String timeStr = String.format("%02d:%02d", scheduledCal.get(Calendar.HOUR_OF_DAY), scheduledCal.get(Calendar.MINUTE));
                    Toast.makeText(getContext(), "Hoạt động bắt đầu lúc " + timeStr, Toast.LENGTH_SHORT).show();
                    return false;
                }
            } else {
                // Nếu không phải Daily (Sự kiện 1 lần): So sánh toàn bộ thời gian
                if (currentCal.getTimeInMillis() < scheduledCal.getTimeInMillis()) {
                    Toast.makeText(getContext(), "Chưa đến giờ bắt đầu sự kiện!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true; // Đủ điều kiện
    }
    // [MỚI] Hàm mở màn hình Focus
    private void openFocusScreen(Activity activity) {
        Intent intent = new Intent(requireContext(), FocusActivity.class);
        intent.putExtra("title", activity.getTitle());
        intent.putExtra("duration", activity.getDurationSeconds());
        intent.putExtra("imgUrl", activity.getImageUrl());

        // Start FocusActivity và đợi kết quả
        focusActivityLauncher.launch(intent);
    }

    // --- ANIMATION & MODE SWITCHING ---

    private void performMoodAnimation(Mood mood, View startView) {
        this.selectedMood = mood;
        this.isEditingMode = true;

        int[] startLoc = new int[2];
        startView.getLocationOnScreen(startLoc);
        int[] endLoc = new int[2];
        imgHeaderIcon.getLocationOnScreen(endLoc);

        float scaleX = (float) imgHeaderIcon.getWidth() / startView.getWidth();
        float scaleY = (float) imgHeaderIcon.getHeight() / startView.getHeight();

        Glide.with(this).asBitmap().load(mood.getIconUrl()).into(imgAnimationFloat);
        imgAnimationFloat.setVisibility(View.VISIBLE);
        imgAnimationFloat.setX(startLoc[0]);
        imgAnimationFloat.setY(startLoc[1] - getStatusBarHeight());
        imgAnimationFloat.getLayoutParams().width = startView.getWidth();
        imgAnimationFloat.getLayoutParams().height = startView.getHeight();
        imgAnimationFloat.requestLayout();

        moodRecyclerView.animate().alpha(0f).setDuration(200).start();

        imgAnimationFloat.animate()
                .translationX(endLoc[0] - startLoc[0])
                .translationY(endLoc[1] - startLoc[1])
                .scaleX(scaleX).scaleY(scaleY)
                .setDuration(500)
                .setInterpolator(new PathInterpolator(0.165f, 0.84f, 0.44f, 1.0f))
                .withEndAction(() -> {
                    imgHeaderIcon.setVisibility(View.VISIBLE);
                    Glide.with(this).asBitmap().load(mood.getIconUrl()).into(imgHeaderIcon);
                    textViewGreeting.setText(mood.getName());

                    imgAnimationFloat.setVisibility(View.GONE);
                    imgMainDisplay.setVisibility(View.VISIBLE);
                    Glide.with(this).load(mood.getIconUrl()).into(imgMainDisplay);

                    moodRecyclerView.setVisibility(View.GONE);
                    inputLayoutContent.setVisibility(View.VISIBLE);
                    edtContent.requestFocus();
                    toggleGroupContentType.setVisibility(View.GONE);
                    btnAddActivity.setVisibility(View.GONE);

                    iconNavLeft.setImageResource(R.drawable.outline_arrow_back_ios_24);
                    imgSendIcon.setVisibility(View.VISIBLE);
                }).start();
    }

    public int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }

    // Hàm này được gọi KHI FocusActivity trả về OK
    private void enterCameraModeActivity(Activity activity) {
        this.isEditingMode = true;

        textViewGreeting.setText(activity.getTitle());
        imgHeaderIcon.setVisibility(View.GONE);
        activityRecyclerView.setVisibility(View.GONE);
        cameraPreviewView.setVisibility(View.VISIBLE);
        startCamera();
        inputLayoutContent.setVisibility(View.GONE);
        btnAddActivity.setVisibility(View.GONE);

        iconNavLeft.setImageResource(R.drawable.outline_arrow_back_ios_24);
        iconNavRight.setImageResource(R.drawable.outline_cameraswitch_24);
        imgSendIcon.setVisibility(View.GONE);
    }

    private void switchToMoodTab() {
        isMoodTabSelected = true;
        isEditingMode = false;
        toggleGroupContentType.setVisibility(View.VISIBLE);
        btnAddActivity.setVisibility(View.GONE);

        textViewGreeting.setText("Heyy, What's up?");
        imgHeaderIcon.setVisibility(View.INVISIBLE);
        moodRecyclerView.setVisibility(View.VISIBLE);
        moodRecyclerView.setAlpha(1f);
        activityRecyclerView.setVisibility(View.GONE);
        imgMainDisplay.setVisibility(View.GONE);
        cameraPreviewView.setVisibility(View.GONE);
        btnFlash.setVisibility(View.GONE);
        inputLayoutContent.setVisibility(View.GONE);
        edtContent.setText("");
        stopCamera();

        iconNavLeft.setImageResource(R.drawable.outline_apps_24);
        iconNavRight.setImageResource(R.drawable.outline_person_add_24);
        imgSendIcon.setVisibility(View.GONE);
    }

    private void switchToActivityTab() {
        isMoodTabSelected = false;
        isEditingMode = false;
        toggleGroupContentType.setVisibility(View.VISIBLE);
        btnAddActivity.setVisibility(View.VISIBLE);

        textViewGreeting.setText("Bạn đang làm gì?");
        imgHeaderIcon.setVisibility(View.GONE);
        activityRecyclerView.setVisibility(View.VISIBLE);
        moodRecyclerView.setVisibility(View.GONE);
        imgMainDisplay.setVisibility(View.GONE);
        cameraPreviewView.setVisibility(View.GONE);
        btnFlash.setVisibility(View.GONE);
        inputLayoutContent.setVisibility(View.GONE);
        edtContent.setText("");
        stopCamera();

        iconNavLeft.setImageResource(R.drawable.outline_apps_24);
        iconNavRight.setImageResource(R.drawable.outline_person_add_24);
        imgSendIcon.setVisibility(View.GONE);
    }

    private void setupEventHandlers() {
        toggleGroupContentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabMood) switchToMoodTab();
                else switchToActivityTab();
            }
        });

        btnFlash.setOnClickListener(v -> toggleFlash());

        btnNavLeft.setOnClickListener(v -> handleNavLeftClick());

        btnNavRight.setOnClickListener(v -> {
            if (!isMoodTabSelected && isEditingMode && imgMainDisplay.getVisibility() == View.GONE) {
                flipCamera(); // Chỉ flip camera ở chế độ Activity Check-in khi đang preview
            } else {
                new FriendsBottomSheet().show(getChildFragmentManager(), "FriendsSheet");
            }
        });

        // Bật BottomSheet thêm Activity
        btnAddActivity.setOnClickListener(v -> {
            new AddActivityBottomSheet().show(getChildFragmentManager(), "AddActivitySheet");
        });

        containerShutter.setOnClickListener(v -> handleShutterClick());
    }

    // --- LOGIC SHUTTER ---

    // 2. Sửa hàm handleNavLeftClick: Điều hướng logic
    private void handleNavLeftClick() {
        if (isEditingMode) {
            // --- LOGIC BACK BUTTON (Khi đang ở Camera/Preview) ---
            if (!isMoodTabSelected && imgMainDisplay.getVisibility() == View.VISIBLE) {
                // Đang xem lại ảnh vừa chụp -> Hủy ảnh
                discardCapturedPhoto();
            } else {
                // Quay lại danh sách
                if (isMoodTabSelected) switchToMoodTab();
                else switchToActivityTab();
            }
        } else {
            // --- LOGIC ĐỔI THEME (Khi đang ở màn hình chính) ---
            toggleTheme();
        }
    }

    private void handleShutterClick() {
        if (isMoodTabSelected) {
            if (selectedMood != null) performPost();
            else Toast.makeText(getContext(), "Chọn cảm xúc trước đã!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Activity Tab Logic
        if (imgMainDisplay.getVisibility() == View.VISIBLE) {
            // Đã chụp ảnh xong và đang xem lại -> Gửi bài (Check-in)
            performPost();
            return;
        }
        if (cameraPreviewView.getVisibility() == View.VISIBLE) {
            // Đang mở Camera -> Chụp ảnh
            takePhoto();
            return;
        }
        // Nếu chưa chọn activity (chưa vào mode camera)
        Toast.makeText(getContext(), "Chọn hoạt động để bắt đầu!", Toast.LENGTH_SHORT).show();
    }

    private void performPost() {
        String content = edtContent.getText().toString();

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }

        if (isMoodTabSelected) {
            // Đăng Mood
            viewModel.createPost(content, selectedMood);
        } else {
            // Check-in Activity (Log)
            String imagePath = currentPhotoFile != null ? currentPhotoFile.getAbsolutePath() : null;
            if (selectedActivity != null) {
                viewModel.checkInActivity(selectedActivity, imagePath, content);
            }
        }
    }

    // --- CAMERA UTILS ---
    private void startCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1001);
            return;
        }
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().setTargetResolution(new Size(1020, 1020)).build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), currentCameraSelector, preview, imageCapture);

                // Chỉ hiện nút flash nếu là cam sau
                boolean isBackCamera = currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA;
                btnFlash.setVisibility(isBackCamera ? View.VISIBLE : View.GONE);

            } catch (Exception e) { Log.e("Camera", "Error", e); }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void toggleFlash() {
        if (imageCapture == null) return;
        flashMode = (flashMode == ImageCapture.FLASH_MODE_OFF) ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF;
        imageCapture.setFlashMode(flashMode);
        btnFlash.setImageResource(flashMode == ImageCapture.FLASH_MODE_ON ? R.drawable.baseline_flash_on_24 : R.drawable.baseline_flash_off_24);
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        File photoFile = new File(requireContext().getExternalCacheDir(), "checkin_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                currentPhotoFile = photoFile;
                showReviewUI(photoFile);
            }
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(getContext(), "Lỗi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReviewUI(File photoFile) {
        stopCamera();
        cameraPreviewView.setVisibility(View.GONE);
        btnFlash.setVisibility(View.GONE);
        imgMainDisplay.setVisibility(View.VISIBLE);
        Glide.with(this).load(photoFile).into(imgMainDisplay);

        inputLayoutContent.setVisibility(View.VISIBLE);
        edtContent.requestFocus();

        imgSendIcon.setVisibility(View.VISIBLE);
        iconNavLeft.setImageResource(R.drawable.outline_close_24); // Icon hủy ảnh
        iconNavRight.setVisibility(View.INVISIBLE);
    }

    private void discardCapturedPhoto() {
        currentPhotoFile = null;
        imgMainDisplay.setVisibility(View.GONE);
        inputLayoutContent.setVisibility(View.GONE);
        edtContent.setText("");

        cameraPreviewView.setVisibility(View.VISIBLE);
        startCamera(); // Mở lại camera

        imgSendIcon.setVisibility(View.GONE);
        iconNavLeft.setImageResource(R.drawable.outline_arrow_back_ios_24); // Icon quay lại danh sách
        iconNavRight.setVisibility(View.VISIBLE);
    }

    private void stopCamera() { if (cameraProvider != null) cameraProvider.unbindAll(); }

    private void flipCamera() {
        currentCameraSelector = (currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) ? CameraSelector.DEFAULT_BACK_CAMERA : CameraSelector.DEFAULT_FRONT_CAMERA;
        startCamera();
    }

    private void observeViewModel() {
        viewModel.getUploadStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "Thành công!", Toast.LENGTH_SHORT).show();
                if (isMoodTabSelected) switchToMoodTab();
                else switchToActivityTab();
            }

        });

        viewModel.getMoods().observe(getViewLifecycleOwner(), res -> {
            if (res.data != null) moodAdapter.setList(res.data);
        });

        viewModel.getJoinedActivities().observe(getViewLifecycleOwner(), res -> {
            if (res.data != null) {
                // LỌC: Ẩn activity đã hoàn thành mục tiêu hôm nay
                List<Activity> filtered = new ArrayList<>();
                for (Activity activity : res.data) {
                    if (activity.getProgress() < activity.getTarget()) {
                        filtered.add(activity);
                    }
                }
                activityAdapter.setList(filtered);

                // Nếu không còn activity nào khả dụng → có thể hiển thị thông báo (tùy chọn)
                if (filtered.isEmpty()) {
                    Toast.makeText(getContext(), "Hôm nay bạn đã hoàn thành tất cả hoạt động!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}