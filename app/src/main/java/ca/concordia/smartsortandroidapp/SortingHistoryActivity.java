package ca.concordia.smartsortandroidapp;

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

    private Spinner spinnerFilterType, spinnerFilterDate, spinnerFilterVolume;

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

        // Initialize spinners
        spinnerFilterType = contentView.findViewById(R.id.spinner_filter_type);
        spinnerFilterDate = contentView.findViewById(R.id.spinner_filter_date);
        spinnerFilterVolume = contentView.findViewById(R.id.spinner_filter_volume);

        // Set spinner values
        spinnerFilterType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Arrays.asList("All", "0 Can", "1 Bottle", "2 Others")));
        spinnerFilterDate.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Arrays.asList("All", "Today", "This Week")));
        spinnerFilterVolume.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Arrays.asList("All", "Low", "Medium", "High")));

        // Set shared listener
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerFilterType.setOnItemSelectedListener(filterListener);
        spinnerFilterDate.setOnItemSelectedListener(filterListener);
        spinnerFilterVolume.setOnItemSelectedListener(filterListener);

        // Fetch data
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
        String selectedVolume = spinnerFilterVolume.getSelectedItem().toString();

        List<PredictionResult> filteredList = new ArrayList<>();

        for (PredictionResult item : fullList) {
            boolean matchType = selectedType.equals("All") || item.getPrediction().equalsIgnoreCase(selectedType);
            boolean matchDate = selectedDate.equals("All") || matchesDate(item.getTimestamp(), selectedDate);
            boolean matchVolume = selectedVolume.equals("All") || (item.getVolume() != null && item.getVolume().equalsIgnoreCase(selectedVolume));

            if (matchType && matchDate && matchVolume) {
                filteredList.add(item);
            }
        }

        adapter = new HistoryAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);
    }

    private boolean matchesDate(com.google.firebase.Timestamp timestamp, String selectedDate) {
        if (timestamp == null) return false;

        Calendar cal = Calendar.getInstance();
        Date itemDate = timestamp.toDate();
        cal.setTime(new Date());

        if (selectedDate.equals("Today")) {
            return isSameDay(itemDate, cal.getTime());
        } else if (selectedDate.equals("This Week")) {
            int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
            cal.setTime(itemDate);
            return cal.get(Calendar.WEEK_OF_YEAR) == currentWeek;
        }
        return true;
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}