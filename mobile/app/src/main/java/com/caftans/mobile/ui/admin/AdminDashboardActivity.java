package com.caftans.mobile.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.caftans.mobile.R;
import com.caftans.mobile.ui.auth.LoginActivity;
import com.caftans.mobile.utils.TokenManager;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnManageReservations = findViewById(R.id.btnManageReservations);
        Button btnManageCaftans = findViewById(R.id.btnManageCaftans);
        Button btnLogout = findViewById(R.id.btnLogout);

        btnManageReservations.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminReservationListActivity.class);
            startActivity(intent);
        });

        btnManageCaftans.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCaftanListActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            TokenManager.getInstance(this).clear();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
