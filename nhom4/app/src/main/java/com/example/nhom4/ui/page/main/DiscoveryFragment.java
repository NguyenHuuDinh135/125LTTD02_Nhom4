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
import com.example.nhom4.ui.viewmodel.DiscoveryViewModel;

public class DiscoveryFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private ChatListAdapter chatAdapter;

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
        // operatorRecyclerView = view.findViewById(R.id.OperatorRecyclerView); // Nếu chưa dùng thì tạm ẩn

        setupChatRecyclerView();
        observeViewModel();
    }

    private void setupChatRecyclerView() {
        chatAdapter = new ChatListAdapter(getContext());
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void observeViewModel() {
        // Lắng nghe danh sách hội thoại đã được xử lý logic từ ViewModel
        viewModel.getConversations().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    // Có thể hiện ProgressBar nếu muốn
                    break;
                case SUCCESS:
                    if (resource.data != null) {
                        chatAdapter.setList(resource.data);
                    }
                    break;
                case ERROR:
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    // Nếu muốn refresh thủ công khi quay lại
    @Override
    public void onResume() {
        super.onResume();
        // viewModel.loadConversations(); // Optional: Nếu Repository chưa dùng Realtime Listener
    }
}
