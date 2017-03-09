package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.StockArrayListProvider;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;
import static com.udacity.stockhawk.ui.MainActivity.STOCK_LOADER;

/**
 * Created by Jonathan on 09/03/2017.
 */

public class WidgetService extends RemoteViewsService {

    private StockArrayListProvider provider;


    @Override
    public void onCreate() {
        super.onCreate();
        QuoteSyncJob.initialize(this);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("appWidget", "onGetViewFactory()");
           return new StockArrayListProvider(this, intent);

    }




    @Override
    public void onDestroy() {

    }
}
