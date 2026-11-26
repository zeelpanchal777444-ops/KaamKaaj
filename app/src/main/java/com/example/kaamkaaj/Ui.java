package com.example.kaamkaaj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

final class Ui {
    private Ui() {}

    interface TwoCb {
        /** Returns an error message, or null when everything worked. */
        String on(String a, String b);
    }

    static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    /** Adds system bar insets on top of the view's own padding (edge-to-edge safe). */
    static void pad(View v, boolean top, boolean bottom) {
        final int l = v.getPaddingLeft(), t = v.getPaddingTop(), r = v.getPaddingRight(), b = v.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(v, (x, in) -> {
            Insets s = in.getInsets(WindowInsetsCompat.Type.systemBars());
            x.setPadding(l, t + (top ? s.top : 0), r, b + (bottom ? s.bottom : 0));
            return in;
        });
    }

    /** White status bar icons for screens with a dark coloured background. */
    static void whiteStatus(Activity a) {
        WindowCompat.getInsetsController(a.getWindow(), a.getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
    }

    /** Bottom navigation shared by Home, Bills, Expenses, Tasks and Profile. */
    static void bottomNav(final Activity a, final int selectedId) {
        BottomNavigationView nav = a.findViewById(R.id.bottomNav);
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId && !(id == R.id.nav_home && a instanceof CategoryActivity)) return true;
            Intent i;
            if (id == R.id.nav_home) {
                i = new Intent(a, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_profile) {
                i = new Intent(a, ProfileActivity.class);
            } else {
                i = new Intent(a, CategoryActivity.class);
                i.putExtra("type", id == R.id.nav_bills ? 0 : id == R.id.nav_expenses ? 1 : 4);
            }
            a.startActivity(i);
            a.overridePendingTransition(0, 0);
            if (!(a instanceof MainActivity) && id != R.id.nav_home) a.finish();
            return true;
        });
    }

    private static int[] colorsFor(Store.Item it) {
        int s = Store.status(it);
        if (it.done) return new int[]{R.color.good, R.color.good_bg};
        if (s == 2) return new int[]{R.color.bad, R.color.bad_bg};
        if (s == 1) return new int[]{R.color.warn, R.color.warn_bg};
        return new int[]{R.color.neutral_fg, R.color.neutral_bg};
    }

    /** Status pill such as "Due today", "Tomorrow", "Paid". */
    static void badge(TextView tv, Store.Item it) {
        String t = Store.badge(it);
        if (t.isEmpty()) {
            tv.setVisibility(View.GONE);
            return;
        }
        Context c = tv.getContext();
        int[] col = colorsFor(it);
        tv.setVisibility(View.VISIBLE);
        tv.setText(t);
        tv.setTextColor(ContextCompat.getColor(c, col[0]));
        tv.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(c, col[1])));
    }

    /** Coloured bar on the left edge of a card. */
    static void stripe(View v, Store.Item it) {
        Context c = v.getContext();
        int s = Store.status(it);
        int res = it.done ? R.color.good : s == 2 ? R.color.bad : s == 1 ? R.color.warn : R.color.stripe_neutral;
        v.setBackgroundColor(ContextCompat.getColor(c, res));
    }

    /** Emoji (or first letter for family members) on a tinted rounded square. */
    static void icon(TextView tv, Store.Item it) {
        boolean letter = it.type == 5 && !it.title.isEmpty();
        tv.setText(letter ? it.title.substring(0, 1).toUpperCase() : Store.EMOJI[it.type]);
        tv.setBackgroundTintList(ColorStateList.valueOf(Store.TINT[it.type]));
    }

    /** Small dialog with two text fields (used for reset / change password). */
    static void twoFields(final Activity a, String title, String h1, boolean email1, String h2,
                          final String okMsg, final TwoCb cb) {
        View v = a.getLayoutInflater().inflate(R.layout.dialog_two_fields, null);
        TextInputLayout l1 = v.findViewById(R.id.f1Layout);
        TextInputLayout l2 = v.findViewById(R.id.f2Layout);
        final TextInputEditText e1 = v.findViewById(R.id.f1Input);
        final TextInputEditText e2 = v.findViewById(R.id.f2Input);
        l1.setHint(h1);
        l2.setHint(h2);
        e1.setInputType(email1
                ? (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        e2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new MaterialAlertDialogBuilder(a)
                .setTitle(title)
                .setView(v)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (d, w) -> {
                    String err = cb.on(String.valueOf(e1.getText()), String.valueOf(e2.getText()));
                    Snackbar.make(a.findViewById(android.R.id.content), err == null ? okMsg : err,
                            Snackbar.LENGTH_LONG).show();
                })
                .show();
    }
}
