package com.udacity.stockhawk.ui;


import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    public static final String[] ARABIC_NUMERALS = {"\u0660", "\u0661", "\u0662", "\u0663", "\u0664", "\u0665", "\u0666", "\u0667", "\u0668", "\u0669"};
    private final Context context;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }
    public static String convertToArabicNumerals(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char p = input.charAt(i);
            if (Character.isDigit(p)) {
                int pInt = (int)p;
                if (pInt >= 48 && pInt <= 57)
                    builder.append(ARABIC_NUMERALS[(int)p - 48]);
                else
                    builder.append(p);
            }
            else
                builder.append(p);
        }
        return builder.toString();
    }
    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {

        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }


    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);

        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        cursor.moveToPosition(position);
        holder.name.setText(cursor.getString(Contract.Quote.POSITION_NAME));
        holder.symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
        holder.priceValue = cursor.getFloat(Contract.Quote.POSITION_PRICE);
        String priceString = dollarFormat.format(holder.priceValue);


        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
        }
        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);
        if (context.getResources().getConfiguration().locale.getLanguage().equals("ar")) {
            priceString = convertToArabicNumerals(priceString);
            change = convertToArabicNumerals(change);
            percentage = convertToArabicNumerals(percentage);
        }

        holder.price.setText(priceString);
        if (PrefUtils.getDisplayMode(context)
                .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            holder.change.setText(change);
        } else {
            holder.change.setText(percentage);
        }
        String accessibilityChange = holder.change.getText().toString();
        if (accessibilityChange.startsWith("+") || accessibilityChange.startsWith("-"))
            accessibilityChange = accessibilityChange.substring(1);
        if (rawAbsoluteChange > 0)
            holder.itemView.setContentDescription(holder.name.getText() + context.getString(R.string.rose_by, accessibilityChange, priceString));
        else
            holder.itemView.setContentDescription(holder.name.getText() + context.getString(R.string.fell_by, accessibilityChange, priceString));
        holder.parseHistoryData(cursor.getString(Contract.Quote.POSITION_HISTORY));


    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }



    interface StockAdapterOnClickHandler {
        void onClick(String symbol);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.symbol)
        TextView symbol;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.change)
        TextView change;
        private DataPoint[] points;
        private double priceValue, mostRecent, secondMostRecent;
        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            int symbolColumn = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            clickHandler.onClick(cursor.getString(symbolColumn));
            GraphView graphView = new GraphView(context);
            plotLineGraph(graphView);
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setView(graphView)
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            if (!name.getText().toString().isEmpty())
                builder.setTitle(context.getString(R.string.history_for, name.getText().toString()));
            builder.create().show();

        }
        void parseHistoryData(String history) {
            String[] eachQuote = history.split("\n");
            points = new DataPoint[eachQuote.length];
            for (int i = 0; i < points.length; i++) {
                String[] quoteData = eachQuote[points.length-1-i].split(", ");
                points[i] = new DataPoint(Long.parseLong(quoteData[0]), Double.parseDouble(quoteData[1]));
                if (i == 0)
                    mostRecent = Double.parseDouble(quoteData[1]);
                if (i == 1)
                    secondMostRecent = Double.parseDouble(quoteData[1]);
            }
            if (priceValue == 0.00) {
                price.setText(Double.toString(mostRecent));
                double absoluteChange = secondMostRecent - mostRecent;
                double relativeChange = absoluteChange / mostRecent;
                String changeFormatted = dollarFormatWithPlus.format(absoluteChange);
                String percentage = percentageFormat.format(relativeChange);

                if (PrefUtils.getDisplayMode(context)
                        .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
                    change.setText(changeFormatted);
                } else {
                    change.setText(percentage);
                }
            }
        }
        private void plotLineGraph(GraphView historyGraph) {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

            historyGraph.addSeries(new LineGraphSeries<DataPoint>(points));
            historyGraph.setTitle(context.getString(R.string.history));
            historyGraph.getViewport().setScalable(true);
            historyGraph.getViewport().setScrollable(true);
            historyGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context, format));
            historyGraph.getGridLabelRenderer().setVerticalAxisTitle(context.getString(R.string.value));
            historyGraph.getGridLabelRenderer().setHorizontalLabelsAngle(135);
        }


    }
}
