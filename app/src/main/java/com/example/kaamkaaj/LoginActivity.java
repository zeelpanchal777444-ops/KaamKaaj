package com.example.kaamkaaj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    boolean signup = false;
    TextInputLayout nameL, emailL, passL, confirmL;
    TextInputEditText nameE, emailE, passE, confirmE;
    TextView title, toggle, forgot;
    MaterialButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        if (Store.session(this) != null) {
            go();
            return;
        }
        setContentView(R.layout.activity_login);
        Ui.whiteStatus(this);
        Ui.pad(findViewById(R.id.loginHeader), true, false);
        Ui.pad(findViewById(R.id.loginBody), false, true);

        nameL = findViewById(R.id.nameLayout);
        emailL = findViewById(R.id.emailLayout);
        passL = findViewById(R.id.passLayout);
        confirmL = findViewById(R.id.confirmLayout);
        nameE = findViewById(R.id.nameInput);
        emailE = findViewById(R.id.emailInput);
        passE = findViewById(R.id.passInput);
        confirmE = findViewById(R.id.confirmInput);
        title = findViewById(R.id.formTitle);
        toggle = findViewById(R.id.toggleText);
        forgot = findViewById(R.id.forgotText);
        btn = findViewById(R.id.actionBtn);

        toggle.setOnClickListener(v -> {
            signup = !signup;
            apply();
        });
        forgot.setOnClickListener(v -> Ui.twoFields(this, "Reset password", "Registered email", true,
                "New password (min 6)", "Password updated. Please log in.",
                (e, p) -> Store.resetPassword(this, e, p)));
        btn.setOnClickListener(v -> submit());
        apply();
    }

    void apply() {
        nameL.setVisibility(signup ? View.VISIBLE : View.GONE);
        confirmL.setVisibility(signup ? View.VISIBLE : View.GONE);
        forgot.setVisibility(signup ? View.GONE : View.VISIBLE);
        title.setText(signup ? "Create your account" : "Welcome back");
        btn.setText(signup ? "Sign up" : "Log in");
        toggle.setText(signup ? "Already have an account? Log in" : "New here? Create an account");
        nameL.setError(null);
        emailL.setError(null);
        passL.setError(null);
        confirmL.setError(null);
    }

    void submit() {
        String name = String.valueOf(nameE.getText()).trim();
        String email = String.valueOf(emailE.getText()).trim();
        String pw = String.valueOf(passE.getText());
        String confirm = String.valueOf(confirmE.getText());
        nameL.setError(null);
        emailL.setError(null);
        passL.setError(null);
        confirmL.setError(null);
        boolean ok = true;
        if (signup && name.isEmpty()) {
            nameL.setError("Enter your name");
            ok = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailL.setError("Enter a valid email");
            ok = false;
        }
        if (pw.length() < 6) {
            passL.setError("At least 6 characters");
            ok = false;
        }
        if (signup && !pw.equals(confirm)) {
            confirmL.setError("Passwords do not match");
            ok = false;
        }
        if (!ok) return;
        String err = signup ? Store.signUp(this, name, email, pw) : Store.login(this, email, pw);
        if (err != null) {
            Snackbar.make(btn, err, Snackbar.LENGTH_LONG).show();
            return;
        }
        go();
    }

    void go() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
