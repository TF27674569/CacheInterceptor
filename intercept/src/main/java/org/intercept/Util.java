package org.intercept;

import android.util.Log;

public class Util {
    private static final String TAG = "Cache Http";
    private static boolean sDebug = false;


    static void debug(boolean debug) {
        sDebug = debug;
    }

    static void logger(String msg) {
        if (sDebug) {
            Log.d(TAG, msg);
        }
    }
}
