package com.example.nhom4.ui.page.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.nhom4.R;
import com.example.nhom4.data.bean.Activity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ActivityWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private List<Activity> activities = new ArrayList<>();

    public ActivityWidgetRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() {
        activities.clear();

        var currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d("WidgetFactory", "User not logged in");
            return;
        }

        try {
            QuerySnapshot snapshot = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("activities")
                            .whereEqualTo("creatorId", currentUser.getUid())
                            .orderBy("createdAt", Query.Direction.DESCENDING) // ← THÊM DÒNG NÀY
                            // .limit(50) // nếu cần lấy nhiều hơn
                            .get(),
                    8, TimeUnit.SECONDS
            );

            for (var doc : snapshot.getDocuments()) {
                Activity activity = doc.toObject(Activity.class);
                if (activity != null) {
                    activity.setId(doc.getId());
                    if (activity.getProgress() < activity.getTarget()) {
                        activities.add(activity);
                    }
                }
            }

            Log.d("WidgetFactory", "Loaded " + activities.size() + " unfinished activities");

        } catch (Exception e) {
            Log.e("WidgetFactory", "Load failed", e);
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Activity activity = activities.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.item_widget_activity);

        views.setTextViewText(R.id.tv_title, activity.getTitle());

        long durationMin = activity.getDurationSeconds() / 60;
        String desc = String.format(Locale.getDefault(), "%d phút • %s",
                durationMin, activity.isDaily() ? "Hằng ngày" : "Mục tiêu: " + activity.getTarget());
        views.setTextViewText(R.id.tv_desc, desc);

        views.setTextViewText(R.id.tv_progress,
                String.format(Locale.getDefault(), "%d/%d", activity.getProgress(), activity.getTarget()));

        int percent = activity.getTarget() > 0 ?
                (int) (((float) activity.getProgress() / activity.getTarget()) * 100) : 0;
        views.setProgressBar(R.id.progress_bar, 100, percent, false);

        views.setImageViewResource(R.id.img_icon, R.drawable.ic_launcher_foreground);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("ACTIVITY_ID", activity.getId());
        views.setOnClickFillInIntent(R.id.item_root, fillInIntent);

        return views;
    }

    @Override public int getCount() { return activities.size(); }
    @Override public RemoteViews getLoadingView() { return null; }
    @Override public int getViewTypeCount() { return 1; }
    @Override public long getItemId(int position) { return position; }
    @Override public boolean hasStableIds() { return true; }
    @Override public void onDestroy() { activities.clear(); }
}