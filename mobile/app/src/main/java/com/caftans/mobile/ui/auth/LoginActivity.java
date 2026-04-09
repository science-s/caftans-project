package com.caftans.mobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.User;
import com.caftans.mobile.ui.main.MainActivity;
import com.caftans.mobile.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = TokenManager.getInstance(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        TextView tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> performLogin());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        ApiService apiService = ApiClient.getApiService();
        ApiService.LoginRequest request = new ApiService.LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                setLoading(false);

                Log.d("LoginActivity", "Response code: " + response.code());
                Log.d("LoginActivity", "Response successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    Log.d("LoginActivity",
                            "Access token: " + (apiResponse.getAccessToken() != null ? "Present" : "Null"));
                    Log.d("LoginActivity", "User: " + (apiResponse.getUser() != null ? "Present" : "Null"));
                    Log.d("LoginActivity", "Error: " + apiResponse.getError());
                    Log.d("LoginActivity", "Message: " + apiResponse.getMessage());

                    if (apiResponse.getAccessToken() != null) {
                        // Save token and user info
                        tokenManager.saveToken(apiResponse.getAccessToken());
                        if (apiResponse.getUser() != null) {
                            tokenManager.saveUserId(apiResponse.getUser().getId());
                            tokenManager.saveUserEmail(apiResponse.getUser().getEmail());
                            tokenManager.saveUserRole(apiResponse.getUser().getRole());
                        }

                        Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();

                        // Navigate based on role
                        Intent intent;
                        if (apiResponse.getUser() != null && "admin".equals(apiResponse.getUser().getRole())) {
                            intent = new Intent(LoginActivity.this,
                                    com.caftans.mobile.ui.admin.AdminDashboardActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = "Erreur de connexion";
                        if (apiResponse.getError() != null) {
                            errorMsg = apiResponse.getError();
                        } else if (apiResponse.getMessage() != null) {
                            errorMsg = apiResponse.getMessage();
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Gestion des erreurs HTTP
                    String errorMsg = "Erreur de connexion (Code: " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("LoginActivity", "Error body: " + errorBody);

                            // Essayer d'extraire le message d'erreur du JSON
                            if (errorBody.contains("\"error\"")) {
                                try {
                                    // Essayer de parser le JSON proprement
                                    int start = errorBody.indexOf("\"error\"") + 9;
                                    int end = errorBody.indexOf("\"", start);
                                    if (end > start) {
                                        errorMsg = errorBody.substring(start, end);
                                    } else {
                                        // Essayer une autre méthode de parsing
                                        int colonIndex = errorBody.indexOf(":", errorBody.indexOf("\"error\""));
                                        if (colonIndex > 0) {
                                            int quoteStart = errorBody.indexOf("\"", colonIndex) + 1;
                                            int quoteEnd = errorBody.indexOf("\"", quoteStart);
                                            if (quoteEnd > quoteStart) {
                                                errorMsg = errorBody.substring(quoteStart, quoteEnd);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("LoginActivity", "Error parsing error message", e);
                                }
                            }
                        }
                        if (response.body() != null && response.body().getError() != null) {
                            errorMsg = response.body().getError();
                        }
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Error reading error body", e);
                    }
                    Log.e("LoginActivity", "Final error message: " + errorMsg);
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                setLoading(false);
                Log.e("LoginActivity", "Login failed", t);
                String errorMsg = "Erreur de connexion au serveur";
                if (t.getMessage() != null) {
                    Log.e("LoginActivity", "Error message: " + t.getMessage());
                    if (t.getMessage().contains("Failed to connect")
                            || t.getMessage().contains("Unable to resolve host")) {
                        errorMsg = "Impossible de se connecter au serveur.\nVérifiez que le serveur Flask est démarré.";
                    } else {
                        errorMsg = "Erreur: " + t.getMessage();
                    }
                }
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }
}
