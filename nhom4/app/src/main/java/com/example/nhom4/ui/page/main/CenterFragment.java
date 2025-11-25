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
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.R;
import com.example.nhom4.data.Resource;
import com.example.nhom4.ui.adapter.VerticalPagerAdapter;
import com.example.nhom4.ui.viewmodel.MainViewModel;

public class CenterFragment extends Fragment {

    private ViewPager2 viewPagerVertical;
    private VerticalPagerAdapter adapter;

    // Sử dụng MainViewModel để lấy dữ liệu Post (ViewModel này chứa logic PostRepository)
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_center_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init ViewModel (Dùng requireActivity() để chia sẻ dữ liệu với MainFragment nếu cần)
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        viewPagerVertical = view.findViewById(R.id.viewPagerVertical);

        setupAdapter();
        observeViewModel();
    }

    private void setupAdapter() {
        adapter = new VerticalPagerAdapter(this);
        viewPagerVertical.setAdapter(adapter);
        viewPagerVertical.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
    }

    private void observeViewModel() {
        // Lắng nghe danh sách bài viết từ ViewModel
        viewModel.getPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Cập nhật dữ liệu vào Adapter (VerticalPagerAdapter cần có hàm setPostList)
                adapter.setPostList(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Lỗi tải bài viết: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void navigateToCamera() {
        if (viewPagerVertical != null) {
            viewPagerVertical.setCurrentItem(0, true);
        }
    }

    // Không cần onResume load lại vì ViewModel sử dụng SnapshotListener (Realtime)
}
