package com.caftans.mobile.ui.reservation;

import android.os.Bundle;
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
import com.caftans.mobile.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReservationActivity extends AppCompatActivity {
    
    private TextView tvCaftanName;
    private EditText etStartDate, etEndDate, etNotes;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private int caftanId;
    private TokenManager tokenManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        
        caftanId = getIntent().getIntExtra("caftan_id", -1);
        String caftanName = getIntent().getStringExtra("caftan_name");
        
        if (caftanId == -1) {
            Toast.makeText(this, "Caftan invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        tokenManager = TokenManager.getInstance(this);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        tvCaftanName = findViewById(R.id.tvCaftanName);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etNotes = findViewById(R.id.etNotes);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        
        tvCaftanName.setText(caftanName);
        
        // Set default dates (today and tomorrow)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        etStartDate.setText(sdf.format(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        etEndDate.setText(sdf.format(calendar.getTime()));
        
        btnSubmit.setOnClickListener(v -> submitReservation());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    private void submitReservation() {
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir les dates", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String token = tokenManager.getAuthHeader();
        if (token == null) {
            Toast.makeText(this, "Vous devez être connecté", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setLoading(true);
        
        ApiService apiService = ApiClient.getApiService();
        ApiService.ReservationRequest request = new ApiService.ReservationRequest(
            caftanId, startDate, endDate, notes
        );
        
        apiService.createReservation(token, request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ReservationActivity.this, "Réservation créée avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String error = "Erreur lors de la réservation";
                    if (response.body() != null && response.body().getError() != null) {
                        error = response.body().getError();
                    }
                    Toast.makeText(ReservationActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ReservationActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
    }
}

