package com.ssm.sweetdreamer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import java.lang.ref.WeakReference;


public class LoadingActivity extends Activity {

    private static class MyHandler extends Handler {
        private final WeakReference<LoadingActivity> mActivity;
        //예상하기 힘든 각종 참조와 버그와 불필요한 객체가 삭제되지 않아 메모리가 낭비되는 것을 막을 수 있다.

        public MyHandler(LoadingActivity activity) {
            mActivity = new WeakReference<LoadingActivity>(activity);
        }

        @Override   // LoginActivity 실행
        public void handleMessage(Message msg) {
            LoadingActivity activity = mActivity.get();
            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.fade, R.anim.cycle_7);    // 애니메이션 효과
            activity.finish();
        }
    }

    private final MyHandler mHandler = new MyHandler(this);
    private SharedPreferences pref; // 데이터 공유를 위한 class
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        pref = getSharedPreferences("pref",MODE_PRIVATE);
        //if(pref.getString("id","").equals(""))
            mHandler.sendEmptyMessageDelayed(0, 1000);      // 현재 로딩화면을 1초(1000)간 표시
        /*else {
            INFO.USERID = pref.getString("id","");
            INFO.USERPW = pref.getString("pw","");
            INFO.MEMBERTOKEN = pref.getString("token","");
            System.out.println("TOKEN = "+INFO.MEMBERTOKEN);
            Intent bgIntent = new Intent(this,BGService.class);
            bgIntent.putExtra("id",INFO.USERID);
            stopService(bgIntent);
            startService(bgIntent);
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade, R.anim.cycle_7);
            finish();
        }*/
    }

}
