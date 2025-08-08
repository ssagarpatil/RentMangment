package com.ss.rentmangment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvMobileDisplay, tvBusinessName, tvAddress, tvBirthDate;
    private MaterialButton btnLogout;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String userMobile, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupToolbar();

        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        getUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvMobileDisplay = findViewById(R.id.tvMobileDisplay);
        tvBusinessName = findViewById(R.id.tvBusinessName);
        tvAddress = findViewById(R.id.tvAddress);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getUserData() {
        // Get data from SharedPreferences first
        userMobile = sharedPreferences.getString("userMobile", "");
        userName = sharedPreferences.getString("userName", "User");

        // Also check for data from Intent (for first-time login)
        Intent intent = getIntent();
        if (intent.hasExtra("mobile")) {
            userMobile = intent.getStringExtra("mobile");
            userName = intent.getStringExtra("name");
        }

        // Update UI with basic info
        tvWelcome.setText("Welcome, " + userName + "!");
        tvMobileDisplay.setText("Mobile: +91 " + userMobile);

        // Fetch complete user data from Firebase
        if (!userMobile.isEmpty()) {
            fetchUserDataFromFirebase();
        }
    }

    private void fetchUserDataFromFirebase() {
        db.collection("users")
                .document(userMobile)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String businessName = document.getString("businessName");
                            String address = document.getString("address");
                            String birthDate = document.getString("birthDate");

                            tvBusinessName.setText("Business: " + (businessName != null ? businessName : "Not specified"));
                            tvAddress.setText("Address: " + (address != null ? address : "Not specified"));
                            tvBirthDate.setText("Birth Date: " + (birthDate != null ? birthDate : "Not specified"));
                        }
                    } else {
                        Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to Login
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
