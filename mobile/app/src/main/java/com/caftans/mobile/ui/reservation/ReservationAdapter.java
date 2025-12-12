package com.caftans.mobile.ui.reservation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.caftans.mobile.R;
import com.caftans.mobile.data.models.Reservation;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
    
    private List<Reservation> reservations;
    
    public ReservationAdapter(List<Reservation> reservations) {
        this.reservations = reservations;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        
        if (reservation.getCaftan() != null) {
            holder.tvCaftanName.setText(reservation.getCaftan().getName());
        }
        
        holder.tvStartDate.setText("Du: " + reservation.getStartDate());
        holder.tvEndDate.setText("Au: " + reservation.getEndDate());
        holder.tvStatus.setText("Statut: " + reservation.getStatus());
        
        if (reservation.getNotes() != null && !reservation.getNotes().isEmpty()) {
            holder.tvNotes.setText("Notes: " + reservation.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return reservations.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvCaftanName;
        TextView tvStartDate;
        TextView tvEndDate;
        TextView tvStatus;
        TextView tvNotes;
        
        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvCaftanName = itemView.findViewById(R.id.tvCaftanName);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNotes = itemView.findViewById(R.id.tvNotes);
        }
    }
}

