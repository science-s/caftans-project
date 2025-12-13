package com.caftans.mobile.ui.payment;

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
import java.text.NumberFormat;
import java.util.Locale;

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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        
        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);
        reservationCount = getIntent().getIntExtra("reservation_count", 0);
        
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
        
        // TODO: Intégrer avec une passerelle de paiement réelle (Stripe, PayPal, CMI, etc.)
        // Pour l'instant, simulation du paiement
        simulatePayment();
    }
    
    private void simulatePayment() {
        // Simulation d'un délai de traitement
        new android.os.Handler().postDelayed(() -> {
            setLoading(false);
            Toast.makeText(this, "Paiement effectué avec succès!", Toast.LENGTH_LONG).show();
            
            // Ici, tu pourrais mettre à jour le statut des réservations à "approved"
            // et rediriger vers une page de confirmation
            
            finish();
        }, 2000);
    }
    
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnConfirmPayment.setEnabled(!loading);
    }
}

