package com.example.nhom4.ui.page.friend;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.ui.adapter.FriendRequestAdapter;
import com.example.nhom4.ui.adapter.UserSuggestionAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView rcvRequests, rcvSuggest;
    private TextView txtSectionTitle1; // Để đổi text "Your Friends" thành "Friend Requests"

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    private List<User> suggestionList = new ArrayList<>();
    private List<User> requestList = new ArrayList<>();

    private UserSuggestionAdapter suggestionAdapter;
    private FriendRequestAdapter requestAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Ép giao diện sáng/tối nếu cần (tùy chọn)
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        return inflater.inflate(R.layout.bottom_sheet_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Init Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        currentUserId = currentUser.getUid();

        // 2. Init Views
        // rcvFriends ở đây mình dùng để hiển thị Lời mời kết bạn (Requests)
        rcvRequests = view.findViewById(R.id.rcvFriends);
        rcvSuggest = view.findViewById(R.id.rcvSuggestions);

        // Tìm textview tiêu đề để đổi tên cho hợp ngữ cảnh
        // Giả sử trong layout bottom_sheet_friend, TextView "Your Friends" là con đầu tiên của LinearLayout trong ScrollView
        // Hoặc bạn nên đặt ID cho TextView đó trong XML là @+id/tv_section_1
        // Ở đây mình tìm tạm bằng cách duyệt view hoặc bạn thêm ID vào XML nhé.
        // Ví dụ code giả định bạn đã thêm ID tv_section_friend_list vào layout
        // txtSectionTitle1 = view.findViewById(R.id.tv_section_friend_list);
        // if(txtSectionTitle1 != null) txtSectionTitle1.setText("Lời mời kết bạn");

        // 3. Setup Adapters
        setupAdapters();

        // 4. Load Data
        loadSuggestions();
        loadFriendRequests();
    }

    private void setupAdapters() {
        // --- Adapter cho Gợi ý (Gửi kết bạn) ---
        suggestionAdapter = new UserSuggestionAdapter(suggestionList, userToRequest -> {
            sendFriendRequest(userToRequest);
        });
        rcvSuggest.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvSuggest.setAdapter(suggestionAdapter);

        // --- Adapter cho Lời mời (Chấp nhận) ---
        requestAdapter = new FriendRequestAdapter(requestList, userToAccept -> {
            acceptFriendRequest(userToAccept);
        });
        rcvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvRequests.setAdapter(requestAdapter);
    }

    // ================= FIREBASE LOGIC =================

    // 1. Load danh sách gợi ý (User chưa là bạn)
    private void loadSuggestions() {
        db.collection("users")
                .limit(20) // Lấy giới hạn để test
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    suggestionList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Không hiển thị chính mình
                        if (!doc.getId().equals(currentUserId)) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                user.setUid(doc.getId()); // Set ID để dùng sau này
                                suggestionList.add(user);
                            }
                        }
                    }
                    suggestionAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FriendSheet", "Lỗi load user", e));
    }

    // 2. Load lời mời kết bạn (recipientId == mình, status == pending)
    private void loadFriendRequests() {
        db.collection("relationships")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    requestList.clear();
                    if (querySnapshots.isEmpty()) {
                        requestAdapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot doc : querySnapshots) {
                        String requesterId = doc.getString("requesterId");

                        // Load thông tin User của người gửi
                        db.collection("users").document(requesterId).get()
                                .addOnSuccessListener(userDoc -> {
                                    User user = userDoc.toObject(User.class);
                                    if (user != null) {
                                        user.setUid(requesterId);
                                        // Lưu relationshipId tạm vào object user hoặc 1 map riêng để update
                                        // Ở đây mình giả định User bean có field extra hoặc mình query lại khi accept
                                        requestList.add(user);
                                        requestAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }

    // 3. Gửi lời mời kết bạn
    private void sendFriendRequest(User targetUser) {
        Map<String, Object> relationship = new HashMap<>();
        relationship.put("requesterId", currentUserId);
        relationship.put("recipientId", targetUser.getUid()); // targetUser.getId() là UID lấy từ Firestore
        relationship.put("status", "pending");
        relationship.put("createdAt", FieldValue.serverTimestamp());
        relationship.put("members", java.util.Arrays.asList(currentUserId, targetUser.getUid()));

        db.collection("relationships")
                .add(relationship)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Đã gửi lời mời đến " + targetUser.getUsername(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi gửi lời mời", Toast.LENGTH_SHORT).show();
                });
    }

    // 4. Chấp nhận lời mời
    private void acceptFriendRequest(User requesterUser) {
        // Tìm document relationship để update
        db.collection("relationships")
                .whereEqualTo("requesterId", requesterUser.getUid())
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (DocumentSnapshot doc : querySnapshots) {
                        doc.getReference().update("status", "accepted")
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Đã trở thành bạn bè!", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    // ================= UI CONFIGURATION =================

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) return;

        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

        if (bottomSheet != null) {
            int height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.95);
            bottomSheet.getLayoutParams().height = height;
            bottomSheet.requestLayout();

            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(height);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            ShapeAppearanceModel shapeModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setTopLeftCornerSize(24f)
                    .setTopRightCornerSize(24f)
                    .build();

            // --- SỬA LỖI Ở ĐÂY ---
            // Sử dụng R.attr.colorSurface thay vì đường dẫn dài dòng dễ gây lỗi
            // Hoặc hardcode màu trắng nếu không quan trọng theme
            int colorSurface = MaterialColors.getColor(bottomSheet, com.google.android.material.R.attr.colorSurface);

            MaterialShapeDrawable drawable = new MaterialShapeDrawable(shapeModel);
            drawable.setTint(colorSurface);
            drawable.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
            drawable.setElevation(8f);
            bottomSheet.setBackground(drawable);
        }
    }


}
