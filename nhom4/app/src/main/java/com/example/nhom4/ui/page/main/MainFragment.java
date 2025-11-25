package com.example.nhom4.ui.page.main;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.ui.adapter.ActivityAdapter;
import com.example.nhom4.ui.adapter.MoodAdapter;
import com.example.nhom4.ui.adapter.PostAdapter;
import com.example.nhom4.ui.page.friend.FriendsBottomSheet;
import com.example.nhom4.ui.viewmodel.MainViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    // --- UI COMPONENTS ---
    private SwitchMaterial modeSwitch; // Nút bật/tắt Camera
    private MaterialButtonToggleGroup toggleGroupContentType; // Chọn Mood hoặc Activity

    private RecyclerView moodRecyclerView;
    private RecyclerView activityRecyclerView;

    private PreviewView cameraPreviewView;
    private ImageView imgMoodPreview; // Ảnh preview khi KHÔNG bật camera
    private ImageView btnCloseCamera;
    private ImageView btnNavRight;
    private View bottomBarLayout;
    private View btnBottomAction; // Nút Shutter ở Bottom Bar

    // --- POST LIST COMPONENTS ---
    private ViewPager2 viewPagerPosts;
    private PostAdapter postAdapter;

    // --- ADAPTERS & DATA ---
    private MoodAdapter moodAdapter;
    private ActivityAdapter activityAdapter;

    // Trạng thái UI hiện tại
    private Mood selectedMood = null;
    private Activity selectedActivity = null;
    private boolean isMoodTabSelected = true; // Mặc định là Tab Mood

    // --- VIEWMODEL & CAMERA ---
    private MainViewModel viewModel;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Init ViewModel (Share với Activity để dữ liệu tồn tại lâu hơn)
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // 2. Init Views & Adapters
        initViews(view);
        setupRecyclers();
        setupPostViewPager();

        // 3. Setup Events
        setupEventHandlers();

        // 4. Observe Data from ViewModel
        observeViewModel();

        // 5. Start Camera if permission granted (or wait for toggle)
        // Mặc định ban đầu tắt camera
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
        btnCloseCamera = view.findViewById(R.id.btnCloseCamera);

        viewPagerPosts = view.findViewById(R.id.viewPagerPosts);
        bottomBarLayout = view.findViewById(R.id.bottom_bar);

        if (bottomBarLayout != null) {
            btnBottomAction = bottomBarLayout.findViewById(R.id.btn_shutter);
            btnNavRight = bottomBarLayout.findViewById(R.id.btn_nav_right);
        }
    }

    private void setupRecyclers() {
        // 1. Mood Adapter (Ngang)
        moodAdapter = new MoodAdapter(new ArrayList<>(), mood -> {
            this.selectedMood = mood;
            if (isMoodTabSelected && !modeSwitch.isChecked()) updatePreviewImage();
        });
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        moodRecyclerView.setAdapter(moodAdapter);

        // 2. Activity Adapter (Dọc)
        List<Activity> activities = new ArrayList<>();
        activities.add(new Activity("Chạy bộ"));
        activities.add(new Activity("Đọc sách"));
        activities.add(new Activity("Cafe"));
        activities.add(new Activity("Làm việc"));
        activities.add(new Activity("Hẹn hò"));

        activityAdapter = new ActivityAdapter(activities, activity -> {
            this.selectedActivity = activity;
            Toast.makeText(getContext(), "Đã chọn: " + activity.getTitle(), Toast.LENGTH_SHORT).show();
            modeSwitch.setChecked(true); // Tự động bật camera khi chọn hoạt động
        });
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        activityRecyclerView.setAdapter(activityAdapter);
    }

    private void setupPostViewPager() {
        if (viewPagerPosts != null) {
            postAdapter = new PostAdapter(requireActivity());
            viewPagerPosts.setAdapter(postAdapter);
            viewPagerPosts.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        }
    }

    private void setupEventHandlers() {
        // Toggle Mood/Activity
        toggleGroupContentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabMood) switchToMoodTab();
                else if (checkedId == R.id.btnTabActivity) switchToActivityTab();
            }
        });

        // Camera Switch
        modeSwitch.setOnCheckedChangeListener((v, isChecked) -> toggleCameraMode(isChecked));

        // Buttons Actions
        if (btnBottomAction != null) {
            btnBottomAction.setOnClickListener(v -> handleMainAction());
        }
        if (btnNavRight != null) {
            btnNavRight.setOnClickListener(v -> showFriendsBottomSheet());
        }
        btnCloseCamera.setOnClickListener(v -> modeSwitch.setChecked(false));
    }

    private void observeViewModel() {
        // 1. Listen for Posts
        viewModel.getPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // FIX: Check for null before setting list to prevent Crash
                if (postAdapter != null) {
                    postAdapter.setPostList(resource.data);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Lắng nghe danh sách Mood
        viewModel.getMoods().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                moodAdapter.setList(resource.data);
            }
        });

        // 3. Lắng nghe trạng thái Upload bài viết
        viewModel.getUploadStatus().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    Toast.makeText(getContext(), "Đang đăng bài...", Toast.LENGTH_SHORT).show();
                    break;
                case SUCCESS:
                    Toast.makeText(getContext(), "Đăng thành công!", Toast.LENGTH_SHORT).show();
                    resetUIAfterPost();
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    // --- UI LOGIC ---

    private void switchToMoodTab() {
        isMoodTabSelected = true;
        moodRecyclerView.setVisibility(View.VISIBLE);
        activityRecyclerView.setVisibility(View.GONE);
        if (!modeSwitch.isChecked()) {
            imgMoodPreview.setVisibility(View.VISIBLE);
            updatePreviewImage();
        }
    }

    private void switchToActivityTab() {
        isMoodTabSelected = false;
        moodRecyclerView.setVisibility(View.GONE);
        imgMoodPreview.setVisibility(View.GONE);
        if (!modeSwitch.isChecked()) {
            activityRecyclerView.setVisibility(View.VISIBLE);
        } else {
            activityRecyclerView.setVisibility(View.GONE);
        }
    }

    private void toggleCameraMode(boolean turnOn) {
        if (turnOn) {
            // Bật Camera: Ẩn hết ảnh tĩnh và list activity
            imgMoodPreview.setVisibility(View.GONE);
            activityRecyclerView.setVisibility(View.GONE);

            cameraPreviewView.setVisibility(View.VISIBLE);
            btnCloseCamera.setVisibility(View.VISIBLE);
            startCamera();
        } else {
            // Tắt Camera:
            cameraPreviewView.setVisibility(View.GONE);
            btnCloseCamera.setVisibility(View.GONE);
            if (cameraProvider != null) cameraProvider.unbindAll();

            if (isMoodTabSelected) {
                imgMoodPreview.setVisibility(View.VISIBLE);
                updatePreviewImage();
            } else {
                activityRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updatePreviewImage() {
        if (selectedMood != null) {
            Glide.with(this).load(selectedMood.getIconUrl()).into(imgMoodPreview);
        } else {
            imgMoodPreview.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    // --- CAMERA & ACTION ---

    private void handleMainAction() {
        if (isMoodTabSelected && selectedMood == null) {
            Toast.makeText(getContext(), "Chọn cảm xúc đã nào!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isMoodTabSelected && selectedActivity == null) {
            Toast.makeText(getContext(), "Chọn hoạt động đã nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (modeSwitch.isChecked()) {
            takePhotoAndProcess();
        } else {
            if (isMoodTabSelected) {
                // Đăng Mood không ảnh
                showPostDialog(null, selectedMood.getName(), selectedMood.getIconUrl());
            } else {
                Toast.makeText(getContext(), "Bật camera để chụp ảnh hoạt động!", Toast.LENGTH_SHORT).show();
                modeSwitch.setChecked(true);
            }
        }
    }

    private void takePhotoAndProcess() {
        if (imageCapture == null) return;

        File photoFile = new File(requireContext().getExternalCacheDir(), "post_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        String title = isMoodTabSelected ? selectedMood.getName() : selectedActivity.getTitle();
                        showPostDialog(photoFile.getAbsolutePath(), title, null);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getContext(), "Lỗi chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPostDialog(String imagePath, String title, String iconUrl) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_post);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);
        }

        ImageView imgPreview = dialog.findViewById(R.id.dlg_img_preview);
        TextView txtTitle = dialog.findViewById(R.id.dlg_txt_title);
        EditText edtContent = dialog.findViewById(R.id.dlg_edt_content);
        MaterialButton btnSend = dialog.findViewById(R.id.dlg_btn_send);

        txtTitle.setText(title);
        if (imagePath != null) {
            imgPreview.setImageURI(Uri.fromFile(new File(imagePath)));
        } else if (iconUrl != null) {
            Glide.with(this).load(iconUrl).into(imgPreview);
        }

        btnSend.setOnClickListener(v -> {
            String caption = edtContent.getText().toString();
            String activityTitle = (!isMoodTabSelected && selectedActivity != null) ? selectedActivity.getTitle() : null;

            // Gọi ViewModel để xử lý đăng bài thay vì tự làm trong Fragment
            viewModel.createPost(caption, imagePath, isMoodTabSelected ? selectedMood : null, activityTitle);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void resetUIAfterPost() {
        modeSwitch.setChecked(false);
        toggleGroupContentType.check(R.id.btnTabMood);
        if (viewPagerPosts != null) viewPagerPosts.setCurrentItem(0, true);
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
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);
            } catch (Exception e) { Log.e("Camera", "Error", e); }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    // Gọi FriendsBottomSheet Fragment thay vì tự build dialog
    private void showFriendsBottomSheet() {
        FriendsBottomSheet bottomSheet = new FriendsBottomSheet();
        bottomSheet.show(getChildFragmentManager(), "FriendsSheet");
    }
}
