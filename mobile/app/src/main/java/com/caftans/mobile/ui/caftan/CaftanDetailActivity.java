package com.caftans.mobile.ui.caftan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Caftan;
import com.caftans.mobile.ui.reservation.ReservationActivity;
import com.caftans.mobile.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaftanDetailActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView tvName, tvDescription, tvPrice, tvCategory, tvAvailability;
    private Button btnReserve;
    private ProgressBar progressBar;
    private Caftan caftan;
    private int caftanId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caftan_detail);

        caftanId = getIntent().getIntExtra("caftan_id", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imageView = findViewById(R.id.imageView);
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrice = findViewById(R.id.tvPrice);
        tvCategory = findViewById(R.id.tvCategory);
        tvAvailability = findViewById(R.id.tvAvailability);
        btnReserve = findViewById(R.id.btnReserve);
        btnReserve.setText("Ajouter au panier");
        progressBar = findViewById(R.id.progressBar);

        btnReserve.setOnClickListener(v -> {
            if (caftan != null) {
                Intent intent = new Intent(CaftanDetailActivity.this,
                        com.caftans.mobile.ui.reservation.ReservationFormActivity.class);
                intent.putExtra("caftan_id", caftan.getId());
                intent.putExtra("caftan_name", caftan.getName());
                intent.putExtra("caftan_price", caftan.getPricePerDay());
                intent.putExtra("caftan_image", caftan.getImageUrl());
                startActivity(intent);
            }
        });

        loadCaftanDetails();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadCaftanDetails() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        ApiService apiService = ApiClient.getApiService();
        apiService.getCaftan(caftanId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(android.view.View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    caftan = response.body().getCaftan();
                    if (caftan != null) {
                        displayCaftanDetails();
                    }
                } else {
                    Toast.makeText(CaftanDetailActivity.this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(CaftanDetailActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCaftanDetails() {
        if (caftan == null)
            return;

        tvName.setText(caftan.getName());
        tvDescription.setText(caftan.getDescription());
        tvPrice.setText(String.format("%.0f MAD/jour", caftan.getPricePerDay()));
        tvCategory.setText(caftan.getCategoryName());
        tvAvailability.setText(caftan.isAvailable() ? "Disponible" : "Non disponible");

        String imageUrl = caftan.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullUrl = imageUrl.startsWith("http") ? imageUrl : ApiClient.BASE_URL_WITHOUT_API + imageUrl;
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imageView);
        }

        btnReserve.setEnabled(caftan.isAvailable());
    }
}
