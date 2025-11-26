package com.example.nhom4.ui.page.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.ui.adapter.ActivityAdapter;
import com.example.nhom4.ui.adapter.MoodAdapter;
import com.example.nhom4.ui.page.friend.FriendsBottomSheet;
import com.example.nhom4.ui.viewmodel.MainViewModel;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    // --- UI COMPONENTS ---
    private SwitchMaterial modeSwitch;
    private MaterialButtonToggleGroup toggleGroupContentType;
    private RecyclerView moodRecyclerView, activityRecyclerView;
    private PreviewView cameraPreviewView;
    private ImageView imgMoodPreview, imgCapturedDisplay;
    private EditText edtCaptionOverlay;

    // --- Bottom Bar ---
    private View btnNavLeft, btnNavRight, containerShutter;
    private ImageView imgSendIcon, iconNavLeft, iconNavRight;

    // [ĐÃ XÓA] ViewPager2 và PostAdapter vì MainFragment chỉ lo Camera
    // private ViewPager2 viewPagerPosts;
    // private PostAdapter postAdapter;

    private MoodAdapter moodAdapter;
    private ActivityAdapter activityAdapter;

    // --- STATE ---
    private Mood selectedMood = null;
    private Activity selectedActivity = null;
    private boolean isMoodTabSelected = true;

    // Camera State
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private CameraSelector currentCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
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
        // [ĐÃ XÓA] setupPostViewPager(); -> Không gọi hàm này nữa
        setupEventHandlers();
        observeViewModel();

        toggleCameraMode(false);
        toggleGroupContentType.check(R.id.btnTabMood);
    }

    private void initViews(View view) {
        modeSwitch = view.findViewById(R.id.modeSwitch);
        toggleGroupContentType = view.findViewById(R.id.toggleGroupContentType);
        moodRecyclerView = view.findViewById(R.id.mood_recycler_view);
        activityRecyclerView = view.findViewById(R.id.activity_recycler_view);

        cameraPreviewView = view.findViewById(R.id.cameraPreviewView);
        imgMoodPreview = view.findViewById(R.id.imgMoodPreview);
        imgCapturedDisplay = view.findViewById(R.id.imgCapturedDisplay);
        edtCaptionOverlay = view.findViewById(R.id.edtCaptionOverlay);

        // [ĐÃ XÓA] viewPagerPosts = view.findViewById(R.id.viewPagerPosts);

        // Init Bottom Bar Views
        View bottomBar = view.findViewById(R.id.bottom_bar);
        btnNavLeft = bottomBar.findViewById(R.id.btn_nav_left);
        iconNavLeft = (ImageView) btnNavLeft;

        btnNavRight = bottomBar.findViewById(R.id.btn_nav_right);
        iconNavRight = (ImageView) btnNavRight;

        containerShutter = bottomBar.findViewById(R.id.container_shutter);
        imgSendIcon = bottomBar.findViewById(R.id.img_send_icon);
    }

    private void setupRecyclers() {
        moodAdapter = new MoodAdapter(new ArrayList<>(), mood -> {
            this.selectedMood = mood;
            if (isMoodTabSelected && !modeSwitch.isChecked()) updatePreviewImage();
        });
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        moodRecyclerView.setAdapter(moodAdapter);

        List<Activity> activities = new ArrayList<>();
        activities.add(new Activity("Chạy bộ"));
        activities.add(new Activity("Hẹn hò"));
        activityAdapter = new ActivityAdapter(activities, activity -> {
            this.selectedActivity = activity;
            modeSwitch.setChecked(true);
        });
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        activityRecyclerView.setAdapter(activityAdapter);
    }

    // [ĐÃ XÓA] Hàm setupPostViewPager()

    private void setupEventHandlers() {
        toggleGroupContentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabMood) switchToMoodTab();
                else switchToActivityTab();
            }
        });

        modeSwitch.setOnCheckedChangeListener((v, isChecked) -> toggleCameraMode(isChecked));

        btnNavLeft.setOnClickListener(v -> {
            if (imgCapturedDisplay.getVisibility() == View.VISIBLE) {
                discardCapturedPhoto();
            } else {
                Toast.makeText(getContext(), "Open Menu", Toast.LENGTH_SHORT).show();
            }
        });

        btnNavRight.setOnClickListener(v -> {
            if (modeSwitch.isChecked() && imgCapturedDisplay.getVisibility() == View.GONE) {
                flipCamera();
            } else {
                showFriendsBottomSheet();
            }
        });

        containerShutter.setOnClickListener(v -> {
            if (imgCapturedDisplay.getVisibility() == View.VISIBLE) {
                performPost();
            } else {
                handleShutterClick();
            }
        });
    }

    private void toggleCameraMode(boolean turnOn) {
        if (turnOn) {
            imgMoodPreview.setVisibility(View.GONE);
            activityRecyclerView.setVisibility(View.GONE);
            cameraPreviewView.setVisibility(View.VISIBLE);
            imgCapturedDisplay.setVisibility(View.GONE);
            edtCaptionOverlay.setVisibility(View.GONE);

            iconNavLeft.setImageResource(R.drawable.outline_apps_24);
            iconNavRight.setImageResource(R.drawable.outline_cameraswitch_24);
            iconNavRight.setVisibility(View.VISIBLE);
            imgSendIcon.setVisibility(View.GONE);

            startCamera();
        } else {
            cameraPreviewView.setVisibility(View.GONE);
            if (cameraProvider != null) cameraProvider.unbindAll();

            imgCapturedDisplay.setVisibility(View.GONE);
            edtCaptionOverlay.setVisibility(View.GONE);

            iconNavLeft.setImageResource(R.drawable.outline_apps_24);
            iconNavRight.setImageResource(R.drawable.outline_person_add_24);
            iconNavRight.setVisibility(View.VISIBLE);
            imgSendIcon.setVisibility(View.GONE);

            if (isMoodTabSelected) {
                imgMoodPreview.setVisibility(View.VISIBLE);
                updatePreviewImage();
            } else {
                activityRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void flipCamera() {
        if (currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            currentCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        } else {
            currentCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        }
        startCamera();
    }

    private void handleShutterClick() {
        if (isMoodTabSelected && selectedMood == null) {
            Toast.makeText(getContext(), "Chọn cảm xúc đã nào!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (modeSwitch.isChecked()) {
            takePhoto();
        } else {
            Toast.makeText(getContext(), "Bật camera để chụp ảnh nhé!", Toast.LENGTH_SHORT).show();
            modeSwitch.setChecked(true);
        }
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
        cameraProvider.unbindAll();
        cameraPreviewView.setVisibility(View.GONE);
        imgCapturedDisplay.setVisibility(View.VISIBLE);
        Glide.with(this).load(photoFile).into(imgCapturedDisplay);

        edtCaptionOverlay.setVisibility(View.VISIBLE);
        edtCaptionOverlay.setText("");
        edtCaptionOverlay.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        imm.showSoftInput(edtCaptionOverlay, InputMethodManager.SHOW_IMPLICIT);

        iconNavLeft.setImageResource(R.drawable.outline_close_24);
        iconNavRight.setVisibility(View.INVISIBLE);
        imgSendIcon.setVisibility(View.VISIBLE);
    }

    private void discardCapturedPhoto() {
        currentPhotoFile = null;
        edtCaptionOverlay.setText("");
        toggleCameraMode(true);
    }

    private void performPost() {
        String caption = edtCaptionOverlay.getText().toString();
        String activityTitle = (!isMoodTabSelected && selectedActivity != null) ? selectedActivity.getTitle() : null;
        String imagePath = currentPhotoFile != null ? currentPhotoFile.getAbsolutePath() : null;
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtCaptionOverlay.getWindowToken(), 0);
        viewModel.createPost(caption, imagePath, isMoodTabSelected ? selectedMood : null, activityTitle);
    }

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
                imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), currentCameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e("Camera", "Error", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void observeViewModel() {
        viewModel.getUploadStatus().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Đăng thành công!", Toast.LENGTH_SHORT).show();
                    discardCapturedPhoto();
                    modeSwitch.setChecked(false);
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // [ĐÃ XÓA] Observer của viewModel.getPosts() vì MainFragment không hiển thị list post
        /*
        viewModel.getPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.data != null && postAdapter != null) postAdapter.setPostList(resource.data);
        });
        */

        viewModel.getMoods().observe(getViewLifecycleOwner(), resource -> {
            if (resource.data != null) moodAdapter.setList(resource.data);
        });
    }

    private void switchToMoodTab() {
        isMoodTabSelected = true;
        moodRecyclerView.setVisibility(View.VISIBLE);
        activityRecyclerView.setVisibility(View.GONE);
        if (!modeSwitch.isChecked()) updatePreviewImage();
    }

    private void switchToActivityTab() {
        isMoodTabSelected = false;
        moodRecyclerView.setVisibility(View.GONE);
        activityRecyclerView.setVisibility(View.VISIBLE);
        imgMoodPreview.setVisibility(View.GONE);
    }

    private void updatePreviewImage() {
        if (selectedMood != null) Glide.with(this).load(selectedMood.getIconUrl()).into(imgMoodPreview);
        else imgMoodPreview.setImageResource(R.drawable.ic_launcher_foreground);
    }

    private void showFriendsBottomSheet() {
        new FriendsBottomSheet().show(getChildFragmentManager(), "FriendsSheet");
    }
}
