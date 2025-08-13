package ca.concordia.smartsortandroidapp;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Activity_Settings extends NavigationBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_bar);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        View contentView = getLayoutInflater().inflate(R.layout.activity_settings, contentFrame, false);
        contentFrame.addView(contentView);

        setupDrawer();
        Switch notificationsSwitch = contentView.findViewById(R.id.toggleButton);
        notificationsSwitch.setChecked(areNotificationsEnabled());
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Toast.makeText(Activity_Settings.this, "Redirecting to settings to " + (isChecked ? "enable" : "disable")+ " notifications ...", Toast.LENGTH_LONG).show();
                openNotificationSettings();
        });
        Button editProfileButton = contentView.findViewById(R.id.button_edit_profile);
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Activity_Settings.this, Activity_ChangePassword.class);
            startActivity(intent);
        });

        Button SigningOut = contentView.findViewById(R.id.SignOut);
        SigningOut.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Intent intent1 = new Intent(Activity_Settings.this, Activity_Login.class);
            startActivity(intent1);
            finish();
        });


        Button contactUsButton = contentView.findViewById(R.id.button_contact_us);
        contactUsButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@gmail.com"));
            try {
                startActivity(emailIntent);
            } catch (Exception e) {
                Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show();
            }
        });

        Spinner comparisonSpinner = contentView.findViewById(R.id.spinner_recyclable_comparison);
        String[] options = {"Bottles", "Cans", "Cans & Bottles"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        comparisonSpinner.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        String savedComparison = prefs.getString("current_recyclable", "cans_bottles");
        int selectedPosition = 2;
        switch (savedComparison) {
            case "bottles":
                selectedPosition = 0;
                break;
            case "cans":
                selectedPosition = 1;
                break;
            case "cans_bottles":
                selectedPosition = 2;
                break;
        }
        comparisonSpinner.setSelection(selectedPosition);

        comparisonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] values = {"bottles", "cans", "cans_bottles"};
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("current_recyclable", values[position]);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }
    private boolean areNotificationsEnabled() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return manager.areNotificationsEnabled();
    }
    @Override
    protected void onResume() {
        super.onResume();
        Switch notificationsSwitch = findViewById(R.id.toggleButton);
        notificationsSwitch.setChecked(areNotificationsEnabled());
    }
    private void openNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + getPackageName()));
        }
        startActivity(intent);
    }

}