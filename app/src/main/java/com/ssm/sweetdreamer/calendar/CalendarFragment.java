package com.ssm.sweetdreamer.calendar;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

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

/**
 * Created by Administrator on 2015-07-13.
 */
public class CalendarFragment extends Fragment implements AdapterView.OnItemClickListener {
    public static int SUNDAY = 1;
    public static int MONDAY = 2;
    public static int TUESDAY = 3;
    public static int WEDNSESDAY = 4;
    public static int THURSDAY = 5;
    public static int FRIDAY = 6;
    public static int SATURDAY = 7;

    private int mPageNumber;

    private Button btn_nextMonth;
    private Button btn_prevMonth;
    private Button btn_nowMonth;

    GridView gv_calendar;
    TextView tv_calendar_title;
    int thisMonth;
    int thisYear;

    Calendar mPrevMonthCalendar;
    Calendar mThisMonthCalendar;
    Calendar mNextMonthCalendar;

    ArrayList<DayInfo> mDayList;
    CalendarAdapter mCalAdapter;
    int mSleepQual[][];
    int mRegIdx[][];
    int mSeq[][];
    float mSleepTime[][];
    String mSleepStartTime[][];
    String mSleepEndTime[][];

    int threadSet;
    private ProgressDialog loadingDialog;
    private AlertDialog.Builder alert;


    public static CalendarFragment create(int pageNum) {
        CalendarFragment frag = new CalendarFragment();
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
        v = inflater.inflate(R.layout.frag_calandar, container, false);
        gv_calendar = (GridView) v.findViewById(R.id.gv_calendar);
        tv_calendar_title = (TextView) v.findViewById(R.id.tv_calendar_month);
        btn_nextMonth = (Button) v.findViewById(R.id.btn_calendar_next);
        btn_prevMonth = (Button) v.findViewById(R.id.btn_calendar_prev);
        btn_nowMonth = (Button) v.findViewById(R.id.btn_calendar_now);

        gv_calendar.setOnItemClickListener(this);
        btn_nextMonth.setOnClickListener(mMonthClickListener);
        btn_prevMonth.setOnClickListener(mMonthClickListener);
        btn_nowMonth.setOnClickListener(mMonthClickListener);
        mDayList = new ArrayList<DayInfo>();
        mSleepQual = new int[3][32];
        mRegIdx = new int[3][32];
        mSeq = new int[3][32];
        mSleepTime = new float[3][32];
        mSleepStartTime = new String[3][32];
        mSleepEndTime = new String[3][32];

        return v;
    }

    @Override
    public void onResume() {
        loadingDialog = ProgressDialog.show(getActivity(),"","Calendar Loading...",true);
        alert = new AlertDialog.Builder(getActivity());
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        super.onResume();

        mThisMonthCalendar = Calendar.getInstance();
        mThisMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        threadSet=0;

        setCalendar();
    }

    private void setCalendar(){
        for(int i=0 ; i<32 ; i++)
        {
            mSleepQual[0][i] =0; mSleepQual[1][i] =0; mSleepQual[2][i]=0;
            mSeq[0][i]=0; mSeq[1][i]=0; mSeq[2][i]=0;
            mRegIdx[0][i]=0; mRegIdx[1][i]=0; mRegIdx[2][i]=0;
            mSleepTime[0][i]=0; mSleepTime[1][i]=0; mSleepTime[2][i]=0;
        }
        try {
            requestCalendar(mThisMonthCalendar);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            System.out.print("loop ");
            if(threadSet<0){
                System.out.println("Fail to download calendar");
                break;
            }
            else if(threadSet!=3)
                continue;

            else {
                threadSet=0;
                getCalendar(mThisMonthCalendar);
                loadingDialog.dismiss();
                break;
            }
        }
    }

    private void requestCalendar(final Calendar calendar) throws IOException {
        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        HttpRequest client = new HttpRequest();
        //이전달
        client.doGetRequest(INFO.BASICURL +"member/"+ INFO.MEMBERTOKEN + "/calendar/" + mYear + "/" + mMonth, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                threadSet-=5;
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
                String responseStr = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jObj = new JSONObject(responseStr);
                        JSONArray jArr = jObj.getJSONArray("data");

                        for (int k = 0; k < jArr.length(); k++) {
                            jObj = jArr.getJSONObject(k);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String starttime = jObj.getString("START_TIME");
                            Calendar calStart = Calendar.getInstance();
                            calStart.setTime(format.parse(starttime));
                            String endtime = jObj.getString("END_TIME");
                            Calendar calEnd = Calendar.getInstance();
                            calEnd.setTime(format.parse(endtime));
                            int j = calEnd.get(Calendar.DAY_OF_MONTH);
                            mSleepQual[0][j] = Integer.parseInt(jObj.getString("deepSleepIndex"));
                            mRegIdx[0][j] =Integer.parseInt(jObj.getString("regIdx"));
                            mSeq[0][j] = Integer.parseInt(jObj.getString("seq"));
                            mSleepStartTime[0][j] = starttime;
                            mSleepEndTime[0][j] = endtime;
                            long resultTime = calEnd.getTime().getTime() - calStart.getTime().getTime();
                            mSleepTime[0][j] = (float)resultTime / (1000*60*60);
                        }

                    } catch (JSONException e) { e.printStackTrace(); }
                    catch(ParseException e){e.printStackTrace();}
                    threadSet+=1;
                }
                else
                    threadSet-=5;
            }
        });
        //이번달
        client.doGetRequest(INFO.BASICURL +"member/"+ INFO.MEMBERTOKEN + "/calendar/" + mYear + "/" + (mMonth+1), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                threadSet=-5;
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
                String responseStr = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jObj = new JSONObject(responseStr);
                        JSONArray jArr = jObj.getJSONArray("data");

                        for (int k = 0; k < jArr.length(); k++) {
                            jObj = jArr.getJSONObject(k);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String starttime = jObj.getString("START_TIME");
                            String endtime = jObj.getString("END_TIME");
                            Calendar calStart = Calendar.getInstance();
                            calStart.setTime(format.parse(starttime));
                            Calendar calEnd = Calendar.getInstance();
                            calEnd.setTime(format.parse(endtime));
                            int j = calEnd.get(Calendar.DAY_OF_MONTH);
                            mSleepQual[1][j] = Integer.parseInt(jObj.getString("deepSleepIndex"));
                            mRegIdx[1][j] = Integer.parseInt(jObj.getString("regIdx"));
                            mSeq[1][j] = Integer.parseInt(jObj.getString("seq"));
                            mSleepStartTime[1][j] = starttime;
                            mSleepEndTime[1][j] = endtime;
                            long resultTime = calEnd.getTime().getTime() - calStart.getTime().getTime();
                            mSleepTime[1][j] = (float)resultTime / (1000*60*60);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    threadSet += 1;
                }
                else
                    threadSet-=5;
            }
        });
        //다음달
        client.doGetRequest(INFO.BASICURL +"member/"+ INFO.MEMBERTOKEN + "/calendar/" + mYear + "/" + (mMonth +2), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                threadSet-=5;
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
                String responseStr = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jObj = new JSONObject(responseStr);
                        JSONArray jArr = jObj.getJSONArray("data");

                        for (int k = 0; k < jArr.length(); k++) {
                            jObj = jArr.getJSONObject(k);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String starttime = jObj.getString("START_TIME");
                            Calendar calStart = Calendar.getInstance();
                            calStart.setTime(format.parse(starttime));
                            String endtime = jObj.getString("END_TIME");
                            Calendar calEnd = Calendar.getInstance();
                            calEnd.setTime(format.parse(endtime));
                            int j = calEnd.get(Calendar.DAY_OF_MONTH);
                            mSleepQual[2][j] = Integer.parseInt(jObj.getString("deepSleepIndex"));
                            mRegIdx[2][j] =Integer.parseInt(jObj.getString("regIdx"));
                            mSeq[2][j] = Integer.parseInt(jObj.getString("seq"));
                            mSleepStartTime[2][j] = starttime;
                            mSleepEndTime[2][j] = endtime;
                            long resultTime = calEnd.getTime().getTime() - calStart.getTime().getTime();
                            mSleepTime[2][j] = (float)resultTime / (1000*60*60);
                        }


                    } catch (JSONException e) { e.printStackTrace(); }
                    catch(ParseException e){e.printStackTrace();}
                    threadSet+=1;
                }
                else
                    threadSet-=5;
            }
        });

    }


    private void getCalendar(Calendar calendar) {

        int lastMonthStartDay;
        int dayOfMonth;
        int thisMonthLastDay;

        mDayList.clear();

        dayOfMonth = calendar.get(Calendar.DAY_OF_WEEK);
        thisMonthLastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.add(Calendar.MONTH, -1);

        lastMonthStartDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.add(Calendar.MONTH,1);

        if(dayOfMonth == SUNDAY)
            dayOfMonth+=7;

        lastMonthStartDay -=  (dayOfMonth-1)-1;

        thisMonth = mThisMonthCalendar.get(Calendar.MONTH)+1;
        thisYear = mThisMonthCalendar.get(Calendar.YEAR);
        tv_calendar_title.setText(mThisMonthCalendar.get(Calendar.YEAR) + "년 "
                +(mThisMonthCalendar.get(Calendar.MONTH)+1)+"월");

        DayInfo day;

        for(int i=0 ; i<dayOfMonth-1; i++)
        {
            int date = lastMonthStartDay-i;
            day = new DayInfo();
            day.setDay(Integer.toString(date));
            day.setInMonth(false);
            day.setRegIdx(mRegIdx[0][i]);
            day.setSleepQual(mSleepQual[0][i]);
            day.setSleepTime(mSleepTime[0][i]);
            day.setSleepStartTime(mSleepStartTime[0][i]);
            day.setSleepEndTime(mSleepEndTime[0][i]);
            mDayList.add(day);
        }
        for(int i=1; i <= thisMonthLastDay; i++)
        {
            day = new DayInfo();
            day.setDay(Integer.toString(i));
            day.setInMonth(true);
            day.setRegIdx(mRegIdx[1][i]);
            day.setSleepQual(mSleepQual[1][i]);
            day.setSleepTime(mSleepTime[1][i]);
            day.setSleepStartTime(mSleepStartTime[1][i]);
            day.setSleepEndTime(mSleepEndTime[1][i]);
            mDayList.add(day);
        }
        for(int i=1; i<42-(thisMonthLastDay+dayOfMonth-1)+1; i++)
        {
            day = new DayInfo();
            day.setDay(Integer.toString(i));
            day.setInMonth(false);
            day.setRegIdx(mRegIdx[2][i]);
            day.setSleepQual(mSleepQual[2][i]);
            day.setSleepTime(mSleepTime[2][i]);
            day.setSleepStartTime(mSleepStartTime[2][i]);
            day.setSleepEndTime(mSleepEndTime[2][i]);
            mDayList.add(day);
        }

        initCalendarAdapter();
    }
    private void initCalendarAdapter(){
        mCalAdapter = new CalendarAdapter(mDayList,this.getActivity(),R.layout.calendar_item);
        gv_calendar.setAdapter(mCalAdapter);
    }

    private Calendar getLastMonth(Calendar calendar)
    {
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        calendar.add(Calendar.MONTH, -1);
        tv_calendar_title.setText(mThisMonthCalendar.get(Calendar.YEAR) + "년 "
                + (mThisMonthCalendar.get(Calendar.MONTH) + 1) + "월");
        return calendar;
    }

    private Calendar getNextMonth(Calendar calendar)
    {
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        calendar.add(Calendar.MONTH, +1);
        tv_calendar_title.setText(mThisMonthCalendar.get(Calendar.YEAR) + "년 "
                + (mThisMonthCalendar.get(Calendar.MONTH) + 1) + "월");
        return calendar;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        MainActivity.menuSelect(mPageNumber);
        super.setUserVisibleHint(isVisibleToUser);
    }

    private Button.OnClickListener mMonthClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            loadingDialog.show();
            switch (v.getId()) {
                case R.id.btn_calendar_next:
                    mThisMonthCalendar = getNextMonth(mThisMonthCalendar);
                    setCalendar();
                    break;
                case R.id.btn_calendar_prev:
                    mThisMonthCalendar = getLastMonth(mThisMonthCalendar);
                    setCalendar();
                    break;
                case R.id.btn_calendar_now:
                    mThisMonthCalendar = Calendar.getInstance();
                    mThisMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    setCalendar();
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mDayList.get(position).getSleepTime()==0.0f)
            return;

        Intent intent = new Intent(getActivity(),DateActivity.class);
        intent.putExtra("regIdx",mDayList.get(position).getRegIdx());
        intent.putExtra("sleepStartTime",mDayList.get(position).getSleepStartTime());
        intent.putExtra("sleepEndTime",mDayList.get(position).getSleepEndTime());
        intent.putExtra("sleepTime",mDayList.get(position).getSleepTime());
        intent.putExtra("year",thisYear);
        intent.putExtra("month",thisMonth);
        intent.putExtra("day",Integer.parseInt(mDayList.get(position).getDay()));
        startActivity(intent);
    }
}