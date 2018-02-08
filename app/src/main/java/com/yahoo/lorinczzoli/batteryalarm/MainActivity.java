package com.yahoo.lorinczzoli.batteryalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_LOW_BATTERY_ALARM_ENABLED = "KEY_LOW_BATTERY_ALARM_ENABLED";
    public static final String KEY_CHARGER_DISCONNECTED_ALARM_ENABLED = "KEY_CHARGER_DISCONNECTED_ALARM_ENABLED";
    public static final String KEY_SOUND_TYPE = "KEY_SOUND_TYPE";
    public static final String KEY_SNOOZE_ACTIVE = "KEY_SNOOZE_ACTIVE";
    public static final String KEY_SNOOZE_SEC = "KEY_SNOOZE_SEC";
    public static final String PREF_NAME = "Example01";

    private static Ringtone r = null;

    CheckBox chkLowBattery, chkChargerDisconnected, chkSnooze;
    Button btnStopAlarmSound;
    RadioButton rdoRingtone, rdoAlarm;
    EditText edtSnooze;
    TextView txtSnooze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get UI
        setContentView(R.layout.activity_main);
        chkLowBattery = findViewById(R.id.chkLowBattery);
        chkChargerDisconnected = findViewById(R.id.chkChargerDisconnected);

        chkSnooze = findViewById(R.id.chkSnooze);
        edtSnooze = findViewById(R.id.edtSnooze);
        edtSnooze.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                onClick(chkSnooze);
            }
        });
        txtSnooze = findViewById(R.id.txtSnooze);

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

        int nSnoozeSec = sp.getInt(KEY_SNOOZE_SEC, 0);
        Log.e("BATT", Integer.toString(nSnoozeSec) );

        if (nSnoozeSec > 0) chkSnooze.setChecked(true);
        else
        {
            edtSnooze.setEnabled(false);
            txtSnooze.setEnabled(false);
        }

        nSnoozeSec = Math.abs(nSnoozeSec);
        edtSnooze.setText( Integer.toString(nSnoozeSec) );
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
            case R.id.chkSnooze:
                bIsChecked = chkSnooze.isChecked();
                edtSnooze.setEnabled(bIsChecked);
                txtSnooze.setEnabled(bIsChecked);

                if (bIsChecked) txtSnooze.requestFocus();

                int nSnoozeSec = 0;
                String sSnoozeSec = edtSnooze.getText().toString();

                Log.e("BATT", "sSnoozeSec = '" + sSnoozeSec + "'");
                if (sSnoozeSec.isEmpty() == false) nSnoozeSec = Integer.valueOf(sSnoozeSec);
                if (bIsChecked == false) nSnoozeSec *= -1;

                Log.e("BATT", nSnoozeSec + " sec");

                sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                spe = sp.edit();

                bIsChecked = chkChargerDisconnected.isChecked();
                spe.putInt(KEY_SNOOZE_SEC, nSnoozeSec);

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
