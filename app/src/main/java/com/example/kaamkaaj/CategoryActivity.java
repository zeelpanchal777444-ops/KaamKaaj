package com.example.kaamkaaj;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@SuppressLint({"SetTextI18n", "NotifyDataSetChanged", "CalendarMonth", "LocalVariableCouldBeFinal", "ResourceType"})
public class CategoryActivity extends AppCompatActivity {
    interface LongCb {
        void on(long v);
    }

    static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    static final int[] PALETTE = {0xFF5B5FEF, 0xFF22B8F0, 0xFFFF6B6B, 0xFFFFB800, 0xFF2E9E6B, 0xFF9B6BF0};

    int type;
    int monthOffset = 0;   // expenses only: 0 = this month, -1 = last month ...
    List<Store.Item> all = new ArrayList<>();
    final List<Store.Item> list = new ArrayList<>();
    Adapter adapter;
    TextView summary, emptyText, hintText, monthLabel, warrantyText;
    View monthRow, warrantyBox;
    DonutView donut;
    LinearLayout legend;
    LinearProgressIndicator warrantyProgress;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        type = getIntent().getIntExtra("type", 0);
        if (type < 0 || type >= Store.NAMES.length) type = 0;

        Ui.pad(findViewById(R.id.header), true, false);
        int navId = type == 0 ? R.id.nav_bills : type == 1 ? R.id.nav_expenses : type == 4 ? R.id.nav_tasks : R.id.nav_home;
        Ui.bottomNav(this, navId);

        ((TextView) findViewById(R.id.title)).setText(Store.EMOJI[type] + "  " + Store.NAMES[type]);
        summary = findViewById(R.id.summary);
        emptyText = findViewById(R.id.emptyText);
        hintText = findViewById(R.id.hintText);
        monthRow = findViewById(R.id.monthRow);
        monthLabel = findViewById(R.id.monthLabel);
        donut = findViewById(R.id.donut);
        legend = findViewById(R.id.legend);
        warrantyBox = findViewById(R.id.warrantyBox);
        warrantyText = findViewById(R.id.warrantyText);
        warrantyProgress = findViewById(R.id.warrantyProgress);
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        int chart = type == 1 ? View.VISIBLE : View.GONE;
        monthRow.setVisibility(chart);
        donut.setVisibility(chart);
        legend.setVisibility(chart);
        warrantyBox.setVisibility(View.GONE);
        findViewById(R.id.prevMonth).setOnClickListener(v -> {
            monthOffset--;
            refresh();
        });
        findViewById(R.id.nextMonth).setOnClickListener(v -> {
            if (monthOffset < 0) {
                monthOffset++;
                refresh();
            }
        });

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        rv.setAdapter(adapter);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder a, @NonNull RecyclerView.ViewHolder b) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                if (pos >= 0 && pos < list.size()) delete(list.get(pos));
                else adapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(rv);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAdd(null));
        refresh();
    }

    static boolean sameMonth(long date, int y, int m) {
        Calendar u = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        u.setTimeInMillis(date);
        return u.get(Calendar.YEAR) == y && u.get(Calendar.MONTH) == m;
    }

    void refresh() {
        all = Store.load(this);
        Calendar t = Calendar.getInstance();
        t.add(Calendar.MONTH, monthOffset);
        final int ty = t.get(Calendar.YEAR), tm = t.get(Calendar.MONTH);

        list.clear();
        double total = 0, open = 0;
        int pending = 0;
        for (Store.Item it : all) {
            if (it.type != type) continue;
            if (type == 1 && !sameMonth(it.date, ty, tm)) continue;
            list.add(it);
            total += it.amount;
            if (!it.done) {
                open += it.amount;
                pending++;
            }
        }
        Collections.sort(list, (a, b) -> {
            if (a.done != b.done) return a.done ? 1 : -1;
            if (type == 1) return Long.compare(b.date, a.date);
            long x = a.date == 0 ? Long.MAX_VALUE : a.date;
            long y = b.date == 0 ? Long.MAX_VALUE : b.date;
            return Long.compare(x, y);
        });
        adapter.notifyDataSetChanged();
        emptyText.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        hintText.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);

        String s;
        if (type == 0) s = "Pending " + Store.money(open) + "  •  " + pending + " bills";
        else if (type == 1) s = "Total " + Store.money(total) + "  •  " + list.size() + (list.size() == 1 ? " expense" : " expenses");
        else if (type == 2) s = pending + " upcoming" + (total > 0 ? "  •  Spent " + Store.money(total) : "");
        else if (type == 3) s = list.size() + (list.size() == 1 ? " product" : " products");
        else if (type == 4) s = pending + " pending  •  " + (list.size() - pending) + " done";
        else if (type == 5) s = list.size() + (list.size() == 1 ? " member" : " members");
        else s = pending + " upcoming reminders";

        if (type == 1) {
            monthLabel.setText(MONTHS[tm] + " " + ty);
            showChart(total);
        }
        if (type == 3) s = showWarranty();
        summary.setText(s);
    }

    /** Donut chart + legend for the selected month (top 5 categories, rest grouped as Other). */
    void showChart(double total) {
        Map<String, Double> cats = new HashMap<>();
        for (Store.Item it : list) {
            String k = it.title.trim().toLowerCase(Locale.ROOT);
            Double old = cats.get(k);
            cats.put(k, (old == null ? 0 : old) + it.amount);
        }
        List<Map.Entry<String, Double>> es = new ArrayList<>(cats.entrySet());
        Collections.sort(es, (a, b) -> Double.compare(b.getValue(), a.getValue()));

        int n = Math.min(5, es.size());
        boolean other = es.size() > 5;
        float[] vals = new float[n + (other ? 1 : 0)];
        String[] names = new String[vals.length];
        double rest = 0;
        for (int i = 0; i < es.size(); i++) {
            if (i < n) {
                String nm = es.get(i).getKey();
                names[i] = nm.isEmpty() ? "-" : nm.substring(0, 1).toUpperCase() + nm.substring(1);
                vals[i] = es.get(i).getValue().floatValue();
            } else {
                rest += es.get(i).getValue();
            }
        }
        if (other) {
            names[n] = "Other";
            vals[n] = (float) rest;
        }
        donut.setData(vals, PALETTE, Store.money(total));

        legend.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);
        for (int i = 0; i < vals.length; i++) {
            View row = inf.inflate(R.layout.item_legend, legend, false);
            row.findViewById(R.id.lDot).setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(PALETTE[i % PALETTE.length]));
            ((TextView) row.findViewById(R.id.lName)).setText(names[i]);
            ((TextView) row.findViewById(R.id.lAmt)).setText(Store.money(vals[i]));
            int pct = total > 0 ? (int) Math.round(100 * vals[i] / total) : 0;
            ((TextView) row.findViewById(R.id.lPct)).setText(pct + "%");
            legend.addView(row);
        }
    }

    /** "Next to expire" box with a progress bar; returns the summary line. */
    String showWarranty() {
        Store.Item next = null;
        int soonCount = 0, expired = 0;
        for (Store.Item it : list) {
            if (it.date == 0) continue;
            long d = Store.daysLeft(it.date);
            if (d < 0) {
                expired++;
            } else {
                if (d <= 60) soonCount++;
                if (next == null || it.date < next.date) next = it;
            }
        }
        if (next != null) {
            warrantyBox.setVisibility(View.VISIBLE);
            warrantyText.setText("Next to expire: " + next.title + "  •  " + Store.daysLeft(next.date) + " days left");
            if (next.date2 != 0 && next.date > next.date2) {
                long total = next.date - next.date2;
                long used = Store.todayUtc() - next.date2;
                warrantyProgress.setProgress((int) Math.max(0, Math.min(100, 100 * used / total)));
                warrantyProgress.setVisibility(View.VISIBLE);
            } else {
                warrantyProgress.setVisibility(View.GONE);
            }
        } else {
            warrantyBox.setVisibility(View.GONE);
        }
        return list.size() + (list.size() == 1 ? " product" : " products")
                + "  •  " + soonCount + " expiring soon  •  " + expired + " expired";
    }

    void pickDate(String title, final LongCb cb) {
        MaterialDatePicker<Long> p = MaterialDatePicker.Builder.datePicker().setTitleText(title).build();
        p.addOnPositiveButtonClickListener(cb::on);
        p.show(getSupportFragmentManager(), "date");
    }

    void showAdd(final Store.Item editing) {
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
        final TextInputEditText timeE = v.findViewById(R.id.timeInput);

        final boolean hasExtra = !Store.EXTRA_HINT[type].isEmpty();
        final boolean hasDate2 = !Store.DATE2_LABEL[type].isEmpty();
        titleL.setHint(Store.TITLE_HINT[type]);
        extraL.setHint(Store.EXTRA_HINT[type]);
        amountL.setHint(Store.AMOUNT_HINT[type]);
        dateL.setHint(Store.DATE_LABEL[type]);
        date2L.setHint(Store.DATE2_LABEL[type]);
        timeL.setHint("Time");
        notesL.setHint(Store.NOTE_HINT[type]);
        extraL.setVisibility(hasExtra ? View.VISIBLE : View.GONE);
        amountL.setVisibility(Store.AMOUNT[type] ? View.VISIBLE : View.GONE);
        date2L.setVisibility(hasDate2 ? View.VISIBLE : View.GONE);
        timeL.setVisibility(Store.TIME[type] ? View.VISIBLE : View.GONE);
        amountE.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        final long[] chosen = {editing != null ? editing.date : 0};
        final long[] chosen2 = {editing != null ? editing.date2 : 0};
        final int[] mins = {editing != null ? editing.mins : -1};

        // prefill when editing
        if (editing != null) {
            titleE.setText(editing.title, false);
            notesE.setText(editing.notes, false);
            if (editing.amount > 0) {
                amountE.setText(editing.amount == Math.floor(editing.amount)
                        ? String.valueOf((long) editing.amount) : String.valueOf(editing.amount));
            }
            if (editing.date != 0) dateE.setText(Store.dateStr(editing.date));
            if (editing.date2 != 0) date2E.setText(Store.dateStr(editing.date2));
            if (editing.mins >= 0) timeE.setText(Store.timeStr(editing.mins));
        }

        // repeat dropdown for bills, free text for product / brand
        if (type == 0) {
            extraE.setInputType(InputType.TYPE_NULL);
            extraE.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Store.REPEAT));
            extraE.setText(editing != null && !editing.extra.isEmpty() ? editing.extra : "None", false);
        } else if (hasExtra) {
            extraL.setEndIconMode(TextInputLayout.END_ICON_NONE);
            if (editing != null) extraE.setText(editing.extra, false);
        }

        titleE.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Store.SUGGEST[type]));

        // tasks: assign to a family member
        if (type == 4) {
            List<String> names = new ArrayList<>();
            names.add("Me");
            for (Store.Item it : all) if (it.type == 5) names.add(it.title);
            notesE.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names));
            notesE.setOnFocusChangeListener((x, focus) -> {
                if (focus) notesE.showDropDown();
            });
        }

        dateE.setOnClickListener(x -> pickDate(Store.DATE_LABEL[type], sel -> {
            chosen[0] = sel;
            dateE.setText(Store.dateStr(sel));
        }));
        date2E.setOnClickListener(x -> pickDate(Store.DATE2_LABEL[type], sel -> {
            chosen2[0] = sel;
            date2E.setText(Store.dateStr(sel));
        }));
        timeE.setOnClickListener(x -> {
            int m = mins[0] >= 0 ? mins[0] : 540;
            final MaterialTimePicker tp = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(m / 60)
                    .setMinute(m % 60)
                    .setTitleText("Reminder time")
                    .build();
            tp.addOnPositiveButtonClickListener(y -> {
                mins[0] = tp.getHour() * 60 + tp.getMinute();
                timeE.setText(Store.timeStr(mins[0]));
            });
            tp.show(getSupportFragmentManager(), "time");
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle((editing != null ? "Edit " : "Add to ") + Store.NAMES[type])
                .setView(v)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    String title = String.valueOf(titleE.getText()).trim();
                    if (title.isEmpty()) return;
                    Store.Item it = editing != null ? editing : new Store.Item();
                    if (editing == null) {
                        it.id = System.currentTimeMillis();
                        it.type = type;
                    }
                    it.title = title;
                    it.notes = String.valueOf(notesE.getText()).trim();
                    it.extra = hasExtra ? String.valueOf(extraE.getText()).trim() : "";
                    try {
                        it.amount = Double.parseDouble(String.valueOf(amountE.getText()).trim());
                    } catch (NumberFormatException e) {
                        it.amount = 0;
                    }
                    it.date = chosen[0];
                    it.date2 = chosen2[0];
                    it.mins = mins[0];
                    if ((type == 1 || type == 6) && it.date == 0) it.date = Store.todayUtc();
                    if (editing == null) all.add(it);
                    Store.save(this, all);
                    Reminders.schedule(this, it);
                    refresh();
                })
                .show();
    }

    /** When a repeating bill is marked paid, create the next one automatically. */
    void spawnNext(Store.Item it) {
        if (it.type != 0 || it.date == 0 || it.extra == null) return;
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(it.date);
        switch (it.extra) {
            case "Weekly":
                c.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case "Monthly":
                c.add(Calendar.MONTH, 1);
                break;
            case "Yearly":
                c.add(Calendar.YEAR, 1);
                break;
            default:
                return;
        }
        long nd = c.getTimeInMillis();
        for (Store.Item o : all) {
            if (o.type == 0 && o.title.equals(it.title) && o.date == nd) return;
        }
        Store.Item n = new Store.Item();
        n.id = System.currentTimeMillis();
        n.type = 0;
        n.title = it.title;
        n.amount = it.amount;
        n.notes = it.notes;
        n.extra = it.extra;
        n.date = nd;
        all.add(n);
        Reminders.schedule(this, n);
    }

    void delete(final Store.Item it) {
        all.remove(it);
        Store.save(this, all);
        Reminders.cancel(this, it);
        refresh();
        Snackbar.make(findViewById(R.id.root), "Deleted “" + it.title + "”", Snackbar.LENGTH_LONG)
                .setAnchorView(fab)
                .setAction("Undo", x -> {
                    all.add(it);
                    Store.save(this, all);
                    Reminders.schedule(this, it);
                    refresh();
                }).show();
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            final Store.Item it = list.get(position);
            h.title.setText(it.title);
            h.title.setAlpha(it.done ? 0.5f : 1f);
            Ui.icon(h.icon, it);
            Ui.stripe(h.stripe, it);
            Ui.badge(h.badge, it);

            if (Store.AMOUNT[type] && it.amount > 0) {
                h.amount.setVisibility(View.VISIBLE);
                h.amount.setText(Store.money(it.amount));
            } else {
                h.amount.setVisibility(View.GONE);
            }

            StringBuilder sb = new StringBuilder();
            if ((type == 2 || type == 3) && !it.extra.isEmpty()) add(sb, it.extra);
            if (type == 0 && !it.extra.isEmpty() && !it.extra.equals("None")) add(sb, "🔁 " + it.extra);
            if (it.date != 0) {
                String d = Store.dateStr(it.date);
                if (type == 6 && it.mins >= 0) d += "  •  " + Store.timeStr(it.mins);
                add(sb, d);
            }
            if (type == 2 && it.date2 != 0) add(sb, "Last: " + Store.dateStr(it.date2));
            if (type == 3 && it.date2 != 0) add(sb, "Bought " + Store.dateStr(it.date2));
            if (!it.notes.isEmpty()) add(sb, (type == 4 ? "For " : "") + it.notes);
            h.sub.setText(sb.toString());
            h.sub.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);

            h.check.setOnCheckedChangeListener(null);
            h.check.setVisibility(Store.CHECK[type] ? View.VISIBLE : View.GONE);
            h.check.setChecked(it.done);
            h.check.setOnCheckedChangeListener((b, checked) -> {
                it.done = checked;
                if (checked) spawnNext(it);
                Store.save(CategoryActivity.this, all);
                Reminders.schedule(CategoryActivity.this, it);
                h.itemView.post(CategoryActivity.this::refresh);
            });
            h.itemView.setOnClickListener(x -> showAdd(it));
        }

        void add(StringBuilder sb, String part) {
            if (sb.length() > 0) sb.append("  •  ");
            sb.append(part);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView title, sub, amount, badge, icon;
            View stripe;
            CheckBox check;

            VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.itemName);
                sub = v.findViewById(R.id.itemValue);
                amount = v.findViewById(R.id.itemAmount);
                badge = v.findViewById(R.id.itemBadge);
                icon = v.findViewById(R.id.itemIcon);
                stripe = v.findViewById(R.id.stripe);
                check = v.findViewById(R.id.itemCheck);
            }
        }
    }
}
