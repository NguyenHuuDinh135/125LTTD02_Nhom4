package com.example.nhom4.ui.page.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;
import com.example.nhom4.ui.page.activity.DetailActivity;

public class ActivityListWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.activity_list_widget);

        // Set RemoteAdapter cho ListView
        Intent serviceIntent = new Intent(context, ActivityWidgetRemoteViewsService.class);
        views.setRemoteAdapter(R.id.lv_activities, serviceIntent);

        views.setEmptyView(R.id.lv_activities, R.id.tv_empty);

        // ===== TEMPLATE INTENT: MỞ THẲNG DETAILACTIVITY =====
        Intent templateIntent = new Intent(context, DetailActivity.class);
        templateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Không cần putExtra cố định ở đây, vì sẽ được fill từ factory

        PendingIntent templatePendingIntent = PendingIntent.getActivity(
                context,
                0,
                templateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Áp dụng template cho ListView – mỗi item sẽ nhận ACTIVITY_ID từ fillInIntent
        views.setPendingIntentTemplate(R.id.lv_activities, templatePendingIntent);

        // Optional: Click vào phần nền widget (không phải item) → mở MainActivity
        Intent rootIntent = new Intent(context, MainActivity.class);
        rootIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent rootPendingIntent = PendingIntent.getActivity(
                context,
                1,  // requestCode khác để tránh conflict
                rootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root_layout, rootPendingIntent);

        // Cập nhật widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.lv_activities);
    }

    public static void forceUpdate(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName widget = new ComponentName(context, ActivityListWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(widget);
        if (ids.length > 0) {
            manager.notifyAppWidgetViewDataChanged(ids, R.id.lv_activities);
        }
    }
}