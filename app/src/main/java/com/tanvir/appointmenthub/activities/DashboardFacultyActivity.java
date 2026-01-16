package com.tanvir.appointmenthub.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tanvir.appointmenthub.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardFacultyActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private SimpleAdapter adapter;
    private List<String> appointmentList;

    private FirebaseAuth mAuth;
    private DatabaseReference appointmentsRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_faculty);

        initFirebase();
        initViews();
        loadFacultyInfo();
        loadAppointments();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcomeFaculty);
        recyclerView = findViewById(R.id.recyclerAppointments);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();
        adapter = new SimpleAdapter(appointmentList);
        recyclerView.setAdapter(adapter);
    }

    private void loadFacultyInfo() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        usersRef.child(uid).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.getValue(String.class);
                            tvWelcome.setText(getString(R.string.welcome_faculty, name != null ? name : ""));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadAppointments() {
        progressBar.setVisibility(View.VISIBLE);
        if (mAuth.getCurrentUser() == null) return;
        String facultyId = mAuth.getCurrentUser().getUid();

        appointmentsRef.orderByChild("requestedToId")
                .equalTo(facultyId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        appointmentList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // build a simple display string using available fields
                            String title = ds.child("title").getValue(String.class);
                            String status = ds.child("status").getValue(String.class);
                            String by = ds.child("requestedById").getValue(String.class);
                            String display = (title != null ? title : "Appointment") + " - " + (status != null ? status : "");
                            if ("PENDING".equals(status)) {
                                appointmentList.add(display + (by != null ? " (from: " + by + ")" : ""));
                            }
                        }

                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        if (appointmentList.isEmpty()) {
                            Toast.makeText(DashboardFacultyActivity.this,
                                    "No pending appointments",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DashboardFacultyActivity.this,
                                error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private static class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.VH> {
        private final List<String> data;

        SimpleAdapter(List<String> data) { this.data = data; }

        static class VH extends RecyclerView.ViewHolder {
            TextView title;
            VH(View v) { super(v); title = v.findViewById(R.id.itemTitle); }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faculty_appointment, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String t = data.get(position);
            holder.title.setText(t);
        }

        @Override
        public int getItemCount() { return data.size(); }
    }
}
