//package com.ss.rentmangment;
//
//import android.app.DatePickerDialog;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Base64;
//import android.util.Patterns;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Map;
//
//public class RegistrationActivity extends AppCompatActivity {
//
//    // UI Components
//    private TextInputEditText etName, etMobile, etPin, etBusinessName, etAddress, etBirthDate, etEmail;
//    private MaterialButton btnRegister, btnAddSignature;
//    private ImageView ivSignaturePreview;
//
//    // Firebase and SharedPreferences
//    private FirebaseFirestore db;
//    private SharedPreferences sharedPreferences;
//
//    // Signature handling
//    private static final int SIGNATURE_REQUEST_CODE = 100;
//    private String digitalSignatureBase64 = "";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_registration);
//
//        initializeViews();
//        db = FirebaseFirestore.getInstance();
//        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//
//        setupDatePicker();
//        setupClickListeners();
//    }
//
//    private void initializeViews() {
//        etName = findViewById(R.id.etName);
//        etMobile = findViewById(R.id.etMobile);
//        etPin = findViewById(R.id.etPin);
//        etBusinessName = findViewById(R.id.etBusinessName);
//        etAddress = findViewById(R.id.etAddress);
//        etBirthDate = findViewById(R.id.etBirthDate);
//        etEmail = findViewById(R.id.etEmail);
//        btnRegister = findViewById(R.id.btnRegister);
//
//        // Signature components
//        ivSignaturePreview = findViewById(R.id.ivSignaturePreview);
//        btnAddSignature = findViewById(R.id.btnAddSignature);
//    }
//
//    private void setupDatePicker() {
//        etBirthDate.setOnClickListener(v -> {
//            Calendar calendar = Calendar.getInstance();
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//            DatePickerDialog datePickerDialog = new DatePickerDialog(
//                    RegistrationActivity.this,
//                    (view, selectedYear, selectedMonth, selectedDay) -> {
//                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
//                        etBirthDate.setText(date);
//                    },
//                    year, month, day
//            );
//            datePickerDialog.show();
//        });
//    }
//
//    private void setupClickListeners() {
//        btnRegister.setOnClickListener(v -> registerUser());
//
//        findViewById(R.id.tvLoginRedirect).setOnClickListener(v -> {
//            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
//            finish();
//        });
//
//        // Signature button click listener
//        btnAddSignature.setOnClickListener(v -> {
//            Intent intent = new Intent(RegistrationActivity.this, SignatureActivity.class);
//            startActivityForResult(intent, SIGNATURE_REQUEST_CODE);
//        });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == SIGNATURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
//            digitalSignatureBase64 = data.getStringExtra("signature");
//
//            if (digitalSignatureBase64 != null && !digitalSignatureBase64.isEmpty()) {
//                // Convert base64 back to bitmap for preview
//                byte[] decodedString = Base64.decode(digitalSignatureBase64, Base64.DEFAULT);
//                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                ivSignaturePreview.setImageBitmap(decodedBitmap);
//                btnAddSignature.setText("Update Signature");
//
//                Toast.makeText(this, "Signature captured successfully", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void registerUser() {
//        String name = etName.getText().toString().trim();
//        String mobile = etMobile.getText().toString().trim();
//        String pin = etPin.getText().toString().trim();
//        String businessName = etBusinessName.getText().toString().trim();
//        String address = etAddress.getText().toString().trim();
//        String birthDate = etBirthDate.getText().toString().trim();
//        String email = etEmail.getText().toString().trim();
//
//        if (validateInputs(name, mobile, pin, businessName, address, birthDate, email)) {
//            // Check if mobile number or email already exists
//            checkUserExists(mobile, email, name, pin, businessName, address, birthDate);
//        }
//    }
//
//    private void checkUserExists(String mobile, String email, String name, String pin, String businessName, String address, String birthDate) {
//        // Check mobile number first
//        db.collection("users")
//                .whereEqualTo("mobile", mobile)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        if (!task.getResult().isEmpty()) {
//                            Toast.makeText(this, "Mobile number already registered!", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // Check email
//                            checkEmailExists(email, mobile, name, pin, businessName, address, birthDate);
//                        }
//                    } else {
//                        Toast.makeText(this, "Error checking mobile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void checkEmailExists(String email, String mobile, String name, String pin, String businessName, String address, String birthDate) {
//        db.collection("users")
//                .whereEqualTo("email", email)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        if (!task.getResult().isEmpty()) {
//                            Toast.makeText(this, "Email already registered!", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // Both mobile and email are unique, proceed with registration
//                            saveUserToFirebase(name, mobile, pin, businessName, address, birthDate, email);
//                        }
//                    } else {
//                        Toast.makeText(this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private boolean validateInputs(String name, String mobile, String pin, String businessName, String address, String birthDate, String email) {
//        if (TextUtils.isEmpty(name)) {
//            etName.setError("Name is required");
//            etName.requestFocus();
//            return false;
//        }
//        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
//            etMobile.setError("Valid 10-digit mobile number is required");
//            etMobile.requestFocus();
//            return false;
//        }
//        if (TextUtils.isEmpty(pin) || pin.length() != 4) {
//            etPin.setError("4-digit PIN is required");
//            etPin.requestFocus();
//            return false;
//        }
//        if (TextUtils.isEmpty(businessName)) {
//            etBusinessName.setError("Business name is required");
//            etBusinessName.requestFocus();
//            return false;
//        }
//        if (TextUtils.isEmpty(address)) {
//            etAddress.setError("Address is required");
//            etAddress.requestFocus();
//            return false;
//        }
//        if (TextUtils.isEmpty(birthDate)) {
//            etBirthDate.setError("Birth date is required");
//            etBirthDate.requestFocus();
//            return false;
//        }
//        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            etEmail.setError("Valid email address is required");
//            etEmail.requestFocus();
//            return false;
//        }
//        if (digitalSignatureBase64.isEmpty()) {
//            Toast.makeText(this, "Digital signature is required. Please draw your signature.", Toast.LENGTH_LONG).show();
//            btnAddSignature.requestFocus();
//            return false;
//        }
//        return true;
//    }
//
//    private void saveUserToFirebase(String name, String mobile, String pin, String businessName, String address, String birthDate, String email) {
//        // Show loading message
//        Toast.makeText(this, "Saving registration data...", Toast.LENGTH_SHORT).show();
//
//        Map<String, Object> user = new HashMap<>();
//        user.put("name", name);
//        user.put("mobile", mobile);
//        user.put("pin", pin);
//        user.put("businessName", businessName);
//        user.put("address", address);
//        user.put("birthDate", birthDate);
//        user.put("email", email);
//        user.put("digitalSignature", digitalSignatureBase64); // Save signature as base64 string
//        user.put("timestamp", System.currentTimeMillis());
//        user.put("createdDate", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
//
//        db.collection("users")
//                .document(mobile) // Using mobile as document ID
//                .set(user)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
//
//                    // Use SplashActivity's enhanced session management
//                    SplashActivity.saveLoginSession(sharedPreferences, mobile, name, true);
//
//                    // Navigate to dashboard
//                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
//                    intent.putExtra("mobile", mobile);
//                    intent.putExtra("name", name);
//                    startActivity(intent);
//                    finish();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                });
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
//}



    package com.ss.rentmangment;

    import android.app.DatePickerDialog;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.os.Bundle;
    import android.text.TextUtils;
    import android.util.Base64;
    import android.util.Patterns;
    import android.widget.ImageView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.material.button.MaterialButton;
    import com.google.android.material.textfield.TextInputEditText;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.Calendar;
    import java.util.HashMap;
    import java.util.Map;

    public class RegistrationActivity extends AppCompatActivity {

        // UI Components
        private TextInputEditText etName, etMobile, etPin, etBusinessName, etAddress, etBirthDate, etEmail;
        private MaterialButton btnRegister, btnAddSignature;
        private ImageView ivSignaturePreview;

        // Firebase and SharedPreferences
        private DatabaseReference usersRef;
        private SharedPreferences sharedPreferences;

        // Signature handling
        private static final int SIGNATURE_REQUEST_CODE = 100;
        private String digitalSignatureBase64 = "";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_registration);

            initializeViews();
            usersRef = FirebaseDatabase.getInstance().getReference("users");
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
            etEmail = findViewById(R.id.etEmail);
            btnRegister = findViewById(R.id.btnRegister);
            ivSignaturePreview = findViewById(R.id.ivSignaturePreview);
            btnAddSignature = findViewById(R.id.btnAddSignature);
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

            btnAddSignature.setOnClickListener(v -> {
                Intent intent = new Intent(RegistrationActivity.this, SignatureActivity.class);
                startActivityForResult(intent, SIGNATURE_REQUEST_CODE);
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == SIGNATURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
                digitalSignatureBase64 = data.getStringExtra("signature");

                if (digitalSignatureBase64 != null && !digitalSignatureBase64.isEmpty()) {
                    byte[] decodedString = Base64.decode(digitalSignatureBase64, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivSignaturePreview.setImageBitmap(decodedBitmap);
                    btnAddSignature.setText("Update Signature");

                    Toast.makeText(this, "Signature captured successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void registerUser() {
            String name = etName.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String pin = etPin.getText().toString().trim();
            String businessName = etBusinessName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String birthDate = etBirthDate.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (validateInputs(name, mobile, pin, businessName, address, birthDate, email)) {
                checkUserExists(mobile, email, name, pin, businessName, address, birthDate);
            }
        }

        /*** === Realtime DB implementation below === ***/
        private void checkUserExists(String mobile, String email, String name, String pin, String businessName, String address, String birthDate) {
            // Check if mobile exists
            usersRef.child(mobile).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(RegistrationActivity.this, "Mobile number already registered!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Check email exists
                        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot emailSnapshot) {
                                if (emailSnapshot.exists()) {
                                    Toast.makeText(RegistrationActivity.this, "Email already registered!", Toast.LENGTH_SHORT).show();
                                } else {
                                    saveUserToRealtimeDB(name, mobile, pin, businessName, address, birthDate, email);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(RegistrationActivity.this, "Error checking email: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(RegistrationActivity.this, "Error checking mobile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private boolean validateInputs(String name, String mobile, String pin, String businessName, String address, String birthDate, String email) {
            if (TextUtils.isEmpty(name)) {
                etName.setError("Name is required");
                return false;
            }
            if (TextUtils.isEmpty(mobile) || !mobile.matches("\\d{10}")) {
                etMobile.setError("Enter valid 10-digit mobile number");
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
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Valid email address is required");
                return false;
            }
            if (digitalSignatureBase64.isEmpty()) {
                Toast.makeText(this, "Digital signature is required", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        private void saveUserToRealtimeDB(String name, String mobile, String pin, String businessName, String address, String birthDate, String email) {
            Toast.makeText(this, "Saving registration data...", Toast.LENGTH_SHORT).show();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", name);
            userInfo.put("mobile", mobile);
            userInfo.put("pin", pin);
            userInfo.put("businessName", businessName);
            userInfo.put("address", address);
            userInfo.put("birthDate", birthDate);
            userInfo.put("email", email);
            userInfo.put("digitalSignature", digitalSignatureBase64);
            userInfo.put("timestamp", System.currentTimeMillis());
            userInfo.put("createdDate", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));

            // Now we save under: users/{mobile}/info
            usersRef.child(mobile).child("info").setValue(userInfo)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        SplashActivity.saveLoginSession(sharedPreferences, mobile, name, true);

                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        intent.putExtra("mobile", mobile);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }