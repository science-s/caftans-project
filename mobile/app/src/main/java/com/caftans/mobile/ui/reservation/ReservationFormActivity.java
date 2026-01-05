package com.caftans.mobile.ui.reservation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Caftan;
import com.caftans.mobile.data.room.AppDatabase;
import com.caftans.mobile.data.room.CartItem;
import com.caftans.mobile.utils.TokenManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationFormActivity extends AppCompatActivity {

    private TextView tvCaftanName;
    private EditText etStartDate, etEndDate, etNotes;
    private Button btnSubmit;
    private int caftanId;
    private String caftanName;
    private double caftanPrice;
    private String caftanImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation); // Reusing the same layout

        caftanId = getIntent().getIntExtra("caftan_id", -1);
        caftanName = getIntent().getStringExtra("caftan_name");

        // We'll fetch details if needed, but for now we expect them or fetch them.
        // Actually, to store price and image in CartItem, we might need to fetch the
        // Caftan details if not passed.
        // The previous activity passed only ID and Name.
        // Let's quickly fetch the caftan details here to get the price and image, or
        // pass them in Intent.
        // Passing in Intent is faster if CaftanDetailActivity has them.
        // CaftanDetailActivity HAS them. I will update CaftanDetailActivity to pass
        // them.

        // For now, let's look for them in intent
        caftanPrice = getIntent().getDoubleExtra("caftan_price", 0.0);
        caftanImage = getIntent().getStringExtra("caftan_image");

        if (caftanId == -1) {
            Toast.makeText(this, "Caftan invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Réserver");
        }

        tvCaftanName = findViewById(R.id.tvCaftanName);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etNotes = findViewById(R.id.etNotes);
        btnSubmit = findViewById(R.id.btnSubmit);
        // Reuse ID btnSubmit but change text logic if needed (Layout says "Réserver",
        // maybe change to "Ajouter au panier" dynamically)
        btnSubmit.setText("Ajouter au panier");

        tvCaftanName.setText(caftanName);

        // Set default dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        etStartDate.setText(sdf.format(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        etEndDate.setText(sdf.format(calendar.getTime()));

        btnSubmit.setOnClickListener(v -> addToCart());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void addToCart() {
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir les dates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to Room
        CartItem item = new CartItem(caftanId, caftanName, caftanImage, caftanPrice, startDate, endDate, notes);
        AppDatabase.getInstance(this).cartDao().insert(item);

        Toast.makeText(this, "Ajouté au panier", Toast.LENGTH_SHORT).show();
        finish();
    }
}
