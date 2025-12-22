package com.example.nhom4.ui.page.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.ui.adapter.ChatListAdapter;
import com.example.nhom4.ui.adapter.OperatorAdapter;
import com.example.nhom4.ui.viewmodel.DiscoveryViewModel;

/**
 * Trang khám phá: nửa trên là danh sách chat gần đây, nửa dưới là hoạt động nổi bật.
 */
public class DiscoveryFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private RecyclerView activityRecyclerView;

    private ChatListAdapter chatAdapter;
    private OperatorAdapter activityAdapter;

    private DiscoveryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(DiscoveryViewModel.class);

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        activityRecyclerView = view.findViewById(R.id.OperatorRecyclerView);

        setupChatRecyclerView();
        setupActivityRecyclerView();

        observeViewModel();
    }

    /**
     * RecyclerView hiển thị conversation lịch sử.
     */
    private void setupChatRecyclerView() {
        chatAdapter = new ChatListAdapter(getContext());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    /**
     * [CẬP NHẬT] Setup Activity RecyclerView với Adapter mới (có listener).
     */
    private void setupActivityRecyclerView() {
        // Thêm listener xử lý sự kiện click vào item activity
        activityAdapter = new OperatorAdapter(getContext(), activity -> {
            // Xử lý khi click vào activity (Ví dụ: Chuyển sang trang chi tiết hoặc check-in)
            Toast.makeText(getContext(), "Đã chọn: " + activity.getTitle(), Toast.LENGTH_SHORT).show();
        });

        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityRecyclerView.setAdapter(activityAdapter);
    }

    /**
     * Quan sát dữ liệu conversation và activity từ ViewModel.
     */
    private void observeViewModel() {
        // 1. Lắng nghe danh sách Chat
        viewModel.getConversations().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) chatAdapter.setList(resource.data);
                    break;
                case ERROR:
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING: break;
            }
        });

        // 2. Lắng nghe danh sách Activity
        viewModel.getActivities().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) activityAdapter.setList(resource.data);
                    break;
                case ERROR:
                    Toast.makeText(getContext(), "Lỗi tải hoạt động: " + resource.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING: break;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadConversations();
            viewModel.loadJoinedActivities();
        }
    }
}