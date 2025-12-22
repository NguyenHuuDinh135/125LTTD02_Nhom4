package com.example.nhom4;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.emoji2.bundled.BundledEmojiCompatConfig;
import androidx.emoji2.text.EmojiCompat;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo EmojiCompat với bundled font (luôn hoạt động, không cần download)
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);
    }
}
