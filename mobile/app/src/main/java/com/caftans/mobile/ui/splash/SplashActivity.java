package com.caftans.mobile.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.caftans.mobile.R;
import com.caftans.mobile.ui.auth.LoginActivity;
import com.caftans.mobile.ui.main.MainActivity;
import com.caftans.mobile.utils.TokenManager;

public class SplashActivity extends AppCompatActivity {
    
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        TokenManager tokenManager = TokenManager.getInstance(this);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (tokenManager.isLoggedIn()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}

