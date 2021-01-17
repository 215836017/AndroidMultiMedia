package com.test.demoaudio.utils;

public class TimeUtil {

    public static String getTime(int time) {
        int minute;
        int second;
        String strMinute;
        String strSecond;

        String strTime = "";

        if (time >= 0 && time < 10) {
            strTime = "00:0" + time;

        } else if (time >= 10) {
            minute = time / 60;
            second = time % 60;

            strMinute = minute + "";
            if (minute < 10) {
                strMinute = "0" + minute;
            }

            strSecond = second + "";
            if (second < 10) {
                strSecond = "0" + second;
            }

            strTime = strMinute + ":" + strSecond;
        }

        return strTime;
    }
}
