package com.ssm.sweetdreamer.calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.ssm.sweetdreamer.HttpRequest;
import com.ssm.sweetdreamer.INFO;
import com.ssm.sweetdreamer.R;
import com.ssm.sweetdreamer.summary.SummaryFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class DateActivity extends Activity implements View.OnClickListener{

    private TextView tv_title;
    private TextView tv_sleepTime;
    private TextView tv_sleepStartTime;
    private TextView tv_sleepEndTime;
    private TextView tv_sleepQual;
    private GraphView gv_sleepStep;
    private LineGraphSeries<DataPoint> series;
    private GraphView gv_noise;
    private BarGraphSeries<DataPoint> series2;
    private ArrayList<String> wavPath;
    private int regIdx;
    private float sleepTime;
    private String sleepStartTime;
    private String sleepEndTime;

    private int year;
    private int month;
    private int day;

    private float calcX=0.0f;

    private ProgressDialog loadingDialog;
    private AlertDialog.Builder alert;

    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_date);


        mp = new MediaPlayer();
        alert = new AlertDialog.Builder(DateActivity.this);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Intent intent = getIntent();
        regIdx = intent.getIntExtra("regIdx", 0);
        sleepTime = intent.getFloatExtra("sleepTime", 0.0f);
        sleepStartTime = intent.getStringExtra("sleepStartTime");
        sleepEndTime = intent.getStringExtra("sleepEndTime");

        year = intent.getIntExtra("year", 1900);
        month = intent.getIntExtra("month", 1);
        day = intent.getIntExtra("day", 1);

        tv_title = (TextView)findViewById(R.id.tv_date_title);
        tv_sleepTime = (TextView)findViewById(R.id.tv_date_sleeptime);
        tv_sleepStartTime = (TextView)findViewById(R.id.tv_date_starttime);
        tv_sleepEndTime = (TextView)findViewById(R.id.tv_date_endtime);
        tv_sleepQual = (TextView)findViewById(R.id.tv_date_sleepQual);

        gv_sleepStep = (GraphView)findViewById(R.id.gv_date_sleep);
        gv_noise = (GraphView)findViewById(R.id.gv_date_noise);

        wavPath = new ArrayList<String>();
        series = new LineGraphSeries<DataPoint>();
        series2 = new BarGraphSeries<DataPoint>();
        series2.setColor(Color.rgb(72, 154, 216));
        series2.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                if(mp.isPlaying())
                    return;
                double d= dataPoint.getX()/(double)calcX;
                int x = (int)d;

                System.out.println("-----------idx : "+x);

                String url = "http://laravel.ssm.n-pure.net/app/public/upload/"+wavPath.get(x);
                System.out.println("noise path : "+wavPath.get(x));

                try {
                    DateActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alert.setMessage("소음 재생시작");
                            alert.show();
                        }
                    });
                    mp.reset();
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mp.setDataSource(url);
                    mp.prepare();
                    mp.start();
                    System.out.println("::::Noise play:::::");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        series.setColor(Color.rgb(72, 154, 216));
        series.setThickness(4);
        series.setBackgroundColor(0xFF0000);

        tv_title.setText(year + "년 " + month + "월 " + day + "일");

        gv_sleepStep.getGridLabelRenderer().setGridColor(Color.BLACK);
        gv_sleepStep.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        gv_sleepStep.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);

        gv_noise.getGridLabelRenderer().setGridColor(Color.BLACK);
        gv_noise.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        gv_noise.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);



        String str_sleepTime = new java.text.DecimalFormat("#.#").format(sleepTime);
        tv_sleepTime.setText(str_sleepTime+"시간");
        tv_sleepStartTime.setText(sleepStartTime);
        tv_sleepEndTime.setText(sleepEndTime);

        loadingDialog = ProgressDialog.show(this,"","Graph Loading...",true);
        HttpRequest client = new HttpRequest();
        try {
            client.doGetRequest(INFO.BASICURL + "member/" + INFO.MEMBERTOKEN + "/calendar/" + regIdx, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    loadingDialog.dismiss();
                    DateActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alert.setMessage("로딩 실패했습니다. 네트워크를 확인해주세요.");
                            alert.show();
                        }
                    });

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if(response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {
                            int arr[] = new int[4];
                            for(int i=0 ; i<4 ; i++)
                                arr[i]=0;
                            JSONObject jObj = new JSONObject(responseStr);
                            JSONArray jArr = jObj.getJSONObject("data").getJSONArray("sleepRecords");//그래프 그릴 정보
                            jObj = jObj.getJSONObject("data").getJSONObject("sleepInfo");
                            int total = Integer.parseInt(jObj.getString("totalRecord"));
                            int deep =Integer.parseInt(jObj.getString("deepRecord"));
                            final int percent = (int)((float)deep/(float)total*100);

                            float hour = (float)jArr.length()/120.0f;
                            SummaryFragment.LASTDAYSLEEPHOUR = hour;
                            float intervalX = hour/(float)jArr.length();
                            calcX = intervalX*20.0f;
                            float dx=0;

                            int maxPeak=0;
                            String noisePath="";
                            for(int i=0 ; i<jArr.length() ; i++)
                            {
                                jObj = jArr.getJSONObject(i);
                                arr[jObj.getInt("sleepStep")-1]++;
                                int peak = jObj.getInt("noisePeak");
                                if(maxPeak < peak) {
                                    maxPeak = peak;
                                    noisePath = jObj.getString("noise");
                                }
                                if(i%20 == 19)
                                {
                                    wavPath.add(noisePath);
                                    int max=0;
                                    int maxIdx=0;
                                    for(int j=0 ; j<4 ; j++)
                                    {
                                        if(max<arr[j]){
                                            max=arr[j];
                                            maxIdx=j;
                                        }
                                        arr[j]=0;
                                    }
                                    DataPoint data = new DataPoint(dx,maxIdx+1);
                                    series.appendData(data, true, 100);
                                    DataPoint data2 = new DataPoint(dx,maxPeak);
                                    System.out.println("::::dx = "+dx+" :::maxPeak="+maxPeak);
                                    series2.appendData(data2, true, 100);
                                    maxPeak=0;
                                }
                                dx+=intervalX;
                            }
                            DateActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_sleepQual.setText(percent + "%");
                                    gv_sleepStep.addSeries(series);
                                    gv_sleepStep.getGridLabelRenderer().setNumHorizontalLabels((int) SummaryFragment.LASTDAYSLEEPHOUR + 2);
                                    gv_sleepStep.setTitle("수면단계 그래프");
                                    gv_sleepStep.setTitleColor(Color.BLACK);
                                    gv_sleepStep.setTitleTextSize(30.0f);
                                    gv_sleepStep.getGridLabelRenderer().setVerticalAxisTitle("수면 단계(1-4)");
                                    gv_sleepStep.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLACK);
                                    gv_sleepStep.getGridLabelRenderer().setHorizontalAxisTitle("수면 시간별(Hour)");
                                    gv_sleepStep.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
                                    gv_noise.addSeries(series2);
                                    gv_noise.getGridLabelRenderer().setNumHorizontalLabels((int) SummaryFragment.LASTDAYSLEEPHOUR + 2);
                                    gv_noise.setTitle("수면소음 그래프");
                                    gv_noise.setTitleColor(Color.BLACK);
                                    gv_noise.setTitleTextSize(30.0f);
                                    gv_noise.getGridLabelRenderer().setVerticalLabelsVisible(false);
                                    gv_noise.getGridLabelRenderer().setVerticalAxisTitle("소음크기");
                                    gv_noise.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLACK);
                                    gv_noise.getGridLabelRenderer().setHorizontalAxisTitle("수면 시간별(Hour)");
                                    gv_noise.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
                                    loadingDialog.dismiss();
                                }

                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onClick(View v) {
    }
}
