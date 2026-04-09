package com.caftans.mobile.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Caftan;
import com.caftans.mobile.data.models.Category;
import com.caftans.mobile.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditCaftanActivity extends AppCompatActivity {

    public static final String EXTRA_CAFTAN = "com.caftans.mobile.EXTRA_CAFTAN";

    private TextInputEditText etName, etDescription, etPrice;
    private Spinner spinnerCategory;
    private CheckBox cbAvailable;
    private Button btnSave, btnDelete, btnPickImage;
    private ImageView ivPreview;
    private ProgressBar progressBar;
    private TextView tvTitle;

    private Caftan currentCaftan;
    private List<Category> categories = new ArrayList<>();
    private ArrayAdapter<Category> categoryAdapter;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).into(ivPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_caftan);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cbAvailable = findViewById(R.id.cbAvailable);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnPickImage = findViewById(R.id.btnPickImage);
        ivPreview = findViewById(R.id.ivPreview);
        progressBar = findViewById(R.id.progressBar);
        tvTitle = findViewById(R.id.tvTitle);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        loadCategories();

        if (getIntent().hasExtra(EXTRA_CAFTAN)) {
            // Edit Mode
            String caftanJson = getIntent().getStringExtra(EXTRA_CAFTAN);
            currentCaftan = new Gson().fromJson(caftanJson, Caftan.class);

            tvTitle.setText("Modifier Caftan");
            populateFields();
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            // Add Mode
            tvTitle.setText("Ajouter Caftan");
            btnDelete.setVisibility(View.GONE);
        }

        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveCaftan());
        btnDelete.setOnClickListener(v -> deleteCaftan());
    }

    private void loadCategories() {
        ApiService apiService = ApiClient.getApiService();
        apiService.getCategories().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> cats = response.body().getCategories();
                    if (cats != null) {
                        categories.clear();
                        categories.addAll(cats);
                        categoryAdapter.notifyDataSetChanged();

                        // Select correct category if editing
                        if (currentCaftan != null) {
                            for (int i = 0; i < categories.size(); i++) {
                                if (categories.get(i).getId() == currentCaftan.getCategoryId()) {
                                    spinnerCategory.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(AddEditCaftanActivity.this, "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields() {
        etName.setText(currentCaftan.getName());
        etDescription.setText(currentCaftan.getDescription());
        etPrice.setText(String.valueOf(currentCaftan.getPricePerDay()));
        cbAvailable.setChecked("available".equals(currentCaftan.getAvailabilityStatus()));

        if (currentCaftan.getImageUrl() != null && !currentCaftan.getImageUrl().isEmpty()) {
            String fullUrl = currentCaftan.getImageUrl().startsWith("http") ? currentCaftan.getImageUrl()
                    : ApiClient.BASE_URL_WITHOUT_API + currentCaftan.getImageUrl();
            Glide.with(this).load(fullUrl).into(ivPreview);
        }
    }

    private void saveCaftan() {
        String nameStr = etName.getText().toString().trim();
        String descriptionStr = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();

        if (nameStr.isEmpty() || descriptionStr.isEmpty() || priceStr.isEmpty() || selectedCategory == null) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate price format
        try {
            Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Prix invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        String token = "Bearer " + TokenManager.getInstance(this).getToken();

        // Create RequestBody for text parts
        RequestBody name = createPartFromString(nameStr);
        RequestBody description = createPartFromString(descriptionStr);
        RequestBody pricePerDay = createPartFromString(priceStr);
        RequestBody categoryId = createPartFromString(String.valueOf(selectedCategory.getId()));
        RequestBody availabilityStatus = createPartFromString(cbAvailable.isChecked() ? "available" : "unavailable");

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            try {
                File file = createTempFileFromUri(selectedImageUri);
                RequestBody requestFile = RequestBody
                        .create(MediaType.parse(getContentResolver().getType(selectedImageUri)), file);
                imagePart = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du traitement de l'image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
        }

        if (currentCaftan != null) {
            // Update
            apiService
                    .updateCaftanMultipart(token, currentCaftan.getId(), name, description, pricePerDay, categoryId,
                            availabilityStatus, imagePart)
                    .enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(AddEditCaftanActivity.this, "Caftan mis à jour", Toast.LENGTH_SHORT)
                                        .show();
                                finish();
                            } else {
                                Toast.makeText(AddEditCaftanActivity.this, "Erreur mise à jour: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddEditCaftanActivity.this, "Erreur réseau: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Create
            if (imagePart == null) {
                Toast.makeText(this, "Veuillez choisir une image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            apiService
                    .createCaftanMultipart(token, name, description, pricePerDay, categoryId, availabilityStatus,
                            imagePart)
                    .enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            progressBar.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(AddEditCaftanActivity.this, "Caftan créé", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // Check for 500 or detailed error
                                Toast.makeText(AddEditCaftanActivity.this, "Erreur création: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(AddEditCaftanActivity.this, "Erreur réseau: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void deleteCaftan() {
        if (currentCaftan == null)
            return;

        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        String token = "Bearer " + TokenManager.getInstance(this).getToken();

        apiService.deleteCaftan(token, currentCaftan.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AddEditCaftanActivity.this, "Caftan supprimé", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddEditCaftanActivity.this, "Erreur suppression", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddEditCaftanActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MultipartBody.FORM, descriptionString);
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("header", ".jpg", getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
        return tempFile;
    }
}
