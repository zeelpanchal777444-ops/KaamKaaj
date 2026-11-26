package com.example.kaamkaaj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Ui.pad(findViewById(R.id.content), true, false);
        Ui.bottomNav(this, R.id.nav_profile);

        String name = Store.userName(this);
        String initial = name.isEmpty() ? "Y" : name.substring(0, 1).toUpperCase();
        ((TextView) findViewById(R.id.avatar)).setText(initial);
        ((TextView) findViewById(R.id.pName)).setText(name);
        ((TextView) findViewById(R.id.postAvatar)).setText(initial);
        ((TextView) findViewById(R.id.postName)).setText(name);

        findViewById(R.id.settingsBtn).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = Store.userName(this);
        String initial = name.isEmpty() ? "Y" : name.substring(0, 1).toUpperCase();
        ((TextView) findViewById(R.id.avatar)).setText(initial);
        ((TextView) findViewById(R.id.pName)).setText(name);
        ((TextView) findViewById(R.id.postAvatar)).setText(initial);
        ((TextView) findViewById(R.id.postName)).setText(name);
    }
}
