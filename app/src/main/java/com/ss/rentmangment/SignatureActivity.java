package com.ss.rentmangment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;

public class SignatureActivity extends AppCompatActivity {

    private SignatureView signatureView;
    private MaterialButton btnClear, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        signatureView = findViewById(R.id.signatureView);
        btnClear = findViewById(R.id.btnClear);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupClickListeners() {
        btnClear.setOnClickListener(v -> {
            signatureView.clearSignature();
            Toast.makeText(this, "Signature cleared", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> saveSignature());
    }

    private void saveSignature() {
        if (signatureView.isEmpty()) {
            Toast.makeText(this, "Please draw your signature first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get signature as bitmap
        Bitmap signatureBitmap = signatureView.getSignatureBitmap();

        // Convert bitmap to base64 string
        String signatureString = bitmapToBase64(signatureBitmap);

        // Return result to registration activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("signature", signatureString);
        setResult(RESULT_OK, resultIntent);
        finish();

        Toast.makeText(this, "Signature saved successfully", Toast.LENGTH_SHORT).show();
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
