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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use the layout that includes toolbar, drawer, and content_frame
        setContentView(R.layout.navigation_bar);

        // Inflate the actual screen content (history UI) inside the drawer layout
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        View contentView = getLayoutInflater().inflate(R.layout.activity_history, contentFrame, false);
        contentFrame.addView(contentView);

        // Setup hamburger menu + drawer
        setupDrawer();

        controller = new ResultController();

        recyclerView = findViewById(R.id.recycler_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, fullList);
        recyclerView.setAdapter(adapter);

        Spinner typeFilter = findViewById(R.id.spinner_filter_type);

        controller.listenToPredictionResults(results -> {
            runOnUiThread(() -> {
                fullList.clear();
                fullList.addAll(results);
                filterByType(typeFilter.getSelectedItem().toString());
            });
        });


        // Spinner filter setup
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("All","0 Can","1 Bottle","2 Others"));
        typeFilter.setAdapter(spinnerAdapter);

        typeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterByType(typeFilter.getSelectedItem().toString());
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

    }

    private void filterByType(String type) {
        if (type.equals("All")) {
            adapter = new HistoryAdapter(this, fullList);
        } else {
            List<PredictionResult> filtered = new ArrayList<>();
            for (PredictionResult result : fullList) {
                if (result.getPrediction().equalsIgnoreCase(type)) {
                    filtered.add(result);
                }
            }
            adapter = new HistoryAdapter(this, filtered);
        }
        recyclerView.setAdapter(adapter);
    }
}