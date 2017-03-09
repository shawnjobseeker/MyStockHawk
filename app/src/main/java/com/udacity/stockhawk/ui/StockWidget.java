package com.udacity.stockhawk.ui;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.WidgetService;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;


/**
 * Created by Jonathan on 09/03/2017.
 */

public class StockWidget extends AppWidgetProvider {


    //from https://laaptu.wordpress.com/2013/07/19/android-app-widget-with-listview/
    @Override
    public void onUpdate(Context context, AppWidgetManager
            appWidgetManager,int[] appWidgetIds) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_view);


        for (int appWidgetId : appWidgetIds) {
            Intent updateIntent = new Intent(context, WidgetService.class);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            updateIntent.setData(Uri.parse(updateIntent.toUri(Intent.URI_INTENT_SCHEME)));

                views.setRemoteAdapter(R.id.list_view, updateIntent);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.d("appWidget", "updated");
        }

    }


}
