package com.tanvir.appointmenthub.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import com.tanvir.appointmenthub.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardAdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fab;
    private final List<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(getString(R.string.admin_dashboard_title));

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        fab = findViewById(R.id.fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminAdapter(items);
        recyclerView.setAdapter(adapter);

        loadItems();

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(true);
                    // simulate refresh
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadItems();
                            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                            Toast.makeText(DashboardAdminActivity.this, getString(R.string.refreshed), Toast.LENGTH_SHORT).show();
                        }
                    }, 800);
                }
            });
        }

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(DashboardAdminActivity.this, getString(R.string.add_appointment), Toast.LENGTH_SHORT).show();
                    // TODO: open create appointment/admin action
                }
            });
        }
    }

    private void loadItems() {
        items.clear();
        for (int i = 1; i <= 12; i++) {
            items.add("Admin item " + i);
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private static class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.VH> {
        private final List<String> data;

        AdminAdapter(List<String> data) {
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_appointment, parent, false);
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
