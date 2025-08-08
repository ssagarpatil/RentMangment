package com.ss.rentmangment;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class DashboardActivity extends AppCompatActivity {

    TextView btnHome, btnRooms, btnTenants, btnPayments, btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Bind Views
        btnHome = findViewById(R.id.btn_home);
        btnRooms = findViewById(R.id.btn_rooms);
        btnTenants = findViewById(R.id.btn_tenants);
        btnPayments = findViewById(R.id.btn_payments);
        btnSettings = findViewById(R.id.btn_settings);

        // Load default fragment & highlight
        loadFragment(new HomeFragment());
        highlightTab(btnHome);

        // Click Listeners
        btnHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            highlightTab(btnHome);
        });

        btnRooms.setOnClickListener(v -> {
            loadFragment(new RoomsFragment());
            highlightTab(btnRooms);
        });

        btnTenants.setOnClickListener(v -> {
            loadFragment(new TenantsFragment());
            highlightTab(btnTenants);
        });

//        btnPayments.setOnClickListener(v -> {
//            loadFragment(new PaymentsFragment());
//            highlightTab(btnPayments);
//        });

        btnSettings.setOnClickListener(v -> {
            loadFragment(new SettingsFragment());
            highlightTab(btnSettings);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    // Method to highlight the selected tab
    private void highlightTab(TextView selectedTab) {
        // Reset all
        btnHome.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        btnRooms.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        btnTenants.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        btnPayments.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        btnSettings.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        btnHome.setTextColor(getResources().getColor(android.R.color.black));
        btnRooms.setTextColor(getResources().getColor(android.R.color.black));
        btnTenants.setTextColor(getResources().getColor(android.R.color.black));
        btnPayments.setTextColor(getResources().getColor(android.R.color.black));
        btnSettings.setTextColor(getResources().getColor(android.R.color.black));

        // Highlight selected
        selectedTab.setBackgroundColor(getResources().getColor(R.color.teal_200));
        selectedTab.setTextColor(getResources().getColor(android.R.color.white));
    }
}
