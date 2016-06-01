package com.ssm.sweetdreamer;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTSubScriber implements MqttCallback {

    private static final String LOG_TAG = "MqttSubscriber";

    //MQTT설정
    private MqttClient client;
    //private static final String MQTT_BROKER_URL = "my.n-pure.net";
    private static final String MQTT_BROKER_URL = "my.n-pure.net";

    private static final int MQTT_BROKER_PORT = 1883;
    private String MQTT_BROKER_PROTOCOL;
    private MqttConnectOptions connectionOpt;

            private final String MQTT_TOPIC = "/"+INFO.USERID;

            public MQTTSubScriber(){
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

    public void subscribe() {
        if(client == null){
            System.out.println("client is null...");
            return;
        }
        try {
            client.connect(connectionOpt);
            int qos = 2;
            client.subscribe(MQTT_TOPIC, qos);

        }catch(MqttException e){
            System.out.println("Subscribe exception :: "+e.getMessage()+"/" +e.toString());
        }

    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("MQTT Connection is lost");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        //메시지 수신 처리
        System.out.println(">>>>Received Topic : "+s);

        String a = new String(mqttMessage.getPayload());
      System.out.println(">>> Received Message : "+a);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
