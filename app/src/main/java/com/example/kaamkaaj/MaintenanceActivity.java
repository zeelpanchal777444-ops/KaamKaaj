package com.example.kaamkaaj;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceActivity extends AppCompatActivity {

    TextView goodCount, warnCount, alertCount, statusHeading, statusSub, emptyText;
    LinearLayout maintContainer;
    List<Store.Item> all = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        String userName = Store.userName(this);
        ((TextView) findViewById(R.id.profileAvatar)).setText(userName.isEmpty() ? "S" : userName.substring(0, 1).toUpperCase());

        goodCount = findViewById(R.id.goodCount);
        warnCount = findViewById(R.id.warnCount);
        alertCount = findViewById(R.id.alertCount);
        statusHeading = findViewById(R.id.statusHeading);
        statusSub = findViewById(R.id.statusSub);
        emptyText = findViewById(R.id.emptyText);
        maintContainer = findViewById(R.id.maintContainer);

        findViewById(R.id.addMaintBtn).setOnClickListener(v -> showAddDialog(null));

        seedDefaultsIfNeeded();
        refresh();
    }

    void seedDefaultsIfNeeded() {
        all = Store.load(this);
        boolean hasMaint = false;
        for (Store.Item it : all) {
            if (it.type == 2) hasMaint = true;
        }

        if (!hasMaint) {
            long today = Store.todayUtc();
            String[][] defaults = {
                    {"AC Servicing", "Living Room AC", "1500", String.valueOf(today + 86400000L * 2), String.valueOf(today - 86400000L * 90), "Clean filters & check gas", "false"},
                    {"Washing Machine Service", "Whirlpool 7kg", "800", String.valueOf(today - 86400000L), String.valueOf(today - 86400000L * 180), "Check drum & inlet pipe", "false"},
                    {"Refrigerator Repair", "Samsung Double Door", "1200", String.valueOf(today + 86400000L * 15), String.valueOf(today - 86400000L * 200), "Cooling check", "false"},
                    {"Water Purifier Service", "Kent RO", "600", String.valueOf(today + 86400000L * 30), String.valueOf(today - 86400000L * 150), "Change sediment filter", "false"}
            };
            for (String[] d : defaults) {
                Store.Item it = new Store.Item();
                it.id = System.currentTimeMillis() + (long)(Math.random() * 1000);
                it.type = 2;
                it.title = d[0];
                it.extra = d[1];
                it.amount = Double.parseDouble(d[2]);
                it.date = Long.parseLong(d[3]);
                it.date2 = Long.parseLong(d[4]);
                it.notes = d[5];
                it.done = Boolean.parseBoolean(d[6]);
                all.add(it);
            }
            Store.save(this, all);
        }
    }

    void refresh() {
        all = Store.load(this);
        maintContainer.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);

        List<Store.Item> items = new ArrayList<>();
        int good = 0, warn = 0, alert = 0;

        for (Store.Item it : all) {
            if (it.type == 2) {
                items.add(it);
                if (it.done) {
                    good++;
                } else {
                    int s = Store.status(it);
                    if (s == 2) alert++;
                    else if (s == 1) warn++;
                    else good++;
                }
            }
        }

        goodCount.setText(String.valueOf(good));
        warnCount.setText(String.valueOf(warn));
        alertCount.setText(String.valueOf(alert));

        if (alert > 0) {
            statusHeading.setText("Needs attention");
            statusSub.setText(alert + (alert == 1 ? " equipment is" : " equipments are") + " overdue for service.");
        } else if (warn > 0) {
            statusHeading.setText("Check soon");
            statusSub.setText(warn + (warn == 1 ? " equipment needs" : " equipments need") + " service soon.");
        } else {
            statusHeading.setText("Looking fine!");
            statusSub.setText("All equipments are up to date.");
        }

        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        for (Store.Item it : items) {
            View view = inf.inflate(R.layout.item_entry, maintContainer, false);
            TextView title = view.findViewById(R.id.itemName);
            TextView sub = view.findViewById(R.id.itemValue);
            TextView amt = view.findViewById(R.id.itemAmount);
            TextView badge = view.findViewById(R.id.itemBadge);
            TextView icon = view.findViewById(R.id.itemIcon);
            View stripe = view.findViewById(R.id.stripe);
            CheckBox check = view.findViewById(R.id.itemCheck);

            title.setText(it.title);
            title.setAlpha(it.done ? 0.5f : 1f);
            icon.setText("🛠️");
            Ui.stripe(stripe, it);

            if (it.amount > 0) {
                amt.setVisibility(View.VISIBLE);
                amt.setText(Store.money(it.amount));
            } else {
                amt.setVisibility(View.GONE);
            }

            StringBuilder sb = new StringBuilder();
            if (!it.extra.isEmpty()) sb.append("Product: ").append(it.extra);
            if (it.date != 0) {
                if (sb.length() > 0) sb.append("  •  ");
                sb.append("Next: ").append(Store.dateStr(it.date));
            }
            if (it.date2 != 0) {
                if (sb.length() > 0) sb.append("  •  ");
                sb.append("Last: ").append(Store.dateStr(it.date2));
            }
            if (!it.notes.isEmpty()) {
                if (sb.length() > 0) sb.append("  •  ");
                sb.append(it.notes);
            }
            sub.setText(sb.toString());
            sub.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);

            badge.setText(Store.badge(it));
            if (it.done) {
                badge.setText("Completed");
                badge.setBackgroundColor(0xFFDDF5E8);
                badge.setTextColor(0xFF1F8A5B);
            }

            check.setVisibility(View.VISIBLE);
            check.setChecked(it.done);
            check.setOnCheckedChangeListener((b, checked) -> {
                it.done = checked;
                Store.save(this, all);
                Reminders.schedule(this, it);
                refresh();
                Snackbar.make(maintContainer, "Service marked as " + (checked ? "Completed" : "Pending"), Snackbar.LENGTH_SHORT).show();
            });

            view.setOnClickListener(v -> showAddDialog(it));
            maintContainer.addView(view);
        }
    }

    void pickDate(String title, final CategoryActivity.LongCb cb) {
        MaterialDatePicker<Long> p = MaterialDatePicker.Builder.datePicker().setTitleText(title).build();
        p.addOnPositiveButtonClickListener(cb::on);
        p.show(getSupportFragmentManager(), "date");
    }

    void showAddDialog(final Store.Item editing) {
        View v = getLayoutInflater().inflate(R.layout.dialog_item, null);
        TextInputLayout titleL = v.findViewById(R.id.titleLayout);
        TextInputLayout extraL = v.findViewById(R.id.extraLayout);
        TextInputLayout amountL = v.findViewById(R.id.amountLayout);
        TextInputLayout dateL = v.findViewById(R.id.dateLayout);
        TextInputLayout date2L = v.findViewById(R.id.date2Layout);
        TextInputLayout timeL = v.findViewById(R.id.timeLayout);
        TextInputLayout notesL = v.findViewById(R.id.notesLayout);

        final MaterialAutoCompleteTextView titleE = v.findViewById(R.id.titleInput);
        final MaterialAutoCompleteTextView extraE = v.findViewById(R.id.extraInput);
        final MaterialAutoCompleteTextView notesE = v.findViewById(R.id.notesInput);
        final TextInputEditText amountE = v.findViewById(R.id.amountInput);
        final TextInputEditText dateE = v.findViewById(R.id.dateInput);
        final TextInputEditText date2E = v.findViewById(R.id.date2Input);
        View timeContainer = timeL;

        titleL.setHint("Service Name (e.g. AC Servicing)");
        extraL.setHint("Product Name / Equipment");
        amountL.setHint("Service Cost (₹)");
        dateL.setHint("Next Service Date");
        date2L.setHint("Last Service Date");
        notesL.setHint("Service Notes");

        extraL.setVisibility(View.VISIBLE);
        amountL.setVisibility(View.VISIBLE);
        date2L.setVisibility(View.VISIBLE);
        timeContainer.setVisibility(View.GONE);
        amountE.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        final long[] chosenNext = {editing != null ? editing.date : Store.todayUtc() + 86400000L * 30};
        final long[] chosenLast = {editing != null ? editing.date2 : Store.todayUtc()};

        if (editing != null) {
            titleE.setText(editing.title, false);
            extraE.setText(editing.extra, false);
            notesE.setText(editing.notes, false);
            if (editing.amount > 0) amountE.setText(String.valueOf(editing.amount));
            if (editing.date != 0) dateE.setText(Store.dateStr(editing.date));
            if (editing.date2 != 0) date2E.setText(Store.dateStr(editing.date2));
        } else {
            dateE.setText(Store.dateStr(chosenNext[0]));
            date2E.setText(Store.dateStr(chosenLast[0]));
        }

        String[] suggestions = {"AC Servicing", "Refrigerator Repair", "Washing Machine Service", "Water Purifier Service", "Bike Servicing", "Car Servicing", "Plumbing", "Electrical Repair"};
        titleE.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions));

        dateE.setOnClickListener(x -> pickDate("Next Service Date", sel -> {
            chosenNext[0] = sel;
            dateE.setText(Store.dateStr(sel));
        }));

        date2E.setOnClickListener(x -> pickDate("Last Service Date", sel -> {
            chosenLast[0] = sel;
            date2E.setText(Store.dateStr(sel));
        }));

        new MaterialAlertDialogBuilder(this)
                .setTitle(editing != null ? "Edit Maintenance Service" : "Add Maintenance Service")
                .setView(v)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    if (titleE.getText() == null) return;
                    String title = titleE.getText().toString().trim();
                    String product = extraE.getText() != null ? extraE.getText().toString().trim() : "";
                    String notes = notesE.getText() != null ? notesE.getText().toString().trim() : "";
                    if (title.isEmpty()) return;

                    Store.Item it = editing != null ? editing : new Store.Item();
                    if (editing == null) {
                        it.id = System.currentTimeMillis();
                        it.type = 2;
                    }
                    it.title = title;
                    it.extra = product;
                    it.notes = notes;
                    try {
                        it.amount = amountE.getText() != null && !amountE.getText().toString().isEmpty()
                                ? Double.parseDouble(amountE.getText().toString().trim()) : 0;
                    } catch (NumberFormatException e) {
                        it.amount = 0;
                    }
                    it.date = chosenNext[0];
                    it.date2 = chosenLast[0];

                    if (editing == null) all.add(it);
                    Store.save(this, all);
                    Reminders.schedule(this, it);
                    refresh();
                    Snackbar.make(maintContainer, "Maintenance service saved", Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }
}
