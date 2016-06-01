package com.ssm.sweetdreamer.alarm;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AlarmFragment extends Fragment implements View.OnClickListener {
    private int mPageNumber;
    private Button btn_add;
    private ArrayList<AlarmItem> listAlarm;
    AlarmListAdapter adapter;
    private AlertDialog.Builder alert;
    private ProgressDialog loadingDialog;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    public static AlarmFragment create(int pageNum) {
        AlarmFragment frag = new AlarmFragment();
        Bundle args = new Bundle();
        args.putInt("page", pageNum);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt("page");
        alert = new AlertDialog.Builder(getActivity());

        pref = getActivity().getSharedPreferences("pref", getActivity().MODE_PRIVATE);
        editor = pref.edit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.frag_alarm, container, false);

        btn_add = (Button) v.findViewById(R.id.btn_alarm_add);
        btn_add.setOnClickListener(this);
        listAlarm = new ArrayList<AlarmItem>();
        adapter = new AlarmListAdapter(getActivity(), 0, listAlarm);
        ListView listView = (ListView) v.findViewById(R.id.lv_alarm);
        listView.setOnItemClickListener(modifyListener);
        listView.setOnItemLongClickListener(deleteListener);
        listView.setAdapter(adapter);
        return v;
    }

    @Override
    public void onResume() {

        loadingDialog = ProgressDialog.show(getActivity(), "", "Alarm List Loading...", true);
        super.onResume();
        listAlarm.clear();
        HttpRequest client = new HttpRequest();
        try {
            client.doGetRequest(INFO.BASICURL + "alarm/" + INFO.MEMBERTOKEN, new Callback() {
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
                    if (response.isSuccessful()) {
                        String responseStr = response.body().string();
                        try {
                            JSONObject jObj = new JSONObject(responseStr);
                            JSONArray jArr = jObj.getJSONArray("data");
                            for (int i = 0; i < jArr.length(); i++) {
                                jObj = jArr.getJSONObject(i);
                                String waketime = jObj.getString("waketime");
                                String waketime2 = jObj.getString("waketime2");
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(format.parse(waketime));
                                Calendar cal2 = Calendar.getInstance();
                                cal2.setTime(format.parse(waketime2));
                                long diffMin = (cal2.getTimeInMillis() - cal.getTimeInMillis()) / 60000;
                                int gab = (int) diffMin / 2;
                                cal.add(Calendar.MINUTE, gab);
                                int repeat = Integer.parseInt(jObj.getString("isrepeat"));
                                int weekday = Integer.parseInt(jObj.getString("repeat_day"));
                                String seq = jObj.getString("seq");
                                AlarmItem item = new AlarmItem(seq, cal, weekday, repeat, gab);
                                listAlarm.add(item);
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    loadingDialog.dismiss();

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        MainActivity.menuSelect(mPageNumber);
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), AddAlarmActivity.class);
        getActivity().startActivity(intent);
    }

    public AdapterView.OnItemClickListener modifyListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AlarmItem item = adapter.getItem(position);
            Intent intent = new Intent(getActivity(), ModifyAlarmActivity.class);
            intent.putExtra("hour",item.getCal().get(Calendar.HOUR_OF_DAY));
            intent.putExtra("min",item.getCal().get(Calendar.MINUTE));
            intent.putExtra("repeat", item.getRepeat());
            intent.putExtra("scope", item.getScope());
            intent.putExtra("weekday",item.getWeekday());
            intent.putExtra("seq",Integer.parseInt(item.getSeq()));
            intent.putExtra("position",position);
            getActivity().startActivity(intent);
        }
    };
    public AdapterView.OnItemLongClickListener deleteListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            AlarmItem item = adapter.getItem(position);
            final String seq = item.getSeq();
            alert.setMessage("알람을 삭제하시겠습니까?");
            alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    HttpRequest client = new HttpRequest();
                    try {
                        client.doDeleteRequest(INFO.BASICURL +"alarm/"+ INFO.MEMBERTOKEN + "/" + seq, new Callback() {
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
                                if(response.isSuccessful()) {
                                    String responseStr = response.body().string();
                                    try {
                                        JSONObject jObj = new JSONObject(responseStr);
                                        if(!jObj.getBoolean("error"))
                                        {
                                            listAlarm.remove(position);
                                            int alarmCnt = pref.getInt("alarmCnt",0);
                                            for(int i=position ; i<alarmCnt;i++)
                                            {
                                                //editor.putInt("alarmCnt",alarmCnt+1);
                                                editor.putString("startwaketime" + i, pref.getString("startwaketime"+(i+1),""));
                                                editor.putString("endwaketime"+i,pref.getString("endwaketime"+(i+1),""));
                                                editor.putString("weekday"+i,pref.getString("weekday"+(i+1),""));
                                                editor.putInt("repeat"+i,pref.getInt("repeat"+(i+1),0));
                                            }
                                            editor.remove("startwaketime"+alarmCnt);
                                            editor.remove("endwaketime"+alarmCnt);
                                            editor.remove("weekday"+alarmCnt);
                                            editor.remove("repeat"+alarmCnt);
                                            editor.remove("alarmed"+position);
                                            editor.remove("alarmedTime"+position);
                                            editor.putInt("alarmCnt", alarmCnt-1);
                                            editor.commit();
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });
                                        }
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
            });
            alert.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.show();
            return true;
        }
    };
}