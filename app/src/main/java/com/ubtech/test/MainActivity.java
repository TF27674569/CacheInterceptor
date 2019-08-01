package com.ubtech.test;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import org.intercept.CacheInterceptor;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {


    private TextView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CacheInterceptor.debug(true);
        webView = findViewById(R.id.webView);
    }

    public void insert(View view) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new CacheInterceptor(this))
                .build();

        Request request = new Request.Builder()
                .url("https://www.baidu.com/")
                .get()
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TAG", "onFailure: " + e.getMessage());
            }

            @SuppressLint("CheckResult")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                String string = body.string();
                Log.e("TAG", "onResponse: msg:" + response.message() + "  data" + string);
                Observable.just(string)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                webView.setText(Html.fromHtml(s).toString());
                            }
                        });
            }
        });

    }

}
