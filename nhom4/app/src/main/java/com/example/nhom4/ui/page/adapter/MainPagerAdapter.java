package com.example.nhom4.ui.page.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.ui.page.calendar.CalendarFragment;
import com.example.nhom4.ui.page.DiscoveryFragment;
import com.example.nhom4.ui.page.feed.FeedContainerFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CalendarFragment();      // Trái
            case 2:
                return new DiscoveryFragment();     // Phải
            default:
                return new FeedContainerFragment(); // Giữa (Vị trí 1)
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Tổng cộng 3 màn hình
    }
}
