package com.caftans.mobile.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Reservation;
import com.caftans.mobile.ui.reservation.ReservationAdapter;
import com.caftans.mobile.utils.TokenManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReservationListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private ProgressBar progressBar;
    private List<Reservation> reservationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reservation_list);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Use existing adapter, might need tweaks for admin actions later
        adapter = new ReservationAdapter(reservationList);
        adapter.setOnItemClickListener(reservation -> {
            // On click listener - open dialog to change status
            showStatusDialog(reservation);
        });
        recyclerView.setAdapter(adapter);

        loadReservations();
    }

    private void loadReservations() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        String token = "Bearer " + TokenManager.getInstance(this).getToken();

        apiService.getReservations(token).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Reservation> reservations = response.body().getReservations();
                    if (reservations != null) {
                        reservationList.clear();
                        reservationList.addAll(reservations);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(AdminReservationListActivity.this, "Failed to load reservations", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminReservationListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void showStatusDialog(Reservation reservation) {
        // Simple implementation: Cycle through statuses or show dialog
        // For now, let's just make a simple call to approve if pending
        if ("pending".equals(reservation.getStatus())) {
            updateStatus(reservation, "confirmed");
        } else if ("confirmed".equals(reservation.getStatus())) {
            updateStatus(reservation, "completed");
        } else {
            Toast.makeText(this, "Status: " + reservation.getStatus(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus(Reservation reservation, String newStatus) {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        String token = "Bearer " + TokenManager.getInstance(this).getToken();

        ApiService.StatusRequest request = new ApiService.StatusRequest(newStatus);

        apiService.updateReservationStatus(token, reservation.getId(), request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminReservationListActivity.this, "Status updated to " + newStatus,
                            Toast.LENGTH_SHORT).show();
                    loadReservations(); // Reload list
                } else {
                    Toast.makeText(AdminReservationListActivity.this, "Failed to update status", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminReservationListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
