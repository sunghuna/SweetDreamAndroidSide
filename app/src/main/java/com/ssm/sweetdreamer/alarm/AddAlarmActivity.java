package com.ssm.sweetdreamer.alarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class AddAlarmActivity extends Activity implements View.OnClickListener{

    private TimePicker tp_date;
    private NumberPicker np_alarmscope;
    private ToggleButton toggle_day[]=new ToggleButton[7];
    private final String str_day[] = {"월","화","수","목","금","토","일"};
    private final int i_day[] = {1,2,4,8,16,32,64};
    private Button btn_complete;
    private CheckBox cb_repeat;
    private CheckBox cb_once;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private ProgressDialog loadingDialog;
    private AlertDialog.Builder alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addalarm);
        tp_date = (TimePicker)findViewById(R.id.tp_alarm);
        np_alarmscope = (NumberPicker)findViewById(R.id.np_alarmscope);
        toggle_day[0] = (ToggleButton)findViewById(R.id.toggle_monday);
        toggle_day[1] = (ToggleButton)findViewById(R.id.toggle_tuesday);
        toggle_day[2] = (ToggleButton)findViewById(R.id.toggle_wednsday);
        toggle_day[3] = (ToggleButton)findViewById(R.id.toggle_thursday);
        toggle_day[4] = (ToggleButton)findViewById(R.id.toggle_friday);
        toggle_day[5] = (ToggleButton)findViewById(R.id.toggle_saturday);
        toggle_day[6] = (ToggleButton)findViewById(R.id.toggle_sunday);
        btn_complete = (Button)findViewById(R.id.btn_alarm_complete);
        cb_repeat = (CheckBox)findViewById(R.id.cb_repeat);
        cb_once = (CheckBox)findViewById(R.id.cb_once);
        pref = getSharedPreferences("pref",MODE_PRIVATE);
        editor = pref.edit();

        btn_complete.setOnClickListener(this);

        np_alarmscope.setMaxValue(120);
        for(int i=0 ; i<7 ; i++) {
            toggle_day[i].setText(str_day[i]);
            toggle_day[i].setOnClickListener(this);
        }

        alert = new AlertDialog.Builder(this);
        alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }
    @Override
    public void onClick(View v) {
        for(int i=0 ; i<7 ; i++)
        {
            if(v == toggle_day[i]) {
                toggle_day[i].setText(str_day[i]);
                if(toggle_day[i].isChecked())
                    toggle_day[i].setBackgroundColor(Color.rgb(100, 194, 113));
                else
                    toggle_day[i].setBackgroundColor(Color.rgb(180, 75, 55));

                return;
            }
        }
        int day=0;
        final int repeat=(cb_repeat.isChecked())?1:0;
        int scope = np_alarmscope.getValue();

        loadingDialog = ProgressDialog.show(AddAlarmActivity.this,"","알람 서버에 등록중..:",true);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, tp_date.getCurrentHour());
        cal.set(Calendar.MINUTE, tp_date.getCurrentMinute());
        cal.set(Calendar.SECOND, 0);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        cal.add(Calendar.MINUTE,-scope);
        final String waketime = format.format(cal.getTime());
        cal.add(Calendar.MINUTE,scope*2);
        final String waketime2 = format.format(cal.getTime());

        for(int i=0 ; i<7 ; i++)
            if(toggle_day[i].isChecked()) day+= i_day[i];

        final String daySetting = Integer.toString(day);
        HttpRequest client = new HttpRequest();
        try {
            client.doPostRequest(INFO.BASICURL+"alarm/"+INFO.MEMBERTOKEN,
                    "{\"isrepeat\":"+repeat+",\"waketime\":\"2015:11:11 "+waketime+"\",\"waketime2\":\"2015:11:11 "+waketime2+"\",\"repeatday\":"+daySetting+"}",
                    new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            loadingDialog.dismiss();
                            AddAlarmActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alert.setMessage("알람 등록 실패!!");
                                    alert.show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            if(response.isSuccessful()){
                                String responseStr = response.body().string();
                                try {
                                    JSONObject jObj = new JSONObject(responseStr);
                                    if(!jObj.getBoolean("error")){
                                        int alarmCnt = pref.getInt("alarmCnt",0);
                                        editor.putInt("alarmCnt",alarmCnt+1);
                                        editor.putString("startwaketime" + alarmCnt, "2010:01:01 "+waketime);
                                        editor.putString("endwaketime"+alarmCnt,"2080:01:01 "+waketime2);
                                        editor.putString("weekday"+alarmCnt,daySetting);
                                        editor.putInt("repeat"+alarmCnt,repeat);
                                        editor.commit();
                                        AddAlarmActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                alert.setMessage("알람이 등록되었습니다.");
                                                alert.show();
                                            }
                                        });
                                    }
                                    else{
                                        AddAlarmActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                alert.setMessage("알람 등록 실패!!");
                                                alert.show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {e.printStackTrace();}
                            }
                            else{
                                AddAlarmActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alert.setMessage("알람 등록 실패!!");
                                        alert.show();
                                    }
                                });
                            }
                            loadingDialog.dismiss();

                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
