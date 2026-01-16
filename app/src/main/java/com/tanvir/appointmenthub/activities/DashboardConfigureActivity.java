package com.tanvir.appointmenthub.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tanvir.appointmenthub.R;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DashboardConfigureActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private final List<String> items = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fab;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_configure);

        toolbar = findViewById(R.id.toolbar);
        tvGreeting = findViewById(R.id.tvGreeting);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_view);
        fab = findViewById(R.id.fab);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(getString(R.string.configure_title));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(items);
        recyclerView.setAdapter(adapter);

        loadData();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // simulate refresh
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(DashboardConfigureActivity.this, getString(R.string.refreshed), Toast.LENGTH_SHORT).show();
                    }
                }, 800);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open ConfigureActivity
                Intent intent = new Intent(DashboardConfigureActivity.this, ConfigureActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadData() {
        items.clear();
        // sample data; replace with real data loading
        for (int i = 1; i <= 6; i++) {
            items.add("Config item " + i);
        }
        adapter.notifyDataSetChanged();
    }

    private static class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {
        private final List<String> data;

        AppointmentAdapter(List<String> data) {
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            final String t = data.get(position);
            holder.title.setText(t);
            holder.subtitle.setText(holder.itemView.getContext().getString(R.string.tap_to_view));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), t + " clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}

