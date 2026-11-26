package com.example.kaamkaaj;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class FamilyChoresActivity extends AppCompatActivity {

    LinearLayout membersRow, choresContainer;
    TextView emptyText;
    List<Store.Item> all = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_chores);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        String userName = Store.userName(this);
        ((TextView) findViewById(R.id.greetingTitle)).setText("Hi, " + (userName.isEmpty() ? "Sara" : userName));
        ((TextView) findViewById(R.id.userAvatar)).setText(userName.isEmpty() ? "S" : userName.substring(0, 1).toUpperCase());

        membersRow = findViewById(R.id.membersRow);
        choresContainer = findViewById(R.id.choresContainer);
        emptyText = findViewById(R.id.emptyText);

        findViewById(R.id.addMemberBtn).setOnClickListener(v -> showAddMemberDialog());
        findViewById(R.id.addChoreBtn).setOnClickListener(v -> showAddChoreDialog(null));
        findViewById(R.id.assignChoreBottomBtn).setOnClickListener(v -> showAddChoreDialog(null));

        seedDefaultsIfNeeded();
        refresh();
    }

    void seedDefaultsIfNeeded() {
        all = Store.load(this);
        boolean hasMembers = false;
        boolean hasChores = false;
        for (Store.Item it : all) {
            if (it.type == 5) hasMembers = true;
            if (it.type == 4) hasChores = true;
        }

        if (!hasMembers) {
            String[] defaultMembers = {"Father", "Mother", "You", "Lily"};
            for (String m : defaultMembers) {
                Store.Item member = new Store.Item();
                member.id = System.currentTimeMillis() + (long)(Math.random() * 1000);
                member.type = 5;
                member.title = m;
                member.notes = m.equals("You") ? "Self" : "Family";
                all.add(member);
            }
        }

        if (!hasChores) {
            String[][] defaultChores = {
                    {"Electricity Bill", "Father", "25 Points", "false"},
                    {"Grocery Shopping", "Mother", "30 Points", "true"},
                    {"Water Can Booking", "You", "15 Points", "false"},
                    {"Cleaning", "Lily", "40 Points", "false"}
            };
            for (String[] c : defaultChores) {
                Store.Item chore = new Store.Item();
                chore.id = System.currentTimeMillis() + (long)(Math.random() * 1000);
                chore.type = 4;
                chore.title = c[0];
                chore.extra = c[1]; // Assignee
                chore.notes = c[2]; // Points/Note
                chore.done = Boolean.parseBoolean(c[3]);
                all.add(chore);
            }
        }
        Store.save(this, all);
    }

    void refresh() {
        all = Store.load(this);

        // Render Members
        membersRow.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);
        List<Store.Item> members = new ArrayList<>();
        for (Store.Item it : all) {
            if (it.type == 5) members.add(it);
        }

        for (Store.Item m : members) {
            View memberView = inf.inflate(R.layout.item_quick, membersRow, false);
            TextView ic = memberView.findViewById(R.id.qIcon);
            ic.setText(m.title.isEmpty() ? "M" : m.title.substring(0, 1).toUpperCase());
            ((TextView) memberView.findViewById(R.id.qName)).setText(m.title);
            TextView count = memberView.findViewById(R.id.qCount);
            count.setVisibility(View.GONE);
            memberView.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                    .setTitle("Member: " + m.title)
                    .setMessage("Role: " + (m.notes.isEmpty() ? "Family Member" : m.notes))
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Delete", (d, w) -> {
                        all.remove(m);
                        Store.save(this, all);
                        refresh();
                    })
                    .show());
            membersRow.addView(memberView);
        }

        // Render Chores
        choresContainer.removeAllViews();
        List<Store.Item> chores = new ArrayList<>();
        for (Store.Item it : all) {
            if (it.type == 4) chores.add(it);
        }

        emptyText.setVisibility(chores.isEmpty() ? View.VISIBLE : View.GONE);

        for (Store.Item chore : chores) {
            View choreView = inf.inflate(R.layout.item_entry, choresContainer, false);
            TextView itemName = choreView.findViewById(R.id.itemName);
            TextView itemValue = choreView.findViewById(R.id.itemValue);
            TextView itemBadge = choreView.findViewById(R.id.itemBadge);
            TextView itemIcon = choreView.findViewById(R.id.itemIcon);
            View stripe = choreView.findViewById(R.id.stripe);
            CheckBox check = choreView.findViewById(R.id.itemCheck);

            itemName.setText(chore.title);
            itemName.setAlpha(chore.done ? 0.5f : 1f);
            itemIcon.setText("🧹");
            stripe.setBackgroundColor(chore.done ? 0xFF1F8A5B : 0xFF2E5A54);

            StringBuilder sb = new StringBuilder();
            if (!chore.extra.isEmpty()) sb.append("Assigned to: ").append(chore.extra);
            if (!chore.notes.isEmpty()) {
                if (sb.length() > 0) sb.append("  •  ");
                sb.append(chore.notes);
            }
            itemValue.setText(sb.toString());
            itemValue.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);

            itemBadge.setText(chore.done ? "Completed" : "Pending");
            itemBadge.setBackgroundColor(chore.done ? 0xFFDDF5E8 : 0xFFFFF1CC);
            itemBadge.setTextColor(chore.done ? 0xFF1F8A5B : 0xFFB87800);

            check.setVisibility(View.VISIBLE);
            check.setChecked(chore.done);
            check.setOnCheckedChangeListener((b, checked) -> {
                chore.done = checked;
                Store.save(this, all);
                refresh();
                Snackbar.make(choresContainer, "Task marked as " + (checked ? "Completed" : "Pending"), Snackbar.LENGTH_SHORT).show();
            });

            choreView.setOnClickListener(v -> showAddChoreDialog(chore));
            choresContainer.addView(choreView);
        }
    }

    void showAddMemberDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_two_fields, null);
        TextInputLayout nameL = v.findViewById(R.id.f1Layout);
        TextInputLayout relationL = v.findViewById(R.id.f2Layout);
        TextInputEditText nameE = v.findViewById(R.id.f1Input);
        TextInputEditText relationE = v.findViewById(R.id.f2Input);

        nameL.setHint("Member Name (e.g. Brother)");
        relationL.setHint("Relation / Role (e.g. Sibling)");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Family Member")
                .setView(v)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (d, w) -> {
                    if (nameE.getText() == null) return;
                    String name = nameE.getText().toString().trim();
                    String relation = relationE.getText() != null ? relationE.getText().toString().trim() : "";
                    if (name.isEmpty()) return;
                    Store.Item m = new Store.Item();
                    m.id = System.currentTimeMillis();
                    m.type = 5;
                    m.title = name;
                    m.notes = relation;
                    all.add(m);
                    Store.save(this, all);
                    refresh();
                    Snackbar.make(choresContainer, "Member added", Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }

    void showAddChoreDialog(final Store.Item editing) {
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

        titleL.setHint("Chore Title (e.g. Electricity Bill)");
        extraL.setHint("Assignee Member");
        notesL.setHint("Points / Notes (e.g. 25 Points)");

        amountL.setVisibility(View.GONE);
        dateL.setVisibility(View.GONE);
        date2L.setVisibility(View.GONE);
        timeL.setVisibility(View.GONE);
        extraL.setVisibility(View.VISIBLE);

        List<String> memberNames = new ArrayList<>();
        for (Store.Item it : all) {
            if (it.type == 5) memberNames.add(it.title);
        }
        if (memberNames.isEmpty()) memberNames.add("You");

        extraE.setInputType(android.text.InputType.TYPE_NULL);
        extraE.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, memberNames));

        String[] sampleChores = {"Electricity Bill", "Grocery Shopping", "Water Can Booking", "Kitchen Cleaning", "Medicine Purchase", "Cleaning"};
        titleE.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sampleChores));

        if (editing != null) {
            titleE.setText(editing.title, false);
            extraE.setText(editing.extra, false);
            notesE.setText(editing.notes, false);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(editing != null ? "Edit Chore" : "Assign New Chore")
                .setView(v)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    if (titleE.getText() == null) return;
                    String title = titleE.getText().toString().trim();
                    String assignee = extraE.getText() != null ? extraE.getText().toString().trim() : "";
                    String notes = notesE.getText() != null ? notesE.getText().toString().trim() : "";
                    if (title.isEmpty()) return;

                    Store.Item chore = editing != null ? editing : new Store.Item();
                    if (editing == null) {
                        chore.id = System.currentTimeMillis();
                        chore.type = 4;
                    }
                    chore.title = title;
                    chore.extra = assignee.isEmpty() ? "You" : assignee;
                    chore.notes = notes.isEmpty() ? "20 Points" : notes;

                    if (editing == null) all.add(chore);
                    Store.save(this, all);
                    refresh();
                    Snackbar.make(choresContainer, "Chore saved successfully", Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }
}
