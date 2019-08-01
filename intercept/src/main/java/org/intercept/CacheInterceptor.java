package org.intercept;

import android.content.Context;

import org.cache.CacheManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;


/**
 * 缓存拦截器
 */
public class CacheInterceptor implements Interceptor {

    // 默认优先缓存
    private PriorityStrategy mStrategy;

    public static void debug(boolean isDebug) {
        Util.debug(isDebug);
    }

    /**
     * 默认网络请求
     */
    public CacheInterceptor(Context context) {
        this(context, true);
    }

    /**
     * @param priorityCache true 优先缓存  false 优先网络
     */
    public CacheInterceptor(Context context, boolean priorityCache) {
        this(context, priorityCache, 20 * 1024 * 1024L, "http_cache");
    }

    /**
     * 默认网络请求
     *
     * @param size 缓存大小
     */
    public CacheInterceptor(Context context, long size) {
        this(context, true, size, "http_cache");
    }

    /**
     * @param priorityCache true 优先缓存  false 优先网络
     */
    public CacheInterceptor(Context context, boolean priorityCache, long size, String cacheName) {
        CacheManager.getInstance().init(context, size, cacheName);
        mStrategy = priorityCache ? new CacheStrategy() : new OnLineStrategy();
    }


    @Override
    public Response intercept(final Chain chain) throws IOException {
        return mStrategy.response(chain);
    }


}
