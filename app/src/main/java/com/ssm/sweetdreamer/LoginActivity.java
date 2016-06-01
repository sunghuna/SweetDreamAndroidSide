package com.ssm.sweetdreamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends Activity {

    private EditText ed_id;
    private EditText ed_pw;
    private ProgressDialog loginDialog; //시간이 걸리는 작업은 사용자들에게 진행중이라는 메시지를 남기기 위해 ProgressDialog 를 이용.
    private AlertDialog.Builder alert;  //팝업창을 디자인
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pref = getSharedPreferences("pref",MODE_PRIVATE);
        Button btn_login = (Button) findViewById(R.id.btn_login_login);
        Button btn_signup = (Button)findViewById(R.id.btn_login_signup);
        ed_id = (EditText) findViewById(R.id.ed_login_id);
        ed_pw = (EditText) findViewById(R.id.ed_login_pw);
        alert = new AlertDialog.Builder(LoginActivity.this);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {   // OK 버튼 만들어서 누르면 꺼짐
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        btn_signup.setOnClickListener(new View.OnClickListener() {     // 등록 버튼 누르면 Intent로 SignupActivity 실행
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {   // 로그인 버튼 누르면
            @Override
            public void onClick(View v) {
                loginDialog = ProgressDialog.show(LoginActivity.this, "", "Wait for Login...", true);   // progress 표현(Spinner 형태)
                HttpRequest client = new HttpRequest(); // 서버와 연결
                try {   // 연결해서 멤버 , 로그인 , pw 가져옴
                    client.doPostRequest(INFO.BASICURL + "member/" + ed_id.getText().toString() + "/login", "{\"userpw\":\"" + ed_pw.getText().toString() + "\"}", new Callback()
                    {

                        @Override
                        public void onFailure(Request request, IOException e) {
                            loginDialog.dismiss();  // 로그인 실패시 progress diaglog 종료
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alert.setMessage("로그인 시도에 실패하였습니다. 네트워크를 확인해주세요.");
                                    alert.show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            if (response.isSuccessful()) {  // 응답 받아옴
                                String responseStr = response.body().string();  // GET방식으로 받아옴
                                    try {
                                    JSONObject jsonObject = new JSONObject(responseStr);    // DB에서 JSON 형태로 받을 때
                                        /* 로그인 성공시!!*/
                                    if (!jsonObject.getBoolean("error")) {  // 에러 안났을 때
                                        INFO.MEMBERTOKEN = jsonObject.getString("data");    // ????
                                        INFO.USERID = ed_id.getText().toString();
                                        INFO.USERPW = ed_pw.getText().toString();
                                        // DB에서 회원 정보 가져와서 SharedPreferences 에다가 넣기
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("id", INFO.USERID);
                                        editor.putString("pw", INFO.USERPW);
                                        editor.putString("token", INFO.MEMBERTOKEN);
                                        editor.commit();
                                        Intent bgIntent = new Intent(LoginActivity.this,BGService.class);   // 로그인 되면 BGService 실행
                                        bgIntent.putExtra("id",INFO.USERID);
                                        stopService(bgIntent);  // 중지
                                        startService(bgIntent);
                                        loginDialog.dismiss();  // 로그인 성공시 progress dialog 종료
                                        System.out.println("TOKEN : "+INFO.MEMBERTOKEN);

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class); // MainActivity 실행
                                        startActivity(intent);
                                        finish();
                                    } else {    // 에러 났을 때 (로그인 실패)
                                        loginDialog.dismiss();
                                        LoginActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                alert.setMessage("아이디와 비밀번호를 확인해주세요.");
                                                alert.show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {    // 응답이 안 왔을 때
                                loginDialog.dismiss();
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alert.setMessage("서버 오류!");
                                        alert.show();
                                    }
                                });
                            }
                        }
                    });
                } catch (IOException e) {e.printStackTrace();}
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_loading, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
