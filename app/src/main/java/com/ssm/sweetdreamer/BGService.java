package com.ssm.sweetdreamer;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.ssm.sweetdreamer.alarm.NewAlarmActivity;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BGService extends Service implements MqttCallback {


    private static final String LOG_TAG = "MqttSubscriber";

    //MQTT설정
    private MqttClient client;
    //private static final String MQTT_BROKER_URL = "211.189.20.31";
    private static final String MQTT_BROKER_URL = "my.n-pure.net";
    private static final int MQTT_BROKER_PORT = 1883;
    private String MQTT_BROKER_PROTOCOL;
    private MqttConnectOptions connectionOpt;

    private String MQTT_TOPIC;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private int alarmCnt;
    private ArrayList<Calendar> startAlarmList;
    private ArrayList<Calendar> endAlarmList;
    private ArrayList<String> weekdayList;
    private ArrayList<Integer> repeatList;

    private int label[] = new int[10];
    private int idx = 0;

    // 델타 사운드와 실제 알람사운드
    private MediaPlayer deltaSound;
    private MediaPlayer alarmSound;

    @Override   // 서비스와 컴포넌트 사이의 인터페이스를 정의
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("BGService OnCreate!!");
        //프로토콜 설정
        MQTT_BROKER_PROTOCOL = "tcp://"+MQTT_BROKER_URL+":"+MQTT_BROKER_PORT;

        //커넥션 옵션 설정
        connectionOpt = new MqttConnectOptions();
        connectionOpt.setCleanSession(true);

        //MQTT 클라이언트 생성
            try{
            MemoryPersistence mp = new MemoryPersistence();
            client = new MqttClient(MQTT_BROKER_PROTOCOL,"test",mp);

            //콜백 등록
            client.setCallback(this);
        }
        catch (MqttException e){
            System.out.println(">>MqttExceoption :: "+e.getMessage()+" / " + e.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("BGService OnStartCommand!!");
        String id = intent.getStringExtra("id");
        System.out.println("MQTT ID : "+id);
        MQTT_TOPIC = "/"+id+"/sleepStep";
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();
        startAlarmList = new ArrayList<Calendar>();
        endAlarmList = new ArrayList<Calendar>();
        weekdayList = new ArrayList<String>();
        repeatList = new ArrayList<Integer>();


        deltaSound = MediaPlayer.create(this,R.raw.delta);
        deltaSound.setLooping(true);    // 음악재생
        alarmSound = MediaPlayer.create(this, R.raw.alarmsong);
        alarmSound.setLooping(true);    // 음악재생

        for(int i=0 ; i<10 ; i++)
            label[i]=0;

        if(client == null){
            System.out.println("client is null...");
            return 0;
        }
        try {
            client.connect(connectionOpt);
            int qos = 2;
            client.subscribe(MQTT_TOPIC, qos);
        }catch(MqttException e){
            System.out.println("Subscribe exception :: "+e.getMessage()+"/" +e.toString());
        }
        return START_REDELIVER_INTENT;  // 재생성과 onStartCommand() 호출
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("MQTT : MQTT Connection is lost ");
    }

    @Override   // 메세지 받으면
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        System.out.println("MQTT Received : [TOPIC]" + s);
        String a = new String(mqttMessage.getPayload());
        System.out.println("MQTT Received : [Message]"+a);

        startAlarmList.clear();
        endAlarmList.clear();
        weekdayList.clear();
        repeatList.clear();
        alarmCnt = pref.getInt("alarmCnt",0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss"); // 더 쉽게 날짜 Formatting 을 위한 class
        Calendar nowCal = Calendar.getInstance();   // 현재 날짜와 시간 정보를 가진 Calendar 객체를 생성한다.
        for(int i=0 ; i< alarmCnt ; i++)
        {
            Calendar cal =Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(pref.getString("startwaketime"+i,"")));   // date 객체의 날짜와 시간 정보를 현재 객체로 생성한다.
                // 현재 객체의 년, 월, 일 값을 다른 값으로 설정한다.
                cal.set(Calendar.YEAR,nowCal.get(Calendar.YEAR));
                cal.set(Calendar.MONTH,nowCal.get(Calendar.MONTH));
                cal.set(Calendar.DAY_OF_MONTH, nowCal.get(Calendar.DAY_OF_MONTH));
                cal2.setTime(sdf.parse(pref.getString("endwaketime" + i, "")));
                cal2.set(Calendar.YEAR,nowCal.get(Calendar.YEAR));
                cal2.set(Calendar.MONTH, nowCal.get(Calendar.MONTH));
                cal2.set(Calendar.DAY_OF_MONTH, nowCal.get(Calendar.DAY_OF_MONTH));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            startAlarmList.add(cal);    // 알람에 추가시킴
            endAlarmList.add(cal2);
            weekdayList.add(pref.getString("weekday" + i, "0"));
            repeatList.add(pref.getInt("repeat"+i,0));
        }

        long now = nowCal.getTimeInMillis();

        int weekDayToday = nowCal.get(Calendar.DAY_OF_WEEK);//일 월 화 수 목 금 토
        Boolean weekDayCheck[] = new Boolean[8];


        for(int i=0 ; i<alarmCnt ; i++)
        {
            Calendar compCal = endAlarmList.get(i);
            compCal.set(nowCal.get(Calendar.YEAR),nowCal.get(Calendar.MONTH),nowCal.get(Calendar.DAY_OF_MONTH));

            long end = compCal.getTimeInMillis();   // 객체의 시간을 1/1000초 단위로 변경하여 반환한다.

            int weekDay = Integer.parseInt(weekdayList.get(i));// 1월 2화 4수 8목 16금 32토 64일 // 1일 2월 3화 4수 5목 6금 7토


            for(int k=0 ; k<8 ; k++)
                weekDayCheck[k]=false;
            if(weekDay-64 >= 0) {
                weekDayCheck[1] = true;
                weekDay -= 64;
            }
            if(weekDay-32 >= 0 ){
                weekDayCheck[6] = true;
                weekDay-=32;
            }
            if(weekDay-16>=0){
                weekDayCheck[5] = true;
                weekDay-=16;
            }
            if(weekDay-8>=0){
                weekDayCheck[4] = true;
                weekDay-=8;
            }
            if(weekDay-4>=0){
                weekDayCheck[3] = true;
                weekDay-=4;
            }
            if(weekDay-2>=0){
                weekDayCheck[2] = true;
                weekDay-=2;
            }
            if(weekDay-1>=0){
                weekDayCheck[1] = true;
                weekDay-=1;
            }
            if(!weekDayCheck[weekDayToday])
                continue;

            //알람허용범위 마지노선일때 무조건 알람을 울려주기 위한 조건문 , 알람을 실행시킴(intent로 NewAlarmActivity불러서)
            if(end - now <= 40000 && end-now>=0) {
                //이미 알람이 울려 종료를 했을때 처리(그날에 대해서는 해당알람은 울리지않아야함
                if(pref.getBoolean("alarmed"+i,false)){
                    if(pref.getLong("alarmedTime" + i, 0) < now)//알람범위 지나갔으면
                    {
                        editor.remove("alarmed" + i);
                        editor.remove("alarmedTime"+i);
                        editor.commit();
                    }
                    else//알람범위내에 있으면
                        continue;
                }
                deltaSound.stop();
                deltaSound.prepare();
                System.out.println(":::: 마지노선 Alarm!!! ::::");
                editor.putBoolean("alarmed"+i,true);
                editor.putLong("alarmedTime"+i,end);
                editor.commit();
                Intent intent = new Intent(this, NewAlarmActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            }
        }
        
        //얕은 잠일때 알람 && 바이노럴 비트 사운드
        label[idx] =Integer.parseInt(a);
        idx=(idx+1)%10;
        if(a.equals("1") || a.equals("2")) {
            int cnt=0;
            for(int i=0 ; i<10 ; i++)
            {
                if(label[i] == 1 || label[i] == 2)
                    cnt++;

            }

            if(cnt>=6) {//최근 10개(5분)동안 60%이상이 얕은수면단계라면, 얕은잠으로 판정
                Boolean isAlarm = pref.getBoolean("isAlarm",false);
                if(!deltaSound.isPlaying() && !isAlarm){
                    //바이노럴 비트사운드 재생
                    deltaSound.start();
                    System.out.println("::::Bit Sound Play::::");
                }
                //System.out.println("AlarmCnt : "+alarmCnt);

                for (int i = 0; i < alarmCnt; i++) {
                    long start = startAlarmList.get(i).getTimeInMillis();
                    long end = endAlarmList.get(i).getTimeInMillis();

                    System.out.println("alarmed : "+pref.getBoolean("alarmed"+i,false) + " alarmedTime: "+pref.getLong("alarmedTime"+i,0));
                    //알람이 울렸고 사용자가 종료를 눌렀을때에 해당 알람에 대해서 그날에는 울리지 않게하기 위한 조건문
                    if(pref.getBoolean("alarmed"+i,false))
                    {
                        if(pref.getLong("alarmedTime"+i,0)<now)//알람범위 지나갔으면
                        {
                            editor.remove("alarmed" + i);
                            editor.remove("alarmedTime" + i);
                            editor.commit();
                        }
                        else//알람범위내에 있으면
                            continue;
                    }

                    if (start <= now && end >= now && !isAlarm) {//알림시각 허용범위내에 있다면 알람을 울린다.(알람 실행 Intent로 NewAlarmActivity 부름)
                        deltaSound.stop();
                        deltaSound.prepare();
                        System.out.println(":::: Alarm!!! :::: "+start+ "::::"+now+"::::"+end);
                        editor.putBoolean("alarmed"+i,true);
                        editor.putLong("alarmedTime"+i,end);
                        editor.commit();
                        Intent intent = new Intent(this, NewAlarmActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            }
            //깊은 수면에 빠졌는데 바이노럴이 울리고있다면 끄기
            else if(deltaSound.isPlaying()){
                deltaSound.stop();
                deltaSound.prepare();
            }
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("MQTT Complete");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            client.disconnect();
            client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        System.out.println("BGService OnDestroy.....");
        deltaSound.stop();
    }
}

