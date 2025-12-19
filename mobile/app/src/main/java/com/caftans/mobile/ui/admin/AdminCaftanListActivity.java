package com.caftans.mobile.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Caftan;
import com.caftans.mobile.ui.caftan.CaftanAdapter;
import com.caftans.mobile.ui.caftan.CaftanDetailActivity;
import com.caftans.mobile.utils.TokenManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCaftanListActivity extends AppCompatActivity implements CaftanAdapter.OnCaftanClickListener {

    private RecyclerView recyclerView;
    private CaftanAdapter adapter;
    private ProgressBar progressBar;
    private List<Caftan> caftanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_caftan_list);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new CaftanAdapter(caftanList, this);
        // We might want to add an OnItemClickListener to the adapter if it doesn't have
        // one,
        // or rely on the adapter's existing click handling which usually goes to
        // DetailActivity.
        // For admin, we might want to intercept or just go to detail and have admin
        // options there?
        // Let's stick to standard behavior: click -> detail.
        // But for Admin, we likely want delete/edit options.
        // But for Admin, we likely want delete/edit options.
        // For now, let's just list them.
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditCaftanActivity.class);
            startActivity(intent);
        });

        loadCaftans();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        // Reload list when coming back from add/edit
        loadCaftans();
    }

    private void loadCaftans() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();

        // null for category, search, availability to get all
        apiService.getCaftans(null, null, null).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Caftan> caftans = response.body().getCaftans();
                    if (caftans != null) {
                        caftanList.clear();
                        caftanList.addAll(caftans);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(AdminCaftanListActivity.this, "Failed to load caftans", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminCaftanListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCaftanClick(Caftan caftan) {
        Intent intent = new Intent(this, AddEditCaftanActivity.class);
        intent.putExtra(AddEditCaftanActivity.EXTRA_CAFTAN, new com.google.gson.Gson().toJson(caftan));
        startActivity(intent);
    }
}
