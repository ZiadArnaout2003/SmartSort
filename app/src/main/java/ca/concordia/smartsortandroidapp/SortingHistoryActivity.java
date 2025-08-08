package ca.concordia.smartsortandroidapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class SortingHistoryActivity extends NavigationBar {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<PredictionResult> fullList = new ArrayList<>();
    private ResultController controller;

    private Spinner spinnerFilterType, spinnerFilterDate;

    private Date startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.navigation_bar);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        View contentView = getLayoutInflater().inflate(R.layout.activity_history, contentFrame, false);
        contentFrame.addView(contentView);

        setupDrawer();

        controller = new ResultController();

        recyclerView = contentView.findViewById(R.id.recycler_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, fullList);
        recyclerView.setAdapter(adapter);

        spinnerFilterType = contentView.findViewById(R.id.spinner_filter_type);
        spinnerFilterDate = contentView.findViewById(R.id.spinner_filter_date);

        // Type filter
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("All", "0 Can", "1 Bottle", "2 Others"));
        spinnerFilterType.setAdapter(typeAdapter);

        // Date filter with Custom Range
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("All", "Today", "This Week", "Custom Range"));
        spinnerFilterDate.setAdapter(dateAdapter);

        // Listener for filters
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = spinnerFilterDate.getSelectedItem().toString();
                if (parent == spinnerFilterDate && selected.equals("Custom Range")) {
                    showCustomDateRangePicker();
                } else {
                    applyFilters();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerFilterType.setOnItemSelectedListener(filterListener);
        spinnerFilterDate.setOnItemSelectedListener(filterListener);

        // Load data from Firestore
        controller.listenToPredictionResults(results -> {
            runOnUiThread(() -> {
                fullList.clear();
                fullList.addAll(results);
                applyFilters();
            });
        });
    }

    private void applyFilters() {
        String selectedType = spinnerFilterType.getSelectedItem().toString();
        String selectedDate = spinnerFilterDate.getSelectedItem().toString();

        List<PredictionResult> filteredList = new ArrayList<>();

        for (PredictionResult item : fullList) {
            boolean matchType = selectedType.equals("All") || item.getPrediction().equalsIgnoreCase(selectedType);
            boolean matchDate = matchesDate(item.getTimestamp() != null ? item.getTimestamp().toDate() : null, selectedDate);

            if (matchType && matchDate) {
                filteredList.add(item);
            }
        }

        adapter = new HistoryAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);
    }

    private boolean matchesDate(Date date, String selectedDate) {
        if (date == null) return false;

        Calendar now = Calendar.getInstance();

        switch (selectedDate) {
            case "Today":
                return isSameDay(date, new Date());
            case "This Week":
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(date);
                return now.get(Calendar.WEEK_OF_YEAR) == cal1.get(Calendar.WEEK_OF_YEAR)
                        && now.get(Calendar.YEAR) == cal1.get(Calendar.YEAR);
            case "Custom Range":
                if (startDate != null && endDate != null) {
                    return !date.before(startDate) && !date.after(endDate);
                }
                return true;
            default:
                return true; // "All"
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void showCustomDateRangePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog startPicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar startCal = Calendar.getInstance();
            startCal.set(year, month, dayOfMonth, 0, 0, 0);
            startDate = startCal.getTime();

            DatePickerDialog endPicker = new DatePickerDialog(this, (view2, year2, month2, dayOfMonth2) -> {
                Calendar endCal = Calendar.getInstance();
                endCal.set(year2, month2, dayOfMonth2, 23, 59, 59);
                endDate = endCal.getTime();

                applyFilters();

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            endPicker.show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        startPicker.show();
    }
}

