package com.example.nhom4.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.nhom4.ui.page.calendar.HabitFragment;
import com.example.nhom4.ui.page.calendar.StreakFragment;

/**
 * Adapter cho ViewPager2 tại màn hình Calendar, quản lý 2 tab Streak và Habit.
 */
public class CalendarPagerAdapter extends FragmentStateAdapter {

    /**
     * @param fragment fragment cha chứa ViewPager2 (dùng childFragmentManager)
     */
    public CalendarPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    /**
     * Tạo Fragment tương ứng với từng tab.
     * @param position
     * @return
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new StreakFragment(); // Tab đầu tiên: lịch streak
        }
        return new HabitFragment(); // Tab thứ hai: danh sách habit
    }

    /**
     * Trả về số lượng tab.
     * @return
     */
    @Override
    public int getItemCount() {
        return 2; // Số lượng Tabs (Streak & Habit)
    }
}
