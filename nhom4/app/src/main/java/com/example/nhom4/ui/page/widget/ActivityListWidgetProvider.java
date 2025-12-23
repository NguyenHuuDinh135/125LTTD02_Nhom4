package com.example.nhom4.ui.page.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.nhom4.MainActivity;
import com.example.nhom4.R;

public class ActivityListWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.activity_list_widget);

            // Trỏ đến RemoteViewsService
            Intent intent = new Intent(context, ActivityListWidgetService.class);
            views.setRemoteAdapter(R.id.list_activities, intent);

            // Empty view
            views.setEmptyView(R.id.list_activities, R.id.widget_empty_text);

            // Click template (nếu cần mở activity cụ thể)
            Intent clickIntent = new Intent(context, MainActivity.class);
            views.setPendingIntentTemplate(R.id.list_activities, PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}