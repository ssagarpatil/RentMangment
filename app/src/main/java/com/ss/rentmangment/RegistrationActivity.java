package com.ss.rentmangment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText etName, etMobile, etPin, etBusinessName, etAddress, etBirthDate;
    private MaterialButton btnRegister;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initializeViews();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        setupDatePicker();
        setupClickListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etPin = findViewById(R.id.etPin);
        etBusinessName = findViewById(R.id.etBusinessName);
        etAddress = findViewById(R.id.etAddress);
        etBirthDate = findViewById(R.id.etBirthDate);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupDatePicker() {
        etBirthDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegistrationActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        etBirthDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        findViewById(R.id.tvLoginRedirect).setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String businessName = etBusinessName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        if (validateInputs(name, mobile, pin, businessName, address, birthDate)) {
            // Check if mobile number already exists
            db.collection("users")
                    .whereEqualTo("mobile", mobile)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                Toast.makeText(this, "Mobile number already registered!", Toast.LENGTH_SHORT).show();
                            } else {
                                saveUserToFirebase(name, mobile, pin, businessName, address, birthDate);
                            }
                        } else {
                            Toast.makeText(this, "Error checking user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean validateInputs(String name, String mobile, String pin, String businessName, String address, String birthDate) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
            etMobile.setError("Valid 10-digit mobile number is required");
            return false;
        }
        if (TextUtils.isEmpty(pin) || pin.length() != 4) {
            etPin.setError("4-digit PIN is required");
            return false;
        }
        if (TextUtils.isEmpty(businessName)) {
            etBusinessName.setError("Business name is required");
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            return false;
        }
        if (TextUtils.isEmpty(birthDate)) {
            etBirthDate.setError("Birth date is required");
            return false;
        }
        return true;
    }

    private void saveUserToFirebase(String name, String mobile, String pin, String businessName, String address, String birthDate) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("mobile", mobile);
        user.put("pin", pin);
        user.put("businessName", businessName);
        user.put("address", address);
        user.put("birthDate", birthDate);
        user.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(mobile) // Using mobile as document ID
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

                    // Save login state
                    saveLoginState(mobile, name);

                    // Navigate to dashboard
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    intent.putExtra("mobile", mobile);
                    intent.putExtra("name", name);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveLoginState(String mobile, String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userMobile", mobile);
        editor.putString("userName", name);
        editor.apply();
    }
}
