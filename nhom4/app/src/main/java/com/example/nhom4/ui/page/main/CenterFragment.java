package com.example.nhom4.ui.page.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        observePosts();
        observeOpenPostId(); // Quan trọng: lắng nghe yêu cầu mở post từ widget
    }

    private void setupAdapter() {
        adapter = new VerticalPagerAdapter(this);
        viewPagerVertical.setAdapter(adapter);
        viewPagerVertical.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPagerVertical.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    // Observe danh sách bài viết
    private void observePosts() {
        viewModel.getPosts().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                adapter.setPostList(resource.data);
                tryScrollToOpenPost(); // Sau khi cập nhật data → thử mở post nếu cần
            }
        });
    }

    // Observe yêu cầu mở một post cụ thể (từ widget)
    private void observeOpenPostId() {
        viewModel.getOpenPostId().observe(getViewLifecycleOwner(), postId -> {
            if (postId != null) {
                tryScrollToOpenPost(); // Mỗi khi có postId mới → thử scroll ngay
            }
        });
    }

    // Hàm chung: thử scroll đến post nếu có trong adapter
    private void tryScrollToOpenPost() {
        String postId = viewModel.getOpenPostId().getValue();
        if (postId == null || adapter == null || adapter.getItemCount() <= 1) {  // <=1: chỉ có Camera hoặc chưa có data
            return;
        }

        // Duyệt qua các position trong ViewPager
        for (int viewPagerPosition = 1; viewPagerPosition < adapter.getItemCount(); viewPagerPosition++) {
            String currentPostId = adapter.getPostIdAt(viewPagerPosition - 1);  // -1 để chuyển sang index list

            if (postId.equals(currentPostId)) {
                Log.d("CenterFragment", "Scrolling to post: " + postId + " at ViewPager position " + viewPagerPosition);
                viewPagerVertical.setCurrentItem(viewPagerPosition, false);
                viewModel.clearOpenPostId();
                return;
            }
        }

        Log.d("CenterFragment", "Post " + postId + " not found yet – waiting for next data update");
    }

    public void navigateToCamera() {
        if (viewPagerVertical != null) {
            viewPagerVertical.setCurrentItem(0, true);
        }
    }
}