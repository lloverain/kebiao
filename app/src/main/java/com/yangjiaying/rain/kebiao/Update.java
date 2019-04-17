package com.yangjiaying.rain.kebiao;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Update {
    public void gengxing(){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2/kebiao")
                .get()
                .build();
    }
}
