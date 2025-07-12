package ca.concordia.smartsortandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ResultController controller;
    private ArrayList<String> resultList;
    private ArrayAdapter<String> adapter;
    private ListView resultListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ClassificationService.class));

        resultListView = findViewById(R.id.resultListView);
        resultList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resultList);
        resultListView.setAdapter(adapter);

        controller = new ResultController();
        controller.loadAllResults(this, resultList, adapter);
    }
}
