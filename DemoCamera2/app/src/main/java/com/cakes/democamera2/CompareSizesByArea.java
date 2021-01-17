package com.cakes.democamera2;

import android.os.Build;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.util.Comparator;

class CompareSizesByArea implements Comparator<Size> {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int compare(Size lhs, Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    }

}