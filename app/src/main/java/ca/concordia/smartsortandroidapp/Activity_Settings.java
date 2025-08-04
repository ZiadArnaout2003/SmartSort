package ca.concordia.smartsortandroidapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Activity_Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        // Edit Profile button
        Button editProfileButton = findViewById(R.id.button_edit_profile);
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Activity_Settings.this, Activity_ChangePassword.class);
            startActivity(intent);
        });

        // Sign out Settings button
        Button SigningOut = findViewById(R.id.SignOut);
        SigningOut.setOnClickListener(v -> {
            // Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            Intent intent1 = new Intent(Activity_Settings.this, Activity_Login.class);
            startActivity(intent1);
            finish();
        });

        // Contact Us button, opens email client
        Button contactUsButton = findViewById(R.id.button_contact_us);
        contactUsButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@gmail.com"));
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show();
            }
        });

        // New: Setup Spinner for bar chart comparison
        Spinner comparisonSpinner = findViewById(R.id.spinner_recyclable_comparison);
        String[] options = {"Recyclable vs Non-Recyclable", "Bottles vs Others", "Cans vs Others", "Cans & Bottles vs Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        comparisonSpinner.setAdapter(adapter);

        // Load saved preference and set initial selection
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String savedComparison = prefs.getString("bar_chart_comparison", "recyclable_vs_non_recyclable");
        int selectedPosition = 0;
        switch (savedComparison) {
            case "bottles_vs_others":
                selectedPosition = 1;
                break;
            case "cans_vs_others":
                selectedPosition = 2;
                break;
            case "can_bottles_vs_others":
                selectedPosition = 3;
                break;
            // Default is 0: "recyclable_vs_non_recyclable"
        }
        comparisonSpinner.setSelection(selectedPosition);

        // Save selection to SharedPreferences
        comparisonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] values = {"recyclable_vs_non_recyclable", "bottles_vs_others", "cans_vs_others", "can_bottles_vs_others"};
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("bar_chart_comparison", values[position]);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }
}