package com.ssm.sweetdreamer.summary;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.ssm.sweetdreamer.HttpRequest;
import com.ssm.sweetdreamer.INFO;
import com.ssm.sweetdreamer.MainActivity;
import com.ssm.sweetdreamer.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Administrator on 2015-07-13.
 */
public class SummaryFragment extends Fragment {
    public static float LASTDAYSLEEPHOUR;
    private int mPageNumber;

    private GraphView graph;
    private GraphView graph2;
    private TextView tv_sleepQual;
    private TextView tv_sleepAvg;
    private LineGraphSeries<DataPoint> series;
    private BarGraphSeries<DataPoint> series2;

    private ProgressDialog loadingDialog;
    private AlertDialog.Builder alert;
    public static SummaryFragment create(int pageNum){
        SummaryFragment frag = new SummaryFragment();
        Bundle args = new Bundle();
        args.putInt("page", pageNum);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt("page");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.frag_pattern,container,false);
        graph = (GraphView)v.findViewById(R.id.gv_pattern_sleep);
        graph2 = (GraphView)v.findViewById(R.id.gv_pattern_sleeptime);
        tv_sleepQual = (TextView)v.findViewById(R.id.tv_pattern_sleepQual);
        tv_sleepAvg = (TextView)v.findViewById(R.id.tv_pattern_sleepAvg);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        alert = new AlertDialog.Builder(getActivity());
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        graph.getGridLabelRenderer().setGridColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.DKGRAY);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.DKGRAY);
        //graph.getViewport().setBackgroundColor(Color.DKGRAY);

        loadingDialog = ProgressDialog.show(getActivity(),"","Graph Loading..",true);
        HttpRequest client = new HttpRequest();
        try {
            client.doGetRequest(INFO.BASICURL+"member/"+ INFO.MEMBERTOKEN+"/sleepQuality",mSleepQualCallback);
            client.doGetRequest(INFO.BASICURL+"member/"+INFO.MEMBERTOKEN+"/averageSleepTime",mSleepAvgCallback);
            client.doGetRequest(INFO.BASICURL+"member/"+INFO.MEMBERTOKEN+"/sleepDistribution",mSleepDistributeCallback);
            client.doGetRequest(INFO.BASICURL+"member/"+INFO.MEMBERTOKEN+"/recentSleepRecord",mSleepGraphCallback); }
        catch (IOException e) {
            e.printStackTrace();
        }

        series = new LineGraphSeries<DataPoint>();

        series.setColor(Color.rgb(72,154,216));
        series.setThickness(4);
        series.setBackgroundColor(0xFF0000);

        series2 = new BarGraphSeries<DataPoint>();

        series2.setColor(Color.rgb(72,154,216));
        series2.setSpacing(30);

        graph2.getGridLabelRenderer().setGridColor(Color.BLACK);
        graph2.getGridLabelRenderer().setHorizontalLabelsColor(Color.DKGRAY);
        graph2.getGridLabelRenderer().setVerticalLabelsColor(Color.DKGRAY);
        //graph2.getViewport().setBackgroundColor(Color.DKGRAY);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        MainActivity.menuSelect(mPageNumber);
        super.setUserVisibleHint(isVisibleToUser);
    }

    private Callback mSleepQualCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException e) {
            loadingDialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    alert.setMessage("로딩에 실패하였습니다. 네트워크를 확인해주세요.");
                    alert.show();
                }
            });

        }


        @Override
        public void onResponse(Response response) throws IOException {
            if(response.isSuccessful())
            {
                String responseStr = response.body().string();
                try {

                JSONObject jObj = new JSONObject(responseStr);
                jObj = jObj.getJSONObject("data");
                int total = jObj.getInt("totalRecord");
                int deep = jObj.getInt("deepRecord");
                final float qual = ((float)deep/(float)total)*100;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String str_qual = new java.text.DecimalFormat("#.#").format(qual);
                        tv_sleepQual.setText(str_qual+"%");
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            }

        }
    };

    private Callback mSleepAvgCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException e) {

            loadingDialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    alert.setMessage("로딩에 실패하였습니다. 네트워크를 확인해주세요.");
                    alert.show();
                }
            });
        }

        @Override
        public void onResponse(Response response) throws IOException {
            if(response.isSuccessful())
            {
                String responseStr = response.body().string();
                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    jObj = jObj.getJSONObject("data");
                    final float avg = ((float)jObj.getInt("totalSleepTime")/(float)jObj.getInt("numItem"))/3600.0f;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String str_avg = new java.text.DecimalFormat("#.#").format(avg);
                            tv_sleepAvg.setText(str_avg+"시간");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    };

    private Callback mSleepDistributeCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException e) {

            loadingDialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    alert.setMessage("로딩에 실패하였습니다. 네트워크를 확인해주세요.");
                    alert.show();
                }
            });
        }

        @Override
        public void onResponse(Response response) throws IOException {
            if(response.isSuccessful())
            {
                String responseStr = response.body().string();
                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    JSONArray jArr = jObj.getJSONArray("data");
                    for(int i=0 ; i<jArr.length() ; i++)
                    {
                        jObj = jArr.getJSONObject(i);
                        DataPoint data = new DataPoint(jObj.getInt("regHour"),jObj.getInt("regHourCount"));
                        series2.appendData(data,true,24);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        graph2.addSeries(series2);
                        graph2.getGridLabelRenderer().setNumHorizontalLabels(24);
                        graph2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
                        graph2.getGridLabelRenderer().setHorizontalAxisTitle("0시                6시              12시            18시        ");
                        graph2.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
                        graph2.getGridLabelRenderer().setVerticalLabelsVisible(false);
                        graph2.getGridLabelRenderer().setVerticalAxisTitle("수면량 (%)");
                        graph2.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLACK);
                        graph2.setTitle("수면 시간별 분포도");
                        graph2.setTitleColor(Color.BLACK);
                        graph2.setTitleTextSize(30.0f);
                    }
                });


            }
        }
    };

    private Callback mSleepGraphCallback = new Callback() {
        @Override
        public void onFailure(Request request, IOException e) {

            loadingDialog.dismiss();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    alert.setMessage("로딩에 실패하였습니다. 네트워크를 확인해주세요.");
                    alert.show();
                }
            });
        }

        @Override
        public void onResponse(Response response) throws IOException {
            if(response.isSuccessful()){
                String responseStr = response.body().string();
                try {
                    int arr[] = new int[4];
                    for(int i=0 ; i<4 ; i++)
                        arr[i]=0;
                    JSONObject jObj = new JSONObject(responseStr);
                    JSONArray jArr = jObj.getJSONArray("data");
                    float hour = (float)jArr.length()/120.0f;
                    LASTDAYSLEEPHOUR = hour;
                    float intervalX = hour/(float)jArr.length();
                    float dx = 0;
                    for(int i=0 ; i<jArr.length() ; i++)
                    {
                        jObj = jArr.getJSONObject(i);
                        arr[jObj.getInt("sleepStep")-1]++;
                        if(i%20 == 19)
                        {
                            int max=0;
                            int maxIdx=0;
                            for(int j=0 ; j<4 ; j++)
                            {
                                if(max<arr[j]) {
                                    max=arr[j];
                                    maxIdx = j;
                                }
                                arr[j]=0;
                            }
                            DataPoint data = new DataPoint(dx,maxIdx+1);
                            series.appendData(data,true,100);
                        }
                        dx+=intervalX;
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        graph.addSeries(series);
                        graph.getGridLabelRenderer().setNumHorizontalLabels((int) LASTDAYSLEEPHOUR + 2);
                        graph.setTitle("수면단계 그래프");
                        graph.setTitleColor(Color.BLACK);
                        graph.setTitleTextSize(30.0f);
                        graph.getGridLabelRenderer().setVerticalAxisTitle("수면 단계(1-4)");
                        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLACK);
                        graph.getGridLabelRenderer().setHorizontalAxisTitle("수면 시간별(Hour)");
                        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
                        loadingDialog.dismiss();
                    }
                });


            }

        }
    };
}