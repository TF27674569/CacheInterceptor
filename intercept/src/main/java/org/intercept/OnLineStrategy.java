package org.intercept;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 优先网络
 */
public class OnLineStrategy extends PriorityStrategy {

    @Override
    public Response response(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        Response response;
        try {
            String data = getOnLineDataAndCache(chain);
            response = getResponseFromData(request, data, "on line : data form net work!");
        } catch (Throwable e) {
            Util.logger("失败，读取缓存 " + e.getMessage());
            String cacheData = getCacheData(request);
            Util.logger("缓存-> " + cacheData);
            // 如果请求失败，并且没有缓存，则将异常抛出
            if (TextUtils.isEmpty(cacheData)) {
                throw e;
            }
            response = getResponseFromData(request, cacheData, "on line :data form disk!");
        }
        return response;
    }
}
