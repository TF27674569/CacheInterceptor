package org.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.cache.lru.DiskLruCache;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CacheManager {
    private static final String TAG = "CacheManager";

    private long mCacheSize = 20 * 1024 * 1024L;
    private String mCacheDirName = "http";
    private DiskLruCache mDiskLruCache;
    private static volatile CacheManager mInstance;

    private CacheManager() {

    }

    public static CacheManager getInstance() {
        if (mInstance == null) {
            synchronized (CacheManager.class) {
                if (mInstance == null) {
                    mInstance = new CacheManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        init(context, mCacheSize, mCacheDirName);
    }


    /**
     * 初始化
     *
     * @param context   上下文
     * @param size      缓存大小
     * @param cacheName 缓存目录名
     */
    public void init(Context context, long size, String cacheName) {
        mCacheSize = size;
        mCacheDirName = cacheName;
        File diskCacheDir = getDiskCacheDir(context, cacheName);
        // 不存在，创建一个新的
        if (!diskCacheDir.exists()) {
            boolean b = diskCacheDir.mkdirs();
            Log.d(TAG, "init: 缓存路径不存在，创建新的缓存路径是否成功->" + b + "  缓存路径：" + diskCacheDir.getAbsolutePath());
        }

        // 可用空间大于缓存空间才给与缓存
        if (diskCacheDir.getUsableSpace() > mCacheSize) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, getVersion(context), 1, mCacheSize);

                Log.d(TAG, "init:DiskLruCache  open success !");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "init:DiskLruCache  open fail e:" + e.getMessage());
            }
        } else {
            Log.d(TAG, "缓存空间不够" + mCacheSize);
        }
    }


    public void put(String key, String value) {
        if (mDiskLruCache != null) {
            OutputStream os = null;
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(encryptMD5(key));
                os = editor.newOutputStream(0);
                os.write(value.getBytes());
                os.flush();
                editor.commit();
                mDiskLruCache.flush();
                Log.d(TAG, "缓存成功：" + value);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "缓存失败：" + value);
            } finally {
                close(os);
            }
        }
    }


    public String get(String key) {
        if (mDiskLruCache != null) {
            FileInputStream fis = null;
            ByteArrayOutputStream bos = null;
            try {
                DiskLruCache.Snapshot snapshot = mDiskLruCache.get(encryptMD5(key));
                if (snapshot != null) {
                    fis = (FileInputStream) snapshot.getInputStream(0);
                    bos = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];

                    int len;
                    while ((len = fis.read(buf)) != -1) {
                        bos.write(buf, 0, len);
                    }

                    byte[] data = bos.toByteArray();
                    String value = new String(data);
                    Log.d(TAG, "读取成功: key->" + key + "  value->" + value);
                    return value;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Log.d(TAG, "读取失败: key->" + key + "  msg->" + e.getMessage());
            } finally {
                close(fis, bos);
            }
        } else {
            Log.d(TAG, "mDiskLruCache==null  key->" + key);
        }
        return "";
    }

    public boolean remove(String key) {
        if (this.mDiskLruCache != null) {
            try {
                return mDiskLruCache.remove(encryptMD5(key));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void delete(Context context) {
        File diskCacheDir = getDiskCacheDir(context, "responses");
        deleteContents(diskCacheDir);
    }

    private void deleteContents(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteContents(file);
                } else {
                    file.delete();
                }
            }
        }
    }


    private int getVersion(Context context) {
        try {
            PackageInfo info = context.getApplicationContext().getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 缓存目录名称
     *
     * @param context
     * @param uniqueName
     * @return
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }


    /**
     * MD5加密
     */
    private static String encryptMD5(String string) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            int length = hash.length;
            for (int i = 0; i < length; ++i) {
                byte b = hash[i];
                if ((b & 255) < 16) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 255));
            }
            return hex.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return string;
        }
    }


    private void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 是否可用缓存功能
     */
    public boolean isUseCache() {
        return mDiskLruCache != null;
    }
}
