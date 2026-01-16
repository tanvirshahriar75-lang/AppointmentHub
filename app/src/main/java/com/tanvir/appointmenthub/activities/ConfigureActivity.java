package com.tanvir.appointmenthub.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tanvir.appointmenthub.R;

public class ConfigureActivity extends AppCompatActivity {

    private EditText serverEdit;
    private EditText portEdit;
    private Button saveBtn;

    private static final String PREFS_NAME = "appointment_prefs";
    private static final String KEY_SERVER = "server";
    private static final String KEY_PORT = "port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Configure");
            setSupportActionBar(toolbar);
            // show up button
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        serverEdit = findViewById(R.id.serverEdit);
        portEdit = findViewById(R.id.portEdit);
        saveBtn = findViewById(R.id.saveBtn);

        // Load existing values
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        serverEdit.setText(prefs.getString(KEY_SERVER, ""));
        portEdit.setText(prefs.getString(KEY_PORT, ""));

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String server = serverEdit.getText().toString().trim();
                String port = portEdit.getText().toString().trim();

                if (server.isEmpty()) {
                    Toast.makeText(ConfigureActivity.this, "Please enter server", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save to preferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_SERVER, server);
                editor.putString(KEY_PORT, port);
                editor.apply();

                Toast.makeText(ConfigureActivity.this, "Configuration saved", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
