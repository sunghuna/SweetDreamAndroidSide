package com.ssm.sweetdreamer.alarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ssm.sweetdreamer.R;

import java.util.Calendar;
import java.util.List;

public class AlarmListAdapter extends ArrayAdapter<AlarmItem> {
    public AlarmListAdapter(Context context, int resource, List<AlarmItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlarmItem item = getItem(position);
        if(convertView==null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.alarm_item,parent,false);
        }

        TextView tv_hour = (TextView)convertView.findViewById(R.id.tv_alarmitem_hour);
        TextView tv_min = (TextView)convertView.findViewById(R.id.tv_alarmitem_min);
        TextView tv_week[] = new TextView[7];
        tv_week[0] = (TextView)convertView.findViewById(R.id.tv_alarmitem_monday);
        tv_week[1] = (TextView)convertView.findViewById(R.id.tv_alarmitem_tuesday);
        tv_week[2] = (TextView)convertView.findViewById(R.id.tv_alarmitem_wednsday);
        tv_week[3] = (TextView)convertView.findViewById(R.id.tv_alarmitem_thursday);
        tv_week[4] = (TextView)convertView.findViewById(R.id.tv_alarmitem_friday);
        tv_week[5] = (TextView)convertView.findViewById(R.id.tv_alarmitem_saturday);
        tv_week[6] = (TextView)convertView.findViewById(R.id.tv_alarmitem_sunday);
        TextView tv_repeat = (TextView)convertView.findViewById(R.id.tv_alarmitem_repeat);

        Calendar cal = item.getCal();
        tv_hour.setText(cal.get(Calendar.HOUR_OF_DAY)+"시");
        tv_min.setText(cal.get(Calendar.MINUTE)+"분");

        int weekday = item.getWeekday();
        int repeat = item.getRepeat();

        Boolean weekbit[] = new Boolean[7]; //월화수목금토일
        for(int k=0 ; k < 7 ; k++)
        {
            if((weekday & (1<<k)) != 0)
                weekbit[k] =true;
            else
                weekbit[k] = false;
        }

        for(int i=0 ; i<7 ; i++)
        {
            if(weekbit[i])
                tv_week[i].setVisibility(View.VISIBLE);
            else
                tv_week[i].setVisibility(View.INVISIBLE);
        }
        if(repeat==1)
            tv_repeat.setVisibility(View.VISIBLE);
        else
            tv_repeat.setVisibility(View.INVISIBLE);

        return convertView;
    }
}
