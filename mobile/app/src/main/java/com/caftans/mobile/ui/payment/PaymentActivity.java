package com.caftans.mobile.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.room.AppDatabase;
import com.caftans.mobile.data.room.CartDao;
import com.caftans.mobile.data.room.CartItem;
import com.caftans.mobile.utils.TokenManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvTotalAmount;
    private TextView tvReservationCount;
    private EditText etCardNumber;
    private EditText etCardHolder;
    private EditText etExpiryDate;
    private EditText etCVV;
    private Button btnConfirmPayment;
    private ProgressBar progressBar;
    private double totalAmount;
    private int reservationCount;
    private CartDao cartDao;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);
        reservationCount = getIntent().getIntExtra("reservation_count", 0);

        cartDao = AppDatabase.getInstance(this).cartDao();
        tokenManager = TokenManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Paiement");
        }

        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvReservationCount = findViewById(R.id.tvReservationCount);
        etCardNumber = findViewById(R.id.etCardNumber);
        etCardHolder = findViewById(R.id.etCardHolder);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etCVV = findViewById(R.id.etCVV);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        progressBar = findViewById(R.id.progressBar);

        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        tvTotalAmount.setText(format.format(totalAmount) + " MAD");
        tvReservationCount.setText(reservationCount + " réservation(s)");

        btnConfirmPayment.setOnClickListener(v -> processPayment());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void processPayment() {
        String cardNumber = etCardNumber.getText().toString().trim();
        String cardHolder = etCardHolder.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String cvv = etCVV.getText().toString().trim();

        // Validation
        if (cardNumber.isEmpty() || cardHolder.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cardNumber.length() < 16) {
            Toast.makeText(this, "Numéro de carte invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cvv.length() < 3) {
            Toast.makeText(this, "CVV invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Mock payment processing
        new android.os.Handler().postDelayed(() -> {
            // Payment Successful
            syncReservationsToBackend();
        }, 2000);
    }

    private void syncReservationsToBackend() {
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            setLoading(false);
            Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> items = cartDao.getAll();
        if (items.isEmpty()) {
            finishPayment();
            return;
        }

        AtomicInteger pendingRequests = new AtomicInteger(items.size());
        AtomicInteger successCount = new AtomicInteger(0);
        ApiService apiService = ApiClient.getApiService();

        for (CartItem item : items) {
            // Create reservation on backend with status "approved" (paid)
            ApiService.ReservationRequest request = new ApiService.ReservationRequest(
                    item.caftanId,
                    item.startDate,
                    item.endDate,
                    item.notes != null ? item.notes : "",
                    "approved" // Explicitly setting status to approved/paid
            );

            apiService.createReservation(token, request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()) {
                        cartDao.delete(item); // Remove from local cart ONLY if sync succeeded
                        successCount.incrementAndGet();
                    } else {
                        // Keep in cart if failed?
                        // Instructions say: "On payment failure: Keep cart intact".
                        // Here payment IS successful (mocked), but SYNC failed.
                        // Technically if sync fails we might want to keep it or retry.
                        // For now, we leave it in cart (it won't be deleted) so user can try again or
                        // see error.
                    }
                    checkCompletion(pendingRequests, successCount, items.size());
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    // Network error, leave in cart
                    checkCompletion(pendingRequests, successCount, items.size());
                }
            });
        }
    }

    private void checkCompletion(AtomicInteger pending, AtomicInteger success, int total) {
        if (pending.decrementAndGet() == 0) {
            runOnUiThread(() -> {
                setLoading(false);
                if (success.get() == total) {
                    Toast.makeText(PaymentActivity.this, "Paiement et réservation effectués avec succès!",
                            Toast.LENGTH_LONG).show();
                    // Navigate to history or home.
                    Intent intent = new Intent(PaymentActivity.this, com.caftans.mobile.ui.main.MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    int failed = total - success.get();
                    Toast.makeText(PaymentActivity.this,
                            "Paiement reçu mais " + failed + " réservations n'ont pas pu être synchronisées.",
                            Toast.LENGTH_LONG).show();
                    // We finish anyway, or keep on screen?
                    // Better to finish and let them see items still in cart?
                    // Or ideally if partial success, we return to cart.
                    finish();
                }
            });
        }
    }

    private void finishPayment() {
        setLoading(false);
        Toast.makeText(this, "Paiement effectué!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnConfirmPayment.setEnabled(!loading);
        etCardNumber.setEnabled(!loading);
        etCardHolder.setEnabled(!loading);
        etExpiryDate.setEnabled(!loading);
        etCVV.setEnabled(!loading);
    }
}
