package com.ssm.sweetdreamer.setting;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ssm.sweetdreamer.LoginActivity;
import com.ssm.sweetdreamer.MainActivity;
import com.ssm.sweetdreamer.R;
import com.ssm.sweetdreamer.calendar.CalendarAdapter;
import com.ssm.sweetdreamer.calendar.DayInfo;

import java.util.ArrayList;
import java.util.Calendar;

public class SettingFragment extends Fragment {
    private int mPageNumber;
    private AlertDialog.Builder alert;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public static SettingFragment create(int pageNum){
        SettingFragment frag = new SettingFragment();
        Bundle args = new Bundle();
        args.putInt("page", pageNum);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt("page ");
        context = getActivity();
        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v;

        v = inflater.inflate(R.layout.frag_setting,container,false);
        Button btn_logout = (Button)v.findViewById(R.id.btn_setting_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert = new AlertDialog.Builder(getActivity());
                alert.setMessage("정말 로그아웃 하시겠습니까?");
                alert.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editor.remove("id");
                        editor.remove("pw");
                        editor.commit();
                        getActivity().stopService(new Intent("com.ssm.sweetdreamer.BGService"));
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        getActivity().startActivity(intent);
                        getActivity().finish();
                    }
                });
                alert.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.show();
            }
        });

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        MainActivity.menuSelect(3);
        super.setUserVisibleHint(isVisibleToUser);
    }
}