package com.ss.rentmangment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsFragment extends Fragment {

    // UI Components
    private TextView tvWelcome, tvMobileDisplay, tvBusinessName, tvAddress, tvBirthDate, tvEmail;
    private ImageView ivUserSignature, ivMenuDots;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Firebase and SharedPreferences
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private SharedPreferences sharedPreferences;
    private String userMobile, userName;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeViews(view);
        getUserData();
        setupClickListeners();
        setupNavigationDrawer();

        return view;
    }

    private void initializeViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvMobileDisplay = view.findViewById(R.id.tvMobileDisplay);
        tvBusinessName = view.findViewById(R.id.tvBusinessName);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivUserSignature = view.findViewById(R.id.ivUserSignature);
        ivMenuDots = view.findViewById(R.id.ivMenuDots);
        drawerLayout = view.findViewById(R.id.drawerLayout);
        navigationView = view.findViewById(R.id.navigationView);
    }

    private void getUserData() {
        // Get data from SharedPreferences first
        userMobile = sharedPreferences.getString("mobile", "");
        userName = sharedPreferences.getString("name", "User");

        // Also check for data from Activity Intent (for first-time login)
        if (getActivity() != null && getActivity().getIntent() != null) {
            Intent intent = getActivity().getIntent();
            if (intent.hasExtra("mobile")) {
                userMobile = intent.getStringExtra("mobile");
                userName = intent.getStringExtra("name");
            }
        }

        // Update UI with basic info
        tvWelcome.setText("Welcome, " + userName + "!");
        tvMobileDisplay.setText("Mobile: +91 " + userMobile);

        // Update navigation header
        updateNavigationHeader();

        // Fetch complete user data from Firebase Realtime Database
        if (!userMobile.isEmpty()) {
            fetchUserDataFromRealtimeDatabase();
        }
    }

    private void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.navUserName);
        TextView navUserMobile = headerView.findViewById(R.id.navUserMobile);
        ImageView navUserImage = headerView.findViewById(R.id.navUserImage);

        navUserName.setText(userName);
        navUserMobile.setText("+91 " + userMobile);
        // You can set a profile image here if available
    }

    private void fetchUserDataFromRealtimeDatabase() {
        // Now we look inside "info"
        usersRef.child(userMobile).child("info")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Fetch all user data including signature
                            String businessName = dataSnapshot.child("businessName").getValue(String.class);
                            String address = dataSnapshot.child("address").getValue(String.class);
                            String birthDate = dataSnapshot.child("birthDate").getValue(String.class);
                            String email = dataSnapshot.child("email").getValue(String.class);
                            String digitalSignature = dataSnapshot.child("digitalSignature").getValue(String.class);

                            // Update UI with fetched data
                            tvBusinessName.setText("Business: " + (businessName != null ? businessName : "Not specified"));
                            tvAddress.setText("Address: " + (address != null ? address : "Not specified"));
                            tvBirthDate.setText("Birth Date: " + (birthDate != null ? birthDate : "Not specified"));
                            tvEmail.setText("Email: " + (email != null ? email : "Not specified"));

                            // Display digital signature
                            displaySignature(digitalSignature);
                        } else {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error loading user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
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
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading signature", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // No signature available
            ivUserSignature.setImageResource(R.drawable.ic_signature_placeholder);
        }
    }

    private void setupClickListeners() {
        // Three dots menu click listener
        ivMenuDots.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Optional: Click listener to view signature in full screen
        ivUserSignature.setOnClickListener(v -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Signature view clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_edit_profile) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                intent.putExtra("userMobile", userMobile);
                startActivity(intent);
            } else if (id == R.id.nav_change_pin) {
                showChangePinDialog();
            } else if (id == R.id.nav_notifications) {
                showNotificationSettings();
            } else if (id == R.id.nav_privacy) {
                showPrivacySettings();
            } else if (id == R.id.nav_backup) {
                showBackupOptions();
            } else if (id == R.id.nav_help) {
                showHelpCenter();
            } else if (id == R.id.nav_about) {
                showAboutApp();
            } else if (id == R.id.nav_logout) {
                showLogoutConfirmationDialog();
            }

            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });
    }

    // All your existing methods (showChangePinDialog, logout, etc.) remain the same...

    private void showChangePinDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_pin, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setTitle("Change PIN");

        AlertDialog dialog = builder.create();

        // Get dialog views
        TextView etCurrentPin = dialogView.findViewById(R.id.etCurrentPin);
        TextView etNewPin = dialogView.findViewById(R.id.etNewPin);
        TextView etConfirmPin = dialogView.findViewById(R.id.etConfirmPin);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnUpdate = dialogView.findViewById(R.id.btnUpdate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpdate.setOnClickListener(v -> {
            String currentPin = etCurrentPin.getText().toString().trim();
            String newPin = etNewPin.getText().toString().trim();
            String confirmPin = etConfirmPin.getText().toString().trim();

            if (validatePinChange(currentPin, newPin, confirmPin)) {
                updatePinInDatabase(currentPin, newPin, dialog);
            }
        });

        dialog.show();
    }

    private void showNotificationSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Notification Settings");

        String[] options = {"Push Notifications", "Email Notifications", "SMS Notifications"};
        boolean[] checkedItems = {true, false, true}; // Default settings

        builder.setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
            // Handle notification preference changes
            String setting = options[which];
            Toast.makeText(getContext(), setting + " " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            Toast.makeText(getContext(), "Notification settings saved", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPrivacySettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Privacy Settings");
        builder.setMessage("• Profile Visibility: Private\n• Data Sharing: Disabled\n• Analytics: Anonymous\n• Location Access: Disabled");

        builder.setPositiveButton("Manage Privacy", (dialog, which) -> {
            Toast.makeText(getContext(), "Privacy settings opened", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showBackupOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Backup & Sync");
        builder.setMessage("Choose backup options for your data:");

        String[] backupOptions = {"Google Drive Backup", "Local Storage Backup", "Auto Backup"};

        builder.setItems(backupOptions, (dialog, which) -> {
            String selectedOption = backupOptions[which];
            Toast.makeText(getContext(), selectedOption + " selected", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showHelpCenter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Help Center");
        builder.setMessage("• FAQ\n• Contact Support\n• User Guide\n• Video Tutorials\n• Report a Bug");

        builder.setPositiveButton("Visit Help Center", (dialog, which) -> {
            Toast.makeText(getContext(), "Opening help center...", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void showAboutApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("About SS Rent Management");
        builder.setMessage("Version: 1.0.0\nDeveloped by: SS Solutions\n\nA comprehensive rent management solution for modern businesses.\n\n© 2024 SS Solutions. All rights reserved.");

        builder.setPositiveButton("Rate App", (dialog, which) -> {
            Toast.makeText(getContext(), "Opening app store...", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private boolean validatePinChange(String currentPin, String newPin, String confirmPin) {
        if (currentPin.isEmpty() || currentPin.length() != 4) {
            Toast.makeText(getContext(), "Enter valid current PIN", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (newPin.isEmpty() || newPin.length() != 4) {
            Toast.makeText(getContext(), "Enter valid 4-digit new PIN", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!newPin.equals(confirmPin)) {
            Toast.makeText(getContext(), "New PIN and Confirm PIN don't match", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (currentPin.equals(newPin)) {
            Toast.makeText(getContext(), "New PIN must be different from current PIN", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updatePinInDatabase(String currentPin, String newPin, AlertDialog dialog) {
        // First verify current PIN
        usersRef.child(userMobile).child("pin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String storedPin = dataSnapshot.getValue(String.class);
                if (storedPin != null && storedPin.equals(currentPin)) {
                    // Current PIN is correct, update to new PIN
                    usersRef.child(userMobile).child("pin").setValue(newPin)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "PIN updated successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getContext(), "Failed to update PIN", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Current PIN is incorrect", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error verifying PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        // Positive button - Logout
        builder.setPositiveButton("Yes, Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout(); // Call the actual logout method
            }
        });

        // Negative button - Cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Just close the dialog
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Optional: Customize button colors
        if (getContext() != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
        }
    }

    // COMPLETE LOGOUT METHOD FOR FRAGMENT
    private void logout() {
        if (getContext() == null) return;

        // Clear all SharedPreferences data (including enhanced session data)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Show logout message
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to Login with proper flags
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish the parent activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning from edit profile
        if (!userMobile.isEmpty()) {
            fetchUserDataFromRealtimeDatabase();
        }
    }
}
