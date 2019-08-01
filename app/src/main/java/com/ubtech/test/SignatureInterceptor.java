package com.ubtech.test;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 加密拦截器
 * 党建的接口设计 post接口存在一定的问题
 * 所有请求参数并未挡在body当中 而是与get请求一样直接接在url后面
 * 所以此处公有参数处理
 * 1.解析Url参数
 * 2.将参数加密
 * 3.拼接新的url
 */
public class SignatureInterceptor implements Interceptor {
    private static final String TAG = "SignatureInterceptor";

    public static final String TOKEN = "2damHGwMhOcqk5JRisMl";
//    public static final String TIMESTAMP = String.valueOf(System.currentTimeMillis());
    public static final String NONCE = "67890";

    private Map<String, String> mParsms = new LinkedHashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString();
        String requestUrl = getRequestUrl(url);

        //改url后的request
        request = request.newBuilder().url(requestUrl).build();
        return chain.proceed(request);
    }

    private String getRequestUrl(String url) {
        mParsms.clear();
        String timestamp = String.valueOf(System.currentTimeMillis());
        // 添加公有参数
        // 如果请求参数有nonce 下面也可以直接覆盖
        mParsms.put("nonce", NONCE);
        mParsms.put("timestamp", timestamp);

        // 判断请求是否带有参数 可能是空参的post请求
        if (url.contains("?")) {
            // 请求参数封装 含头不含尾 并不需要?
            String strParam = url.substring(url.indexOf("?") + 1);
            String[] params = strParam.split("&");

            for (String param : params) {
                Log.e(TAG, "param: " + param);
                String[] split = param.split("=");
                // 签名不添加 后面会重新签名
                if (!"signature".equals(split[0])) {
                    mParsms.put(split[0], split[1]);
                }
            }
            // 截取baseurl
            url = url.substring(0, url.indexOf("?"));
        }

        return joinParam(url);
    }

    private String joinParam(String url) {
        StringBuilder stringBuffer = new StringBuilder(url);
        stringBuffer.append("?");
        List<String> value = new ArrayList<>();
        for (Map.Entry<String, String> entry : mParsms.entrySet()) {
            stringBuffer.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
            value.add(entry.getValue());
        }
        value.add(TOKEN);// token
        String signature = generateSignature(value.toArray(new String[]{}));
        stringBuffer.append("signature=").append(signature);
        return stringBuffer.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String shaEncode(String inStr) {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        byte[] byteArray = inStr.getBytes(StandardCharsets.UTF_8);
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getSha1(String[] source) {
        Arrays.sort(source);
        StringBuilder builder = new StringBuilder();
        for (String str : source) {
            builder.append(str);
        }
        String newStr = builder.toString();
        return shaEncode(newStr);
    }

    /**
     * @param args
     * @return
     */
    public static String generateSignature(String... args) {
        return getSha1(args);
    }

}
