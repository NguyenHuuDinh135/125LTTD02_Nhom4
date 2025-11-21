package com.example.nhom4.ui.page.friend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.model.FriendRequest;
import com.example.nhom4.data.model.Relationship;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendsBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView rcvFriends, rcvFriendRequests, rcvSuggestions;
    private FriendsAdapter friendsAdapter;
    private FriendRequestAdapter requestAdapter;
    private SuggestionAdapter suggestionAdapter;

    private FriendViewModel friendViewModel;
    private String currentUserId = "userA"; // TODO: lấy từ FirebaseAuth

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        return inflater.inflate(R.layout.bottom_sheet_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Find views ---
        rcvFriends = view.findViewById(R.id.rcvFriends);
        rcvFriendRequests = view.findViewById(R.id.rcvFriendRequests);
        rcvSuggestions = view.findViewById(R.id.rcvSuggestions);

        // --- ViewModel ---
        friendViewModel = new ViewModelProvider(requireActivity()).get(FriendViewModel.class);

        // --- Setup Friends RecyclerView ---
        friendsAdapter = new FriendsAdapter(new ArrayList<>(), currentUserId);
        rcvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvFriends.setAdapter(friendsAdapter);

        // --- Setup Friend Requests RecyclerView ---
        requestAdapter = new FriendRequestAdapter(new ArrayList<>(), new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request, int position) {
                friendViewModel.respondToRequest(request.getRequestId(), true);
            }

            @Override
            public void onDecline(FriendRequest request, int position) {
                friendViewModel.respondToRequest(request.getRequestId(), false);
            }
        });
        rcvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvFriendRequests.setAdapter(requestAdapter);

        // --- Setup Suggestions RecyclerView ---
        suggestionAdapter = new SuggestionAdapter(new ArrayList<>());
        rcvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvSuggestions.setAdapter(suggestionAdapter);

        // --- Load data ---
        loadFriends();
        loadFriendRequests();
        loadSuggestions();

        // --- Gửi yêu cầu kết bạn ---
        suggestionAdapter.setOnAddClickListener(userId -> {
            friendViewModel.sendFriendRequest(currentUserId, userId);

            // Optional: đổi nút thành "Đã gửi" hoặc refresh gợi ý
            rcvSuggestions.post(() -> suggestionAdapter.notifyDataSetChanged());

            // Reload danh sách yêu cầu để thấy yêu cầu vừa gửi
            loadFriendRequests();
        });
    }

    private void loadFriends() {
        friendViewModel.getFriends(currentUserId).observe(getViewLifecycleOwner(), relationships -> {
            friendsAdapter.setList(relationships);
        });
    }

    private void loadFriendRequests() {
        friendViewModel.getIncomingRequests(currentUserId).observe(getViewLifecycleOwner(), requests -> {
            requestAdapter.setRequests(requests);
        });
    }

    private void loadSuggestions() {
        // Dữ liệu mẫu (có thể lấy từ server/Firebase)
        List<String> sampleSuggestions = Arrays.asList("Sabrina", "Laurel", "Britney");
        suggestionAdapter.setList(sampleSuggestions);
    }

    /** Cho phép đổi tài khoản hiện tại */
    public void setCurrentUser(String userId) {
        this.currentUserId = userId;
        loadFriends();
        loadFriendRequests();
        loadSuggestions();
    }

    @Override
    public void onStart() {
        super.onStart();

        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) return;

        FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) return;

        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.95);
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

        int colorSurface = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSurface, 0);
        MaterialShapeDrawable background = new MaterialShapeDrawable(shapeModel);
        background.setTint(colorSurface);
        background.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS);
        background.setElevation(8f);
        bottomSheet.setBackground(background);
    }
}
