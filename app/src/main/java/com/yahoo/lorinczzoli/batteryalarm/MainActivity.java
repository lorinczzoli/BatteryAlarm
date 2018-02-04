package com.yahoo.lorinczzoli.batteryalarm;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_LOW_BATTERY_ALARM_ENABLED = "KEY_LOW_BATTERY_ALARM_ENABLED";
    public static final String KEY_CHARGER_DISCONNECTED_ALARM_ENABLED = "KEY_CHARGER_DISCONNECTED_ALARM_ENABLED";
    public static final String KEY_SOUND_TYPE = "KEY_SOUND_TYPE";
    public static final String KEY_SNOOZE_ACTIVE = "KEY_SNOOZE_ACTIVE";
    public static final String PREF_NAME = "Example01";

    private static Ringtone r = null;

    CheckBox chkLowBattery;
    CheckBox chkChargerDisconnected;
    Button btnStopAlarmSound;
    RadioButton rdoRingtone, rdoAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get UI
        setContentView(R.layout.activity_main);
        chkLowBattery = findViewById(R.id.chkLowBattery);
        chkChargerDisconnected = findViewById(R.id.chkChargerDisconnected);
        btnStopAlarmSound = findViewById(R.id.btnStopAlarmSound);

        rdoRingtone = findViewById(R.id.rdoRingtone);
        rdoAlarm = findViewById(R.id.rdoAlarm);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        boolean bEnabled = sp.getBoolean(KEY_LOW_BATTERY_ALARM_ENABLED, false);
        chkLowBattery.setChecked(bEnabled);

        bEnabled = sp.getBoolean(KEY_CHARGER_DISCONNECTED_ALARM_ENABLED, false);
        chkChargerDisconnected.setChecked(bEnabled);

        int nType = sp.getInt(KEY_SOUND_TYPE, RingtoneManager.TYPE_RINGTONE);
        if (nType == RingtoneManager.TYPE_RINGTONE) rdoRingtone.setChecked(true);
        else if (nType == RingtoneManager.TYPE_ALARM) rdoAlarm.setChecked(true);

        bEnabled = sp.getBoolean(KEY_SNOOZE_ACTIVE, false);
        btnStopAlarmSound.setEnabled(MainReceiver.IsRinging() || bEnabled);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chkLowBattery:
                SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor spe = sp.edit();

                boolean bIsChecked = chkLowBattery.isChecked();
                spe.putBoolean(KEY_LOW_BATTERY_ALARM_ENABLED, bIsChecked);

                spe.commit();

                break;
            case R.id.chkChargerDisconnected:
                sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                spe = sp.edit();

                bIsChecked = chkChargerDisconnected.isChecked();
                spe.putBoolean(KEY_CHARGER_DISCONNECTED_ALARM_ENABLED, bIsChecked);

                spe.commit();

                break;
            case R.id.btnStopAlarmSound:
                MainReceiver.StopRinging();
                MainReceiver.SetSnoozeActive(this, false);
                btnStopAlarmSound.setEnabled(MainReceiver.IsRinging());

                break;
            case R.id.btnSoundTest:
                if (MainReceiver.IsRinging())
                {
                    MainReceiver.StopRinging();
                }
                else
                {
                    MainReceiver.StartRinging(this, 0);
                }
                break;
            case R.id.rdoRingtone:
                sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                spe = sp.edit();

                spe.putInt(KEY_SOUND_TYPE, RingtoneManager.TYPE_RINGTONE);
                spe.commit();

                break;
            case R.id.rdoAlarm:
                sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                spe = sp.edit();

                spe.putInt(KEY_SOUND_TYPE, RingtoneManager.TYPE_ALARM);
                spe.commit();

                break;
            default:
                break;
        }
    }
}
