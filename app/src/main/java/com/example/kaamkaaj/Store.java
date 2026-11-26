package com.example.kaamkaaj;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/** Local accounts + data storage + small helpers. */
final class Store {
    private Store() {}

    static class Item {
        long id;
        int type;
        String title = "";
        String extra = "";   // repeat (bills), product (maintenance), brand (warranty)
        String notes = "";
        double amount;
        long date;           // UTC midnight millis, 0 = none
        long date2;          // second date (last service / purchase date)
        int mins = -1;       // reminder time, minutes after midnight
        boolean done;
    }

    // 0 Bills, 1 Expenses, 2 Maintenance, 3 Warranty, 4 Tasks, 5 Family, 6 Reminders
    static final String[] NAMES = {"Bills & Payments", "Expenses", "Maintenance", "Warranty & Documents",
            "Family Tasks", "Family Members", "Reminders"};
    static final String[] EMOJI = {"\uD83D\uDCA1", "\uD83D\uDED2", "\uD83D\uDD27", "\uD83E\uDDFE",
            "\u2705", "\uD83D\uDC6A", "\u23F0"};
    static final boolean[] AMOUNT = {true, true, true, false, false, false, false};
    static final String[] AMOUNT_HINT = {"Amount (\u20B9)", "Amount (\u20B9)", "Service cost (\u20B9)", "", "", "", ""};
    static final boolean[] CHECK = {true, false, true, false, true, false, true};
    static final String[] TITLE_HINT = {"Bill name", "Category", "Service name", "Product name", "Task", "Name", "Reminder title"};
    static final String[] EXTRA_HINT = {"Repeat", "", "Product name", "Brand", "", "", ""};
    static final String[] DATE_LABEL = {"Due date", "Date", "Next service date", "Warranty expiry date", "Due date", "Birthday", "Date"};
    static final String[] DATE2_LABEL = {"", "", "Last service date", "Purchase date", "", "", ""};
    static final boolean[] TIME = {false, false, false, false, false, false, true};
    static final String[] NOTE_HINT = {"Notes", "Notes (optional)", "Service notes", "Notes / warranty duration",
            "Assigned to", "Relation (e.g. Mother)", "Message (optional)"};
    static final int[] TINT = {0xFFE8E9FF, 0xFFFFEBD9, 0xFFDCEEFF, 0xFFE2F6EC, 0xFFF0E6FF, 0xFFFFE4EE, 0xFFFFF3CC};
    static final String[] REPEAT = {"None", "Weekly", "Monthly", "Yearly"};
    static final String[][] SUGGEST = {
            {"Electricity bill", "Water bill", "Gas bill", "Wi-Fi bill", "Mobile recharge", "Rent", "DTH recharge"},
            {"Grocery", "Electricity", "Water", "Rent", "Internet", "Maintenance", "Shopping", "Other"},
            {"AC servicing", "Refrigerator repair", "Washing machine service", "Water purifier service",
                    "Bike servicing", "Car servicing", "Plumbing", "Electrical repair"},
            {"Samsung TV", "LG Refrigerator", "Washing machine", "AC", "Laptop", "Mobile phone"},
            {"Grocery shopping", "Cleaning", "Electricity bill payment", "Water can booking",
                    "Kitchen cleaning", "Medicine purchase"},
            {"Papa", "Mummy", "Bhai", "Behen", "Dadi", "Dada"},
            {"Pay electricity bill", "Wi-Fi recharge", "Doctor appointment", "Grocery shopping"}};

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("kaamkaaj_store", Context.MODE_PRIVATE);
    }

    // ---------- settings ----------
    static boolean notifEnabled(Context c) {
        return sp(c).getBoolean("notif", true);
    }

    static void setNotif(Context c, boolean on) {
        sp(c).edit().putBoolean("notif", on).apply();
    }

    /** 0 = follow system, 1 = light, 2 = dark */
    static int darkMode(Context c) {
        return sp(c).getInt("dark", 0);
    }

    static void setDark(Context c, int mode) {
        sp(c).edit().putInt("dark", mode).apply();
    }

    // ---------- accounts ----------
    private static String hash(String salt, String pw) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256").digest((salt + pw).getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** Returns an error message, or null on success. */
    static String signUp(Context c, String name, String email, String pw) {
        try {
            email = email.trim().toLowerCase(Locale.ROOT);
            JSONObject users = new JSONObject(sp(c).getString("users", "{}"));
            if (users.has(email)) return "An account with this email already exists";
            String salt = UUID.randomUUID().toString();
            JSONObject u = new JSONObject();
            u.put("name", name.trim());
            u.put("salt", salt);
            u.put("hash", hash(salt, pw));
            users.put(email, u);
            sp(c).edit().putString("users", users.toString()).putString("session", email).apply();
            return null;
        } catch (Exception e) {
            return "Something went wrong, please try again";
        }
    }

    static String login(Context c, String email, String pw) {
        try {
            email = email.trim().toLowerCase(Locale.ROOT);
            JSONObject users = new JSONObject(sp(c).getString("users", "{}"));
            if (!users.has(email)) return "No account found for this email";
            JSONObject u = users.getJSONObject(email);
            if (!u.getString("hash").equals(hash(u.getString("salt"), pw))) return "Wrong password";
            sp(c).edit().putString("session", email).apply();
            return null;
        } catch (Exception e) {
            return "Something went wrong, please try again";
        }
    }

    /** Demo-level reset: sets a new password for an existing account on this phone. */
    static String resetPassword(Context c, String email, String pw) {
        try {
            email = email.trim().toLowerCase(Locale.ROOT);
            if (pw.length() < 6) return "Password must be at least 6 characters";
            JSONObject users = new JSONObject(sp(c).getString("users", "{}"));
            if (!users.has(email)) return "No account found for this email";
            JSONObject u = users.getJSONObject(email);
            String salt = UUID.randomUUID().toString();
            u.put("salt", salt);
            u.put("hash", hash(salt, pw));
            sp(c).edit().putString("users", users.toString()).apply();
            return null;
        } catch (Exception e) {
            return "Something went wrong, please try again";
        }
    }

    static String changePassword(Context c, String oldPw, String newPw) {
        try {
            String email = session(c);
            if (email == null) return "Please log in again";
            if (newPw.length() < 6) return "New password must be at least 6 characters";
            JSONObject users = new JSONObject(sp(c).getString("users", "{}"));
            JSONObject u = users.getJSONObject(email);
            if (!u.getString("hash").equals(hash(u.getString("salt"), oldPw))) return "Current password is wrong";
            String salt = UUID.randomUUID().toString();
            u.put("salt", salt);
            u.put("hash", hash(salt, newPw));
            sp(c).edit().putString("users", users.toString()).apply();
            return null;
        } catch (Exception e) {
            return "Something went wrong, please try again";
        }
    }

    static String session(Context c) {
        return sp(c).getString("session", null);
    }

    static void logout(Context c) {
        sp(c).edit().remove("session").apply();
    }

    static String userName(Context c) {
        try {
            JSONObject users = new JSONObject(sp(c).getString("users", "{}"));
            return users.getJSONObject(session(c)).optString("name", "Your Name");
        } catch (Exception e) {
            return "Your Name";
        }
    }

    static String userPhone(Context c) {
        try {
            JSONObject users = new JSONObject(sp(c).getString("users", "{}"));
            return users.getJSONObject(session(c)).optString("phone", "0000-0000-0000");
        } catch (Exception e) {
            return "0000-0000-0000";
        }
    }

    static String userUsername(Context c) {
        try {
            JSONObject users = new JSONObject(sp(c).getString("users", "{}"));
            return users.getJSONObject(session(c)).optString("username", "@yourname");
        } catch (Exception e) {
            return "@yourname";
        }
    }

    static void updateProfile(Context c, String name, String phone, String username) {
        try {
            String email = session(c);
            if (email == null) return;
            JSONObject users = new JSONObject(sp(c).getString("users", "{}"));
            JSONObject u = users.getJSONObject(email);
            u.put("name", name.trim());
            u.put("phone", phone.trim());
            u.put("username", username.trim());
            users.put(email, u);
            sp(c).edit().putString("users", users.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    // ---------- items ----------
    static List<Item> load(Context c) {
        List<Item> list = new ArrayList<>();
        String s = session(c);
        if (s == null) return list;
        try {
            JSONArray a = new JSONArray(sp(c).getString("items_" + s, "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Item it = new Item();
                it.id = o.optLong("id");
                it.type = o.optInt("type");
                it.title = o.optString("title");
                it.extra = o.optString("extra");
                it.notes = o.optString("notes");
                it.amount = o.optDouble("amount", 0);
                it.date = o.optLong("date");
                it.date2 = o.optLong("date2");
                it.mins = o.optInt("mins", -1);
                it.done = o.optBoolean("done");
                if (it.type >= 0 && it.type < NAMES.length) list.add(it);
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    static void save(Context c, List<Item> list) {
        String s = session(c);
        if (s == null) return;
        JSONArray a = new JSONArray();
        for (Item it : list) {
            try {
                JSONObject o = new JSONObject();
                o.put("id", it.id);
                o.put("type", it.type);
                o.put("title", it.title);
                o.put("extra", it.extra);
                o.put("notes", it.notes);
                o.put("amount", it.amount);
                o.put("date", it.date);
                o.put("date2", it.date2);
                o.put("mins", it.mins);
                o.put("done", it.done);
                a.put(o);
            } catch (Exception ignored) {
            }
        }
        sp(c).edit().putString("items_" + s, a.toString()).apply();
    }

    static void clearData(Context c) {
        String s = session(c);
        if (s != null) sp(c).edit().remove("items_" + s).apply();
    }

    // ---------- formatting ----------
    static String money(double v) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("en-IN"));
        nf.setMaximumFractionDigits(2);
        return "\u20B9" + nf.format(v);
    }

    static String dateStr(long utcMillis) {
        SimpleDateFormat f = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(utcMillis);
    }

    static String timeStr(int mins) {
        int h = mins / 60, m = mins % 60;
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return h12 + ":" + String.format(Locale.ENGLISH, "%02d", m) + (h >= 12 ? " PM" : " AM");
    }

    static long todayUtc() {
        Calendar l = Calendar.getInstance();
        Calendar u = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        u.clear();
        u.set(l.get(Calendar.YEAR), l.get(Calendar.MONTH), l.get(Calendar.DAY_OF_MONTH));
        return u.getTimeInMillis();
    }

    static long daysLeft(long date) {
        return Math.round((date - todayUtc()) / 86400000.0);
    }

    static boolean hasDue(int type) {
        return type == 0 || type == 2 || type == 4 || type == 6;
    }

    static boolean overdue(Item it) {
        return it.date != 0 && !it.done && hasDue(it.type) && daysLeft(it.date) < 0;
    }

    static String dateInfo(int type, long date) {
        if (date == 0) return "";
        long d = daysLeft(date);
        if (type == 1 || type == 5) return dateStr(date);
        if (type == 3) {
            if (d < 0) return "Expired on " + dateStr(date);
            return "Expires " + dateStr(date) + (d == 0 ? " (today)" : " (" + d + " days left)");
        }
        String when;
        if (d < 0) when = "Overdue by " + (-d) + (d == -1 ? " day" : " days");
        else if (d == 0) when = "Due today";
        else if (d == 1) when = "Due tomorrow";
        else when = "Due in " + d + " days";
        return when + " \u2022 " + dateStr(date);
    }

    /** 0 = fine, 1 = due soon, 2 = overdue / expired */
    static int status(Item it) {
        if (it.done || it.date == 0 || it.type == 1 || it.type == 5) return 0;
        long d = daysLeft(it.date);
        if (it.type == 3) return d < 0 ? 2 : d <= 30 ? 1 : 0;
        if (!hasDue(it.type)) return 0;
        return d < 0 ? 2 : d <= 3 ? 1 : 0;
    }

    static String badge(Item it) {
        if (it.done) return it.type == 0 ? "Paid" : it.type == 2 ? "Completed" : "Done";
        if (it.date == 0 || it.type == 1 || it.type == 5) return "";
        long d = daysLeft(it.date);
        if (it.type == 3) return d < 0 ? "Expired" : d <= 60 ? d + " days left" : (d / 30) + " months";
        if (d < 0) return "Overdue";
        if (d == 0) return "Due today";
        if (d == 1) return "Tomorrow";
        if (d <= 14) return "In " + d + " days";
        return "In " + (d / 7) + " weeks";
    }
}
