package ca.concordia.smartsortandroidapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Activity_ChangePassword extends AppCompatActivity {

    private EditText editCurrentPassword, editNewPassword, editConfirmNewPassword;
    private Button btnUpdatePassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        editCurrentPassword = findViewById(R.id.current_password);
        editNewPassword = findViewById(R.id.new_password);
        editConfirmNewPassword = findViewById(R.id.confirm_new_password);
        btnUpdatePassword = findViewById(R.id.update_password_button);

        // Set listener for password update
        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });
    }

    private void updatePassword() {
        String currentPassword = editCurrentPassword.getText().toString().trim();
        String newPassword = editNewPassword.getText().toString().trim();
        String confirmNewPassword = editConfirmNewPassword.getText().toString().trim();

        if (!validatePasswordInputs(currentPassword, newPassword, confirmNewPassword)) {
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentEmail = user.getEmail();
        if (currentEmail == null) {
            Toast.makeText(this, "Unable to retrieve current email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reauthenticate user
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Update password after successful reauthentication
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Activity_ChangePassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                clearPasswordFields();
                            } else {
                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Password update failed";
                                Toast.makeText(Activity_ChangePassword.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Reauthentication failed";
                    Toast.makeText(Activity_ChangePassword.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmNewPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            editCurrentPassword.setError("Current password is required");
            return false;
        }
        if (TextUtils.isEmpty(newPassword)) {
            editNewPassword.setError("New password is required");
            return false;
        }
        if (newPassword.length() < 6) {
            editNewPassword.setError("New password must be at least 6 characters");
            return false;
        }
        if (TextUtils.isEmpty(confirmNewPassword)) {
            editConfirmNewPassword.setError("Confirm password is required");
            return false;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            editConfirmNewPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    private void clearPasswordFields() {
        editCurrentPassword.setText("");
        editNewPassword.setText("");
        editConfirmNewPassword.setText("");
    }
}