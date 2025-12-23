package com.example.nhom4.ui.page.friend;

import android.content.Context;
import android.content.Intent;
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
import androidx.core.content.ContextCompat;
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
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager; // THÊM import
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class FriendsBottomSheet extends BottomSheetDialogFragment {
    private TextInputEditText etSearch;
    private Timer searchTimer;
    private List<User> originalSuggestions = new ArrayList<>();
    private List<FriendRequest> originalRequests = new ArrayList<>();

    private static final long SEARCH_DEBOUNCE_DELAY = 300;

    private RecyclerView rcvSuggestions, rcvRequests;
    private FriendsViewModel viewModel;

    private UserSuggestionAdapter suggestionAdapter;
    private FriendRequestAdapter requestAdapter;

    private MaterialButton btnSeeMoreRequests;
    private MaterialButton btnSeeMoreSuggestions;
    private MaterialTextView tvSectionRequests;
    private MaterialTextView tvSectionSuggestions;

    // Views cho phần mời bạn bè
    private View itemMessenger, itemInstagram, itemTwitter, itemOther;
    private View inviteMessenger, inviteFacebook, inviteInstagram, inviteOther;

    private boolean isSearching = false;

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

        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        initViews(view);
        setupAdapters();
        setupSeeMoreButtons();
        setupSearchListener();
        setupInviteActions(); // <--- MỚI: Cấu hình nút mời
        observeViewModel();
    }

    private void initViews(View view) {
        rcvSuggestions = view.findViewById(R.id.rcvSuggestions);
        rcvRequests = view.findViewById(R.id.rcvFriends);
        btnSeeMoreRequests = view.findViewById(R.id.btn_see_more_1);
        btnSeeMoreSuggestions = view.findViewById(R.id.btn_see_more_2);
        etSearch = view.findViewById(R.id.et_search);
        tvSectionRequests = view.findViewById(R.id.tv_section_requests);
        tvSectionSuggestions = view.findViewById(R.id.tv_section_suggestions);

        // Ánh xạ các nút mời (include)
        itemMessenger = view.findViewById(R.id.item_messenger);
        itemInstagram = view.findViewById(R.id.item_instagram);
        itemTwitter = view.findViewById(R.id.item_twitter);
        itemOther = view.findViewById(R.id.item_other);

        inviteMessenger = view.findViewById(R.id.invite_messenger);
        inviteFacebook = view.findViewById(R.id.invite_facebook);
        inviteInstagram = view.findViewById(R.id.invite_instagram);
        inviteOther = view.findViewById(R.id.invite_other);
    }

    private void setupAdapters() {
        suggestionAdapter = new UserSuggestionAdapter(new ArrayList<>(), user -> viewModel.sendFriendRequest(user.getUid()));
        rcvSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvSuggestions.setAdapter(suggestionAdapter);

        requestAdapter = new FriendRequestAdapter(new ArrayList<>(), new FriendRequestAdapter.OnRequestActionListener() {
            @Override public void onAccept(FriendRequest request) { viewModel.acceptFriendRequest(request.getSenderId()); }
            @Override public void onDecline(FriendRequest request) { viewModel.declineFriendRequest(request.getSenderId()); }
        });
        rcvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvRequests.setAdapter(requestAdapter);
    }

    /**
     * MỚI: Xử lý sự kiện click mời bạn bè
     */
    private void setupInviteActions() {
        String inviteMessage = "Tải ngay AppNhom4 để chat với tui nhé! Link: https://example.com/download";
        View.OnClickListener inviteListener = v -> shareAppInvite(inviteMessage);

        if(itemMessenger != null) itemMessenger.setOnClickListener(inviteListener);
        if(itemInstagram != null) itemInstagram.setOnClickListener(inviteListener);
        if(itemTwitter != null) itemTwitter.setOnClickListener(inviteListener);
        if(itemOther != null) itemOther.setOnClickListener(inviteListener);

        if(inviteMessenger != null) inviteMessenger.setOnClickListener(inviteListener);
        if(inviteFacebook != null) inviteFacebook.setOnClickListener(inviteListener);
        if(inviteInstagram != null) inviteInstagram.setOnClickListener(inviteListener);
        if(inviteOther != null) inviteOther.setOnClickListener(inviteListener);
    }

    private void shareAppInvite(String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Lời mời kết bạn");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(intent, "Mời bạn bè qua..."));
    }

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

    private void updateSeeMoreButton(MaterialButton button, int totalCount, boolean isLimited) {
        if (totalCount > 5) {
            button.setVisibility(View.VISIBLE);
            button.setText(isLimited ? "Xem tất cả" : "Thu gọn");
        } else {
            button.setVisibility(View.GONE);
        }
    }

    private void observeViewModel() {
        viewModel.getSuggestions().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                originalSuggestions = new ArrayList<>(resource.data);
                if (!isSearching) {
                    suggestionAdapter.setUsers(resource.data);
                    suggestionAdapter.setLimitedMode(true);
                    updateSeeMoreButton(btnSeeMoreSuggestions, resource.data.size(), true);
                }
            }
        });

        viewModel.getFriendRequests().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                originalRequests = new ArrayList<>(resource.data);
                if (!isSearching) {
                    requestAdapter.setRequests(resource.data);
                    requestAdapter.setLimitedMode(true);
                    updateSeeMoreButton(btnSeeMoreRequests, resource.data.size(), true);
                }
            }
        });

        // Quan sát kết quả gửi lời mời
        viewModel.getSendResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Đã gửi lời mời kết bạn!", Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát kết quả chấp nhận lời mời
        // Trong FriendsBottomSheet.java
        viewModel.getAcceptResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Đã chấp nhận lời mời!", Toast.LENGTH_SHORT).show();

                // Gửi broadcast ngay lập tức
                LocalBroadcastManager.getInstance(requireContext())
                        .sendBroadcast(new Intent("REFRESH_CHAT_LIST"));

                // Ẩn item đã accept khỏi list hiện tại (UX improvement)
                // (Bạn cần implement hàm removeRequest trong Adapter để xóa item ngay trên UI)
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
        // Quan sát kết quả từ chối lời mời
        viewModel.getDeclineResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), "Đã từ chối lời mời.", Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(searchTimer != null) searchTimer.cancel();
                searchTimer = new Timer();
                searchTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                String keyword = s.toString().trim().toLowerCase(Locale.getDefault());
                                if (keyword.isEmpty()) exitSearchMode();
                                else enterSearchMode(keyword);
                            });
                        }
                    }
                }, SEARCH_DEBOUNCE_DELAY);
            }
        });
    }

    private void enterSearchMode(String keyword) {
        isSearching = true;
        // Ẩn các phần không liên quan
        tvSectionRequests.setVisibility(View.GONE);
        tvSectionSuggestions.setVisibility(View.GONE);
        btnSeeMoreRequests.setVisibility(View.GONE);
        btnSeeMoreSuggestions.setVisibility(View.GONE);
        rcvRequests.setVisibility(View.GONE);

        // Lọc gợi ý
        List<User> filtered = new ArrayList<>();
        for (User user : originalSuggestions) {
            if (user.getUsername() != null && user.getUsername().toLowerCase().contains(keyword)) {
                filtered.add(user);
            }
        }
        suggestionAdapter.setUsers(filtered);
        suggestionAdapter.setLimitedMode(false); // Search thì hiện hết
    }

    private void exitSearchMode() {
        isSearching = false;
        // Hiện lại UI
        tvSectionRequests.setVisibility(View.VISIBLE);
        tvSectionSuggestions.setVisibility(View.VISIBLE);
        rcvRequests.setVisibility(View.VISIBLE);

        suggestionAdapter.setUsers(new ArrayList<>(originalSuggestions));
        suggestionAdapter.setLimitedMode(true);
        updateSeeMoreButton(btnSeeMoreSuggestions, originalSuggestions.size(), true);

        requestAdapter.setRequests(new ArrayList<>(originalRequests));
        requestAdapter.setLimitedMode(true);
        updateSeeMoreButton(btnSeeMoreRequests, originalRequests.size(), true);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                int height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.95);
                bottomSheet.getLayoutParams().height = height;
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(height);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // Style bo góc
                ShapeAppearanceModel shapeModel = new ShapeAppearanceModel().toBuilder()
                        .setTopLeftCornerSize(24f).setTopRightCornerSize(24f).build();
                MaterialShapeDrawable drawable = new MaterialShapeDrawable(shapeModel);
                drawable.setTint(ContextCompat.getColor(requireContext(), R.color.md_theme_surface));
                bottomSheet.setBackground(drawable);
            }
        }
    }
}