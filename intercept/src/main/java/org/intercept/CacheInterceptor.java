package org.intercept;

import android.content.Context;
import android.text.TextUtils;

import org.cache.CacheManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 缓存拦截器
 *
 * head{cache:$value} value is true 优先缓存  false 优先网络
 */
public class CacheInterceptor implements Interceptor {

    // 默认优先缓存
    private PriorityStrategy mStrategy;

    private static final PriorityStrategy CACHE_STRATEGY = new CacheStrategy();
    private static final PriorityStrategy ONLINE_STRATEGY = new OnLineStrategy();

    public static void debug(boolean isDebug) {
        Util.debug(isDebug);
    }


    public CacheInterceptor(Context context) {
        this(context, 20 * 1024 * 1024L, "http_cache");
    }

    /**
     * 默认网络请求
     *
     * @param size 缓存大小
     */
    public CacheInterceptor(Context context, long size) {
        this(context, size, "http_cache");
    }

    public CacheInterceptor(Context context, long size, String cacheName) {
        CacheManager.getInstance().init(context, size, cacheName);
    }


    @Override
    public Response intercept(final Chain chain) throws IOException {
        Request request = chain.request();
        String cacheHead = request.header("cache");
        // 优先缓存
        if (TextUtils.isEmpty(cacheHead) || "true".equals(cacheHead)) {
            mStrategy = CACHE_STRATEGY;
        } else {
            mStrategy = ONLINE_STRATEGY;
        }
        return mStrategy.response(chain);
    }


}
