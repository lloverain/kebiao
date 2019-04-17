package com.yangjiaying.rain.kebiao;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class jiaoyuan extends AppCompatActivity {
    private  String[][] data  = new String[6][7];

    /** 第一个无内容的格子 */
    protected TextView empty;
    /** 星期一的格子 */
    protected TextView monColum;
    /** 星期二的格子 */
    protected TextView tueColum;
    /** 星期三的格子 */
    protected TextView wedColum;
    /** 星期四的格子 */
    protected TextView thrusColum;
    /** 星期五的格子 */
    protected TextView friColum;
    /** 星期六的格子 */
    protected TextView satColum;
    /** 星期日的格子 */
    protected TextView sunColum;
    /** 课程表body部分布局 */
    protected RelativeLayout course_table_layout;
    /** 屏幕宽度 **/
    protected int screenWidth;
    /** 课程格子平均宽度 **/
    protected int aveWidth;
    int gridHeight1 = 0;
    //(0)对应12节；(2)对应34节；(4)对应56节；(6)对应78节；(8)对应于9 10节
    int[] jieci = {0,2,4,6,8,10};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jiaoyuan);

        try {
            byte[] bytes = chucun.xingming.getBytes("utf8");
            chucun.xingming = new String(bytes,"gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SharedPreferences read = getSharedPreferences("rain",MODE_PRIVATE);
        boolean youdata = read.getBoolean("youdata",false);
        if(youdata){
            String shuju = read.getString("data","");
            Document document = Jsoup.parse(shuju);
            jiexi(document);
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }else {
            getkebiao();
        }
        //获得列头的控件
        empty = (TextView) this.findViewById(R.id.test_empty);
        monColum = (TextView) this.findViewById(R.id.test_monday_course);
        tueColum = (TextView) this.findViewById(R.id.test_tuesday_course);
        wedColum = (TextView) this.findViewById(R.id.test_wednesday_course);
        thrusColum = (TextView) this.findViewById(R.id.test_thursday_course);
        friColum = (TextView) this.findViewById(R.id.test_friday_course);
        satColum  = (TextView) this.findViewById(R.id.test_saturday_course);
        sunColum = (TextView) this.findViewById(R.id.test_sunday_course);
        course_table_layout = (RelativeLayout) this.findViewById(R.id.test_course_rl);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //屏幕宽度
        int width = dm.widthPixels;
        //平均宽度
        int aveWidth = width / 8;
        //第一个空白格子设置为25宽
        empty.setWidth(aveWidth * 3/4);
        monColum.setWidth(aveWidth * 33/32 + 1);
        tueColum.setWidth(aveWidth * 33/32 + 1);
        wedColum.setWidth(aveWidth * 33/32 + 1);
        thrusColum.setWidth(aveWidth * 33/32 + 1);
        friColum.setWidth(aveWidth * 33/32 + 1);
        satColum.setWidth(aveWidth * 33/32 + 1);
        sunColum.setWidth(aveWidth * 33/32 + 1);
        this.screenWidth = width;
        this.aveWidth = aveWidth;
        int height = dm.heightPixels;
        int gridHeight = height / 10;
        gridHeight1 = gridHeight;
        //设置课表界面
        //动态生成10 * maxCourseNum个textview
        for(int i = 1; i <= 11; i ++){
            for(int j = 1; j <= 8; j ++){

                TextView tx = new TextView(jiaoyuan.this);
                tx.setId((i - 1) * 8  + j);
                //除了最后一列，都使用course_text_view_bg背景（最后一列没有右边框）
                if(j < 8)
                    tx.setBackgroundDrawable(jiaoyuan.this.
                            getResources().getDrawable(R.drawable.white));
                else
                    tx.setBackgroundDrawable(jiaoyuan.this.
                            getResources().getDrawable(R.drawable.white));
                //相对布局参数
                RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
                        aveWidth * 33 / 32 + 1,
                        gridHeight);
                //文字对齐方式
                tx.setGravity(Gravity.CENTER);
                //字体样式
//                tx.setTextAppearance(this, R.style.courseTableText);
                //如果是第一列，需要设置课的序号（1 到 12）
                if(j == 1)
                {
                    tx.setText(String.valueOf(i));
                    rp.width = aveWidth * 3/4;
                    //设置他们的相对位置
                    if(i == 1)
                        rp.addRule(RelativeLayout.BELOW, empty.getId());
                    else
                        rp.addRule(RelativeLayout.BELOW, (i - 1) * 8);
                }
                else
                {
                    rp.addRule(RelativeLayout.RIGHT_OF, (i - 1) * 8  + j - 1);
                    rp.addRule(RelativeLayout.ALIGN_TOP, (i - 1) * 8  + j - 1);
                    tx.setText("");
                }

                tx.setLayoutParams(rp);
                course_table_layout.addView(tx);
            }
        }

    }

    /**
     * 得到课表
     */
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
                SharedPreferences.Editor editor = getSharedPreferences("rain",MODE_PRIVATE).edit();
                editor.putString("data", String.valueOf(document));
                editor.putBoolean("youdata",true);
                editor.commit();
                jiexi(document);
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }).start();
    }


    private void jiexi(Document document){
        String nianji = document.select("select[name=ddlXN]").select("option[selected=selected]").val();
        String xueqi = document.select("select[name=ddlXQ]").select("option[selected=selected]").val();
        Elements elementsClass = document.select("td");
        Elements trs = document.getElementById("Table1").select("tr");
        for(int i=2,c=0;i<trs.size();i+=2,c++){
            Elements tds = trs.get(i).select("td");
            //如果tr为2,6,10行就从td的第2开始取  过滤上午，下午，晚上
            if(i==2||i==6||i==10){
                for(int j=2,x =0 ;j<tds.size();j++,x++){
                    String a = tds.get(j).html();
                    a = a.replace("<font color=\"red\">","");
                    a = a.replace("</font>","");
                    a = a.replace("&nbsp;","");
                    a = a.replace("<br>","\n");
                    data[c][x] = a;
//                            Log.d(String.valueOf(j),a);
                }
            }else {
                for(int j=1,x =0;j<tds.size();j++,x++){
                    String a = tds.get(j).html();
                    a = a.replace("<font color=\"red\">","");
                    a = a.replace("</font>","");
                    a = a.replace("&nbsp;","");
                    a = a.replace("<br>","\n");
                    data[c][x] = a;
                }
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==1){
                //data.length = 6        data[i].length=7
                for(int i=0,t=0;i<data.length;i++,t+=2){
                    for (int j=0,g=1;j<data[i].length;j++,g++){
//                        Log.d("节课", String.valueOf(t));
//                        Log.d("星期", String.valueOf(g));
//                        Log.d("data"+"["+i+"]"+"["+j+"]",data[i][j]);
                        //有课才显示有颜色
                        if(!data[i][j].equals("")){
                            setCourseMessage(g,jieci[i],data[i][j]);
                        }
                    }
                }
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void setCourseMessage(int xingqi, int jieci, final String courseMessage){
        //五种颜色的背景
        int[] background = {R.drawable.red, R.drawable.green,
                R.drawable.purple, R.drawable.yellow,
                R.drawable.pink};
        // 添加课程信息
        TextView courseInfo = new TextView(this);
        courseInfo.setText(courseMessage);
        //该textview的高度根据其节数的跨度来设置
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                aveWidth * 31 / 32,
                (gridHeight1 - 5) * 2 );
        //textview的位置由课程开始节数和上课的时间（day of week）确定
        rlp.topMargin = 5 + jieci * gridHeight1;
        rlp.leftMargin = 1;
        // 偏移由这节课是星期几决定
        rlp.addRule(RelativeLayout.RIGHT_OF, xingqi);
        //字体剧中
        courseInfo.setGravity(Gravity.CENTER);
        // 设置一种背景
        Random random = new Random();
        courseInfo.setBackgroundResource(background[random.nextInt(5)]);
        courseInfo.setTextSize(12);
        courseInfo.setLayoutParams(rlp);
        courseInfo.setTextColor(Color.WHITE);
        //设置不透明度
        courseInfo.getBackground().setAlpha(222);
        course_table_layout.addView(courseInfo);
        courseInfo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(jiaoyuan.this);
                normalDialog.setIcon(R.mipmap.xiaohui);
                normalDialog.setTitle("课程");
                normalDialog.setMessage(courseMessage);
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //...To-do
                            }
                        });
                // 显示
                normalDialog.show();
                return false;
            }
        });
    }


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
