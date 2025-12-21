package com.example.nhom4.ui.page.main;

import android.Manifest;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.nhom4.ui.page.friend.FriendsBottomSheet;
import com.example.nhom4.ui.viewmodel.MainViewModel;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;

public class MainFragment extends Fragment {

    // --- UI COMPONENTS ---
    private MaterialButtonToggleGroup toggleGroupContentType;
    private RecyclerView moodRecyclerView, activityRecyclerView;
    private PreviewView cameraPreviewView;

    // Các ImageViews quan trọng
    private ImageView imgHeaderIcon;     // Icon tĩnh nhỏ trên Header (Đích đến của animation)
    private ImageView imgMainDisplay;    // Ảnh to trong Card (Hiển thị GIF hoặc ảnh chụp)
    private ImageView imgAnimationFloat; // View ảo dùng để bay
    private ImageView btnFlash;          // Nút Flash

    // Layout nhập liệu
    private View inputLayoutContent;
    private EditText edtContent;

    private TextView textViewGreeting;

    // --- Bottom Bar ---
    private View btnNavLeft, btnNavRight, containerShutter;
    private ImageView imgSendIcon, iconNavLeft, iconNavRight;

    // --- ADAPTERS ---
    private MoodAdapter moodAdapter;
    private ActivityAdapter activityAdapter;

    // --- STATE ---
    private Mood selectedMood = null;
    private Activity selectedActivity = null;
    private boolean isMoodTabSelected = true;
    private boolean isEditingMode = false; // True khi đang nhập nội dung (ẩn list, hiện ảnh to)

    // --- CAMERA STATE ---
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private CameraSelector currentCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;
    private File currentPhotoFile = null;

    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        initViews(view);
        setupRecyclers();
        setupEventHandlers();
        observeViewModel();

        // Mặc định vào tab Mood
        toggleGroupContentType.check(R.id.btnTabMood);
        switchToMoodTab();
    }

    private void initViews(View view) {
        // Header Area
        textViewGreeting = view.findViewById(R.id.textViewGreeting);
        imgHeaderIcon = view.findViewById(R.id.imgHeaderIcon);

        // Input Area
        inputLayoutContent = view.findViewById(R.id.inputLayoutContent);
        edtContent = view.findViewById(R.id.edtContent);

        // Main Card Area
        toggleGroupContentType = view.findViewById(R.id.toggleGroupContentType);
        moodRecyclerView = view.findViewById(R.id.mood_recycler_view);
        activityRecyclerView = view.findViewById(R.id.activity_recycler_view);
        cameraPreviewView = view.findViewById(R.id.cameraPreviewView);
        imgMainDisplay = view.findViewById(R.id.imgMainDisplay);
        btnFlash = view.findViewById(R.id.btnFlash);

        // Floating View
        imgAnimationFloat = view.findViewById(R.id.imgAnimationFloat);

        // Bottom Bar
        View bottomBar = view.findViewById(R.id.bottom_bar);
        btnNavLeft = bottomBar.findViewById(R.id.btn_nav_left);
        iconNavLeft = (ImageView) btnNavLeft;
        btnNavRight = bottomBar.findViewById(R.id.btn_nav_right);
        iconNavRight = (ImageView) btnNavRight;
        containerShutter = bottomBar.findViewById(R.id.container_shutter);
        imgSendIcon = bottomBar.findViewById(R.id.img_send_icon);
    }

    private void setupRecyclers() {
        // --- 1. MOOD ADAPTER ---
        moodAdapter = new MoodAdapter(new ArrayList<>(), (mood, itemView) -> {
            // Khi chọn Mood -> Thực hiện Animation bay
            performMoodAnimation(mood, itemView);
        });
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        moodRecyclerView.setAdapter(moodAdapter);

        // --- 2. ACTIVITY ADAPTER ---
        activityAdapter = new ActivityAdapter(new ArrayList<>(), activity -> {
            enterCameraModeActivity(activity);
        });
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityRecyclerView.setAdapter(activityAdapter);
    }

    // =================================================================================
    // LOGIC ANIMATION & CHUYỂN ĐỔI UI
    // =================================================================================

    // 1. Hiệu ứng bay Mood từ List lên Header
    private void performMoodAnimation(Mood mood, View startView) {
        this.selectedMood = mood;
        this.isEditingMode = true;

        // --- BƯỚC 1: TÍNH TOÁN TỌA ĐỘ ---
        int[] startLoc = new int[2];
        startView.getLocationOnScreen(startLoc);

        int[] endLoc = new int[2];
        imgHeaderIcon.getLocationOnScreen(endLoc); // Đích đến là icon trên Header

        // Tính tỉ lệ phóng to/thu nhỏ để view bay khớp kích thước view đích
        float scaleX = (float) imgHeaderIcon.getWidth() / startView.getWidth();
        float scaleY = (float) imgHeaderIcon.getHeight() / startView.getHeight();

        // --- BƯỚC 2: SETUP VIEW BAY (ẢNH TĨNH) ---
        Glide.with(this).asBitmap().load(mood.getIconUrl()).into(imgAnimationFloat);

        imgAnimationFloat.setVisibility(View.VISIBLE);
        imgAnimationFloat.setAlpha(1f); // Reset độ trong suốt
        imgAnimationFloat.setX(startLoc[0]);
        imgAnimationFloat.setY(startLoc[1] - getStatusBarHeight());
        imgAnimationFloat.getLayoutParams().width = startView.getWidth();
        imgAnimationFloat.getLayoutParams().height = startView.getHeight();
        imgAnimationFloat.requestLayout();

        // Ẩn danh sách Mood ngay lập tức cho gọn
        moodRecyclerView.animate().alpha(0f).setDuration(200).start();

        // --- BƯỚC 3: BẮT ĐẦU BAY ---
        imgAnimationFloat.animate()
                .translationX(endLoc[0] - startLoc[0])
                .translationY(endLoc[1] - startLoc[1])
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setDuration(500) // Thời gian bay
                .setInterpolator(new PathInterpolator(0.165f, 0.84f, 0.44f, 1.0f)) // Curve mượt
                .withEndAction(() -> {
                    // --- BƯỚC 4: KẾT THÚC BAY (ĐẾN NƠI) ---

                    // A. Xử lý Header: Hiện Icon tĩnh & Đổi tên
                    imgHeaderIcon.setVisibility(View.VISIBLE);
                    Glide.with(this).asBitmap().load(mood.getIconUrl()).into(imgHeaderIcon);

                    textViewGreeting.setText(mood.getName());

                    // B. Xử lý View Bay: Mờ dần rồi ẩn (tạo hiệu ứng hòa nhập vào header)
                    imgAnimationFloat.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction(() -> imgAnimationFloat.setVisibility(View.GONE))
                            .start();

                    // C. Xử lý Card Chính (Vùng xám): Hiện dần & Load Ảnh (Động hoặc Tĩnh)
                    imgMainDisplay.setVisibility(View.VISIBLE);
                    imgMainDisplay.setAlpha(0f); // Bắt đầu từ trong suốt

                    // [FIX] Dùng .load() thường để hỗ trợ cả GIF và PNG/JPG tĩnh
                    Glide.with(this).load(mood.getIconUrl()).into(imgMainDisplay);

                    // Fade In ảnh chính
                    imgMainDisplay.animate().alpha(1f).setDuration(400).start();

                    // D. Ẩn list, Hiện Input, Ẩn Toggle
                    moodRecyclerView.setVisibility(View.GONE);

                    inputLayoutContent.setVisibility(View.VISIBLE);
                    inputLayoutContent.setAlpha(0f);
                    inputLayoutContent.animate().alpha(1f).setDuration(300).start();
                    edtContent.requestFocus();

                    toggleGroupContentType.setVisibility(View.GONE); // [YC] Ẩn Toggle khi Edit

                    // E. Bottom Bar
                    iconNavLeft.setImageResource(R.drawable.outline_arrow_back_ios_24);
                    imgSendIcon.setVisibility(View.VISIBLE);
                })
                .start();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) result = getResources().getDimensionPixelSize(resourceId);
        return result;
    }

    // 2. Vào chế độ Camera khi chọn Activity
    private void enterCameraModeActivity(Activity activity) {
        this.selectedActivity = activity;
        this.isEditingMode = true;

        textViewGreeting.setText("Đang " + activity.getTitle());
        imgHeaderIcon.setVisibility(View.GONE); // Activity ko có icon trên header

        // Ẩn list -> Hiện Camera
        activityRecyclerView.setVisibility(View.GONE);
        cameraPreviewView.setVisibility(View.VISIBLE);
        startCamera();

        // Ẩn Input
        inputLayoutContent.setVisibility(View.GONE);

        // Ẩn Toggle (Tùy chọn, để tập trung chụp ảnh)
        // toggleGroupContentType.setVisibility(View.GONE);

        // Bottom Bar
        iconNavLeft.setImageResource(R.drawable.outline_arrow_back_ios_24);
        iconNavRight.setImageResource(R.drawable.outline_cameraswitch_24);
        imgSendIcon.setVisibility(View.GONE);
    }

    // 3. Reset về Tab Mood (Thoát chế độ Edit)
    private void switchToMoodTab() {
        isMoodTabSelected = true;
        isEditingMode = false;

        // Hiện lại Toggle
        toggleGroupContentType.setVisibility(View.VISIBLE);

        // Reset Header
        textViewGreeting.setText("Heyy, What's up?");
        imgHeaderIcon.setVisibility(View.INVISIBLE); // Ẩn nhưng giữ chỗ

        // Reset Card Content
        moodRecyclerView.setVisibility(View.VISIBLE);
        moodRecyclerView.setAlpha(1f);

        activityRecyclerView.setVisibility(View.GONE);
        imgMainDisplay.setVisibility(View.GONE);
        imgMainDisplay.setImageDrawable(null); // Clear ảnh cũ
        cameraPreviewView.setVisibility(View.GONE);
        btnFlash.setVisibility(View.GONE);

        // Ẩn Input
        inputLayoutContent.setVisibility(View.GONE);
        edtContent.setText("");

        stopCamera();

        // Reset Icons
        iconNavLeft.setImageResource(R.drawable.outline_apps_24);
        iconNavRight.setImageResource(R.drawable.outline_person_add_24);
        imgSendIcon.setVisibility(View.GONE);
    }

    // 4. Reset về Tab Activity
    private void switchToActivityTab() {
        isMoodTabSelected = false;
        isEditingMode = false;

        toggleGroupContentType.setVisibility(View.VISIBLE);

        // Reset Header
        textViewGreeting.setText("Bạn đang làm gì?");
        imgHeaderIcon.setVisibility(View.GONE);

        activityRecyclerView.setVisibility(View.VISIBLE);

        moodRecyclerView.setVisibility(View.GONE);
        imgMainDisplay.setVisibility(View.GONE);
        cameraPreviewView.setVisibility(View.GONE);
        btnFlash.setVisibility(View.GONE);

        // Ẩn Input
        inputLayoutContent.setVisibility(View.GONE);
        edtContent.setText("");

        stopCamera();

        iconNavLeft.setImageResource(R.drawable.outline_apps_24);
        iconNavRight.setImageResource(R.drawable.outline_person_add_24);
        imgSendIcon.setVisibility(View.GONE);
    }

    private void setupEventHandlers() {
        // Toggle Tab
        toggleGroupContentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabMood) switchToMoodTab();
                else switchToActivityTab();
            }
        });

        // Flash
        btnFlash.setOnClickListener(v -> toggleFlash());

        // Nav Left (Back/Cancel)
        btnNavLeft.setOnClickListener(v -> handleNavLeftClick());

        // Nav Right (Friends/Flip)
        btnNavRight.setOnClickListener(v -> {
            if (!isMoodTabSelected && isEditingMode && imgMainDisplay.getVisibility() == View.GONE) {
                flipCamera();
            } else {
                new FriendsBottomSheet().show(getChildFragmentManager(), "FriendsSheet");
            }
        });

        // Shutter
        containerShutter.setOnClickListener(v -> handleShutterClick());
    }

    // =================================================================================
    // LOGIC XỬ LÝ CLICK
    // =================================================================================

    private void handleNavLeftClick() {
        if (isEditingMode) {
            // Nếu đang xem ảnh chụp (Activity) -> Hủy
            if (!isMoodTabSelected && imgMainDisplay.getVisibility() == View.VISIBLE) {
                discardCapturedPhoto();
            }
            // Nếu đang Edit Mood hoặc Camera -> Quay về list
            else {
                if (isMoodTabSelected) switchToMoodTab();
                else switchToActivityTab();
            }
        } else {
            Toast.makeText(getContext(), "Open Menu", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleShutterClick() {
        // CASE 1: Tab Mood -> Đăng bài
        if (isMoodTabSelected) {
            if (selectedMood != null) performPost();
            else Toast.makeText(getContext(), "Chọn cảm xúc trước đã!", Toast.LENGTH_SHORT).show();
            return;
        }

        // CASE 2: Tab Activity
        // 2a. Đã chụp xong -> Đăng bài
        if (imgMainDisplay.getVisibility() == View.VISIBLE) {
            performPost();
            return;
        }
        // 2b. Đang bật Camera -> Chụp ảnh
        if (cameraPreviewView.getVisibility() == View.VISIBLE) {
            takePhoto();
            return;
        }
        Toast.makeText(getContext(), "Chọn hoạt động trước đã!", Toast.LENGTH_SHORT).show();
    }

    private void performPost() {
        String caption = edtContent.getText().toString();
        String imagePath = currentPhotoFile != null ? currentPhotoFile.getAbsolutePath() : null;

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(edtContent.getWindowToken(), 0);

        if (isMoodTabSelected) {
            viewModel.createPost(caption, null, selectedMood, null);
        } else {
            viewModel.createPost(caption, imagePath, null, selectedActivity);
        }
    }

    // =================================================================================
    // CAMERA LOGIC
    // =================================================================================

    private void startCamera() {
        toggleGroupContentType.setVisibility(View.GONE);

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

                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new Size(1020, 1020))
                        .setFlashMode(flashMode)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), currentCameraSelector, preview, imageCapture);

                if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    btnFlash.setVisibility(View.VISIBLE);
                } else {
                    btnFlash.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                Log.e("Camera", "Error", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void toggleFlash() {
        if (imageCapture == null) return;

        switch (flashMode) {
            case ImageCapture.FLASH_MODE_OFF:
                flashMode = ImageCapture.FLASH_MODE_ON;
                btnFlash.setImageResource(R.drawable.baseline_flash_on_24);
                break;
            case ImageCapture.FLASH_MODE_ON:
                flashMode = ImageCapture.FLASH_MODE_AUTO;
                btnFlash.setImageResource(R.drawable.baseline_flash_auto_24);
                break;
            default:
                flashMode = ImageCapture.FLASH_MODE_OFF;
                btnFlash.setImageResource(R.drawable.baseline_flash_off_24);
                break;
        }
        imageCapture.setFlashMode(flashMode);
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        File photoFile = new File(requireContext().getExternalCacheDir(), "post_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        currentPhotoFile = photoFile;
                        showReviewUI(photoFile);
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getContext(), "Lỗi chụp: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showReviewUI(File photoFile) {
        stopCamera();
        cameraPreviewView.setVisibility(View.GONE);
        btnFlash.setVisibility(View.GONE);

        imgMainDisplay.setVisibility(View.VISIBLE);
        Glide.with(this).load(photoFile).into(imgMainDisplay);

        // Hiện ô nhập liệu
        inputLayoutContent.setVisibility(View.VISIBLE);
        edtContent.requestFocus();

        // Ẩn Toggle
        toggleGroupContentType.setVisibility(View.GONE);

        imgSendIcon.setVisibility(View.VISIBLE);
        iconNavLeft.setImageResource(R.drawable.outline_close_24);
        iconNavRight.setVisibility(View.INVISIBLE);
    }

    private void discardCapturedPhoto() {
        currentPhotoFile = null;
        imgMainDisplay.setVisibility(View.GONE);

        inputLayoutContent.setVisibility(View.GONE);
        edtContent.setText("");

        // Hiện lại Toggle
        toggleGroupContentType.setVisibility(View.VISIBLE);

        // Bật lại Camera
        cameraPreviewView.setVisibility(View.VISIBLE);
        startCamera();

        imgSendIcon.setVisibility(View.GONE);
        iconNavLeft.setImageResource(R.drawable.outline_arrow_back_ios_24);
        iconNavRight.setVisibility(View.VISIBLE);
    }

    private void stopCamera() {
        if (cameraProvider != null) cameraProvider.unbindAll();
    }

    private void flipCamera() {
        currentCameraSelector = (currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                ? CameraSelector.DEFAULT_BACK_CAMERA
                : CameraSelector.DEFAULT_FRONT_CAMERA;
        startCamera();
    }

    // --- OBSERVER ---
    private void observeViewModel() {
        viewModel.getUploadStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "Đăng thành công!", Toast.LENGTH_SHORT).show();
                if (isMoodTabSelected) switchToMoodTab();
                else switchToActivityTab();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getMoods().observe(getViewLifecycleOwner(), resource -> {
            if (resource.data != null) moodAdapter.setList(resource.data);
        });
        viewModel.getJoinedActivities().observe(getViewLifecycleOwner(), resource -> {
            if (resource.data != null) activityAdapter.setList(resource.data);
        });
    }
}