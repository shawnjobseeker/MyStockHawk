package com.udacity.stockhawk;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.udacity.stockhawk.ui.MainActivity;

import timber.log.Timber;

public class StockHawkApp extends Application {

    public Handler threadHandler;
    private AppCompatActivity currentActivity;
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
        threadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj instanceof String && currentActivity instanceof MainActivity) {
                    if (((MainActivity)currentActivity).networkUp())
                        Toast.makeText(currentActivity, getString(R.string.error_no_such_stock, msg.obj), Toast.LENGTH_SHORT).show();
                    ((MainActivity)currentActivity).dismissSwipeRefresh();
                }

            }
        };

    }

    public void setCurrentActivity(AppCompatActivity currentActivity) {
        this.currentActivity = currentActivity;
    }
}