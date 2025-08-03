package ca.concordia.smartsortandroidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.graphics.Typeface;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.widget.ProgressBar;

public class MainActivity extends NavigationBar {
    private Button detailedHistoryLink;
    private BarChart barChart;
    private PieChart pieChartRecyclable, pieChartOthers;
    private TextView todayTab, weekTab, monthTab;
    private LinearLayout tabContainer;
    private TextView totalCountView;
    private ProgressBar loadingSpinner;
    private ResultController controller;

    private List<PredictionResult> todayResults = new ArrayList<>();
    private List<PredictionResult> weekResults = new ArrayList<>();
    private List<PredictionResult> monthResults = new ArrayList<>();

    private int canCountToday = 0;
    private int bottleCountToday = 0;

    private int canCountWeek = 0;
    private int bottleCountWeek = 0;

    private int canCountMonth = 0;
    private int bottleCountMonth = 0;

    private int recyclableCountToday = 0;
    private int OthersToday = 0;

    private int recyclableCountWeek = 0;
    private int OthersWeek = 0;

    private int recyclableCountMonth = 0;
    private int OthersMonth = 0;
    private String currentRecyclable = "cans_bottles";
    private SharedPreferences cache;
    private Gson gson;
    private Type listType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout with DrawerLayout, Toolbar, NavigationView, and a FrameLayout to hold main content
        setContentView(R.layout.activity_navigation_bar);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        View contentView = getLayoutInflater().inflate(R.layout.activity_main, contentFrame, false);
        contentFrame.addView(contentView);

        setupDrawer();
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentRecyclable = userPrefs.getString("current_recyclable", "cans_bottles"); // default

        barChart = contentView.findViewById(R.id.barChart);
        pieChartRecyclable = contentView.findViewById(R.id.pieChartRecyclable);
        pieChartOthers = contentView.findViewById(R.id.pieChartOthers);
        todayTab = contentView.findViewById(R.id.todayOption);
        weekTab = contentView.findViewById(R.id.weekOption);
        monthTab = contentView.findViewById(R.id.monthOption);
        tabContainer = contentView.findViewById(R.id.stateContainer);
        totalCountView = contentView.findViewById(R.id.totalItems);
        loadingSpinner = contentView.findViewById(R.id.loadingSpinner);
        loadingSpinner.setVisibility(View.VISIBLE);

        controller = new ResultController();

        cache = getSharedPreferences("PredictionCache", MODE_PRIVATE);
        gson = new Gson();
        listType = new TypeToken<List<PredictionResult>>() {}.getType();

        // Load cached data if any
        loadCache();

        setupTabListeners();

        // Subscribe to FCM topic
        FirebaseMessaging.getInstance().subscribeToTopic("binAlerts")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Subscribed to binAlerts");
                    } else {
                        Log.e("FCM", "Subscription failed", task.getException());
                    }
                });

        // Start classification service
        Intent serviceIntent = new Intent(this, ClassificationService.class);
        startService(serviceIntent);

        // Fetch fresh data in background
        fetchLatestLists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.smartsort.CLASSIFICATION_COMPLETE");
        registerReceiver(classificationCompleteReceiver, filter, Context.RECEIVER_EXPORTED);
        fetchLatestLists();// when coming back from other activity
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(classificationCompleteReceiver);
    }

    private int getRecyclableCountForSetting(String period) {
        switch (currentRecyclable) {
            case "cans":
                if (period.equals("today")) return canCountToday;
                else if (period.equals("week")) return canCountWeek;
                else return canCountMonth;

            case "bottles":
                if (period.equals("today")) return bottleCountToday;
                else if (period.equals("week")) return bottleCountWeek;
                else return bottleCountMonth;

            case "cans_bottles":
            default:
                if (period.equals("today")) return canCountToday + bottleCountToday;
                else if (period.equals("week")) return canCountWeek + bottleCountWeek;
                else return canCountMonth + bottleCountMonth;
        }
    }
    private void loadCache() {
        String cachedToday = cache.getString("cache_today", null);
        String cachedWeek = cache.getString("cache_week", null);
        String cachedMonth = cache.getString("cache_month", null);

        if (cachedToday != null && cachedWeek != null && cachedMonth != null) {
            todayResults = gson.fromJson(cachedToday, listType);
            weekResults = gson.fromJson(cachedWeek, listType);
            monthResults = gson.fromJson(cachedMonth, listType);

            // Load cached counts for each period
            recyclableCountToday = cache.getInt("recyclableCount_today", 0);
            OthersToday = cache.getInt("Others_today", 0);
            recyclableCountWeek = cache.getInt("recyclableCount_week", 0);
            OthersWeek = cache.getInt("Others_week", 0);
            recyclableCountMonth = cache.getInt("recyclableCount_month", 0);
            OthersMonth = cache.getInt("Others_month", 0);
            canCountToday = cache.getInt("canCount_today", 0);
            bottleCountToday = cache.getInt("bottleCount_today", 0);
            canCountWeek = cache.getInt("canCount_week", 0);
            bottleCountWeek = cache.getInt("bottleCount_week", 0);
            canCountMonth = cache.getInt("canCount_month", 0);
            bottleCountMonth = cache.getInt("bottleCount_month", 0);

            // Show cached data for today tab by default
            updateUIForPeriod("today");
            loadingSpinner.setVisibility(View.GONE);
        }
        updateUIForPeriod("today");
        loadingSpinner.setVisibility(View.GONE);
    }

    private void fetchLatestLists() {
        controller.fetchAllPredictionResults(results -> {
            todayResults.clear();
            weekResults.clear();
            monthResults.clear();

            Calendar now = Calendar.getInstance();

            for (PredictionResult result : results) {
                Timestamp ts = result.getTimestamp();
                Date date = (ts != null) ? ts.toDate() : null;
                if (date != null) {
                    Calendar predictionCal = Calendar.getInstance();
                    predictionCal.setTime(date);

                    boolean isSameDay = now.get(Calendar.YEAR) == predictionCal.get(Calendar.YEAR)
                            && now.get(Calendar.DAY_OF_YEAR) == predictionCal.get(Calendar.DAY_OF_YEAR);

                    now.setFirstDayOfWeek(Calendar.MONDAY);
                    predictionCal.setFirstDayOfWeek(Calendar.MONDAY);
                    TimeZone timeZone = TimeZone.getDefault();
                    now.setTimeZone(timeZone);
                    predictionCal.setTimeZone(timeZone);

                    boolean isSameWeek = now.get(Calendar.YEAR) == predictionCal.get(Calendar.YEAR)
                            && now.get(Calendar.WEEK_OF_YEAR) == predictionCal.get(Calendar.WEEK_OF_YEAR);

                    boolean isSameMonth = now.get(Calendar.YEAR) == predictionCal.get(Calendar.YEAR)
                            && now.get(Calendar.MONTH) == predictionCal.get(Calendar.MONTH);

                    if (isSameDay) todayResults.add(result);
                    if (isSameWeek) weekResults.add(result);
                    if (isSameMonth) monthResults.add(result);
                }
            }

            // Classify and cache counts for each period separately
            classifyPredictions(todayResults, "today");
            classifyPredictions(weekResults, "week");
            classifyPredictions(monthResults, "month");

            cache.edit()
                    // Cache lists for each period
                    .putString("cache_today", gson.toJson(todayResults))
                    .putString("cache_week", gson.toJson(weekResults))
                    .putString("cache_month", gson.toJson(monthResults))
                    // Cache counts for each period
                    .putInt("canCount_today", canCountToday)
                    .putInt("bottleCount_today", bottleCountToday)
                    .putInt("canCount_week", canCountWeek)
                    .putInt("bottleCount_week", bottleCountWeek)
                    .putInt("canCount_month", canCountMonth)
                    .putInt("bottleCount_month", bottleCountMonth)
                    .putInt("recyclableCount_today", recyclableCountToday)
                    .putInt("Others_today", OthersToday)
                    .putInt("recyclableCount_week", recyclableCountWeek)
                    .putInt("Others_week", OthersWeek)
                    .putInt("recyclableCount_month", recyclableCountMonth)
                    .putInt("Others_month", OthersMonth)
                    // Cache timestamps for each period
                    .putLong("cache_today_time", System.currentTimeMillis())
                    .putLong("cache_week_time", System.currentTimeMillis())
                    .putLong("cache_month_time", System.currentTimeMillis())
                    .apply();

            runOnUiThread(() -> {
                loadingSpinner.setVisibility(View.GONE);
                updateUIForPeriod("today"); // Refresh UI with updated data for today by default
            });
        });
    }

    private void classifyPredictions(List<PredictionResult> results, String period) {
        int recyclable = 0;
        int Others = 0;
        int canCount = 0;     // '0'
        int bottleCount = 0;  // '1'

        for (PredictionResult result : results) {
            String prediction = result.getPrediction();
            if (prediction != null && !prediction.isEmpty()) {
                char firstChar = prediction.charAt(0);
                if (firstChar == '0') {
                    recyclable++;
                    canCount++;
                } else if (firstChar == '1') {
                    recyclable++;
                    bottleCount++;
                } else {
                    Others++;
                }
            }
        }

        switch (period) {
            case "today":
                recyclableCountToday = recyclable;
                OthersToday = Others;
                canCountToday = canCount;
                bottleCountToday = bottleCount;
                break;
            case "week":
                recyclableCountWeek = recyclable;
                OthersWeek = Others;
                canCountWeek = canCount;
                bottleCountWeek = bottleCount;
                break;
            case "month":
                recyclableCountMonth = recyclable;
                OthersMonth = Others;
                canCountMonth = canCount;
                bottleCountMonth = bottleCount;
                break;
        }
    }


    private void setupPieCharts(int recyclableCount, int Others) {
        int total = recyclableCount + Others;
        if (total == 0) total = 1; // avoid division by zero

        setupSinglePieChart(pieChartRecyclable, ((float) recyclableCount / total) * 100, ContextCompat.getColor(this, R.color.green));
        setupSinglePieChart(pieChartOthers, ((float) Others / total) * 100, ContextCompat.getColor(this, R.color.gray));
    }

    private void setupSinglePieChart(PieChart chart, float percentage, int mainColor) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(percentage));
        entries.add(new PieEntry(100 - percentage));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(mainColor, Color.parseColor("#E0E0E0"));
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setDrawCenterText(true);
        chart.setCenterText(String.format("%.0f%%", percentage));
        chart.setCenterTextSize(20f);
        chart.setCenterTextColor(Color.BLACK);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawEntryLabels(false);
        chart.animateY(500);
        chart.invalidate();
    }

    private void setupBarChart(int recyclable, int Others) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, recyclable));
        entries.add(new BarEntry(1, Others));

        BarDataSet dataSet;
        if (barChart.getData() == null || barChart.getData().getDataSetCount() == 0) {
            dataSet = new BarDataSet(entries, "Waste Categories");
            dataSet.setColors(ContextCompat.getColor(this, R.color.green), ContextCompat.getColor(this, R.color.gray));
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setValueTextSize(16f);
            dataSet.setDrawValues(true);

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.5f);
            barChart.setData(data);
        } else {
            dataSet = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        }

        String[] labels = {currentRecyclable, "Others"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        Typeface boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        xAxis.setTypeface(boldTypeface);

        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setAxisMaximum(Math.max(recyclable, Others) + 5);
        yAxisLeft.setTextSize(14f);
        yAxisLeft.setDrawAxisLine(true);
        yAxisLeft.setDrawGridLines(true);

        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateY(500);
        barChart.invalidate();
    }

    private void setupTabListeners() {
        todayTab.setOnClickListener(v -> {
            setSelectedTab(todayTab);
            updateUIForPeriod("today");
        });

        weekTab.setOnClickListener(v -> {
            setSelectedTab(weekTab);
            updateUIForPeriod("week");
        });

        monthTab.setOnClickListener(v -> {
            setSelectedTab(monthTab);
            updateUIForPeriod("month");
        });
    }
    private final BroadcastReceiver classificationCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fetchLatestLists();
        }
    };
    private void setSelectedTab(TextView selectedTab) {
        for (int i = 0; i < (tabContainer).getChildCount(); i++) {
            View view = (tabContainer).getChildAt(i);
            if (view instanceof TextView) {
                TextView tab = (TextView) view;
                tab.setAlpha(0.5f);
                tab.setBackground(null);
                tab.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
        selectedTab.setAlpha(1.0f);
        selectedTab.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_white_background));
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void updateUIForPeriod(String period) {
        List<PredictionResult> results;
        int selectedRecyclable;
        int Others;

        switch (period) {
            case "week":
                results = weekResults;
                selectedRecyclable = getRecyclableCountForSetting("week");
                Others = OthersWeek;
                setSelectedTab(weekTab);
                break;
            case "month":
                results = monthResults;
                selectedRecyclable = getRecyclableCountForSetting("month");
                Others = OthersMonth;
                setSelectedTab(monthTab);
                break;
            case "today":
            default:
                results = todayResults;
                selectedRecyclable = getRecyclableCountForSetting("today");
                Others = OthersToday;
                setSelectedTab(todayTab);
                break;
        }

        int total = selectedRecyclable + Others;
        String text = "Total items: " + total;
        totalCountView.setText(text);
        barChart.setVisibility(View.VISIBLE);
        pieChartRecyclable.setVisibility(View.VISIBLE);
        pieChartOthers.setVisibility(View.VISIBLE);
        if (results == null || results.isEmpty()) {

            // Set no data text and color first
            barChart.setNoDataText("No data to show for " + period);

            Paint paint = barChart.getPaint(Chart.PAINT_INFO);
            paint.setColor(Color.BLACK);
            paint.setTextSize(40f); // increase size (adjust as needed)
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            pieChartRecyclable.setNoDataText("No data");
            Paint paintPieRec = pieChartRecyclable.getPaint(Chart.PAINT_INFO);
            paintPieRec.setColor(ContextCompat.getColor(this, R.color.green));
            paintPieRec.setTextSize(40f);
            paintPieRec.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            pieChartOthers.setNoDataText("No data");
            Paint paintPieNonRec = pieChartOthers.getPaint(Chart.PAINT_INFO);
            paintPieNonRec.setColor(ContextCompat.getColor(this, R.color.gray));
            paintPieNonRec.setTextSize(40f);
            paintPieNonRec.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            barChart.clear();
            pieChartRecyclable.clear();
            pieChartOthers.clear();
            return;
        }

        setupBarChart(selectedRecyclable, Others);
        setupPieCharts(selectedRecyclable, Others);
    }
}