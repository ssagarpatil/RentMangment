package com.ss.rentmangment;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.UUID;

public class AddTenantActivity extends AppCompatActivity {

    private EditText etName, etMobile, etEmail, etRoomNumber, etLeaseStart, etLeaseEnd,
            etRent, etDeposit, etEmergencyName, etEmergencyPhone, etIdProofType, etIdProofNumber, etNotes;
    private MaterialButton btnSave;

    private DatabaseReference usersRef;
    private SharedPreferences sharedPreferences;
    private String adminMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        initViews();

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        adminMobile = sharedPreferences.getString("mobile", "");

        etLeaseStart.setOnClickListener(v -> showDatePicker(etLeaseStart));
        etLeaseEnd.setOnClickListener(v -> showDatePicker(etLeaseEnd));

        btnSave.setOnClickListener(v -> saveTenant());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        etRoomNumber = findViewById(R.id.etRoomNumber);
        etLeaseStart = findViewById(R.id.etLeaseStart);
        etLeaseEnd = findViewById(R.id.etLeaseEnd);
        etRent = findViewById(R.id.etRentAmount);
        etDeposit = findViewById(R.id.etSecurityDeposit);
        etEmergencyName = findViewById(R.id.etEmergencyName);
        etEmergencyPhone = findViewById(R.id.etEmergencyPhone);
        etIdProofType = findViewById(R.id.etIdProofType);
        etIdProofNumber = findViewById(R.id.etIdProofNumber);
        etNotes = findViewById(R.id.etNotes);
        btnSave = findViewById(R.id.btnSaveTenant);
    }

    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    month++;
                    target.setText(dayOfMonth + "/" + month + "/" + year);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    private void saveTenant() {
        String name = etName.getText().toString().trim();
        String tenantMobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String leaseStart = etLeaseStart.getText().toString().trim();
        String leaseEnd = etLeaseEnd.getText().toString().trim();
        String rentStr = etRent.getText().toString().trim();
        String depositStr = etDeposit.getText().toString().trim();
        String idProofType = etIdProofType.getText().toString().trim();
        String idProofNumber = etIdProofNumber.getText().toString().trim();

        // === VALIDATIONS ===

        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter tenant name");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(tenantMobile) || tenantMobile.length() != 10 || !tenantMobile.matches("\\d{10}")) {
            etMobile.setError("Enter valid 10-digit mobile number");
            etMobile.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(etRoomNumber.getText().toString().trim())) {
            etRoomNumber.setError("Enter room number");
            etRoomNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(leaseStart)) {
            etLeaseStart.setError("Select lease start date");
            etLeaseStart.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(leaseEnd)) {
            etLeaseEnd.setError("Select lease end date");
            etLeaseEnd.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(rentStr) || parseDouble(rentStr) <= 0) {
            etRent.setError("Enter valid rent amount");
            etRent.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(depositStr) && parseDouble(depositStr) < 0) {
            etDeposit.setError("Enter valid deposit amount");
            etDeposit.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(idProofType) && idProofType.equalsIgnoreCase("Aadhaar")) {
            if (TextUtils.isEmpty(idProofNumber) || !idProofNumber.matches("\\d{12}")) {
                etIdProofNumber.setError("Enter valid 12-digit Aadhaar number");
                etIdProofNumber.requestFocus();
                return;
            }
        }

        // Optional: Validate emergency contact phone if entered
        String emergencyPhone = etEmergencyPhone.getText().toString().trim();
        if (!TextUtils.isEmpty(emergencyPhone) && (!emergencyPhone.matches("\\d{10}"))) {
            etEmergencyPhone.setError("Enter valid 10-digit phone");
            etEmergencyPhone.requestFocus();
            return;
        }

        // === If all passes, create Tenant object ===

        String tenantId = UUID.randomUUID().toString();

        Tenant tenant = new Tenant(
                tenantId,
                name,
                tenantMobile,
                email,
                etRoomNumber.getText().toString().trim(),
                leaseStart,
                leaseEnd,
                parseDouble(rentStr),
                parseDouble(depositStr),
                "Pending",
                etEmergencyName.getText().toString().trim(),
                emergencyPhone,
                idProofType,
                idProofNumber,
                etNotes.getText().toString().trim(),
                "" // photoUrl placeholder
        );

        // Save under users/{adminMobile}/tenants/{tenantMobile}
        usersRef.child(adminMobile)
                .child("tenants")
                .child(tenantMobile)
                .setValue(tenant)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Tenant saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private double parseDouble(String s) {
        if (TextUtils.isEmpty(s)) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
