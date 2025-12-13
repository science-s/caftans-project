package com.caftans.mobile.ui.reservation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.caftans.mobile.R;
import com.caftans.mobile.data.models.Reservation;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
    
    private List<Reservation> reservations;
    private OnDeleteClickListener onDeleteClickListener;
    
    public interface OnDeleteClickListener {
        void onDeleteClick(Reservation reservation, int position);
    }
    
    public ReservationAdapter(List<Reservation> reservations) {
        this.reservations = reservations;
    }
    
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
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
        
        // Afficher le statut avec traduction
        String statusText = getStatusText(reservation.getStatus());
        holder.tvStatus.setText("Statut: " + statusText);
        
        // Calculer et afficher le prix en utilisant le prix du caftan
        double price = calculatePrice(reservation);
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%.2f MAD", price));
        
        if (reservation.getNotes() != null && !reservation.getNotes().isEmpty()) {
            holder.tvNotes.setText("Notes: " + reservation.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }
        
        // Réinitialiser le listener pour éviter les problèmes de recyclage
        holder.btnDelete.setOnClickListener(null);
        
        // Afficher le bouton supprimer seulement pour les réservations en attente ou approuvées
        if ("pending".equals(reservation.getStatus()) || "approved".equals(reservation.getStatus())) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setEnabled(true);
            holder.btnDelete.setOnClickListener(v -> {
                android.util.Log.d("ReservationAdapter", "Bouton supprimer cliqué pour réservation ID: " + reservation.getId());
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(reservation, position);
                } else {
                    android.util.Log.e("ReservationAdapter", "onDeleteClickListener est null!");
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnDelete.setEnabled(false);
        }
    }
    
    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "En attente";
            case "approved": return "Approuvée";
            case "rejected": return "Refusée";
            case "cancelled": return "Annulée";
            default: return status;
        }
    }
    
    private double calculatePrice(Reservation reservation) {
        if (reservation.getCaftan() == null) {
            return 0.0;
        }
        
        // Utiliser le prix exact du caftan depuis la base de données
        double pricePerDay = reservation.getCaftan().getPricePerDay();
        int numberOfDays = getNumberOfDays(reservation.getStartDate(), reservation.getEndDate());
        
        // Calcul: prix par jour × nombre de jours
        return pricePerDay * numberOfDays;
    }
    
    private int getNumberOfDays(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            
            if (start != null && end != null) {
                long diff = end.getTime() - start.getTime();
                return (int) (diff / (1000 * 60 * 60 * 24)) + 1; // +1 pour inclure le jour de fin
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 1;
    }
    
    public double getTotalPrice() {
        double total = 0.0;
        for (Reservation reservation : reservations) {
            if (reservation.getCaftan() != null && "pending".equals(reservation.getStatus())) {
                total += calculatePrice(reservation);
            }
        }
        return total;
    }
    
    public int getPendingReservationCount() {
        int count = 0;
        for (Reservation reservation : reservations) {
            if ("pending".equals(reservation.getStatus())) {
                count++;
            }
        }
        return count;
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
        TextView tvPrice;
        TextView tvNotes;
        Button btnDelete;
        
        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvCaftanName = itemView.findViewById(R.id.tvCaftanName);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

