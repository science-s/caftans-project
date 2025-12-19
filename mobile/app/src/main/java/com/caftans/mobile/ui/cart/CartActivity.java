package com.caftans.mobile.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Reservation;
import com.caftans.mobile.ui.payment.PaymentActivity;
import com.caftans.mobile.ui.reservation.ReservationAdapter;
import com.caftans.mobile.utils.TokenManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ReservationAdapter adapter;
    private List<Reservation> cartReservations = new ArrayList<>();
    private TokenManager tokenManager;
    private TextView tvTotal;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Mon Panier");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tokenManager = TokenManager.getInstance(this);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvTotal = findViewById(R.id.tvTotal);
        btnPay = findViewById(R.id.btnPay);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter(cartReservations);
        // We reuse the adapter. If delete logic is needed, we can implement it here.
        // The previous adapter code had an OnDeleteClickListener, so we should
        // implement it to allow removing items from cart
        adapter.setOnDeleteClickListener(this::removeReservationFromCart);
        recyclerView.setAdapter(adapter);

        btnPay.setOnClickListener(v -> processPayment());

        loadCartItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCartItems() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getReservations(token).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getReservations() != null) {
                    cartReservations.clear();
                    // Filter only pending reservations for the cart
                    for (Reservation r : response.body().getReservations()) {
                        if ("pending".equals(r.getStatus())) {
                            cartReservations.add(r);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateTotal();
                } else {
                    Toast.makeText(CartActivity.this, "Erreur lors du chargement du panier", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeReservationFromCart(Reservation reservation, int position) {
        String token = tokenManager.getAuthHeader();
        if (token == null)
            return;

        // Logic to cancel/delete the reservation
        // Using same logic as MyReservationsFragment
        ApiService.ReservationRequest request = new ApiService.ReservationRequest(
                reservation.getCaftanId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getNotes() != null ? reservation.getNotes() : "",
                "cancelled");

        ApiClient.getApiService().updateReservation(token, reservation.getId(), request)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CartActivity.this, "Article retiré du panier", Toast.LENGTH_SHORT).show();
                            loadCartItems(); // Reload
                        } else {
                            Toast.makeText(CartActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Toast.makeText(CartActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTotal() {
        double total = 0.0;
        for (Reservation r : cartReservations) {
            // Reusing logic from adapter to calculate price or manually calculating here if
            // needed.
            // Since adapter has helper, we can use it, but calculatePrice is private in
            // Adapter.
            // Fortunately ReservationAdapter has getTotalPrice() but it iterates its own
            // list.
            // Since we passed cartReservations to adapter, adapter.getTotalPrice() should
            // work fine
            // AS LONG AS the adapter logic filters for "pending" which we already ensured
            // by only adding pending items.
        }

        // Actually custom logic might be safer to ensure we display exactly what's
        // visible
        total = adapter.getTotalPrice();

        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        tvTotal.setText(format.format(total) + " MAD");
        btnPay.setEnabled(!cartReservations.isEmpty() && total > 0);
    }

    private void processPayment() {
        double total = adapter.getTotalPrice();
        int count = cartReservations.size();

        if (count == 0 || total == 0) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("total_amount", total);
        intent.putExtra("reservation_count", count);
        startActivity(intent);
    }
}
