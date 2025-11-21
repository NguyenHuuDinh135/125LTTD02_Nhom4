    package com.example.nhom4.ui.adapter;

    import androidx.annotation.NonNull;
    import androidx.fragment.app.Fragment;
    import androidx.fragment.app.FragmentActivity;
    import androidx.viewpager2.adapter.FragmentStateAdapter;

    import com.example.nhom4.ui.page.main.DiscoveryFragment;
    import com.example.nhom4.ui.page.main.CenterFragment;
    import com.example.nhom4.ui.page.calendar.CalendarFragment; // Ví dụ trang bên phải

    public class MainPagerAdapter extends FragmentStateAdapter {

        public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    // Màn hình bên TRÁI
                    return new CalendarFragment(); // Hoặc Fragment bạn muốn
                case 1:
                    // Màn hình GIỮA (Chứa Main + Feed theo chiều dọc)
                    return new CenterFragment();
                case 2:
                    // Màn hình bên PHẢI
                    return new DiscoveryFragment();
                default:
                    return new CenterFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Trái - Giữa - Phải
        }
    }
