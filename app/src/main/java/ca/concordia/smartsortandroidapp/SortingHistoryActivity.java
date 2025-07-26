package ca.concordia.smartsortandroidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

public class SortingHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<PredictionResult> fullList = new ArrayList<>();
    private ResultController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

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
                filterByType(((Spinner) findViewById(R.id.spinner_filter_type)).getSelectedItem().toString());
            });
        });


        // (Optional) Setup filter spinners here
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("All", "Can",  "Bottle", "Others"));
        typeFilter.setAdapter(spinnerAdapter);

        typeFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterByType(typeFilter.getSelectedItem().toString());
            }

            /*@Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }*/

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    private void filterByType(String type) {
        List<PredictionResult> filtered = new ArrayList<>();

        for (PredictionResult result : fullList) {
            String label = result.getPredictionType(); // cleaned label
            result.setPrediction(label);
            if (type.equals("All") || label.equalsIgnoreCase(type)) {
                filtered.add(result);
            }
        }

        adapter = new HistoryAdapter(this, filtered);
        recyclerView.setAdapter(adapter);
    }
}