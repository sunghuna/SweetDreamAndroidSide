package com.ssm.sweetdreamer.alarm;


import java.util.Calendar;

/**
 * Created by Administrator on 2015-07-16.
 */
public class AlarmItem {
    private int weekday;
    private int repeat;
    private int scope;
    private Calendar cal;
    private String seq;

    public AlarmItem(String seq, Calendar cal,int weekday, int repeat, int scope){
        this.seq = seq;
        this.cal = cal;
        this.weekday = weekday;
        this.repeat = repeat;
        this.scope = scope;
    }

    public Calendar getCal() {
        return cal;
    }

    public int getWeekday() {
        return weekday;
    }

    public int getRepeat() {
        return repeat;
    }

    public int getScope() {
        return scope;
    }

    public String getSeq() {
        return seq;
    }
}
