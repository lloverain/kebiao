package com.yangjiaying.rain.kebiao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class guodu extends AppCompatActivity {
    private TextView name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guodu);
//        Intent intent = getIntent();
//        String use = intent.getStringExtra("name");
        name = findViewById(R.id.name);
        name.setText("欢迎你!"+chucun.xingming);
        Integer time = 3000;    //设置等待时间，单位为毫秒
        Handler handler = new Handler();
        //当计时结束时，跳转至主界面
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(guodu.this, jiaoyuan.class));
                guodu.this.finish();
            }
        }, time);
    }
}
