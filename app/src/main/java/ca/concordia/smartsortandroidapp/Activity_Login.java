package ca.concordia.smartsortandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Activity_Login extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button btnSignup;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setStatusBarColor(getColor(R.color.green));
        getWindow().getDecorView().setSystemUiVisibility(0);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find views by ID (based on assumed XML)
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        buttonLogin = findViewById(R.id.btn_login);
        btnSignup = findViewById(R.id.btn_signup);

        // Set login button click listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Validate user input
                if (!validateInputs(email, password)) {
                    return;
                }

                // Attempt login with Firebase
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Activity_Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Show success message
                                    Toast.makeText(Activity_Login.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                    // Redirect to MainActivity
                                    Intent intent = new Intent(Activity_Login.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();  // Optional: Close login activity
                                } else {
                                    // Show error message
                                    String errorMessage = "Login failed. Please check your credentials.";
                                    if (task.getException() != null) {
                                        errorMessage = task.getException().getMessage();
                                    }
                                    Toast.makeText(Activity_Login.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }

        });
        // Set signup button click listener to redirect to RegisterActivity
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_Login.this, Activity_Register.class);
                startActivity(intent);
            }
        });
    }



    // Client-side validation method
    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Invalid email format");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return false;
        }
        if (password.length() < 6) {  // Example: Minimum length check
            editTextPassword.setError("Password must be at least 6 characters");
            return false;
        }
        return true;
    }
}