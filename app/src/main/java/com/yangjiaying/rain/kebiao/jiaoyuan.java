package com.yangjiaying.rain.kebiao;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class jiaoyuan extends AppCompatActivity {
    private WebView kebiao;

    private String html;
    private String htmls;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jiaoyuan);
        kebiao = findViewById(R.id.kebiao);
        try {
            byte[] bytes = chucun.xingming.getBytes("utf8");
            chucun.xingming = new String(bytes,"gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getkebiao();

    }

    private void getkebiao(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document document = null;
                try {
                    document = Jsoup.connect("http://jw.svtcc.edu.cn/xskbcx.aspx?xh=" + chucun.xuehao + "&xm=" + chucun.xingming + "&gnmkdm=N121603")
                            .header("cookie", chucun.cookie)
                            .header("Referer", "http://jw.svtcc.edu.cn/xs_main.aspx?xh=" + chucun.xuehao)
                            .header("Host", "jw.svtcc.edu.cn")
                            .get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                html = document.html();
                html = document.getElementById("Table1").html();
                html = "<table border=\"1\" cellspacing=\"0\">"+html+"</table>";

                i("html",html);


                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }).start();
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                Spanned a = Html.fromHtml(html);
                kebiao.getSettings().setDefaultTextEncodingName("utf8");
                kebiao.loadData(html, "text/html; charset=UTF-8", null);
            }
        }
    };

    public static void i(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int max_str_length = 2001 - tag.length();
        //大于4000时
        while (msg.length() > max_str_length) {
            Log.i(tag, msg.substring(0, max_str_length));
            msg = msg.substring(max_str_length);
        }
        //剩余部分
        Log.i(tag, msg);
    }
}
