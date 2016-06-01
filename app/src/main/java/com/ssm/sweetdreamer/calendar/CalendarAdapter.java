package com.ssm.sweetdreamer.calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssm.sweetdreamer.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015-06-30.
 */
public class CalendarAdapter extends BaseAdapter {
    private ArrayList<DayInfo> mDayList;
    private Context mContext;
    private int mResource;
    private LayoutInflater mLiInflater;

    public CalendarAdapter(ArrayList<DayInfo> mDayList, Context mContext, int mResource) {
        this.mDayList = mDayList;
        this.mContext = mContext;
        this.mResource = mResource;
        this.mLiInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DayInfo day = mDayList.get(position);

        DayViewHolde dayViewHolder;
        if (convertView == null) {
            convertView = mLiInflater.inflate(mResource, null);
            if (position % 7 == 6)
                convertView.setLayoutParams(new GridView.LayoutParams(getCellWidthDP() + getRestCellWidthDP(), getCellHeightDP()));
            else
                convertView.setLayoutParams(new GridView.LayoutParams(getCellWidthDP(), getCellHeightDP()));

            dayViewHolder = new DayViewHolde();
            dayViewHolder.llBackground = (LinearLayout) convertView.findViewById(R.id.ll_calendar_item);
            dayViewHolder.tvDay = (TextView)convertView.findViewById(R.id.tv_calendar_item);
            dayViewHolder.tvSleepTime = (TextView)convertView.findViewById(R.id.tv_calendar_item_sleeptime);
            convertView.setTag(dayViewHolder);
        }
        else
            dayViewHolder = (DayViewHolde) convertView.getTag();

        if(day!=null)
        {
            dayViewHolder.tvDay.setText(day.getDay());
            if(day.inInMonth())
            {
                if(position%7==0)
                    dayViewHolder.tvDay.setTextColor(Color.RED);
                else if(position%7==6)
                    dayViewHolder.tvDay.setTextColor(Color.BLUE);
                else
                    dayViewHolder.tvDay.setTextColor(Color.WHITE);

                int qual= day.getSleepQual();
                float sleepTime = day.getSleepTime();
                if(qual==0) {
                }
                else if(qual ==1 || sleepTime<6.0f)
                    dayViewHolder.llBackground.setBackgroundColor(Color.rgb(217,95,73));
                else if(qual ==2 || sleepTime<7.0f)
                    dayViewHolder.llBackground.setBackgroundColor(Color.rgb(237,194,51));
                else if(qual==3)
                    dayViewHolder.llBackground.setBackgroundColor(Color.rgb(100,194,113));

                if(sleepTime == 0.0f)
                    dayViewHolder.tvSleepTime.setText("");
                else {
                    System.out.println("sleepTime : "+sleepTime+"");
                    String str_sleepTime = new java.text.DecimalFormat("#.#").format(sleepTime);
                    dayViewHolder.tvSleepTime.setText(str_sleepTime + "");
                }

            }
            else
                dayViewHolder.tvDay.setTextColor(Color.DKGRAY);
        }
        return convertView;
    }

    public class DayViewHolde {
        public LinearLayout llBackground;
        public TextView tvDay;
        public TextView tvSleepTime;

    }

    private int getCellWidthDP() {
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int cellWidth = width / 7;

        return cellWidth;
    }

    private int getRestCellWidthDP() {
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int cellWidth = width % 7;

        return cellWidth;
    }

    private int getCellHeightDP() {
        int height = mContext.getResources().getDisplayMetrics().widthPixels;
        int cellHeight = height / 6;

        return cellHeight;
    }
}