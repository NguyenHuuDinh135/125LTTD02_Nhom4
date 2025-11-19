package com.example.nhom4;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nhom4.ui.page.friend.FriendsBottomSheet;
import com.example.nhom4.ui.page.post.PostAdapter; // Import adapter mới

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.viewPagerPosts); // ID mới
        PostAdapter adapter = new PostAdapter(this); // Adapter mới
        viewPager.setAdapter(adapter);
        //Gọi FriendsBottomSheet
        //new FriendsBottomSheet().show(getSupportFragmentManager(), "sheet");
    }
}
    