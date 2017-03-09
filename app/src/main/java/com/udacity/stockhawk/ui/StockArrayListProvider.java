package com.udacity.stockhawk.ui;

import android.app.LauncherActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import yahoofinance.Stock;

import static com.udacity.stockhawk.ui.MainActivity.STOCK_LOADER;
import static com.udacity.stockhawk.ui.StockAdapter.convertToArabicNumerals;

/**
 * Created by Jonathan on 09/03/2017.
 */

public class StockArrayListProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Cursor cursor;
    private int appWidgetId;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;
    public StockArrayListProvider(Context context, Intent intent) {

        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

    }

    @Override
    public void onCreate() {

    }


    @Override
    public int getCount() {
        if (cursor == null)
            return 0;
        Log.d("appWidget", "cursor = " + Integer.toString(cursor.getCount()));
        return cursor.getCount();

    }

    @Override
    public void onDataSetChanged() {

        if (cursor != null)
            cursor.close();
        cursor = context.getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
        Log.d("appWidget", "cursor = " + Integer.toString(cursor.getCount()));
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }

    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }
    @Override
    public RemoteViews getViewAt(int position) {
        cursor.moveToPosition(position);
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.list_item_widget);
        remoteView.setTextViewText(R.id.name, cursor.getString(Contract.Quote.POSITION_NAME));
        remoteView.setTextViewText(R.id.symbol, cursor.getString(Contract.Quote.POSITION_SYMBOL));
        float priceValue = cursor.getFloat(Contract.Quote.POSITION_PRICE);
        String priceString = dollarFormat.format(priceValue);

        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }
        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);
        if (context.getResources().getConfiguration().locale.getLanguage().equals("ar")) {
            priceString = convertToArabicNumerals(priceString);
            change = convertToArabicNumerals(change);
            percentage = convertToArabicNumerals(percentage);
        }
        remoteView.setTextViewText(R.id.price, priceString);
        if (PrefUtils.getDisplayMode(context)
                .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            remoteView.setTextViewText(R.id.change, change);
        } else {
            remoteView.setTextViewText(R.id.change, percentage);
        }
        return remoteView;
    }
}
