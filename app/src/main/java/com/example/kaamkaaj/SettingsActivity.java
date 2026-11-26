package com.example.kaamkaaj;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        refreshProfile();

        findViewById(R.id.editProfileCard).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        CompoundButton notif = findViewById(R.id.notifSwitch);
        notif.setChecked(Store.notifEnabled(this));
        notif.setOnCheckedChangeListener((b, on) -> {
            Store.setNotif(this, on);
            Reminders.scheduleAll(this);
        });

        CompoundButton dark = findViewById(R.id.darkSwitch);
        int mode = Store.darkMode(this);
        boolean systemNight = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        dark.setChecked(mode == 2 || (mode == 0 && systemNight));
        dark.setOnCheckedChangeListener((b, on) -> {
            Store.setDark(this, on ? 2 : 1);
            AppCompatDelegate.setDefaultNightMode(on ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        findViewById(R.id.generalSettingsBtn).setOnClickListener(v ->
                Snackbar.make(v, "General settings configured automatically", Snackbar.LENGTH_SHORT).show());

        findViewById(R.id.languageBtn).setOnClickListener(v ->
                Snackbar.make(v, "App language set to English (default)", Snackbar.LENGTH_SHORT).show());

        findViewById(R.id.contactBtn).setOnClickListener(v ->
                Snackbar.make(v, "Support contact: support@kaamkaaj.app", Snackbar.LENGTH_SHORT).show());

        findViewById(R.id.faqBtn).setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("FAQ")
                .setMessage("Q: How do I track bills?\nA: Go to Bills & Payments tab and tap '+' to add bills and set repeat intervals.\n\nQ: Are reminders local?\nA: Yes, all reminders run locally on your device.")
                .setPositiveButton("OK", null)
                .show());

        findViewById(R.id.tosBtn).setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Terms of Service")
                .setMessage("By using KaamKaaj, you agree to manage your personal household tasks responsibly.")
                .setPositiveButton("OK", null)
                .show());

        findViewById(R.id.policyBtn).setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("User Policy")
                .setMessage("Your data is stored securely and privately on your device.")
                .setPositiveButton("OK", null)
                .show());

        findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            Store.logout(this);
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfile();
    }

    void refreshProfile() {
        String name = Store.userName(this);
        String initial = name.isEmpty() ? "Y" : name.substring(0, 1).toUpperCase();
        ((TextView) findViewById(R.id.settingsAvatar)).setText(initial);
        ((TextView) findViewById(R.id.settingsName)).setText(name);
        ((TextView) findViewById(R.id.settingsUsername)).setText(Store.userUsername(this));
    }
}
