package com.example.nhom4.ui.page.friend;

import android.content.res.Resources;
import android.os.Bundle;
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
import com.example.nhom4.ui.adapter.FriendRequestAdapter;
import com.example.nhom4.ui.adapter.UserSuggestionAdapter;
import com.example.nhom4.ui.viewmodel.FriendsViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.ArrayList;

/**
 * BottomSheet hiển thị song song danh sách gợi ý kết bạn và lời mời đang chờ xử lý.
 */
public class FriendsBottomSheet extends BottomSheetDialogFragment {

    private RecyclerView rcvSuggestions, rcvRequests;
    private FriendsViewModel viewModel;

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

        // 1. Init ViewModel
        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        rcvSuggestions = view.findViewById(R.id.rcvSuggestions);
        rcvRequests = view.findViewById(R.id.rcvFriends);

        setupAdapters();
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
     * Lắng nghe luồng dữ liệu từ FriendsViewModel để cập nhật UI và hiển thị toast.
     */
    private void observeViewModel() {
        // 1. Lắng nghe danh sách gợi ý
        viewModel.getSuggestions().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Cập nhật Adapter (Nếu adapter chưa có setList, tạo mới tạm thời hoặc thêm hàm setList vào adapter)
                suggestionAdapter = new UserSuggestionAdapter(resource.data, user -> viewModel.sendFriendRequest(user));
                rcvSuggestions.setAdapter(suggestionAdapter);
            }
        });

        // 2. Lắng nghe danh sách lời mời
        viewModel.getFriendRequests().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                requestAdapter.setRequests(resource.data); // Đảm bảo Adapter có hàm setRequests
            }
        });

        // 3. Lắng nghe trạng thái hành động (Toast)
        viewModel.getActionStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), resource.data, Toast.LENGTH_SHORT).show();
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });
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
