package com.yahoo.lorinczzoli.batteryalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.provider.Telephony;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainReceiver extends BroadcastReceiver {

    private int nSnoozeDefaultSec = 60;

    public MainReceiver()
    {
    }

    private static Ringtone r = null;

    public static boolean IsRinging() {
        if (r != null)
        {
            if (r.isPlaying() == false) r = null;
        }
        return r != null;
    }

    private static Context contextLast = null;

    public static void StartRinging(Context context, final int nSnoozeSec)
    {
        StopRinging();

        // start the sound
        SharedPreferences sp = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
        int nSoundType = sp.getInt(MainActivity.KEY_SOUND_TYPE, RingtoneManager.TYPE_RINGTONE);
        boolean bSnoozeActive = sp.getBoolean(MainActivity.KEY_SNOOZE_ACTIVE, false);

        if (bSnoozeActive || (nSnoozeSec ==0))
        {
            Uri notification = RingtoneManager.getDefaultUri(nSoundType);
            r = RingtoneManager.getRingtone(context, notification);
            r.play();
        }

        // schedule to snooze in X s
        if ((nSnoozeSec > 30) &&  (bSnoozeActive))
        {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            contextLast = context;
            scheduler.schedule(
                    new Runnable() {
                        @Override
                        public void run() {
                            MainReceiver.StartRinging(MainReceiver.contextLast, nSnoozeSec);
                        }
                    },
                    nSnoozeSec, TimeUnit.SECONDS
            );
        }
    }
    public static void StopRinging()
    {
        if (r != null) {
            if (r.isPlaying() == true) r.stop();
            r = null;
        }
    }

    public static void SetSnoozeActive(Context context, boolean bActive)
    {
        SharedPreferences sp = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();

        spe.putBoolean(MainActivity.KEY_SNOOZE_ACTIVE, bActive);
        spe.commit();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW))
        {
            SharedPreferences sp = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
            boolean bEnabled = sp.getBoolean(MainActivity.KEY_LOW_BATTERY_ALARM_ENABLED, false);

            if (bEnabled)
            {
                SetSnoozeActive(context, true);
                StartRinging(context, nSnoozeDefaultSec);
            }
        }
        else if ( (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) ||
                  (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) )
        {
            StopRinging();
        }
        else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
        {
            SharedPreferences sp = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
            boolean bEnabled = sp.getBoolean(MainActivity.KEY_CHARGER_DISCONNECTED_ALARM_ENABLED, false);

            if (bEnabled)
            {
                SetSnoozeActive(context, true);
                StartRinging(context, nSnoozeDefaultSec);
            }
        }
        else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
        {
            StopRinging();
        }
    }
}
