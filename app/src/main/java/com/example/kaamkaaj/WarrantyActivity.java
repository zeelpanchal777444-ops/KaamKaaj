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

public class WarrantyActivity extends AppCompatActivity {

    TextView totalCount, soonCount, expiredCount, userName, soonTitle, soonSubtitle, emptyText;
    LinearLayout warrantyContainer;
    List<Store.Item> all = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warranty);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        String name = Store.userName(this);
        userName.setText(name.isEmpty() ? "Suman!" : name + "!");
        ((TextView) findViewById(R.id.profileAvatar)).setText(name.isEmpty() ? "S" : name.substring(0, 1).toUpperCase());

        totalCount = findViewById(R.id.totalCount);
        soonCount = findViewById(R.id.soonCount);
        expiredCount = findViewById(R.id.expiredCount);
        soonTitle = findViewById(R.id.soonTitle);
        soonSubtitle = findViewById(R.id.soonSubtitle);
        emptyText = findViewById(R.id.emptyText);
        warrantyContainer = findViewById(R.id.warrantyContainer);

        findViewById(R.id.addWarrantyBtn).setOnClickListener(v -> showAddDialog(null));

        seedDefaultsIfNeeded();
        refresh();
    }

    void seedDefaultsIfNeeded() {
        all = Store.load(this);
        boolean hasWarranty = false;
        for (Store.Item it : all) {
            if (it.type == 3) hasWarranty = true;
        }

        if (!hasWarranty) {
            long today = Store.todayUtc();
            String[][] defaults = {
                    {"Samsung TV", "Samsung", String.valueOf(today - 86400000L * 300), String.valueOf(today + 86400000L * 30), "2 Years Warranty • Bill & Card attached", "false"},
                    {"LG Refrigerator", "LG", String.valueOf(today - 86400000L * 200), String.valueOf(today + 86400000L * 165), "1 Year Warranty • Receipt saved", "false"},
                    {"Washing Machine", "Whirlpool", String.valueOf(today - 86400000L * 100), String.valueOf(today + 86400000L * 990), "3 Years Warranty • Invoice available", "false"},
                    {"Air Conditioner", "Daikin", String.valueOf(today - 86400000L * 400), String.valueOf(today - 86400000L * 10), "5 Years Warranty • Expired recently", "true"}
            };
            for (String[] d : defaults) {
                Store.Item it = new Store.Item();
                it.id = System.currentTimeMillis() + (long)(Math.random() * 1000);
                it.type = 3;
                it.title = d[0];
                it.extra = d[1];
                it.date2 = Long.parseLong(d[2]); // Purchase date
                it.date = Long.parseLong(d[3]);   // Expiry date
                it.notes = d[4];
                it.done = Boolean.parseBoolean(d[5]);
                all.add(it);
            }
            Store.save(this, all);
        }
    }

    void refresh() {
        all = Store.load(this);
        warrantyContainer.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);

        List<Store.Item> items = new ArrayList<>();
        int total = 0, soon = 0, expired = 0;
        Store.Item soonest = null;

        for (Store.Item it : all) {
            if (it.type == 3) {
                items.add(it);
                total++;
                long d = Store.daysLeft(it.date);
                if (d < 0) {
                    expired++;
                } else if (d <= 60) {
                    soon++;
                    if (soonest == null || it.date < soonest.date) soonest = it;
                }
            }
        }

        totalCount.setText(String.valueOf(total));
        soonCount.setText(String.valueOf(soon));
        expiredCount.setText(String.valueOf(expired));

        if (soonest != null) {
            soonTitle.setText(soonest.title + " (" + soonest.extra + ")");
            long d = Store.daysLeft(soonest.date);
            soonSubtitle.setText(d + " days left  •  Expiring soon");
        } else {
            soonTitle.setText("No products expiring soon");
            soonSubtitle.setText("All warranties are active");
        }

        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        for (Store.Item it : items) {
            View view = inf.inflate(R.layout.item_entry, warrantyContainer, false);
            TextView title = view.findViewById(R.id.itemName);
            TextView sub = view.findViewById(R.id.itemValue);
            TextView amt = view.findViewById(R.id.itemAmount);
            TextView badge = view.findViewById(R.id.itemBadge);
            TextView icon = view.findViewById(R.id.itemIcon);
            View stripe = view.findViewById(R.id.stripe);
            CheckBox check = view.findViewById(R.id.itemCheck);

            title.setText(it.title);
            icon.setText("🏷️");
            Ui.stripe(stripe, it);
            amt.setVisibility(View.GONE);

            StringBuilder sb = new StringBuilder();
            if (!it.extra.isEmpty()) sb.append("Brand: ").append(it.extra);
            if (it.date != 0) {
                if (sb.length() > 0) sb.append("  •  ");
                sb.append("Expires: ").append(Store.dateStr(it.date));
            }
            if (it.date2 != 0) {
                if (sb.length() > 0) sb.append("  •  ");
                sb.append("Bought: ").append(Store.dateStr(it.date2));
            }
            if (!it.notes.isEmpty()) {
                if (sb.length() > 0) sb.append("  •  ");
                sb.append(it.notes);
            }
            sub.setText(sb.toString());
            sub.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);

            long d = Store.daysLeft(it.date);
            if (d < 0) {
                badge.setText("Expired");
                badge.setBackgroundColor(0xFFFFE3E3);
                badge.setTextColor(0xFFD93B40);
            } else if (d <= 60) {
                badge.setText(d + " days left");
                badge.setBackgroundColor(0xFFFFF1CC);
                badge.setTextColor(0xFFB87800);
            } else {
                badge.setText("Active");
                badge.setBackgroundColor(0xFFDDF5E8);
                badge.setTextColor(0xFF1F8A5B);
            }

            check.setVisibility(View.GONE);
            view.setOnClickListener(v -> showAddDialog(it));
            warrantyContainer.addView(view);
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
        final TextInputEditText dateE = v.findViewById(R.id.dateInput);
        final TextInputEditText date2E = v.findViewById(R.id.date2Input);

        titleL.setHint("Product Name (e.g. Samsung TV)");
        extraL.setHint("Brand Name (e.g. Samsung)");
        dateL.setHint("Warranty Expiry Date");
        date2L.setHint("Purchase Date");
        notesL.setHint("Warranty Duration & Notes / Bill info");

        amountL.setVisibility(View.GONE);
        timeL.setVisibility(View.GONE);
        extraL.setVisibility(View.VISIBLE);
        date2L.setVisibility(View.VISIBLE);

        final long[] chosenExpiry = {editing != null ? editing.date : Store.todayUtc() + 86400000L * 365};
        final long[] chosenPurchase = {editing != null ? editing.date2 : Store.todayUtc()};

        if (editing != null) {
            titleE.setText(editing.title, false);
            extraE.setText(editing.extra, false);
            notesE.setText(editing.notes, false);
            if (editing.date != 0) dateE.setText(Store.dateStr(editing.date));
            if (editing.date2 != 0) date2E.setText(Store.dateStr(editing.date2));
        } else {
            dateE.setText(Store.dateStr(chosenExpiry[0]));
            date2E.setText(Store.dateStr(chosenPurchase[0]));
        }

        String[] suggestions = {"Samsung TV", "LG Refrigerator", "Washing Machine", "Air Conditioner", "Laptop", "Microwave Oven", "Smart Phone"};
        titleE.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions));

        dateE.setOnClickListener(x -> pickDate("Warranty Expiry Date", sel -> {
            chosenExpiry[0] = sel;
            dateE.setText(Store.dateStr(sel));
        }));

        date2E.setOnClickListener(x -> pickDate("Purchase Date", sel -> {
            chosenPurchase[0] = sel;
            date2E.setText(Store.dateStr(sel));
        }));

        new MaterialAlertDialogBuilder(this)
                .setTitle(editing != null ? "Edit Warranty & Document" : "Add Warranty Product")
                .setView(v)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    if (titleE.getText() == null) return;
                    String title = titleE.getText().toString().trim();
                    String brand = extraE.getText() != null ? extraE.getText().toString().trim() : "";
                    String notes = notesE.getText() != null ? notesE.getText().toString().trim() : "";
                    if (title.isEmpty()) return;

                    Store.Item it = editing != null ? editing : new Store.Item();
                    if (editing == null) {
                        it.id = System.currentTimeMillis();
                        it.type = 3;
                    }
                    it.title = title;
                    it.extra = brand;
                    it.notes = notes.isEmpty() ? "1 Year Warranty • Bill attached" : notes;
                    it.date = chosenExpiry[0];
                    it.date2 = chosenPurchase[0];

                    if (editing == null) all.add(it);
                    Store.save(this, all);
                    Reminders.schedule(this, it);
                    refresh();
                    Snackbar.make(warrantyContainer, "Warranty product saved successfully", Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }
}
