package com.example.kaamkaaj;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    static final String[] QUICK = {"Bills", "Expenses", "Maintenance", "Warranty", "Tasks", "Family", "Reminders"};

    TextView avatar, greeting, hello, statusTitle, statusSub, unpaidText, spentText, nextBillText, maintText, tasksText, remindersText;
    LinearLayout quickRow, upcoming;
    final TextView[] quickCount = new TextView[QUICK.length];

    final ActivityResultLauncher<String> notifPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), ok -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        if (Store.session(this) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        Ui.pad(findViewById(R.id.content), true, false);
        Ui.bottomNav(this, R.id.nav_home);

        avatar = findViewById(R.id.avatar);
        greeting = findViewById(R.id.greeting);
        hello = findViewById(R.id.hello);
        statusTitle = findViewById(R.id.statusTitle);
        statusSub = findViewById(R.id.statusSub);
        unpaidText = findViewById(R.id.unpaidText);
        spentText = findViewById(R.id.spentText);
        nextBillText = findViewById(R.id.nextBillText);
        maintText = findViewById(R.id.maintText);
        tasksText = findViewById(R.id.tasksText);
        remindersText = findViewById(R.id.remindersText);
        quickRow = findViewById(R.id.quickRow);
        upcoming = findViewById(R.id.upcoming);

        avatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.cardBills).setOnClickListener(v -> openCategory(0));
        findViewById(R.id.cardSpent).setOnClickListener(v -> openCategory(1));
        buildQuick();

        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (quickRow == null) return;
        if (Store.session(this) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        refresh();
        Reminders.scheduleAll(this);
    }

    void buildQuick() {
        LayoutInflater inf = LayoutInflater.from(this);
        for (int i = 0; i < QUICK.length; i++) {
            final int type = i;   // QUICK order matches section numbers 0..6
            View v = inf.inflate(R.layout.item_quick, quickRow, false);
            TextView ic = v.findViewById(R.id.qIcon);
            ic.setText(Store.EMOJI[type]);
            ic.setBackgroundTintList(ColorStateList.valueOf(Store.TINT[type]));
            ((TextView) v.findViewById(R.id.qName)).setText(QUICK[i]);
            quickCount[i] = v.findViewById(R.id.qCount);
            v.setOnClickListener(x -> openCategory(type));
            quickRow.addView(v);
        }
    }

    void openCategory(int type) {
        if (type == 2) {
            startActivity(new Intent(this, MaintenanceActivity.class));
            return;
        }
        if (type == 3) {
            startActivity(new Intent(this, WarrantyActivity.class));
            return;
        }
        if (type == 4 || type == 5) {
            startActivity(new Intent(this, FamilyChoresActivity.class));
            return;
        }
        Intent i = new Intent(this, CategoryActivity.class);
        i.putExtra("type", type);
        startActivity(i);
    }

    void refresh() {
        String name = Store.userName(this).trim();
        String first = name.isEmpty() ? "Sara" : name.split(" ")[0];
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        greeting.setText(h < 12 ? "Good Morning" : h < 17 ? "Good Afternoon" : "Good Evening");
        hello.setText(first + " \uD83D\uDC4B");
        avatar.setText(name.isEmpty() ? "S" : name.substring(0, 1).toUpperCase());

        List<Store.Item> all = Store.load(this);
        double unpaid = 0, spent = 0;
        int[] cnt = new int[Store.NAMES.length];
        Calendar now = Calendar.getInstance();
        Calendar u = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        List<Store.Item> soon = new ArrayList<>();
        Store.Item nextBill = null;

        for (Store.Item it : all) {
            if (it.type == 0 && !it.done) {
                unpaid += it.amount;
                cnt[0]++;
                if (nextBill == null || (it.date != 0 && (nextBill.date == 0 || it.date < nextBill.date))) {
                    nextBill = it;
                }
            } else if (it.type == 1) {
                u.setTimeInMillis(it.date);
                if (u.get(Calendar.YEAR) == now.get(Calendar.YEAR) && u.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
                    spent += it.amount;
                    cnt[1]++;
                }
            } else if ((it.type == 2 || it.type == 4) && !it.done) {
                cnt[it.type]++;
            } else if (it.type == 3 || it.type == 5) {
                cnt[it.type]++;
            } else if (it.type == 6 && !it.done && it.date != 0 && Store.daysLeft(it.date) >= 0) {
                cnt[6]++;
            }
            if (it.type != 1 && it.type != 5 && it.date != 0 && !it.done) soon.add(it);
        }
        Collections.sort(soon, (a, b) -> Long.compare(a.date, b.date));

        statusTitle.setText("Dashboard");
        statusSub.setText(cnt[0] + " pending bills • " + cnt[4] + " pending tasks");

        unpaidText.setText(Store.money(unpaid) + " (" + cnt[0] + ")");
        spentText.setText(Store.money(spent));

        if (nextBill != null) {
            long d = nextBill.date != 0 ? Store.daysLeft(nextBill.date) : -1;
            String when = d < 0 ? "Due" : d == 0 ? "Today" : d + " days";
            nextBillText.setText(nextBill.title + " (" + when + ")");
        } else {
            nextBillText.setText("No upcoming");
        }

        maintText.setText(cnt[2] + " records");
        tasksText.setText(cnt[4] + " chores");
        remindersText.setText(cnt[6] + " active");

        String[] unit = {"pending", "this month", "upcoming", "products", "pending", "members", "upcoming"};
        int[] order = {0, 1, 2, 3, 4, 5, 6};
        for (int i = 0; i < QUICK.length; i++) {
            quickCount[i].setText(cnt[order[i]] + " " + unit[i]);
        }

        upcoming.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);
        if (soon.isEmpty()) {
            TextView t = new TextView(this);
            t.setText("Nothing due. Add a bill, task or reminder with a date 🎉");
            t.setPadding(Ui.dp(this, 4), Ui.dp(this, 8), 0, Ui.dp(this, 8));
            upcoming.addView(t);
        }
        for (int i = 0; i < Math.min(5, soon.size()); i++) {
            final Store.Item it = soon.get(i);
            View row = inf.inflate(R.layout.item_due, upcoming, false);
            Ui.icon((TextView) row.findViewById(R.id.uIcon), it);
            ((TextView) row.findViewById(R.id.uTitle)).setText(it.title);
            String amt = it.amount > 0 ? Store.money(it.amount) + "  •  " : "";
            String time = it.type == 6 && it.mins >= 0 ? "  •  " + Store.timeStr(it.mins) : "";
            ((TextView) row.findViewById(R.id.uSub)).setText(amt + Store.dateStr(it.date) + time);
            Ui.stripe(row.findViewById(R.id.uStripe), it);
            Ui.badge((TextView) row.findViewById(R.id.uBadge), it);
            row.setOnClickListener(v2 -> openCategory(it.type));
            upcoming.addView(row);
        }
    }
}
