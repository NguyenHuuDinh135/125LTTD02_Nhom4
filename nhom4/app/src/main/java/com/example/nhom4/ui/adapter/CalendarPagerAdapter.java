package com.example.nhom4.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.ui.page.calendar.HabitFragment;
import com.example.nhom4.ui.page.calendar.StreakFragment;

public class CalendarPagerAdapter extends FragmentStateAdapter {

    public CalendarPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new StreakFragment();
        }
        return new HabitFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // Số lượng Tabs (Streak & Habit)
    }
}
