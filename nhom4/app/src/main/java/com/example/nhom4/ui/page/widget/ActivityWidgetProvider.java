package com.example.nhom4.ui.page.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class ActivityWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // Gọi JobIntentService
        Intent intent = new Intent(context, ActivityWidgetService.class);
        ActivityWidgetService.enqueueWork(context, intent);
        // Bật auto update
        WidgetUpdateScheduler.schedule(context);
    }
    //    Nhận TẤT CẢ broadcast gửi tới widget
    //     * (bao gồm cả broadcast từ AlarmManager)
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            ActivityWidgetService.enqueueWork(context, intent);
        }
    }
}