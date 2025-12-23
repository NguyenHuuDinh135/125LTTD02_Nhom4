package com.example.nhom4.ui.page.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.nhom4.MyApplication; // hoặc context bạn có thể lấy global
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class WidgetLiveUpdater {

    private static final String TAG = "WidgetLiveUpdater";
    private static WidgetLiveUpdater instance;
    private ListenerRegistration listenerRegistration;
    private Context context;

    private WidgetLiveUpdater(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized WidgetLiveUpdater getInstance(Context context) {
        if (instance == null) {
            instance = new WidgetLiveUpdater(context);
        }
        return instance;
    }

    public void startListening() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d(TAG, "User chưa đăng nhập, không listen widget");
            return;
        }

        if (listenerRegistration != null) {
            return; // đã đang listen
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Chỉ lắng nghe post của chính user hiện tại (vì widget hiển thị post mới nhất của mình)
        Query query = FirebaseFirestore.getInstance()
                .collection("posts")
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1);

        listenerRegistration = query.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e(TAG, "Listen failed", error);
                return;
            }

            if (snapshot != null && !snapshot.isEmpty()) {
                // Có thay đổi → có thể là post mới hoặc chỉnh sửa
                // Cập nhật widget ngay lập tức
                updateWidgetNow();
                Log.d(TAG, "Có thay đổi post → cập nhật widget ngay");
            }
        });

        Log.d(TAG, "Bắt đầu lắng nghe post mới cho widget");
    }

    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
            Log.d(TAG, "Dừng lắng nghe widget");
        }
    }

    private void updateWidgetNow() {
        // Gọi service để update widget (tái sử dụng logic cũ của bạn)
        Intent intent = new Intent(context, ActivityWidgetService.class);
        ActivityWidgetService.enqueueWork(context, intent);
    }

    // Gọi method này để force update widget (ví dụ khi mở app)
    public void forceUpdate() {
        updateWidgetNow();
    }
}