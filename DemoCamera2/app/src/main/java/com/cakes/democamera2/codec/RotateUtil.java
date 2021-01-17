package com.cakes.democamera2.codec;

public class RotateUtil {

    public static byte[] flip(byte[] src, int width, int height) {
        for (int i = 0; i < height; i++) {
            int left = 0;
            int right = width - 1;
            while (left < right) {
                byte b = src[i * width + right];
                src[i * width + right] = src[i * width + left];
                src[i * width + left] = b;
                left++;
                right--;
            }
        }
        int uHeader = width * height;
        int uHeight = height / 2;
        for (int i = 0; i < uHeight; i++) {
            int left = 0;
            int right = width - 2;
            while (left < right) {
                byte b = src[uHeader + i * width + right];
                src[uHeader + i * width + right] = src[uHeader + i * width + left];
                src[uHeader + i * width + left] = b;
                b = src[uHeader + i * width + right + 1];
                src[uHeader + i * width + right + 1] = src[uHeader + i * width + left + 1];
                src[uHeader + i * width + left + 1] = b;
                left += 2;
                right -= 2;
            }
        }
        return src;

    }

    public static byte[] rotate270(byte[] src, int width, int height) {
        byte[] dest = new byte[src.length];
        int index = 0;
        for (int i = width - 1; i >= 0; i--) {
            for (int j = 0; j < height; j++) {
                dest[index++] = src[j * width + i];
            }
        }
        int uHeader = index;
        int uHeight = height / 2;
        for (int i = width - 2; i >= 0; i -= 2) {
            for (int j = 0; j < uHeight; j++) {
                dest[index++] = src[uHeader + j * width + i];
                dest[index++] = src[uHeader + j * width + i + 1];
            }
        }
        return dest;
    }


    public static byte[] rotate90(byte[] src, int width, int height) {
        byte[] dest = new byte[src.length];
        int index = 0;
        for (int i = 0; i < width; i++) {
            for (int j = height - 1; j >= 0; j--) {
                dest[index++] = src[j * width + i];
            }
        }
        int uHeader = index;
        int uHeight = height / 2;
        for (int i = 0; i < width; i += 2) {
            for (int j = uHeight - 1; j >= 0; j--) {
                dest[index++] = src[uHeader + j * width + i];
                dest[index++] = src[uHeader + j * width + i + 1];
            }
        }
        return dest;
    }

    public static byte[] rotate180(byte[] src, int width, int height) {
        int top = 0;
        int bottom = height - 1;
        while (top < bottom) {
            for (int i = 0; i < width; i++) {
                byte b = src[bottom * width + width - 1 - i];
                src[bottom * width + width - 1 - i] = src[top * width + i];
                src[top * width + i] = b;
            }
            top++;
            bottom--;
        }
        int uHeader = width * height;
        top = 0;
        bottom = height / 2 - 1;
        while (top < bottom) {
            for (int i = 0; i < width; i += 2) {
                byte b = src[uHeader + bottom * width + width - 2 - i];
                src[uHeader + bottom * width + width - 2 - i] = src[uHeader + top * width + i];
                src[uHeader + top * width + i] = b;

                b = src[uHeader + bottom * width + width - 1 - i];
                src[uHeader + bottom * width + width - 1 - i] = src[uHeader + top * width + i + 1];
                src[uHeader + top * width + i + 1] = b;
            }
            top++;
            bottom--;
        }
        return src;
    }

}
