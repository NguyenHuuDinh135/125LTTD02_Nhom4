package com.example.nhom4.ui.page.friend;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.FriendRequest;
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

    private RecyclerView rcvSuggestions, rcvRequests;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    private List<User> suggestionList = new ArrayList<>();
    private List<FriendRequest> requestList = new ArrayList<>();

    private UserSuggestionAdapter suggestionAdapter;
    private FriendRequestAdapter requestAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        currentUserId = currentUser.getUid();

        rcvSuggestions = view.findViewById(R.id.rcvSuggestions);
        rcvRequests = view.findViewById(R.id.rcvFriends);

        setupAdapters();
        loadSuggestions();
        loadFriendRequests();
    }

    private void setupAdapters() {
        // Adapter gợi ý kết bạn
        suggestionAdapter = new UserSuggestionAdapter(suggestionList, user -> sendFriendRequest(user));
        rcvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvSuggestions.setAdapter(suggestionAdapter);

        // Adapter lời mời kết bạn
        requestAdapter = new FriendRequestAdapter(requestList, new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                db.collection("relationships").document(request.getRequestId())
                        .update("status", "accepted")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Đã chấp nhận " + request.getRequesterId(), Toast.LENGTH_SHORT).show();
                            requestList.remove(request);
                            requestAdapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }

            @Override
            public void onDecline(FriendRequest request) {
                db.collection("relationships").document(request.getRequestId())
                        .update("status", "rejected")
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Đã từ chối " + request.getRequesterId(), Toast.LENGTH_SHORT).show();
                            requestList.remove(request);
                            requestAdapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        });
        rcvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvRequests.setAdapter(requestAdapter);
    }

    // ================= FIREBASE =================

    private void loadFriendRequests() {
        db.collection("relationships")
                .whereEqualTo("recipientId", currentUserId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(query -> {
                    requestList.clear();
                    for (DocumentSnapshot doc : query) {
                        FriendRequest fr = doc.toObject(FriendRequest.class);
                        if (fr != null) {
                            fr.setRequestId(doc.getId());
                            requestList.add(fr);
                        }
                    }
                    requestAdapter.notifyDataSetChanged(); // update RecyclerView
                })
                .addOnFailureListener(e ->
                        Log.e("FriendSheet", "Load requests error", e)
                );
    }

    private void loadSuggestions() {
        db.collection("users")
                .limit(20)
                .get()
                .addOnSuccessListener(query -> {
                    suggestionList.clear();
                    for (DocumentSnapshot doc : query) {
                        if (!doc.getId().equals(currentUserId)) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                user.setUid(doc.getId());
                                suggestionList.add(user);
                            }
                        }
                    }
                    suggestionAdapter.notifyDataSetChanged(); // update RecyclerView
                })
                .addOnFailureListener(e -> Log.e("FriendSheet", "Load suggestions error", e));
    }


    private void sendFriendRequest(User targetUser) {
        Map<String, Object> relationship = new HashMap<>();
        relationship.put("requesterId", currentUserId);
        relationship.put("recipientId", targetUser.getUid());
        relationship.put("status", "pending");
        relationship.put("createdAt", FieldValue.serverTimestamp());
        relationship.put("members", List.of(currentUserId, targetUser.getUid()));

        db.collection("relationships")
                .add(relationship)
                .addOnSuccessListener(docRef ->
                        Toast.makeText(getContext(), "Đã gửi lời mời đến " + targetUser.getUsername(), Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Lỗi gửi lời mời", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= UI BOTTOM SHEET =================

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

            int colorSurface = MaterialColors.getColor(bottomSheet, com.google.android.material.R.attr.colorSurface);
            MaterialShapeDrawable drawable = new MaterialShapeDrawable(shapeModel);
            drawable.setTint(colorSurface);
            drawable.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
            drawable.setElevation(8f);
            bottomSheet.setBackground(drawable);
        }
    }
}
