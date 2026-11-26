package com.example.kaamkaaj;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Ui.whiteStatus(this);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing()) return;
            Class<?> next = Store.session(this) != null ? MainActivity.class : LoginActivity.class;
            startActivity(new Intent(this, next));
            finish();
        }, 1200);
    }
}
