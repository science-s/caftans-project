package com.caftans.mobile.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.caftans.mobile.R;
import com.caftans.mobile.data.room.AppDatabase;
import com.caftans.mobile.data.room.CartDao;
import com.caftans.mobile.ui.caftan.CategoriesFragment;
import com.caftans.mobile.ui.reservation.MyReservationsFragment;
import com.caftans.mobile.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private android.widget.TextView badgeTextView;
    private android.view.View badgeLayout;
    private com.caftans.mobile.utils.TokenManager tokenManager;
    private BottomNavigationView bottomNavigation;
    private CartDao cartDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tokenManager = com.caftans.mobile.utils.TokenManager.getInstance(this);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);
        cartDao = AppDatabase.getInstance(this).cartDao();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new com.caftans.mobile.ui.home.HomeFragment());
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

        int count = cartDao.getAll().size();

        if (count > 0) {
            badgeTextView.setVisibility(android.view.View.VISIBLE);
            badgeTextView.setText(String.valueOf(count));
        } else {
            badgeTextView.setVisibility(android.view.View.GONE);
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            fragment = new com.caftans.mobile.ui.home.HomeFragment();
        } else if (itemId == R.id.nav_categories) {
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

    // Public methods for navigation from HomeFragment
    public void navigateToCategories() {
        bottomNavigation.setSelectedItemId(R.id.nav_categories);
    }

    public void navigateToReservations() {
        bottomNavigation.setSelectedItemId(R.id.nav_reservations);
    }

    public void navigateToProfile() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }
}
