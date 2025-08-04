package ca.concordia.smartsortandroidapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

    }
}