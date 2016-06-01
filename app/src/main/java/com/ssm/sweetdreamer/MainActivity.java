package com.ssm.sweetdreamer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.ssm.sweetdreamer.alarm.AlarmFragment;
import com.ssm.sweetdreamer.calendar.CalendarFragment;
import com.ssm.sweetdreamer.setting.SettingFragment;
import com.ssm.sweetdreamer.summary.SummaryFragment;
import com.viewpagerindicator.UnderlinePageIndicator;

public class MainActivity extends FragmentActivity {

    private static ImageButton btn_menu_pattern;
    private static ImageButton btn_menu_calendar;
    private static ImageButton btn_menu_alarm;
    private static ImageButton btn_menu_setting;
    ViewPager mPager;   // 좌우로 밀어내는것
    ViewPagerAdapter mAdapter;
    UnderlinePageIndicator underlinePageIndicator;  // ?


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPager = (ViewPager)findViewById(R.id.mainPager);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());   // 연결(adapter와)
        mPager.setAdapter(mAdapter);
        underlinePageIndicator = (UnderlinePageIndicator)findViewById(R.id.mainIndicator);
        underlinePageIndicator.setFades(false);
        underlinePageIndicator.setViewPager(mPager);
        underlinePageIndicator.setSelectedColor(0xFF00FFFA);
        btn_menu_pattern = (ImageButton)findViewById(R.id.btn_menu_pattern);
        btn_menu_calendar = (ImageButton)findViewById(R.id.btn_menu_calandar);
        btn_menu_alarm = (ImageButton)findViewById(R.id.btn_menu_alarm);
        btn_menu_setting = (ImageButton)findViewById(R.id.btn_menu_setting);

        btn_menu_alarm.setOnClickListener(mClickListener);
        btn_menu_calendar.setOnClickListener(mClickListener);
        btn_menu_pattern.setOnClickListener(mClickListener);
        btn_menu_setting.setOnClickListener(mClickListener);
        btn_menu_pattern.setSelected(true);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_loading, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override   // back 키 눌렀을 때
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("종료하시겠습니까?");
        alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.show();
    }

    ImageButton.OnClickListener mClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_menu_pattern: // 기본 메뉴
                    mPager.setCurrentItem(0);
                    break;
                case R.id.btn_menu_calandar:    // 캘린더
                    mPager.setCurrentItem(1);
                    break;
                case R.id.btn_menu_alarm:   // 알람
                    mPager.setCurrentItem(2);
                    break;
                case R.id.btn_menu_setting: // 세팅
                    mPager.setCurrentItem(3);
                    break;
            }
        }
    };

    public static void menuSelect(int idx)
    {
        switch(idx){
            case 0: // 메뉴
                btn_menu_pattern.setSelected(true);
                btn_menu_calendar.setSelected(false);
                btn_menu_alarm.setSelected(false);
                btn_menu_setting.setSelected(false);
                break;
            case 1: // 캘린더
                btn_menu_pattern.setSelected(false);
                btn_menu_calendar.setSelected(true);
                btn_menu_alarm.setSelected(false);
                btn_menu_setting.setSelected(false);
                break;
            case 2: // 알람
                btn_menu_pattern.setSelected(false);
                btn_menu_calendar.setSelected(false);
                btn_menu_alarm.setSelected(true);
                btn_menu_setting.setSelected(false);
                break;
            case 3: // 셋팅
                btn_menu_pattern.setSelected(false);
                btn_menu_calendar.setSelected(false);
                btn_menu_alarm.setSelected(false);
                btn_menu_setting.setSelected(true);
                break;
            default:


        }
    }
    // 어뎁터로 받아서 실행시키는 부분
    private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return SummaryFragment.create(position);
                case 1:
                    return CalendarFragment.create(position);
                case 2:
                    return AlarmFragment.create(position);
                case 3:
                    return SettingFragment.create(position);
            }
            return SummaryFragment.create(position);
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

}
