package com.caftans.mobile.ui.caftan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Caftan;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.SearchView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.widget.HorizontalScrollView;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.text.TextUtils;

public class CaftansListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CaftanAdapter adapter;
    private List<Caftan> caftans = new ArrayList<>();
    private int categoryId;

    // Filter UI
    private ChipGroup filterChipGroup;
    private Chip chipCategory, chipColor, chipPrice, chipClear;
    private SearchView searchView;

    // Filter values
    private String selectedColor = null;
    private Double selectedMinPrice = null;
    private Double selectedMaxPrice = null;
    private String searchQuery = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caftans_list);

        categoryId = getIntent().getIntExtra("category_id", -1);
        String categoryName = getIntent().getStringExtra("category_name");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(categoryName != null ? categoryName : "Caftans");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Filters
        filterChipGroup = findViewById(R.id.filterChipGroup);
        chipCategory = findViewById(R.id.chipCategory);
        chipColor = findViewById(R.id.chipColor);
        chipPrice = findViewById(R.id.chipPrice);
        chipClear = findViewById(R.id.chipClear);
        searchView = findViewById(R.id.searchView);

        setupFilters();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(true);

        adapter = new CaftanAdapter(caftans, caftan -> {
            Intent intent = new Intent(CaftansListActivity.this, CaftanDetailActivity.class);
            intent.putExtra("caftan_id", caftan.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadCaftans();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupFilters() {
        // Search View Logic
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                loadCaftans();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    searchQuery = null;
                    loadCaftans();
                }
                return false;
            }
        });

        // Category Chip (Placeholder as we need categories list, assuming handled by
        // chips logic or intent for now)
        // If user wants to CHANGE category, we might need a dialog.
        // For now, let's keep intent category.

        chipCategory.setOnClickListener(v -> {
            // Simple dialog to clear category or select all (reset to -1)
            // Ideally should fetch categories and show selection.
            // For MVP: Prompt to clear category filter if set
            if (categoryId != -1) {
                new AlertDialog.Builder(this)
                        .setTitle("Category")
                        .setMessage("Clear category filter?")
                        .setPositiveButton("Clear", (dialog, which) -> {
                            categoryId = -1;
                            loadCaftans();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Select a category from the Home screen first (Feature pending)",
                        Toast.LENGTH_SHORT).show();
            }
        });

        chipColor.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Filter by Color");

            final EditText input = new EditText(this);
            input.setHint("Enter color (e.g. Red, Blue)");
            builder.setView(input);

            builder.setPositiveButton("Apply", (dialog, which) -> {
                selectedColor = input.getText().toString();
                if (selectedColor.isEmpty())
                    selectedColor = null;
                loadCaftans();
                chipClear.setVisibility(View.VISIBLE);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        chipPrice.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Filter by Price");

            // Custom layout for price range
            // simplified for now using input text or 2 inputs
            // Let's use a simpler prompt for max price since Min is usually 0
            final EditText input = new EditText(this);
            input.setHint("Max Price");
            input.setInputType(
                    android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setView(input);

            builder.setPositiveButton("Apply", (dialog, which) -> {
                try {
                    String max = input.getText().toString();
                    if (!max.isEmpty()) {
                        selectedMaxPrice = Double.parseDouble(max);
                    } else {
                        selectedMaxPrice = null;
                    }
                    loadCaftans();
                    chipClear.setVisibility(View.VISIBLE);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid Price", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        chipClear.setOnClickListener(v -> {
            selectedColor = null;
            selectedMinPrice = null;
            selectedMaxPrice = null;
            searchQuery = null;
            categoryId = -1;
            searchView.setQuery("", false);
            searchView.clearFocus();
            chipClear.setVisibility(View.GONE);
            loadCaftans();
        });
    }

    private void loadCaftans() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        ApiService apiService = ApiClient.getApiService();
        // Passer categoryId seulement s'il est valide (>= 0)
        Integer catId = categoryId >= 0 ? categoryId : null;

        apiService.getCaftans(catId, searchQuery, null, selectedColor, selectedMinPrice, selectedMaxPrice)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        progressBar.setVisibility(android.view.View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Caftan> caftanList = response.body().getCaftans();
                            if (caftanList != null) {
                                caftans.clear();
                                caftans.addAll(caftanList);
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(CaftansListActivity.this, "Erreur lors du chargement des caftans",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        progressBar.setVisibility(android.view.View.GONE);
                        Toast.makeText(CaftansListActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}
