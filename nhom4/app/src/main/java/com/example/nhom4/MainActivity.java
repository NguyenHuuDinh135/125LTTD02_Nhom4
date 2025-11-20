package com.example.nhom4;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.ui.page.friend.FriendsBottomSheet;
import com.example.nhom4.ui.page.post.PostAdapter; // Import adapter mới
import com.example.nhom4.ui.page.adapter.MainPagerAdapter;

import com.example.nhom4.ui.page.friend.FriendsBottomSheet;
import com.example.nhom4.ui.page.post.PostAdapter; // Import adapter mới

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPagerMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);


        viewPagerMain = findViewById(R.id.viewPagerMain);

        // --- QUAN TRỌNG: Khởi tạo và gán Adapter ---
        MainPagerAdapter mainAdapter = new MainPagerAdapter(this);
        viewPagerMain.setAdapter(mainAdapter);

        // Đảm bảo hướng lướt là ngang (Horizontal)
        viewPagerMain.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // Tắt hiệu ứng overscroll nếu muốn cảm giác giống Locket hơn (tuỳ chọn)
        // viewPagerMain.setOverScrollMode(ViewPager2.OVER_SCROLL_NEVER);
    }

    // Hàm hỗ trợ chuyển trang từ code (ví dụ bấm nút)
    public void navigateToCalendar() {
        viewPagerMain.setCurrentItem(1, true); // true là có hiệu ứng lướt
    }

    public void navigateToFeed() {
        viewPagerMain.setCurrentItem(0, true);
        ViewPager2 viewPager = findViewById(R.id.viewPagerPosts); // ID mới
        PostAdapter adapter = new PostAdapter(this); // Adapter mới
        viewPager.setAdapter(adapter);
        //Gọi FriendsBottomSheet
        //new FriendsBottomSheet().show(getSupportFragmentManager(), "sheet");
    }
}
