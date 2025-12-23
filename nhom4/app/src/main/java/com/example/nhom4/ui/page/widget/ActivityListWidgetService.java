package com.example.nhom4.ui.page.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ActivityListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ActivityListRemoteViewsFactory(this.getApplicationContext());
    }
}

class ActivityListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private List<Activity> activities = new ArrayList<>();

    public ActivityListRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() {
        activities.clear();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        CountDownLatch latch = new CountDownLatch(1);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("activities")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        Activity activity = doc.toObject(Activity.class);
                        activity.setId(doc.getId());
                        activities.add(activity);
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        try {
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS); // Chờ tối đa 10s
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onDestroy() {
        activities.clear();
    }

    @Override
    public int getCount() {
        return activities.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position >= activities.size()) return null;

        Activity activity = activities.get(position);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_activity_item);

        views.setTextViewText(R.id.tv_activity_title, activity.getTitle() != null ? activity.getTitle() : "Hoạt động");
        views.setTextViewText(R.id.tv_activity_progress, activity.getProgress() + "/" + activity.getTarget());

        // Ảnh: dùng placeholder trước (vì Picasso đồng bộ không ổn định trong factory)
        views.setImageViewResource(R.id.img_activity_icon, R.drawable.default_image);

        // Click mở app
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("ACTIVITY_ID", activity.getId());
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}