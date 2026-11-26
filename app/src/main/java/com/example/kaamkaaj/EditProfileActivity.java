package com.example.kaamkaaj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class EditProfileActivity extends AppCompatActivity {

    EditText nameInput, phoneInput, emailInput, usernameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        usernameInput = findViewById(R.id.usernameInput);

        String name = Store.userName(this);
        String initial = name.isEmpty() ? "Y" : name.substring(0, 1).toUpperCase();
        ((TextView) findViewById(R.id.editAvatar)).setText(initial);

        nameInput.setText(name);
        phoneInput.setText(Store.userPhone(this));
        emailInput.setText(String.valueOf(Store.session(this)));
        usernameInput.setText(Store.userUsername(this));

        findViewById(R.id.saveBtn).setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newPhone = phoneInput.getText().toString().trim();
            String newUsername = usernameInput.getText().toString().trim();
            if (newName.isEmpty()) {
                nameInput.setError("Name cannot be empty");
                return;
            }
            Store.updateProfile(this, newName, newPhone, newUsername);
            Snackbar.make(v, "Profile updated successfully", Snackbar.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.deleteAccountBtn).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Delete Account?")
                        .setMessage("This will remove all your data and log you out.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, w) -> {
                            for (Store.Item it : Store.load(this)) Reminders.cancel(this, it);
                            Store.clearData(this);
                            Store.logout(this);
                            Intent i = new Intent(this, LoginActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }).show());
    }
}
