package com.tanvir.appointmenthub.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.tanvir.appointmenthub.R;

public class DashboardStudentActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvStudentName;
    private ImageView ivAvatar;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private final List<String> appointments = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_student);

        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvStudentName = findViewById(R.id.tvStudentName);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_view);
        fab = findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(getString(R.string.dashboard_title));

        // sample student name; replace with real data
        tvStudentName.setText("John Doe");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(appointments);
        recyclerView.setAdapter(adapter);

        loadAppointments();

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // simulate network refresh
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadAppointments();
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(DashboardStudentActivity.this, getString(R.string.refreshed), Toast.LENGTH_SHORT).show();
                        }
                    }, 800);
                }
            });
        }

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(DashboardStudentActivity.this, getString(R.string.add_appointment), Toast.LENGTH_SHORT).show();
                    // TODO: open create appointment screen
                }
            });
        }
    }

    private void loadAppointments() {
        appointments.clear();
        // sample data - replace with real fetch
        for (int i = 1; i <= 8; i++) {
            appointments.add("Student Appointment " + i);
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private static class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.VH> {
        private final List<String> data;

        StudentAdapter(List<String> data) {
            this.data = data;
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle;

            VH(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.itemTitle);
                subtitle = itemView.findViewById(R.id.itemSubtitle);
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_appointment, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            final String item = data.get(position);
            holder.title.setText(item);
            holder.subtitle.setText(holder.itemView.getContext().getString(R.string.tap_to_view));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), item + " clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
