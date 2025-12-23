package com.example.nhom4.ui.page.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.nhom4.R;
import com.example.nhom4.ui.adapter.ChatListAdapter;
import com.example.nhom4.ui.adapter.OperatorAdapter;
import com.example.nhom4.ui.page.activity.DetailActivity;
import com.example.nhom4.ui.viewmodel.DiscoveryViewModel;

public class DiscoveryFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private RecyclerView activityRecyclerView;

    private ChatListAdapter chatAdapter;
    private OperatorAdapter activityAdapter;

    private DiscoveryViewModel viewModel;

    // Receiver nhận tín hiệu refresh từ nơi khác (ví dụ: accept friend)
    private final BroadcastReceiver refreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshData();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đăng ký nhận broadcast
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(refreshReceiver, new IntentFilter("REFRESH_CHAT_LIST"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DiscoveryViewModel.class);

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        activityRecyclerView = view.findViewById(R.id.OperatorRecyclerView);

        setupChatRecyclerView();
        setupActivityRecyclerView();

        observeViewModel();
    }

    /**
     * Hàm public để Activity có thể gọi khi vuốt ViewPager tới tab này
     */
    public void refreshData() {
        if (viewModel != null) {
            viewModel.loadConversations();
            viewModel.loadJoinedActivities();
        }
    }

    // Sự kiện Lifecycle: Chạy khi quay lại từ màn hình khác (DetailActivity -> Back)
    // Hoặc khi App từ background -> foreground
    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void setupChatRecyclerView() {
        chatAdapter = new ChatListAdapter(getContext());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void setupActivityRecyclerView() {
        activityAdapter = new OperatorAdapter(getContext(), activity -> {
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra("ACTIVITY", activity);
            startActivity(intent);
        });

        activityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityRecyclerView.setAdapter(activityAdapter);
    }

    private void observeViewModel() {
        viewModel.getConversations().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case SUCCESS:
                    if (resource.data != null) {
                        chatAdapter.setList(resource.data);
                    }
                    break;
                case ERROR:
                    // Có thể ẩn Toast nếu không muốn làm phiền người dùng khi auto-reload
                    // Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING: break;
            }
        });

        viewModel.getActivities().observe(getViewLifecycleOwner(), resource -> {
            if (resource.data != null) activityAdapter.setList(resource.data);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshReceiver);
    }
}