package com.example.nhom4.ui.page.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ActivityWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ActivityWidgetRemoteViewsFactory(this.getApplicationContext());
    }
}