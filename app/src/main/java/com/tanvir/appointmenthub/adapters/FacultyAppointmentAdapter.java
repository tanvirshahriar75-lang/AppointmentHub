package com.tanvir.appointmenthub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tanvir.appointmenthub.R;
import com.tanvir.appointmenthub.models.Appointment;

import java.util.List;

public class FacultyAppointmentAdapter extends RecyclerView.Adapter<FacultyAppointmentAdapter.VH> {
    private final Context context;
    private final List<Appointment> data;

    public FacultyAppointmentAdapter(Context context, List<Appointment> data) {
        this.context = context;
        this.data = data;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.itemTitle);
            tvStatus = itemView.findViewById(R.id.itemSubtitle);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_faculty_appointment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Appointment a = data.get(position);
        holder.tvTitle.setText(a.getTitle() != null ? a.getTitle() : "Appointment");
        holder.tvStatus.setText(a.getStatus() != null ? a.getStatus() : "");
        holder.itemView.setOnClickListener(v -> {
            // placeholder click behavior
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
