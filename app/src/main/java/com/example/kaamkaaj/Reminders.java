package com.example.kaamkaaj;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Bills, maintenance, warranty and tasks: notification the day before and on the due date (9 AM).
 * Reminders: notification at the exact date and time chosen by the user.
 */
final class Reminders {
    private Reminders() {}

    private static int code(Store.Item it, int k) {
        return (int) (it.id % 1000000L) * 2 + k;
    }

    static void scheduleAll(Context c) {
        for (Store.Item it : Store.load(c)) schedule(c, it);
    }

    private static long at(Calendar u, int mins, int addDays) {
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH), mins / 60, mins % 60, 0);
        t.add(Calendar.DAY_OF_MONTH, addDays);
        return t.getTimeInMillis();
    }

    private static void post(Context c, AlarmManager am, Store.Item it, int k, long when, String msg) {
        Intent i = new Intent(c, ReminderReceiver.class);
        i.putExtra("id", code(it, k));
        i.putExtra("t", Store.EMOJI[it.type] + " " + it.title);
        i.putExtra("m", msg);
        PendingIntent pi = PendingIntent.getBroadcast(c, code(it, k), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
    }

    static void schedule(Context c, Store.Item it) {
        cancel(c, it);
        if (it.date == 0 || it.done || it.type == 1 || it.type == 5 || !Store.notifEnabled(c)) return;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Calendar u = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        u.setTimeInMillis(it.date);

        if (it.type == 6) {
            long t = at(u, it.mins >= 0 ? it.mins : 540, 0);
            if (t > System.currentTimeMillis()) {
                post(c, am, it, 0, t, it.notes.isEmpty() ? "Reminder" : it.notes);
            }
            return;
        }
        String amt = it.amount > 0 ? " \u2022 " + Store.money(it.amount) : "";
        for (int k = 0; k < 2; k++) {
            long t = at(u, 540, k == 0 ? -1 : 0);
            if (t <= System.currentTimeMillis()) continue;
            post(c, am, it, k, t, (k == 0 ? "Due tomorrow" : "Due today") + amt);
        }
    }

    static void cancel(Context c, Store.Item it) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        for (int k = 0; k < 2; k++) {
            PendingIntent pi = PendingIntent.getBroadcast(c, code(it, k), new Intent(c, ReminderReceiver.class),
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pi != null) {
                am.cancel(pi);
                pi.cancel();
            }
        }
    }

    /** Shows a sample notification about 5 seconds from now. */
    static void test(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent i = new Intent(c, ReminderReceiver.class);
        i.putExtra("id", 999999);
        i.putExtra("t", "\uD83D\uDCA1 Electricity bill");
        i.putExtra("m", "Due tomorrow \u2022 \u20B91,500");
        PendingIntent pi = PendingIntent.getBroadcast(c, 999999, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
    }
}
