//package com.ss.rentmangment;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private TextInputEditText etMobileLogin, etPinLogin;
//    private MaterialButton btnLogin;
//    private FirebaseFirestore db;
//    private SharedPreferences sharedPreferences;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        initializeViews();
//        db = FirebaseFirestore.getInstance();
//        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//
//        setupClickListeners();
//    }
//
//    private void initializeViews() {
//        etMobileLogin = findViewById(R.id.etMobileLogin);
//        etPinLogin = findViewById(R.id.etPinLogin);
//        btnLogin = findViewById(R.id.btnLogin);
//    }
//
//    private void setupClickListeners() {
//        btnLogin.setOnClickListener(v -> loginUser());
//
//        findViewById(R.id.tvRegisterRedirect).setOnClickListener(v -> {
//            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
//            finish();
//        });
//    }
//
//    private void loginUser() {
//        String mobile = etMobileLogin.getText().toString().trim();
//        String pin = etPinLogin.getText().toString().trim();
//
//        if (validateInputs(mobile, pin)) {
//            db.collection("users")
//                    .document(mobile)
//                    .get()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            if (document.exists()) {
//                                String storedPin = document.getString("pin");
//                                if (pin.equals(storedPin)) {
//                                    String userName = document.getString("name");
//
//                                    // Use SplashActivity's enhanced session management
//                                    SplashActivity.saveLoginSession(sharedPreferences, mobile, userName, true);
//
//                                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
//
//                                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
//                                    intent.putExtra("mobile", mobile);
//                                    intent.putExtra("name", userName);
//                                    startActivity(intent);
//                                    finish();
//                                } else {
//                                    Toast.makeText(this, "Invalid PIN!", Toast.LENGTH_SHORT).show();
//                                }
//                            } else {
//                                Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }
//    }
//
//    private boolean validateInputs(String mobile, String pin) {
//        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
//            etMobileLogin.setError("Valid 10-digit mobile number is required");
//            return false;
//        }
//        if (TextUtils.isEmpty(pin) || pin.length() != 4) {
//            etPinLogin.setError("4-digit PIN is required");
//            return false;
//        }
//        return true;
//    }
//}

package com.ss.rentmangment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etMobileLogin, etPinLogin;
    private MaterialButton btnLogin;
    private DatabaseReference usersRef;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
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

            // Now check in /users/{mobile}/info
            usersRef.child(mobile).child("info")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                Toast.makeText(LoginActivity.this, "User not found ! Please First Register", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String storedPin = snapshot.child("pin").getValue(String.class);
                            if (storedPin != null && storedPin.equals(pin)) {
                                String userName = snapshot.child("name").getValue(String.class);

                                SplashActivity.saveLoginSession(sharedPreferences, mobile, userName, true);

                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                intent.putExtra("mobile", mobile);
                                intent.putExtra("name", userName);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Invalid PIN!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean validateInputs(String mobile, String pin) {
        if (TextUtils.isEmpty(mobile) || !mobile.matches("\\d{10}")) {
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
