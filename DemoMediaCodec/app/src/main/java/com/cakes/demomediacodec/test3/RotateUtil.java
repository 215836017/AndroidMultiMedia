package com.cakes.demomediacodec.test3;

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

    /**
     * 此处为顺时针旋转旋转90度
     *
     * @param data        旋转前的数据
     * @param imageWidth  旋转前数据的宽
     * @param imageHeight 旋转前数据的高
     * @return 旋转后的数据
     */
    public static byte[] rotateNV290(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
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
