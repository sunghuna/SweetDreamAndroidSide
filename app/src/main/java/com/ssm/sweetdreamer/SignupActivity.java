package com.ssm.sweetdreamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SignupActivity extends Activity {

    private EditText ed_id;
    private EditText ed_pw;
    private EditText ed_pw2;
    private ProgressDialog signupDialog;
    private AlertDialog.Builder alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        alert = new AlertDialog.Builder(this);
        ed_id = (EditText)findViewById(R.id.ed_signup_id);
        ed_pw = (EditText)findViewById(R.id.ed_signup_pw);
        ed_pw2 = (EditText)findViewById(R.id.ed_signup_pw2);
        Button btn_confirm = (Button)findViewById(R.id.btn_signup_confirm);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupDialog = ProgressDialog.show(SignupActivity.this,"","Wait for Sign up..",true);
                HttpRequest client = new HttpRequest();
                try {
                    client.doPostRequest(INFO.BASICURL + "member", "{\"userid\":\"" + ed_id.getText().toString() + "\",\"userpw\":\"" + ed_pw.getText().toString() + "\"}", new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            signupDialog.dismiss();
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            if(response.isSuccessful()) {
                                try {
                                    JSONObject jObject = new JSONObject(response.body().string());
                                    signupDialog.dismiss();
                                    if(!jObject.getBoolean("error"))
                                    {
                                        SignupActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                alert.setMessage("회원가입 완료!");
                                                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        SignupActivity.this.finish();
                                                    }
                                                });
                                                alert.show();
                                            }
                                        });
                                    }
                                    else
                                    {
                                        SignupActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                alert.setMessage("회원가입 실패!");
                                                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        SignupActivity.this.finish();
                                                    }
                                                });
                                                alert.show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
