package com.test.demoaudio.utils;

import android.util.Log;

public class LogUtil {

    private static boolean open = true;

    public static void isOpen(boolean isOpen) {
        open = isOpen;
    }

    private static final String log_flag = "Audio_log ";

    public static void d(String tag, String msg) {
        if (open) {
            Log.d(tag, log_flag + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (open) {
            Log.w(tag, log_flag + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (open) {
            Log.e(tag, log_flag + msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (open) {
            Log.e(tag, log_flag + msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if (open) {
            Log.i(tag, log_flag + msg);
        }
    }
}