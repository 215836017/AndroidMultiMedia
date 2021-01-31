package com.test.demovideo.record;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaConstant {

    public static final String DEFAULT_VIDEO_SUFFIX = ".mp4";

    private static long mLastDate;
    // Number of names generated for the same second.
    private static int mSameSecondCount;

    public static String generateImgName(boolean isImage, long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat mFormat = null;
        mFormat = new SimpleDateFormat("'video'_yyyyMMdd_HHmmss");
        String result = mFormat.format(date);
        // If the last name was generated for the same second,
        // we append _1, _2, etc to the name.
        if (dateTaken / 1000 == mLastDate / 1000) {
            mSameSecondCount++;
            result += "_" + mSameSecondCount;
        } else {
            mLastDate = dateTaken;
            mSameSecondCount = 0;
        }
        return result;
    }
}
