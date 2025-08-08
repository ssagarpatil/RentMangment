package com.ss.rentmangment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etMobileLogin, etPinLogin;
    private MaterialButton btnLogin;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        setupClickListeners();
    }

    private void initializeViews() {
        etMobileLogin = findViewById(R.id.etMobileLogin);
        etPinLogin = findViewById(R.id.etPinLogin);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());

        findViewById(R.id.tvRegisterRedirect).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            finish();
        });
    }

    private void loginUser() {
        String mobile = etMobileLogin.getText().toString().trim();
        String pin = etPinLogin.getText().toString().trim();

        if (validateInputs(mobile, pin)) {
            db.collection("users")
                    .document(mobile)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String storedPin = document.getString("pin");
                                if (pin.equals(storedPin)) {
                                    String userName = document.getString("name");

                                    // Use SplashActivity's enhanced session management
                                    SplashActivity.saveLoginSession(sharedPreferences, mobile, userName, true);

                                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                    intent.putExtra("mobile", mobile);
                                    intent.putExtra("name", userName);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(this, "Invalid PIN!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean validateInputs(String mobile, String pin) {
        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
            etMobileLogin.setError("Valid 10-digit mobile number is required");
            return false;
        }
        if (TextUtils.isEmpty(pin) || pin.length() != 4) {
            etPinLogin.setError("4-digit PIN is required");
            return false;
        }
        return true;
    }
}
