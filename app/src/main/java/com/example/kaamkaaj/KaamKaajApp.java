package com.example.kaamkaaj;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class KaamKaajApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        int m = Store.darkMode(this);
        AppCompatDelegate.setDefaultNightMode(m == 2 ? AppCompatDelegate.MODE_NIGHT_YES
                : m == 1 ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
