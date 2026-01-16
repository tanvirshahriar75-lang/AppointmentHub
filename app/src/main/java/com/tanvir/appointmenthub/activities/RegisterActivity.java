package com.tanvir.appointmenthub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.tanvir.appointmenthub.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    // If your project uses a different RTDB host, update this PROJECT_ID
    private static final String PROJECT_ID = "appointmenthub-e9afc37c";
    // Prefer explicit DB URL derived from project id — update if your project uses a different RTDB host
    private static final String DEFAULT_DB_URL = "https://" + PROJECT_ID + "-default-rtdb.firebaseio.com";
    // Values from app/google-services.json — update if different in your project
    private static final String API_KEY = "AIzaSyCCz3ksVXq44WTaiv-MstuKUNrnkJ6yjd4";
    private static final String APP_ID = "1:590990929706:android:461e4d46561d505af7f853";

    private TextInputEditText etName, etEmail, etPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvHaveAccount;
    private Spinner spinnerRole;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ensure Firebase SDK is initialized. This uses values from google-services.json when present.
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        // Try the explicit default DB URL first (often present when google-services.json is missing database_url)
        try {
            Log.i(TAG, "Initializing FirebaseDatabase with explicit URL: " + DEFAULT_DB_URL);
            usersRef = FirebaseDatabase.getInstance(DEFAULT_DB_URL).getReference("users");
            Log.i(TAG, "usersRef initialized with explicit URL: " + DEFAULT_DB_URL);
        } catch (Exception ex) {
            Log.w(TAG, "Explicit DB URL init failed: " + ex.getMessage() + ". Falling back to default instance.");
            try {
                usersRef = FirebaseDatabase.getInstance().getReference("users");
                Log.i(TAG, "usersRef initialized via default FirebaseDatabase.getInstance()");
            } catch (Exception ex2) {
                Log.w(TAG, "Default FirebaseDatabase init failed: " + ex2.getMessage());
                usersRef = null;
            }
        }

        // If still null, try other common hostname variants
        if (usersRef == null) {
            String[] tryUrls = new String[]{
                    "https://" + PROJECT_ID + ".firebaseio.com",
                    "https://" + PROJECT_ID + ".firebasedatabase.app",
                    DEFAULT_DB_URL
            };

            for (String url : tryUrls) {
                try {
                    Log.i(TAG, "Trying FirebaseDatabase URL: " + url);
                    DatabaseReference ref = FirebaseDatabase.getInstance(url).getReference("users");
                    if (ref != null) {
                        usersRef = ref;
                        Log.i(TAG, "Initialized usersRef with URL: " + url);
                        break;
                    }
                } catch (Exception ex) {
                    Log.w(TAG, "FirebaseDatabase init with URL failed: " + url + " -> " + ex.getMessage());
                }
            }

            if (usersRef == null) {
                Log.w(TAG, "Could not initialize a DatabaseReference for Realtime Database. Check google-services.json and Firebase project configuration.");
            }
        }

        // If still unavailable, try programmatic FirebaseOptions init (fallback)
        if (usersRef == null) {
            try {
                Log.i(TAG, "Attempting programmatic FirebaseOptions initialization with DEFAULT_DB_URL");
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey(API_KEY)
                        .setApplicationId(APP_ID)
                        .setDatabaseUrl(DEFAULT_DB_URL)
                        .setProjectId(PROJECT_ID)
                        .build();

                String fallbackName = "fallbackApp";
                FirebaseApp fallbackApp = null;
                for (FirebaseApp app : FirebaseApp.getApps(this)) {
                    if (fallbackName.equals(app.getName())) {
                        fallbackApp = app;
                        break;
                    }
                }

                if (fallbackApp == null) {
                    fallbackApp = FirebaseApp.initializeApp(this, options, fallbackName);
                }

                if (fallbackApp != null) {
                    FirebaseDatabase db = FirebaseDatabase.getInstance(fallbackApp);
                    usersRef = db.getReference("users");
                    Log.i(TAG, "Initialized usersRef via programmatic FirebaseOptions fallback");
                }
            } catch (Exception ex) {
                Log.w(TAG, "Programmatic FirebaseOptions init failed: " + ex.getMessage());
            }
        }

        // Run a small connectivity/write test to assist debugging
        if (usersRef != null) {
            testDatabaseWrite();
        } else {
            Log.w(TAG, "usersRef is null after initialization; skipping connectivity test.");
        }

        initViews();
        setupRoleSpinner();
        setListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvHaveAccount = findViewById(R.id.tvHaveAccount);
        spinnerRole = findViewById(R.id.spinnerRole);
    }

    private void setupRoleSpinner() {
        String[] roles = new String[]{"STUDENT", "FACULTY", "VC", "CHAIRMAN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
    }

    private void setListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        tvHaveAccount.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String role = spinnerRole.getSelectedItem() != null ? spinnerRole.getSelectedItem().toString() : "STUDENT";

        if (!isValid(name, email, password)) return;

        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Save profile to Realtime Database. NOTE: storing raw passwords in the DB is insecure.
                        // For quick compatibility we're storing the password here so the app can authenticate
                        // users against Realtime Database as a fallback; consider hashing or using only
                        // Firebase Authentication for production.
                        saveUserProfile(user.getUid(), name, email, role, password);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean isValid(String name, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

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

    private void saveUserProfile(String uid, String name, String email, String role, String password) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("email", email);
        profile.put("role", role);
        profile.put("status", "ACTIVE");
        // WARNING: storing plaintext passwords is insecure. Use hashing or FirebaseAuth as the canonical auth.
        profile.put("password", password);

        if (usersRef != null) {
            usersRef.child(uid).setValue(profile)
                    .addOnSuccessListener(aVoid -> {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_LONG).show();
                        // Navigate to login or directly to dashboard
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Write to default DB failed: " + e.getMessage());
                        // Show error so developer can see the reason in UI while debugging
                        Toast.makeText(RegisterActivity.this, "DB write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        // Try alternate host patterns
                        tryAlternateDatabaseAndSave(uid, profile);
                    });
        } else {
            // No default DB instance; try alternate URLs directly
            tryAlternateDatabaseAndSave(uid, profile);
        }
    }

    private void tryAlternateDatabaseAndSave(String uid, Map<String, Object> profile) {
        // Try several common endpoint patterns: legacy firebaseio.com and the newer firebasedatabase.app
        String[] variants = new String[]{
                "https://" + PROJECT_ID + ".firebaseio.com",
                "https://" + PROJECT_ID + "-default-rtdb.firebaseio.com",
                "https://" + PROJECT_ID + ".firebasedatabase.app",
                "https://" + PROJECT_ID + "-default-rtdb.firebasedatabase.app"
        };

        tryAlternateDatabaseAndSaveRecursive(uid, profile, variants, 0);
    }

    private void tryAlternateDatabaseAndSaveRecursive(String uid, Map<String, Object> profile, String[] variants, int index) {
        if (index >= variants.length) {
            showLoading(false);
            Toast.makeText(RegisterActivity.this, "Failed to save profile to Realtime Database. Check console/logs and your Firebase project configuration.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = variants[index];
        try {
            Log.i(TAG, "Attempting DB write using URL: " + url);
            DatabaseReference ref = FirebaseDatabase.getInstance(url).getReference("users");
            ref.child(uid).setValue(profile)
                    .addOnSuccessListener(aVoid -> {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, "Registration successful (via " + url + ")", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e2 -> {
                        Log.w(TAG, "Write failed for " + url + " : " + e2.getMessage());
                        // Try next variant
                        tryAlternateDatabaseAndSaveRecursive(uid, profile, variants, index + 1);
                    });
        } catch (Exception ex) {
            Log.w(TAG, "Failed to init DB with url " + url + " : " + ex.getMessage());
            tryAlternateDatabaseAndSaveRecursive(uid, profile, variants, index + 1);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
    }

    private void testDatabaseWrite() {
        try {
            DatabaseReference debugRef = usersRef.getRoot().child("debug").child("ping");
            Map<String, Object> payload = new HashMap<>();
            payload.put("ok", true);
            payload.put("ts", ServerValue.TIMESTAMP);

            debugRef.setValue(payload)
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "RTDB ping write succeeded"))
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "RTDB ping write failed: " + e.getMessage());
                        Toast.makeText(RegisterActivity.this, "RTDB ping write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            Log.w(TAG, "RTDB ping attempt failed: " + e.getMessage());
            Toast.makeText(this, "RTDB ping attempt failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
