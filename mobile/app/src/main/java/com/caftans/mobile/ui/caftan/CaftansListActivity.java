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

public class CaftansListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CaftanAdapter adapter;
    private List<Caftan> caftans = new ArrayList<>();
    private int categoryId;
    
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
        
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
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
    
    private void loadCaftans() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        
        ApiService apiService = ApiClient.getApiService();
        apiService.getCaftans(categoryId, null, null).enqueue(new Callback<ApiResponse>() {
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
                    Toast.makeText(CaftansListActivity.this, "Erreur lors du chargement des caftans", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(CaftansListActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

