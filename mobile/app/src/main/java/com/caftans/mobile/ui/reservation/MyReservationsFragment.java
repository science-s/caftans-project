package com.caftans.mobile.ui.reservation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Reservation;
import com.caftans.mobile.ui.payment.PaymentActivity;
import com.caftans.mobile.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyReservationsFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ReservationAdapter adapter;
    private List<Reservation> reservations = new ArrayList<>();
    private TokenManager tokenManager;
    private TextView tvTotal;
    private TextView tvReservationCount;
    private Button btnPay;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservations, container, false);
        
        tokenManager = TokenManager.getInstance(requireContext());
        
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvReservationCount = view.findViewById(R.id.tvReservationCount);
        btnPay = view.findViewById(R.id.btnPay);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReservationAdapter(reservations);
        adapter.setOnDeleteClickListener((reservation, position) -> deleteReservation(reservation, position));
        recyclerView.setAdapter(adapter);
        
        btnPay.setOnClickListener(v -> processPayment());
        
        loadReservations();
        
        return view;
    }
    
    private void deleteReservation(Reservation reservation, int position) {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getContext(), "Vous devez être connecté", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d("MyReservations", "Tentative d'annulation réservation ID: " + reservation.getId());
        Log.d("MyReservations", "Reservation status actuel: " + reservation.getStatus());
        Log.d("MyReservations", "Token présent: " + (token != null && !token.isEmpty()));
        Log.d("MyReservations", "User ID stocké: " + tokenManager.getUserId());
        
        // Annuler la réservation en mettant le statut à "cancelled"
        ApiService apiService = ApiClient.getApiService();
        
        // Créer la requête avec le statut cancelled pour annuler (pas supprimer)
        ApiService.ReservationRequest request = new ApiService.ReservationRequest(
            reservation.getCaftanId(),
            reservation.getStartDate(),
            reservation.getEndDate(),
            reservation.getNotes() != null ? reservation.getNotes() : "",
            "cancelled"  // Définir le statut à cancelled
        );
        
        Log.d("MyReservations", "Request status: " + request.getStatus());
        Log.d("MyReservations", "Request caftan_id: " + request.getCaftan_id());
        Log.d("MyReservations", "Request start_date: " + request.getStart_date());
        Log.d("MyReservations", "Request end_date: " + request.getEnd_date());
        
        // Appel API pour annuler (mettre à jour le statut à cancelled)
        apiService.updateReservation(token, reservation.getId(), request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                Log.d("MyReservations", "Response code: " + response.code());
                Log.d("MyReservations", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Réservation annulée avec succès", Toast.LENGTH_SHORT).show();
                    // Recharger les réservations
                    loadReservations();
                } else {
                    // Gérer les erreurs d'autorisation
                    if (response.code() == 401 || response.code() == 403) {
                        String error = "Session expirée. Veuillez vous reconnecter.";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e("MyReservations", "Error body: " + errorBody);
                                
                                // Si c'est une erreur d'autorisation, déconnecter l'utilisateur
                                if (response.code() == 401) {
                                    tokenManager.clear();
                                    // Rediriger vers la page de login
                                    if (getActivity() != null) {
                                        Intent intent = new Intent(getActivity(), com.caftans.mobile.ui.auth.LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                    return;
                                }
                                
                                // Extraire le message d'erreur
                                if (errorBody.contains("\"error\"")) {
                                    int start = errorBody.indexOf("\"error\"") + 9;
                                    int end = errorBody.indexOf("\"", start);
                                    if (end > start) {
                                        error = errorBody.substring(start, end);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e("MyReservations", "Error parsing response", e);
                        }
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    } else {
                        String error = "Erreur lors de l'annulation (Code: " + response.code() + ")";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e("MyReservations", "Error body: " + errorBody);
                                if (errorBody.contains("\"error\"")) {
                                    int start = errorBody.indexOf("\"error\"") + 9;
                                    int end = errorBody.indexOf("\"", start);
                                    if (end > start) {
                                        error = errorBody.substring(start, end);
                                    }
                                }
                            }
                            if (response.body() != null && response.body().getError() != null) {
                                error = response.body().getError();
                            }
                        } catch (Exception e) {
                            Log.e("MyReservations", "Error parsing response", e);
                        }
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("MyReservations", "Delete reservation failed", t);
                Toast.makeText(getContext(), "Erreur de connexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateTotal() {
        double total = adapter.getTotalPrice();
        int count = adapter.getPendingReservationCount();
        
        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        
        tvTotal.setText(format.format(total) + " MAD");
        tvReservationCount.setText(count + " réservation(s) en attente");
        
        btnPay.setEnabled(count > 0 && total > 0);
    }
    
    private void processPayment() {
        double total = adapter.getTotalPrice();
        int count = adapter.getPendingReservationCount();
        
        if (count == 0 || total == 0) {
            Toast.makeText(getContext(), "Aucune réservation à payer", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Rediriger vers la page de paiement
        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        intent.putExtra("total_amount", total);
        intent.putExtra("reservation_count", count);
        startActivity(intent);
    }
    
    private void loadReservations() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(getContext(), "Vous devez être connecté", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        
        ApiService apiService = ApiClient.getApiService();
        apiService.getReservations(token).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Reservation> reservationList = response.body().getReservations();
                    if (reservationList != null) {
                        reservations.clear();
                        reservations.addAll(reservationList);
                        adapter.notifyDataSetChanged();
                        updateTotal();
                    }
                } else {
                    Toast.makeText(getContext(), "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

