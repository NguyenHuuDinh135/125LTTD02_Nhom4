package com.example.nhom4.ui.page.main;

import android.os.Bundle;
import android.util.Log;
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
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_center_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        viewPagerVertical = view.findViewById(R.id.viewPagerVertical);
        setupAdapter();
        observeViewModel();
    }

    private void setupAdapter() {
        adapter = new VerticalPagerAdapter(this);
        viewPagerVertical.setAdapter(adapter);
        viewPagerVertical.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        // Tùy chọn: Tắt hiệu ứng overscroll để mượt hơn
        viewPagerVertical.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    private void observeViewModel() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Log để kiểm tra xem có nhận được list mới không
                Log.d("CenterFragment", "New Posts received: " + resource.data.size());

                // Cập nhật list vào adapter.
                // Nhờ hàm getItemId đã sửa, ViewPager sẽ tự biết bài nào mới để hiện ra.
                adapter.setPostList(resource.data);
            } else if (resource.status == Resource.Status.ERROR) {
                Toast.makeText(getContext(), "Lỗi: " + resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void navigateToCamera() {
        if (viewPagerVertical != null) {
            viewPagerVertical.setCurrentItem(0, true);
        }
    }
}