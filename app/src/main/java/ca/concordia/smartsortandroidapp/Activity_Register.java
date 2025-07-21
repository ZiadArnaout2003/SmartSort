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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Activity_Register extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPhone;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Find views by ID
        editTextEmail = findViewById(R.id.RegisterEmail);
        editTextPhone = findViewById(R.id.RegisterPhone);
        editTextPassword = findViewById(R.id.RegisterPassword);
        editTextConfirmPassword = findViewById(R.id.RegisterConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Set register button click listener
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String phone = editTextPhone.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                // Validate user input
                if (!validateInputs(email, phone, password, confirmPassword)) {
                    return;
                }

                // Attempt registration with Firebase
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Activity_Register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Save phone number to Firestore
                                    String uid = mAuth.getCurrentUser().getUid();
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("phone", phone);
                                    db.collection("users").document(uid).set(userData);

                                    // Show success message and redirect
                                    Toast.makeText(Activity_Register.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Activity_Register.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Show error message
                                    String errorMessage = "Registration failed. Please try again.";
                                    if (task.getException() != null) {
                                        errorMessage = task.getException().getMessage();
                                    }
                                    Toast.makeText(Activity_Register.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    // Client-side validation method
    private boolean validateInputs(String email, String phone, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Invalid email format");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Phone number is required");
            return false;
        }
        if (phone.length() < 10) {
            editTextPhone.setError("Phone number must be at least 10 digits");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }
}