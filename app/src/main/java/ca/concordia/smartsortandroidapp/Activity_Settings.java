package ca.concordia.smartsortandroidapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Activity_Settings extends NavigationBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation_bar);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        View contentView = getLayoutInflater().inflate(R.layout.activity_settings, contentFrame, false);
        contentFrame.addView(contentView);

        setupDrawer();

        // Edit Profile button - linking to Change Password Activity as per request
        Button editProfileButton = contentView.findViewById(R.id.button_edit_profile);
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Activity_Settings.this, Activity_ChangePassword.class);
            startActivity(intent);
        });

        // Sign out Settings button
        Button SigningOut = contentView.findViewById(R.id.SignOut);
        SigningOut.setOnClickListener(v -> {
            Intent intent = new Intent(Activity_Settings.this, Activity_Login.class);
            startActivity(intent);
        });

        // Contact Us button - opens email client
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

    }
}