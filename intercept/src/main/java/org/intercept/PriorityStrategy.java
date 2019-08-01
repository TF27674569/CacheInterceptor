package org.intercept;

import org.cache.CacheManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class PriorityStrategy {

    public abstract Response response(Interceptor.Chain chain) throws IOException;

    /**
     * string -> Response
     */
    protected Response getResponseFromData(Request request, String data, String message) {
        return new Response.Builder()
                .code(200)
                .body(ResponseBody.create(null, data))
                .request(request)
                .message(message)
                .protocol(Protocol.HTTP_1_0)
                .build();
    }

    /**
     * 缓存数据
     */
    protected String getCacheData(Request request) {
        String url = request.url().url().toString();
        return CacheManager.getInstance().get(url);
    }

    /**
     * 获取网络数据并且缓存
     */
    protected String getOnLineDataAndCache(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String responseStr = responseBody.string();
            CacheManager.getInstance().put(request.url().toString(), responseStr);
            return responseStr;
        }
        return "";
    }
}
