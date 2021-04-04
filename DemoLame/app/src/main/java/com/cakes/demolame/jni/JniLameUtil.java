package com.cakes.demolame.jni;

public class JniLameUtil {

    static {
        System.loadLibrary("helloLame");
    }

    public static native String getLameVersion();

    public static native void startEncode(String wavPath, String mp3String);
}
