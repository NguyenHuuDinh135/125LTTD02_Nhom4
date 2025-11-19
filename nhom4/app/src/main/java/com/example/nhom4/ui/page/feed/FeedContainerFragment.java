package com.example.nhom4.ui.page.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.R;
import com.example.nhom4.ui.page.post.PostAdapter;

public class FeedContainerFragment extends Fragment {

    private ViewPager2 viewPagerPosts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerPosts = view.findViewById(R.id.viewPagerPosts);

        // --- SỬA ĐỔI TẠI ĐÂY ---
        // Vì PostAdapter hiện tại của bạn đang dùng dữ liệu giả (fake data)
        // và chỉ có constructor nhận FragmentActivity, nên ta chỉ truyền requireActivity().
        PostAdapter adapter = new PostAdapter(requireActivity());
        viewPagerPosts.setAdapter(adapter);

        // Xử lý nút bấm Calendar để chuyển tab của ViewPager Cha (MainPagerAdapter)
        View btnCalendar = view.findViewById(R.id.btnOpenCalendar);
        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v -> {
                // Gọi về Activity để chuyển sang trang Calendar
                if (getActivity() instanceof com.example.nhom4.MainActivity) {
                    ((com.example.nhom4.MainActivity) getActivity()).navigateToCalendar();
                }
            });
        }
    }
}
