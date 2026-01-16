package com.tanvir.appointmenthub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tanvir.appointmenthub.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUser, SPLASH_DELAY);
    }

    private void checkUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in → navigate to Login and clear back stack
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // User logged in → get role from Firestore
            fetchUserRole(currentUser.getUid());
        }
    }

    private void fetchUserRole(String uid) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        Intent intent;

                        if ("STUDENT".equals(role)) {
                            intent = new Intent(this, DashboardStudentActivity.class);
                        } else if ("FACULTY".equals(role)) {
                            intent = new Intent(this, DashboardFacultyActivity.class);
                        } else if ("VC".equals(role)) {
                            intent = new Intent(this, DashboardVCActivity.class);
                        } else if ("CHAIRMAN".equals(role)) {
                            intent = new Intent(this, DashboardChairmanActivity.class);
                        } else if ("ADMIN".equals(role)) {
                            intent = new Intent(this, DashboardAdminActivity.class);
                        } else {
                            intent = new Intent(this, LoginActivity.class);
                        }

                        // Always clear the back stack when leaving the splash
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // User exists in Auth but not in DB → go to login
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SplashActivity", "Error fetching user role", e);
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}
