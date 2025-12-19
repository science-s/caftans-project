package com.caftans.mobile.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.caftans.mobile.R;
import com.caftans.mobile.ui.caftan.CategoriesFragment;
import com.caftans.mobile.ui.reservation.MyReservationsFragment;
import com.caftans.mobile.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private android.widget.TextView badgeTextView;
    private android.view.View badgeLayout;
    private com.caftans.mobile.utils.TokenManager tokenManager;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tokenManager = com.caftans.mobile.utils.TokenManager.getInstance(this);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new CategoriesFragment());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_cart);
        badgeLayout = menuItem.getActionView();
        badgeTextView = badgeLayout.findViewById(R.id.actionbar_badge_textview);

        badgeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.caftans.mobile.ui.cart.CartActivity.class);
            startActivity(intent);
        });

        updateBadgeCount();

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBadgeCount();
    }

    private void updateBadgeCount() {
        if (badgeTextView == null)
            return;

        String token = tokenManager.getAuthHeader();
        if (token == null) {
            badgeTextView.setVisibility(android.view.View.GONE);
            return;
        }

        com.caftans.mobile.data.api.ApiClient.getApiService().getReservations(token)
                .enqueue(new retrofit2.Callback<com.caftans.mobile.data.models.ApiResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.caftans.mobile.data.models.ApiResponse> call,
                            retrofit2.Response<com.caftans.mobile.data.models.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getReservations() != null) {
                            int count = 0;
                            for (com.caftans.mobile.data.models.Reservation r : response.body().getReservations()) {
                                if (!"confirmed".equals(r.getStatus()) && !"cancelled".equals(r.getStatus())
                                        && !"completed".equals(r.getStatus())) {
                                    count++;
                                }
                            }

                            if (count > 0) {
                                badgeTextView.setVisibility(android.view.View.VISIBLE);
                                badgeTextView.setText(String.valueOf(count));
                            } else {
                                badgeTextView.setVisibility(android.view.View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.caftans.mobile.data.models.ApiResponse> call,
                            Throwable t) {
                        // Fail silently for badge
                    }
                });
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_categories) {
            fragment = new CategoriesFragment();
        } else if (itemId == R.id.nav_reservations) {
            fragment = new MyReservationsFragment();
        } else if (itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();

            // Refresh badge when navigating pages just in case
            updateBadgeCount();
            return true;
        }
        return false;
    }
}
