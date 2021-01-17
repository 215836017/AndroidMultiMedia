package com.cakes.democamera2.video;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaConstant {

    public static final String MEDIA_DIR_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "testCamera2/";

    public static final String PHOTO_SUFFIX = ".jpg";

    public static final String VIDEO_BASE_URI = "content://media/external/video/media";
    public static final String DEFAULT_VIDEO_SUFFIX = ".mp4";
    public static final String DEFAULT_VIDEO_MIME_TYPE = "video/mp4";

    private static long mLastDate;
    // Number of names generated for the same second.
    private static int mSameSecondCount;

    public static String generateImgName(boolean isImage, long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat mFormat = null;
        if (isImage) {
            mFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        } else {
            mFormat = new SimpleDateFormat("'VID'_yyyyMMdd_HHmmss");
        }
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
