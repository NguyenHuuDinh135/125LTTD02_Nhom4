package com.example.nhom4.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

/**
 * PageTransformer để tạo hiệu ứng peek (hiển thị một phần page bên cạnh)
 * và depth effect (scale) cho ViewPager2.
 * 
 * Tối ưu với constants và tính toán hiệu quả.
 */
public class PeekPageTransformer implements ViewPager2.PageTransformer {

    // Constants để dễ maintain và thay đổi
    private static final float MIN_SCALE = 0.9f;
    private static final float SCALE_DELTA = 0.1f; // 10% scale reduction per position
    private static final float ALPHA_HIDDEN = 0f;
    private static final float ALPHA_VISIBLE = 1f;

    private final int marginPx;

    public PeekPageTransformer(int marginPx) {
        this.marginPx = marginPx;
    }

    @Override
    public void transformPage(@NonNull View page, float position) {
        // position: 
        // -1: page ở bên trái (hoàn toàn ngoài màn hình)
        // 0: page ở giữa (hiển thị đầy đủ)
        // 1: page ở bên phải (hoàn toàn ngoài màn hình)

        if (position < -1 || position > 1) {
            // Page hoàn toàn ngoài màn hình
            page.setAlpha(ALPHA_HIDDEN);
            return;
        }

        // Page đang trong vùng hiển thị
        page.setAlpha(ALPHA_VISIBLE);

        // Tính scale factor: page ở giữa (position = 0) có scale = 1.0
        // Page bên cạnh (position = ±1) có scale = MIN_SCALE để tạo hiệu ứng depth
        float absPosition = Math.abs(position);
        float scaleFactor = Math.max(MIN_SCALE, 1f - (absPosition * SCALE_DELTA));
        
        page.setScaleX(scaleFactor);
        page.setScaleY(scaleFactor);
    }
}
