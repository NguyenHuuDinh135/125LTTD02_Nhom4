package com.example.nhom4.ui.page;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.R;
import com.example.nhom4.ui.page.story.StoryAdapter;
import com.example.nhom4.ui.page.story.ThumbnailAdapter;

public class StoryYourAllActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_your_all);

        // 1. KHAI BÁO DANH SÁCH 5 ẢNH (Resource ID)
        // Đảm bảo bạn đã copy ảnh vào res/drawable và đặt tên đúng
        int[] myImages = {
                R.drawable.img_1,
                R.drawable.img_1,
                R.drawable.img_1,
                R.drawable.img_1,
                R.drawable.img_1
        };

        // 2. Setup ViewPager (Ảnh to)
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        // Truyền mảng ảnh vào Adapter
        StoryAdapter storyAdapter = new StoryAdapter(myImages);
        viewPager.setAdapter(storyAdapter);

        // 3. Setup RecyclerView (Ảnh nhỏ)
        RecyclerView rvThumbnails = findViewById(R.id.rvThumbnails);
        // Truyền mảng ảnh vào Adapter
        ThumbnailAdapter thumbAdapter = new ThumbnailAdapter(myImages);

        rvThumbnails.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvThumbnails.setAdapter(thumbAdapter);

        // 4. Setup SnapHelper (Căn giữa thumbnail)
        new LinearSnapHelper().attachToRecyclerView(rvThumbnails);

        // 5. Đồng bộ ViewPager -> RecyclerView
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                thumbAdapter.setSelectedPosition(position);
                rvThumbnails.smoothScrollToPosition(position);
            }
        });
    }
}