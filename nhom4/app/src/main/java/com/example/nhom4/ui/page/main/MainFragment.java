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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.nhom4.R;
import com.example.nhom4.data.bean.Activity;
import com.example.nhom4.data.bean.Mood;
import com.example.nhom4.data.bean.Post;
import com.example.nhom4.ui.adapter.ActivityAdapter;
import com.example.nhom4.ui.adapter.MoodAdapter;
import com.example.nhom4.ui.adapter.PostAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // --- DATA ---
    private MoodAdapter moodAdapter;
    private ActivityAdapter activityAdapter;
    private List<Mood> moodList = new ArrayList<>();
    private List<Activity> activityList = new ArrayList<>();

    // Trạng thái hiện tại
    private Mood selectedMood = null;
    private Activity selectedActivity = null;
    private boolean isMoodTabSelected = true; // Mặc định là Tab Mood

    // --- FIREBASE & CAMERA ---
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    // Listener để hủy lắng nghe khi thoát
    private ListenerRegistration postListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        initViews(view);
        setupRecyclers();
        setupPostViewPager(); // Cấu hình hiển thị bài viết

        // 1. Logic Toggle (Chuyển Tab Mood <-> Activity)
        toggleGroupContentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnTabMood) {
                    switchToMoodTab();
                } else if (checkedId == R.id.btnTabActivity) {
                    switchToActivityTab();
                }
            }
        });

        // 2. Logic Switch Camera (Bật/Tắt)
        modeSwitch.setOnCheckedChangeListener((v, isChecked) -> toggleCameraMode(isChecked));

        // 3. Logic Nút Chụp/Gửi (Bottom Bar)
        if (btnBottomAction != null) {
            btnBottomAction.setOnClickListener(v -> handleMainAction());
        }
        if (btnNavRight != null) {
            btnNavRight.setOnClickListener(v -> showFriendsBottomSheet());
        }
        // Nút đóng camera nhanh
        btnCloseCamera.setOnClickListener(v -> modeSwitch.setChecked(false));

        fetchMoodsFromFirestore();

        // --- BẮT ĐẦU LẮNG NGHE BÀI VIẾT (REAL-TIME) ---
        loadPostsFromFirebase();

        // Mặc định ban đầu
        toggleCameraMode(false);
        toggleGroupContentType.check(R.id.btnTabMood);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ngắt lắng nghe Firestore khi thoát màn hình này để tránh rò rỉ bộ nhớ
        if (postListener != null) {
            postListener.remove();
        }
    }

    private void initViews(View view) {
        modeSwitch = view.findViewById(R.id.modeSwitch);
        toggleGroupContentType = view.findViewById(R.id.toggleGroupContentType);

        moodRecyclerView = view.findViewById(R.id.mood_recycler_view);
        activityRecyclerView = view.findViewById(R.id.activity_recycler_view);
        cameraPreviewView = view.findViewById(R.id.cameraPreviewView);
        imgMoodPreview = view.findViewById(R.id.imgMoodPreview);
        btnCloseCamera = view.findViewById(R.id.btnCloseCamera);

        // ViewPager hiển thị bài viết
        viewPagerPosts = view.findViewById(R.id.viewPagerPosts);

        bottomBarLayout = view.findViewById(R.id.bottom_bar);
        if (bottomBarLayout != null) {
            btnBottomAction = bottomBarLayout.findViewById(R.id.btn_shutter);
            // Ánh xạ nút phải
            btnNavRight = bottomBarLayout.findViewById(R.id.btn_nav_right);
        }
    }

    private void setupPostViewPager() {
        if (viewPagerPosts != null) {
            postAdapter = new PostAdapter(requireActivity());
            viewPagerPosts.setAdapter(postAdapter);
            viewPagerPosts.setOrientation(ViewPager2.ORIENTATION_VERTICAL); // Lướt dọc
        }
    }

    // --- HÀM LOAD POST TỪ FIREBASE (REAL-TIME) ---
    private void loadPostsFromFirebase() {
        // Sử dụng addSnapshotListener thay vì get() để cập nhật thời gian thực
        postListener = db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING) // Bài mới nhất lên đầu
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("MainFragment", "Lỗi lắng nghe: ", e);
                        return;
                    }

                    if (snapshots != null) {
                        List<Post> newPostList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                newPostList.add(post);
                            }
                        }

                        // Cập nhật Adapter ngay lập tức
                        if (postAdapter != null) {
                            postAdapter.setPostList(newPostList);
                        }
                    }
                });
    }

    // --- LOGIC CHUYỂN TAB ---
    private void switchToMoodTab() {
        isMoodTabSelected = true;
        moodRecyclerView.setVisibility(View.VISIBLE);
        // Ẩn list activity trong card
        activityRecyclerView.setVisibility(View.GONE);
        // Logic hiển thị ảnh/camera
        if (modeSwitch.isChecked()) {
            cameraPreviewView.setVisibility(View.VISIBLE);
            imgMoodPreview.setVisibility(View.GONE);
        } else {
            cameraPreviewView.setVisibility(View.GONE);
            imgMoodPreview.setVisibility(View.VISIBLE); // Hiện ảnh mood
            updatePreviewImage();
        }
    }

    private void switchToActivityTab() {
        isMoodTabSelected = false;
        // Ẩn list mood ở dưới
        moodRecyclerView.setVisibility(View.GONE);

        // Nếu chưa bật camera -> HIỆN LIST ACTIVITY TRONG CARD
        if (!modeSwitch.isChecked()) {
            activityRecyclerView.setVisibility(View.VISIBLE); // List dọc hiện lên
            imgMoodPreview.setVisibility(View.GONE);
            cameraPreviewView.setVisibility(View.GONE);
        } else {
            // Nếu đang bật camera -> Ẩn list activity để thấy camera
            activityRecyclerView.setVisibility(View.GONE);
        }
    }

    // --- LOGIC BẬT/TẮT CAMERA ---
    private void toggleCameraMode(boolean turnOn) {
        if (turnOn) {
            // Bật Camera: Ẩn hết ảnh tĩnh và list activity
            imgMoodPreview.setVisibility(View.GONE);
            activityRecyclerView.setVisibility(View.GONE); // Ẩn list để soi gương

            cameraPreviewView.setVisibility(View.VISIBLE);
            btnCloseCamera.setVisibility(View.VISIBLE);
            startCamera();
        } else {
            // Tắt Camera:
            cameraPreviewView.setVisibility(View.GONE);
            btnCloseCamera.setVisibility(View.GONE);

            if (cameraProvider != null) cameraProvider.unbindAll();

            // Nếu đang ở tab Activity -> Hiện lại list Activity
            if (!isMoodTabSelected) {
                activityRecyclerView.setVisibility(View.VISIBLE);
                imgMoodPreview.setVisibility(View.GONE);
            } else {
                // Nếu đang ở tab Mood -> Hiện ảnh Mood
                activityRecyclerView.setVisibility(View.GONE);
                imgMoodPreview.setVisibility(View.VISIBLE);
                updatePreviewImage();
            }
        }
    }

    // Cập nhật ảnh preview tĩnh (khi tắt camera)
    private void updatePreviewImage() {
        if (isMoodTabSelected && selectedMood != null) {
            Glide.with(this).load(selectedMood.getIconUrl()).into(imgMoodPreview);
        } else {
            imgMoodPreview.setImageResource(R.drawable.ic_launcher_foreground); // Ảnh mặc định
        }
    }

    // --- XỬ LÝ NÚT BẤM (QUAN TRỌNG NHẤT) ---
    private void handleMainAction() {
        // Validate: Phải chọn Mood hoặc Activity trước
        if (isMoodTabSelected && selectedMood == null) {
            Toast.makeText(getContext(), "Chọn cảm xúc đã nào!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isMoodTabSelected && selectedActivity == null) {
            Toast.makeText(getContext(), "Chọn hoạt động đã nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCameraOn = modeSwitch.isChecked();

        if (isCameraOn) {
            // === TRƯỜNG HỢP 2 (Mood + Ảnh) & TRƯỜNG HỢP 3 (Activity + Ảnh) ===
            // Cả 2 đều cần chụp ảnh từ Camera trước
            takePhotoAndProcess();
        } else {
            // === TRƯỜNG HỢP 1: CHỈ ĐĂNG MOOD (Không ảnh) ===
            if (isMoodTabSelected) {
                showPostDialog(null, selectedMood.getName(), selectedMood.getIconUrl());
            } else {
                // Nếu lỡ ở tab Activity mà tắt camera -> Bắt bật lại hoặc báo lỗi
                Toast.makeText(getContext(), "Bật camera để chụp ảnh hoạt động nhé!", Toast.LENGTH_SHORT).show();
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
                        // Chụp xong -> Xác định Title dựa trên Tab đang chọn
                        String title = isMoodTabSelected ? selectedMood.getName() : selectedActivity.getTitle();

                        // Mở Dialog với đường dẫn ảnh vừa chụp
                        showPostDialog(photoFile.getAbsolutePath(), title, null);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getContext(), "Lỗi chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ============================================================
    // === PHẦN LOGIC ĐĂNG BÀI LÊN FIREBASE ===
    // ============================================================

    /**
     * Hàm trung gian xử lý logic upload
     * Được gọi từ nút "Gửi" trong Dialog
     */
    private void handleUploadAndSave(Dialog dialog, String imagePath, String caption, MaterialButton btnSend) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        // UI Loading
        btnSend.setEnabled(false);
        btnSend.setText("Đang đăng...");

        // TRƯỜNG HỢP 1: CHỈ ĐĂNG MOOD (Không có ảnh chụp từ Camera)
        if (imagePath == null) {
            savePostToFirestore(dialog, null, caption, currentUser.getUid());
        }
        // TRƯỜNG HỢP 2 & 3: CÓ ẢNH (Mood+Ảnh hoặc Activity+Ảnh)
        else {
            uploadImageToStorage(dialog, imagePath, caption, currentUser.getUid(), btnSend);
        }
    }

    /**
     * Bước 1: Upload ảnh lên Firebase Storage
     */
    private void uploadImageToStorage(Dialog dialog, String imagePath, String caption, String userId, MaterialButton btnSend) {
        Uri fileUri = Uri.fromFile(new File(imagePath));

        // Tạo tên file: posts/userId/timestamp.jpg
        String fileName = "posts/" + userId + "/" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = storage.getReference().child(fileName);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Upload thành công -> Lấy link download URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        // Có link ảnh -> Lưu vào Firestore
                        savePostToFirestore(dialog, downloadUrl, caption, userId);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSend.setEnabled(true);
                    btnSend.setText("Gửi");
                });
    }

    /**
     * Bước 2: Lưu thông tin bài viết vào Firestore (Collection "posts")
     */
    private void savePostToFirestore(Dialog dialog, String photoUrl, String caption, String userId) {
        Map<String, Object> postMap = new HashMap<>();

        // Các trường chung
        postMap.put("userId", userId);
        postMap.put("caption", caption);
        postMap.put("photoUrl", photoUrl != null ? photoUrl : ""); // Nếu ko có ảnh thì rỗng
        postMap.put("createdAt", FieldValue.serverTimestamp()); // Lấy giờ server

        // Xử lý riêng từng trường hợp
        if (isMoodTabSelected) {
            // --- ĐĂNG MOOD ---
            postMap.put("type", "mood");
            if (selectedMood != null) {
                postMap.put("moodName", selectedMood.getName());
                postMap.put("moodIconUrl", selectedMood.getIconUrl());
            }
        } else {
            // --- ĐĂNG ACTIVITY ---
            postMap.put("type", "activity");
            if (selectedActivity != null) {
                postMap.put("activityTitle", selectedActivity.getTitle());
            }
        }

        // Ghi vào Firestore
        db.collection("posts")
                .add(postMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // Đóng dialog

                    // Reset trạng thái app về ban đầu
                    modeSwitch.setChecked(false);
                    toggleGroupContentType.check(R.id.btnTabMood);

                    // Tự động cuộn về bài mới nhất
                    if (viewPagerPosts != null) {
                        viewPagerPosts.setCurrentItem(0, true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi lưu bài viết: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // --- DIALOG ---
    private void showPostDialog(String imagePath, String title, String iconUrl) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_post); // Đảm bảo có file layout này

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

        // Logic hiển thị ảnh trong Dialog
        if (imagePath != null) {
            // Có ảnh chụp (Case 2 & 3)
            imgPreview.setImageURI(Uri.fromFile(new File(imagePath)));
        } else if (iconUrl != null) {
            // Chỉ có Mood icon (Case 1)
            Glide.with(this).load(iconUrl).into(imgPreview);
        }

        btnSend.setOnClickListener(v -> {
            String content = edtContent.getText().toString();
            // Gọi hàm xử lý upload & save
            handleUploadAndSave(dialog, imagePath, content, btnSend);
        });

        dialog.show();
    }

    // --- ADAPTER SETUP ---
    private void setupRecyclers() {
        // 1. Mood Adapter (Ngang)
        moodAdapter = new MoodAdapter(moodList, mood -> {
            this.selectedMood = mood;
            if (isMoodTabSelected && !modeSwitch.isChecked()) updatePreviewImage();
        });
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        moodRecyclerView.setAdapter(moodAdapter);

        // 2. Activity Adapter (DỌC)
        activityList.add(new Activity("Chạy bộ"));
        activityList.add(new Activity("Đọc sách"));
        activityList.add(new Activity("Cafe"));
        // ... thêm dữ liệu tùy ý ...

        activityAdapter = new ActivityAdapter(activityList, activity -> {
            this.selectedActivity = activity;
            // KHI CHỌN ACTIVITY -> TỰ ĐỘNG BẬT CAMERA ĐỂ CHỤP
            Toast.makeText(getContext(), "Đã chọn: " + activity.getTitle(), Toast.LENGTH_SHORT).show();
            modeSwitch.setChecked(true); // Bật camera lên
        });

        // QUAN TRỌNG: Đổi thành Vertical (Dọc)
        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        activityRecyclerView.setAdapter(activityAdapter);
    }

    // --- START CAMERA ---
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

    private void fetchMoodsFromFirestore() {
        db.collection("Mood").get().addOnSuccessListener(snapshots -> {
            moodList.clear();
            for (DocumentSnapshot doc : snapshots) {
                moodList.add(new Mood(doc.getString("name"), doc.getString("iconUrl"), Boolean.TRUE.equals(doc.getBoolean("isPremium"))));
            }
            if (moodAdapter != null) moodAdapter.notifyDataSetChanged();
        });
    }

    // --- HÀM HIỂN THỊ BOTTOM SHEET BẠN BÈ ---
    private void showFriendsBottomSheet() {
        // 1. Tạo Dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        // 2. Nạp layout bottom_sheet_friend.xml
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_friend, null);
        bottomSheetDialog.setContentView(sheetView);

        // 3. Ánh xạ các view bên trong Bottom Sheet
        RecyclerView rcvFriends = sheetView.findViewById(R.id.rcvFriends);
        RecyclerView rcvSuggestions = sheetView.findViewById(R.id.rcvSuggestions);

        // 4. Setup dữ liệu giả
        setupFriendListInSheet(rcvFriends);
        setupSuggestionListInSheet(rcvSuggestions);

        // 6. Hiển thị lên
        bottomSheetDialog.show();
    }

    // --- HÀM GIẢ LẬP DỮ LIỆU CHO BẠN BÈ ---
    private void setupFriendListInSheet(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);

        List<Activity> fakeFriends = new ArrayList<>();
        fakeFriends.add(new Activity("Nguyễn Văn A"));
        fakeFriends.add(new Activity("Trần Thị B"));
        fakeFriends.add(new Activity("Lê Văn C"));

        // Sử dụng tạm ActivityAdapter để hiển thị tên
        ActivityAdapter adapter = new ActivityAdapter(fakeFriends, item -> {
            Toast.makeText(getContext(), "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSuggestionListInSheet(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);

        List<Activity> fakeSuggestions = new ArrayList<>();
        fakeSuggestions.add(new Activity("Người lạ 1"));
        fakeSuggestions.add(new Activity("Người lạ 2"));

        ActivityAdapter adapter = new ActivityAdapter(fakeSuggestions, item -> {});
        recyclerView.setAdapter(adapter);
    }
}
