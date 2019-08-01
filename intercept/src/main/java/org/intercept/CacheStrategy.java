package org.intercept;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 优先缓存
 */
public class CacheStrategy extends PriorityStrategy {

    @Override
    public Response response(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        String response = "";
        try {
            // 优先从缓存读取
            response = getCacheData(request);
            if (!TextUtils.isEmpty(response)) {
                Util.logger("读取缓存：" + response);
                return getResponseFromData(request, response, "disk: data form disk!");
            }
        } finally {
            // 没有缓存从网络获取
            // 有缓存这里网络更新数据
            Util.logger("finally：网络获取");
            try {
                response = getOnLineDataAndCache(chain);
            } catch (IOException e) {
                if (TextUtils.isEmpty(response)) {
                    throw e;
                }
            }
        }
        return getResponseFromData(request, response, "disk: data form net work!");
    }
}
