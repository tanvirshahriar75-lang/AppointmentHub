package com.tanvir.appointmenthub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tanvir.appointmenthub.R;

public class LoginActivity extends AppCompatActivity {

    // UI
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvForgotPassword;
    private TextView tvRegister;
    private MaterialButton btnTestCreds; // <--- test credentials button

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initFirebase();
        initViews();
        setListeners();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        // register link
        int registerId = getResources().getIdentifier("tvRegister", "id", getPackageName());
        if (registerId != 0) tvRegister = findViewById(registerId);

        // Use runtime lookup for the test credentials button to avoid resource ID generation issues
        int testBtnId = getResources().getIdentifier("btn_test_creds", "id", getPackageName());
        if (testBtnId != 0) {
            btnTestCreds = findViewById(testBtnId);
        } else {
            btnTestCreds = null; // gracefully handle absence
        }
    }

    private void setListeners() {
        btnLogin.setOnClickListener(v -> validateAndLogin());

        tvForgotPassword.setOnClickListener(v -> resetPassword());

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        }

        // Wire test credentials button to show a dialog with sample accounts
        if (btnTestCreds != null) {
            btnTestCreds.setOnClickListener(v -> showTestAccountsDialog());
        }
    }

    private void showTestAccountsDialog() {
        final String[] labels = new String[]{"Student", "Faculty", "VC", "Chairman", "Admin"};
        // sample credentials — replace with accounts valid in your Firebase project if needed
        final String[][] creds = new String[][]{
                {"student@example.com", "password123"},
                {"faculty@example.com", "password123"},
                {"vc@example.com", "password123"},
                {"chairman@example.com", "password123"},
                {"admin@example.com", "password123"}
        };

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle("Test Accounts")
                .setItems(labels, (dialog, which) -> {
                    if (which >= 0 && which < creds.length) {
                        String email = creds[which][0];
                        String pass = creds[which][1];
                        if (etEmail != null) etEmail.setText(email);
                        if (etPassword != null) etPassword.setText(pass);
                        Toast.makeText(LoginActivity.this, labels[which] + " credentials filled", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        b.show();
    }

    // ================= LOGIN FLOW =================

    private void validateAndLogin() {
        if (etEmail == null || etPassword == null) {
            Toast.makeText(this, "UI elements not initialized properly", Toast.LENGTH_LONG).show();
            return;
        }

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (!isValid(email, password)) return;

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        loadUserProfile(user.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    // FirebaseAuth failed (user may not exist in Auth). Try Realtime Database fallback.
                    attemptRealtimeLogin(email, password);
                });
    }

    /**
     * Fallback login: query Realtime Database by email and compare stored password.
     * Note: storing plaintext passwords is insecure. This fallback is provided per your request
     * but consider relying on Firebase Authentication or hashing passwords for production.
     */
    private void attemptRealtimeLogin(String email, String password) {
        Query q = usersRef.orderByChild("email").equalTo(email);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);

                if (!snapshot.exists()) {
                    Toast.makeText(LoginActivity.this,
                            "Login failed: invalid credentials",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                boolean matched = false;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String dbPassword = child.child("password").getValue(String.class);
                    String status = child.child("status").getValue(String.class);
                    String role = child.child("role").getValue(String.class);

                    if (dbPassword != null && dbPassword.equals(password)) {
                        if (!"ACTIVE".equals(status)) {
                            Toast.makeText(LoginActivity.this,
                                    "Account is inactive",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        matched = true;
                        navigateToDashboard(role);
                        return;
                    }
                }

                if (!matched) {
                    Toast.makeText(LoginActivity.this,
                            "Login failed: invalid credentials",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isValid(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    // ================= USER PROFILE =================

    private void loadUserProfile(String uid) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);

                if (!snapshot.exists()) {
                    Toast.makeText(LoginActivity.this,
                            "User profile not found",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                String role = snapshot.child("role").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);

                if (!"ACTIVE".equals(status)) {
                    Toast.makeText(LoginActivity.this,
                            "Account is inactive",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                navigateToDashboard(role);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ================= NAVIGATION =================

    private void navigateToDashboard(String role) {

        Intent intent;

        if (role == null) {
            Toast.makeText(this,
                    "Invalid user role",
                    Toast.LENGTH_LONG).show();
            return;
        }

        switch (role) {
            case "STUDENT":
                intent = new Intent(this, DashboardStudentActivity.class);
                break;

            case "FACULTY":
                intent = new Intent(this, DashboardFacultyActivity.class);
                break;

            case "VC":
                intent = new Intent(this, DashboardVCActivity.class);
                break;

            case "CHAIRMAN":
                intent = new Intent(this, DashboardChairmanActivity.class);
                break;

            case "ADMIN":
                intent = new Intent(this, DashboardAdminActivity.class);
                break;

            default:
                Toast.makeText(this,
                        "Unknown role",
                        Toast.LENGTH_LONG).show();
                return;
        }

        startActivity(intent);
        finish();
    }

    // ================= FORGOT PASSWORD =================

    private void resetPassword() {
        String email = etEmail != null && etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this,
                    "Enter email to reset password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Password reset email sent",
                                Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // ================= UI HELPERS =================

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
    }
}
