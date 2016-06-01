package com.ssm.sweetdreamer.alarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.ssm.sweetdreamer.HttpRequest;
import com.ssm.sweetdreamer.INFO;
import com.ssm.sweetdreamer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NewAlarmActivity extends Activity implements View.OnClickListener{

    MediaPlayer mp;
    Button btn_ok;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newalarm);

        mp = MediaPlayer.create(this,R.raw.alarmsong);
        mp.start();
        btn_ok = (Button)findViewById(R.id.btn_alarm_ok);
        btn_ok.setOnClickListener(this);
        pref = getSharedPreferences("pref",MODE_PRIVATE);
        editor = pref.edit();

        editor.putBoolean("isAlarm",true);
        editor.commit();


    }
    @Override
    public void onClick(View v) {
        mp.stop();
        editor.putBoolean("isAlarm",false);
        editor.commit();
        finish();

    }
}
