package com.ss.rentmangment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_MOBILE = "mobile";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_LOGIN_TIMESTAMP = "loginTimestamp";
    private static final String KEY_REMEMBER_ME = "rememberMe";

    // Session validity period (30 days in milliseconds)
    private static final long SESSION_VALIDITY = 30L * 24 * 60 * 60 * 1000;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeComponents();
        startSplashTimer();
    }

    private void initializeComponents() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Hide action bar for fullscreen splash
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void startSplashTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAutoLoginEligibility();
            }
        }, SPLASH_DELAY);
    }

    /**
     * Comprehensive method to check if user should be automatically logged in
     */
    private void checkAutoLoginEligibility() {
        Log.d("SplashActivity", "Checking auto-login eligibility...");

        // Step 1: Check if remember me is enabled
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);

        // Step 2: Check basic login state
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

        // Step 3: Check session validity
        boolean isSessionValid = isSessionValid();

        // Step 4: Get stored mobile number
        String userMobile = sharedPreferences.getString(KEY_USER_MOBILE, "");

        Log.d("SplashActivity", "Remember Me: " + rememberMe);
        Log.d("SplashActivity", "Is Logged In: " + isLoggedIn);
        Log.d("SplashActivity", "Session Valid: " + isSessionValid);
        Log.d("SplashActivity", "User Mobile: " + userMobile);

        if (rememberMe && isLoggedIn && isSessionValid && !userMobile.isEmpty()) {
            // All conditions met - proceed with auto-login
            performAutoLogin();
        } else {
            // Clear invalid session and go to login
            clearSession();
            navigateToLogin();
        }
    }

    /**
     * Check if the current session is still valid
     */
    private boolean isSessionValid() {
        long loginTimestamp = sharedPreferences.getLong(KEY_LOGIN_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();

        // Check if session has expired
        return (currentTime - loginTimestamp) < SESSION_VALIDITY;
    }

    /**
     * Perform automatic login by validating user data
     */
    private void performAutoLogin() {
        String userMobile = sharedPreferences.getString(KEY_USER_MOBILE, "");

        if (!userMobile.isEmpty()) {
            Log.d("SplashActivity", "Performing auto-login for: " + userMobile);

            // Verify user data still exists in Firestore
            db.collection("users")
                    .document(userMobile)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // User data exists - refresh session and proceed to dashboard
                                refreshSession();
                                navigateToDashboard();
                                Log.d("SplashActivity", "Auto-login successful");
                            } else {
                                // User data doesn't exist - clear session
                                Log.d("SplashActivity", "User data not found, clearing session");
                                clearSession();
                                navigateToLogin();
                            }
                        } else {
                            Log.e("SplashActivity", "Firestore error: " + task.getException().getMessage());
                            // On error, still allow auto-login if local data is valid
                            navigateToDashboard();
                        }
                    });
        } else {
            // No mobile number stored
            clearSession();
            navigateToLogin();
        }
    }

    /**
     * Refresh the current session timestamp
     */
    private void refreshSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
        Log.d("SplashActivity", "Session refreshed");
    }

    /**
     * Save complete login session with remember me option
     */
    public static void saveLoginSession(SharedPreferences prefs, String mobile, String name, boolean rememberMe) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_MOBILE, mobile);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());
        editor.apply();

        Log.d("SessionManager", "Login session saved for: " + mobile + " (Remember: " + rememberMe + ")");
    }

    /**
     * Clear all session data
     */
    private void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_MOBILE);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_LOGIN_TIMESTAMP);
        editor.remove(KEY_REMEMBER_ME);
        editor.apply();

        Log.d("SplashActivity", "Session cleared");
    }

    /**
     * Check if user has an active session
     */
    public static boolean hasActiveSession(SharedPreferences prefs) {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        boolean rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false);
        long loginTimestamp = prefs.getLong(KEY_LOGIN_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis();
        boolean isSessionValid = (currentTime - loginTimestamp) < SESSION_VALIDITY;

        return isLoggedIn && rememberMe && isSessionValid;
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
