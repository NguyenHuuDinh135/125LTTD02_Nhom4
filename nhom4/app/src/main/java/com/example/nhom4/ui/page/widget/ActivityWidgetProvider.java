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
    }
}