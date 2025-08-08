package com.ss.rentmangment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvMobileDisplay, tvBusinessName, tvAddress, tvBirthDate, tvEmail;
    private ImageView ivUserSignature;
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
//        setupClickListeners();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvMobileDisplay = findViewById(R.id.tvMobileDisplay);
        tvBusinessName = findViewById(R.id.tvBusinessName);
        tvAddress = findViewById(R.id.tvAddress);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        tvEmail = findViewById(R.id.tvEmail);
        ivUserSignature = findViewById(R.id.ivUserSignature);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getUserData() {
        // Get data from SharedPreferences first
        userMobile = sharedPreferences.getString("mobile", "");
        userName = sharedPreferences.getString("name", "User");

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
                            // Fetch all user data including signature
                            String businessName = document.getString("businessName");
                            String address = document.getString("address");
                            String birthDate = document.getString("birthDate");
                            String email = document.getString("email");
                            String digitalSignature = document.getString("digitalSignature");

                            // Update UI with fetched data
                            tvBusinessName.setText("Business: " + (businessName != null ? businessName : "Not specified"));
                            tvAddress.setText("Address: " + (address != null ? address : "Not specified"));
                            tvBirthDate.setText("Birth Date: " + (birthDate != null ? birthDate : "Not specified"));
                            tvEmail.setText("Email: " + (email != null ? email : "Not specified"));

                            // Display digital signature
                            displaySignature(digitalSignature);
                        }
                    } else {
                        Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displaySignature(String digitalSignatureBase64) {
        if (digitalSignatureBase64 != null && !digitalSignatureBase64.isEmpty()) {
            try {
                // Convert base64 string back to bitmap
                byte[] decodedString = Base64.decode(digitalSignatureBase64, Base64.DEFAULT);
                Bitmap signatureBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (signatureBitmap != null) {
                    ivUserSignature.setImageBitmap(signatureBitmap);
                } else {
                    // Show placeholder if bitmap conversion fails
                    ivUserSignature.setImageResource(R.drawable.ic_signature_placeholder);
                }
            } catch (Exception e) {
                // Handle base64 decode error
                ivUserSignature.setImageResource(R.drawable.ic_signature_placeholder);
                Toast.makeText(this, "Error loading signature", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No signature available
            ivUserSignature.setImageResource(R.drawable.ic_signature_placeholder);
        }
    }

//    private void setupClickListeners() {
//        btnLogout.setOnClickListener(v -> logout());
//
//        // Optional: Click listener to view signature in full screen
//        ivUserSignature.setOnClickListener(v -> viewSignatureFullScreen());
//    }

//    private void viewSignatureFullScreen() {
//        // Optional method to view signature in full screen
//        Intent intent = new Intent(this, SignatureViewActivity.class);
//        intent.putExtra("userMobile", userMobile);
//        startActivity(intent);
//    }

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
