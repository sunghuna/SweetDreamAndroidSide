package com.ssm.sweetdreamer.calendar;

/**
 * Created by Administrator on 2015-06-30.
 */
public class DayInfo {
    private float sleepTime;
    private int sleepQual;
    private int regIdx;
    private String day;
    private boolean inMonth;
    private String sleepStartTime;
    private String sleepEndTime;

    public void setSleepTime(float sleepTime){ this.sleepTime = sleepTime;}

    public float getSleepTime(){ return sleepTime; }

    public void setSleepQual(int qual){ this.sleepQual = qual; }

    public int getSleepQual(){ return sleepQual; }

    public void setRegIdx(int regIdx){this.regIdx = regIdx;}

    public int getRegIdx(){ return regIdx; }

    public String getDay(){
        return day;
    }

    public void setDay(String day){
        this.day = day;
    }

    public boolean inInMonth(){
        return inMonth;
    }

    public void setInMonth(boolean inMonth)
    {
        this.inMonth=inMonth;
    }

    public String getSleepEndTime() {
        return sleepEndTime;
    }

    public void setSleepEndTime(String sleepEndTime) {
        this.sleepEndTime = sleepEndTime;
    }

    public String getSleepStartTime() {
        return sleepStartTime;
    }

    public void setSleepStartTime(String sleepStartTime) {
        this.sleepStartTime = sleepStartTime;
    }
}

