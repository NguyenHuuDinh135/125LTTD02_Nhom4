package com.example.nhom4.ui.page.friend;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.data.bean.FriendRequest;
import com.example.nhom4.data.bean.User;
import com.example.nhom4.ui.adapter.FriendRequestAdapter;
import com.example.nhom4.ui.adapter.UserSuggestionAdapter;
import com.example.nhom4.ui.viewmodel.FriendsViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * BottomSheet hiển thị song song danh sách gợi ý kết bạn và lời mời đang chờ xử lý.
 */
public class FriendsBottomSheet extends BottomSheetDialogFragment {
    private TextInputEditText etSearch;
    private Timer searchTimer;
    private List<User> originalSuggestions = new ArrayList<>();

    private static final long SEARCH_DEBOUNCE_DELAY = 300; // ms

    private RecyclerView rcvSuggestions, rcvRequests;
    private FriendsViewModel viewModel;

    private UserSuggestionAdapter suggestionAdapter;
    private FriendRequestAdapter requestAdapter;

    // Thêm 2 nút "Xem tất cả" từ layout
    private MaterialButton btnSeeMoreRequests;
    private MaterialButton btnSeeMoreSuggestions;

    private MaterialTextView tvSectionRequests;
    private MaterialTextView tvSectionSuggestions;
    private boolean isSearching = false;
    private List<FriendRequest> originalRequests = new ArrayList<>();

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

        // 1. Init ViewModel
        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        rcvSuggestions = view.findViewById(R.id.rcvSuggestions);
        rcvRequests = view.findViewById(R.id.rcvFriends);

        // Tìm nút "Xem tất cả" trong layout
        btnSeeMoreRequests = view.findViewById(R.id.btn_see_more_1);
        btnSeeMoreSuggestions = view.findViewById(R.id.btn_see_more_2);

        // Tìm ô search trong layout
        etSearch = view.findViewById(R.id.et_search);
        tvSectionRequests = view.findViewById(R.id.tv_section_requests);
        tvSectionSuggestions = view.findViewById(R.id.tv_section_suggestions);

        setupAdapters();
        setupSeeMoreButtons(); // Xử lý click nút xem thêm
        setupSearchListener();
        observeViewModel();
    }

    /**
     * Khởi tạo 2 adapter cho danh sách gợi ý và lời mời.
     */
    private void setupAdapters() {
        // Adapter Gợi ý
        suggestionAdapter = new UserSuggestionAdapter(new ArrayList<>(), user -> {
            viewModel.sendFriendRequest(user);
        });
        rcvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvSuggestions.setAdapter(suggestionAdapter);

        // Adapter Lời mời
        requestAdapter = new FriendRequestAdapter(new ArrayList<>(), new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                viewModel.acceptRequest(request);
            }

            @Override
            public void onDecline(FriendRequest request) {
                viewModel.declineRequest(request);
            }
        });
        rcvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvRequests.setAdapter(requestAdapter);
    }

    /**
     * Thiết lập sự kiện click cho 2 nút "Xem tất cả"
     */
    private void setupSeeMoreButtons() {
        btnSeeMoreRequests.setOnClickListener(v -> {
            boolean currentLimited = requestAdapter.isLimitedMode();
            requestAdapter.setLimitedMode(!currentLimited);
            updateSeeMoreButton(btnSeeMoreRequests, requestAdapter.getFullItemCount(), !currentLimited);
        });

        btnSeeMoreSuggestions.setOnClickListener(v -> {
            boolean currentLimited = suggestionAdapter.isLimitedMode();
            suggestionAdapter.setLimitedMode(!currentLimited);
            updateSeeMoreButton(btnSeeMoreSuggestions, suggestionAdapter.getFullItemCount(), !currentLimited);
        });
    }

    /**
     * Cập nhật hiển thị nút "Xem tất cả" hoặc "Thu gọn"
     * Ẩn nút nếu tổng số item ≤ 5
     */
    private void updateSeeMoreButton(MaterialButton button, int totalCount, boolean isLimited) {
        if (totalCount > 5) {
            button.setVisibility(View.VISIBLE);
            button.setText(isLimited ? "Xem tất cả" : "Thu gọn");
        } else {
            button.setVisibility(View.GONE);
        }
    }

    /**
     * Lắng nghe luồng dữ liệu từ FriendsViewModel để cập nhật UI và hiển thị toast.
     */
    private void observeViewModel() {
        // 1. Lắng nghe danh sách GỢI Ý KẾT BẠN (suggestions)
        viewModel.getSuggestions().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                originalSuggestions = new ArrayList<>(resource.data); // Lưu danh sách gốc để tìm kiếm

                if (!isSearching) {
                    // FIX: Không tạo mới adapter, chỉ cập nhật dữ liệu để giữ layout
                    suggestionAdapter.setUsers(resource.data);
                    suggestionAdapter.setLimitedMode(true);
                    updateSeeMoreButton(btnSeeMoreSuggestions, resource.data.size(), true);
                }
            }
        });

        // 2. Lắng nghe danh sách LỜI MỜI KẾT BẠN (friend requests)
        viewModel.getFriendRequests().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                originalRequests = new ArrayList<>(resource.data); // Lưu gốc để khôi phục sau tìm kiếm

                if (!isSearching) {
                    requestAdapter.setRequests(resource.data);
                    requestAdapter.setLimitedMode(true);
                    updateSeeMoreButton(btnSeeMoreRequests, resource.data.size(), true);
                }
            }
        });

        // 3. Lắng nghe trạng thái hành động (Toast)
        viewModel.getActionStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                Toast.makeText(getContext(), resource.data, Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR && resource.message != null) {
                Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Thiết lập listener tìm kiếm chỉ cho danh sách gợi ý (không lọc lời mời)
     */
    private void setupSearchListener() {
        searchTimer = new Timer();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchTimer.cancel();
                searchTimer = new Timer();

                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        String keyword = s.toString().trim().toLowerCase(Locale.getDefault());

                        requireActivity().runOnUiThread(() -> {
                            if (keyword.isEmpty()) {
                                exitSearchMode();
                            } else {
                                enterSearchMode(keyword);
                            }
                        });
                    }
                }, SEARCH_DEBOUNCE_DELAY);
            }
        });
    }

    /**
     * Thực hiện lọc chỉ cho danh sách gợi ý người dùng
     * // FIX: Không tạo mới adapter + thêm animation mượt
     */
    private void enterSearchMode(String keyword) {
        isSearching = true;

        // --- ANIMATION: fade out---
        tvSectionRequests.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> tvSectionRequests.setVisibility(View.GONE)).start();
        tvSectionSuggestions.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> tvSectionSuggestions.setVisibility(View.GONE)).start();
        btnSeeMoreRequests.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> btnSeeMoreRequests.setVisibility(View.GONE)).start();
        btnSeeMoreSuggestions.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> btnSeeMoreSuggestions.setVisibility(View.GONE)).start();

        // Ẩn phần lời mời bằng fade
        rcvRequests.animate().alpha(0f).setDuration(300)
                .withEndAction(() -> rcvRequests.setVisibility(View.GONE)).start();

        // Lọc dữ liệu
        List<User> filtered = new ArrayList<>();
        for (User user : originalSuggestions) {
            if (user.getUsername() != null &&
                    user.getUsername().toLowerCase(Locale.getDefault()).contains(keyword)) {
                filtered.add(user);
            }
        }

        suggestionAdapter.setUsers(filtered);
        suggestionAdapter.setLimitedMode(false);

        // Focus nhẹ (scale) - giữ nguyên vị trí
        rcvSuggestions.animate()
                .scaleX(1.02f)
                .scaleY(1.02f)
                .setDuration(300)
                .withEndAction(() -> rcvSuggestions.animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                .start();
    }

    /**
     * // FIX: Animation hiện lại + chỉ cập nhật dữ liệu
     */
    private void exitSearchMode() {
        isSearching = false;

        // Hiện lại phần lời mời bằng fade in
        rcvRequests.setVisibility(View.VISIBLE);
        rcvRequests.setAlpha(0f);
        rcvRequests.animate().alpha(1f).setDuration(300).start();

        // Hiện lại tiêu đề + nút bằng fade in
        tvSectionRequests.setVisibility(View.VISIBLE);
        tvSectionSuggestions.setVisibility(View.VISIBLE);
        tvSectionRequests.setAlpha(0f);
        tvSectionSuggestions.setAlpha(0f);
        tvSectionRequests.animate().alpha(1f).setDuration(300).start();
        tvSectionSuggestions.animate().alpha(1f).setDuration(300).start();

        btnSeeMoreRequests.setVisibility(View.VISIBLE);
        btnSeeMoreSuggestions.setVisibility(View.VISIBLE);
        btnSeeMoreRequests.setAlpha(0f);
        btnSeeMoreSuggestions.setAlpha(0f);
        btnSeeMoreRequests.animate().alpha(1f).setDuration(300).start();
        btnSeeMoreSuggestions.animate().alpha(1f).setDuration(300).start();

        // Khôi phục dữ liệu
        suggestionAdapter.setUsers(new ArrayList<>(originalSuggestions));
        suggestionAdapter.setLimitedMode(true);
        updateSeeMoreButton(btnSeeMoreSuggestions, originalSuggestions.size(), true);

        requestAdapter.setRequests(new ArrayList<>(originalRequests));
        requestAdapter.setLimitedMode(true);
        updateSeeMoreButton(btnSeeMoreRequests, originalRequests.size(), true);
    }

    // ================= UI BOTTOM SHEET CONFIG (Giữ nguyên) =================

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