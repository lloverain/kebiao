package com.yangjiaying.rain.kebiao;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.pgyersdk.update.PgyUpdateManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView yanzhengmatu;//显示验证码图控件
    private EditText zhanghu;//输入账户控件
    private EditText mima;//输入密码控件
    private EditText yanzhengma;//输入验证码控件
    private Button login;//登录按钮
    private CheckBox jizhu;//记住密码多选按钮控件

    private Bitmap bitmap;//验证码的图
    private String s;//cookie
//    private String name;//账户名

    String checkurl="http://www.pgyer.com/apiv1/app/viewGroup";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        String codeversin=getVersion();
        new Thread(new Runnable() {
            @Override
            public void run() {
                check();
            }
        }).start();
        SharedPreferences read = getSharedPreferences("rain", MODE_PRIVATE);
        //----
        Boolean denglu = read.getBoolean("login",false);
        if(denglu){
            Message message = new Message();
            message.what =2;
            handler.sendMessage(message);
        }else {
            Boolean a = read.getBoolean("gou", false);
            if (a) {
                jizhu.setChecked(a);
                String user = read.getString("use", "");
                String pass = read.getString("password", "");
                zhanghu.setText(user);
                mima.setText(pass);
            }
            ChangeImage();
            yanzhengmatu.setOnClickListener(this);
            login.setOnClickListener(this);
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        yanzhengmatu = findViewById(R.id.yanzhengmatu);
        zhanghu = findViewById(R.id.zhanghu);
        mima = findViewById(R.id.mima);
        yanzhengma = findViewById(R.id.yanzhengma);
        login = findViewById(R.id.login);
        jizhu = findViewById(R.id.jizhu);

    }

    /**
     * 得到Cookies和验证码图片
     */
    private void ChangeImage() {
        Request request = new Request.Builder()
                .url("http://jw.svtcc.edu.cn/CheckCode.aspx")
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okhttp3.Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Message message = new Message();
                message.what = 5;
                handler.sendMessage(message);
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                byte[] byte_image = response.body().bytes();
                bitmap = BitmapFactory.decodeByteArray(byte_image, 0, byte_image.length);//获取验证码图片
                Headers headers = response.headers();//获取网页头部
                List<String> cookies = headers.values("Set-Cookie");//取cookie值
                String session = cookies.get(0);
                String cookie = cookies.toString();
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                chucun.__VIEWSTATE = getViewState(cookie);//获取__VIEWSTATE
                s = session.substring(0, session.indexOf(";"));
                chucun.cookie = s;
            }
        });
    }

    /**
     * 获取__VIEWSTATE
     * @param cookie
     * @return __VIEWSTATE
     * @throws UnsupportedOperationException
     * @throws IOException
     */
    public static String getViewState(String cookie)
            throws UnsupportedOperationException,
            IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .header("Cookie", cookie)
                .header("Referer", "http://jw.svtcc.edu.cn/")
                .url("http://jw.svtcc.edu.cn/default2.aspx")
                .build();
        Response response = okHttpClient.newCall(request).execute();
        String s = response.body().string();
        String viewstate = Jsoup.parse(s).select("input[name=__VIEWSTATE]").val();
        return viewstate;
    }

    /*
     * 登录
     * */
    private void LoginServer(String zhanghu, String mima, String yanzhengma) {
        chucun.xuehao = zhanghu;
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody body = new FormBody.Builder()
                .add("__VIEWSTATE", chucun.__VIEWSTATE)
                .add("TextBox1", zhanghu)
                .add("TextBox2", mima)
                .add("TextBox3", yanzhengma)
                .add("RadioButtonList1", "%D1%A7%C9%FA")
                .add("Button1", "")
                .build();

        if (jizhu.isChecked()) {
            SharedPreferences.Editor editor = getSharedPreferences("rain", MODE_PRIVATE).edit();
            editor.putString("use", zhanghu);
            editor.putString("password", mima);
            editor.putBoolean("gou", true);
            editor.commit();
        } else {
            SharedPreferences.Editor editor = getSharedPreferences("rain", MODE_PRIVATE).edit();
            editor.putString("use", zhanghu);
            editor.putString("password", "");
            editor.putBoolean("gou", false);
            editor.commit();
        }


        final Request request = new Request.Builder()
                .addHeader("cookie", s)
                .url("http://jw.svtcc.edu.cn/default2.aspx")
                .post(body)
                .build();
        okhttp3.Call call2 = okHttpClient.newCall(request);
        call2.enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = 6;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                /*你想要执行的下一步功能*/
                byte[] bytes = response.body().bytes();
                String data = new String(bytes, "gb2312");
                Document document = Jsoup.parse(data);
                String title = document.title();
//                Log.d("data", data);//登录之后得到的html
                if("登录".equals(title)){
                    Message message = new Message();
                    message.what = 7;
                    handler.sendMessage(message);
                }else if ("正方教务管理系统".equals(title)) {

                    tiqu(data);
                } else {
                    Message message = new Message();
                    message.what = 4;
                    handler.sendMessage(message);
                }
            }
        });
    }

    /**
     * 1    给验证码控件附图片
     * 2    登陆成功并跳转
     * 3    提示出现不可预料的错误
     * 4    提示服务器验证失败,请稍后重试
     * 5    提示验证码获取失败
     * 6    提示服务器未响应，请稍后再试
     * 7    验证码输入错误时，提示并重新刷新验证码
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            /*
             * 设置验证码图片
             * */
            if (msg.what == 1) {
                yanzhengmatu.setImageBitmap(bitmap);
            }
            /*
             * 登录成功跳转guodu界面
             * */
            if (msg.what == 2) {
                Intent intent = new Intent(MainActivity.this, guodu.class);
//                startActivity(intent);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                MainActivity.this.finish();
            }
            if (msg.what == 3) {
                Toast.makeText(MainActivity.this, "出现不可预料的错误", Toast.LENGTH_SHORT).show();
            }

            if (msg.what == 4) {
                Toast.makeText(MainActivity.this, "服务器验证失败,请稍后重试", Toast.LENGTH_SHORT).show();
            }
            
            if(msg.what == 5){
                Toast.makeText(MainActivity.this, "验证码获取失败", Toast.LENGTH_SHORT).show();
            }
            if(msg.what==6){
                Toast.makeText(MainActivity.this, "服务器未响应，请稍后再试", Toast.LENGTH_SHORT).show();
            }
            if(msg.what == 7){
                showdialog();
            }
            if(msg.what == 8){
                Toast.makeText(MainActivity.this, "请更新最新版", Toast.LENGTH_SHORT).show();
            }
            if(msg.what == 9){
                Toast.makeText(MainActivity.this, "当前版本为最新版", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.yanzhengmatu:
                ChangeImage();
                break;
            case R.id.login:
                String account = zhanghu.getText().toString();
                String password = mima.getText().toString();
                String verificationCode = yanzhengma.getText().toString();
                if("".equals(account)){
                    Toast.makeText(this, "请输入账户", Toast.LENGTH_SHORT).show();
                    zhanghu.requestFocus();
                }else if("".equals(password)){
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                    mima.requestFocus();
                }else if ("".equals(verificationCode)){
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    yanzhengma.requestFocus();
                }else {
                    LoginServer(account, password, verificationCode);
                }

                break;
        }
    }

    /**
     * 提取网页上的姓名
     *
     * @param data
     */
    private void tiqu(String data) {
        Document document = Jsoup.parse(data);
        String element = document.getElementById("xhxm").html();
        if(element==null){
            Message message = new Message();
            message.what = 3;
            handler.sendMessage(message);
        }else {
            chucun.xingming = element.toString();
            //登录成功
            SharedPreferences.Editor editor = getSharedPreferences("rain",MODE_PRIVATE).edit();
            editor.putBoolean("login",true);
            editor.putString("name",element);
            editor.commit();
            Message message = new Message();
            message.what = 2;
            handler.sendMessage(message);
        }

    }

    /**
     * 显示验证码错误提示框
     */
    private void showdialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setIcon(R.mipmap.xiaohui);
        normalDialog.setTitle("Error");
        normalDialog.setMessage("输入的验证码错误！");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        ChangeImage();
                    }
                });
        // 显示
        normalDialog.show();
    }


    private void installAPK(File savedFile) {
        //调用系统的安装方法
        Intent intent=new Intent();
        intent.setAction(intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(savedFile), "application/vnd.android.package-archive");
        startActivity(intent);
        finish();
    }

    /**
     * 检查是否为最新版本
     */
    public void check(){
        //当所用app前版本号
        String codeversin=getVersion();

        String s="aId=c564ff760980ab1a81de3b6d53de5233&_api_key=eaab3e20c4bb29a0e6a88a592fda6077";
        //
        String sss= Httputils.doPost(checkurl,s);
        try {
            JSONObject object=new JSONObject(sss);
            JSONArray array=object.getJSONArray("data");
            for(int i=0;i<array.length();i++){
                JSONObject object1=array.getJSONObject(i);
                String appVersion=object1.getString("appVersion");
                Log.d("服务器版本",appVersion);
                Log.d("本地版本",codeversin);
                double x1=Double.valueOf(appVersion);
                double x2=Double.valueOf(codeversin);
                if(x1>x2){
                    download();
                }else {
                    Message message = new Message();
                    message.what =9;
                    handler.sendMessage(message);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //
    }

    /**
     * 下载
     */
    public  void download(){
        String target = Environment.getExternalStorageDirectory()+ "/update.apk";
        String path="https://www.pgyer.com/apiv2/app/install?appKey=c564ff760980ab1a81de3b6d53de5233&_api_key=eaab3e20c4bb29a0e6a88a592fda6077";

        HttpUtils utils = new HttpUtils();
        utils.download(path, target, new RequestCallBack<File>() {
            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
            }
            @Override
            public void onSuccess(ResponseInfo<File> arg0) {
                String path=Environment.getExternalStorageDirectory()+ "/update.apk";
                File file=new File(path);
                installAPK(file);
            }

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                Log.d("exception", String.valueOf(arg0.getExceptionCode()));
                Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 得到版本号和名
     * @return
     */
    private String getVersion(){
        PackageInfo pkg;
        String versionName="";
        try {
            pkg = getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
            String appName = pkg.applicationInfo.loadLabel(getPackageManager()).toString();
            versionName = pkg.versionName;
            System.out.println("app名:" + appName);
            System.out.println("得到本地版本号:" + versionName);

        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return  versionName;
    }


    /**
     * 打印超长Log
     * @param tag
     * @param msg
     */
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

