package com.example.nhom4.ui.page.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.nhom4.R;
import com.example.nhom4.ui.adapter.VerticalPagerAdapter;

public class CenterFragment extends Fragment {

    private ViewPager2 viewPagerVertical;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_center_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerVertical = view.findViewById(R.id.viewPagerVertical);

        // Gán Adapter dọc vào đây
        VerticalPagerAdapter adapter = new VerticalPagerAdapter(this);
        viewPagerVertical.setAdapter(adapter);

        // Đảm bảo hướng lướt là Dọc
        viewPagerVertical.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
    }
}
