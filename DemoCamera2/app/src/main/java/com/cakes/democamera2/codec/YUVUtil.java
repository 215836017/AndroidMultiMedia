package com.cakes.democamera2.codec;

public class YUVUtil {

    //水平镜像
    public static void yuv_flip_horizontal(byte[] des, byte[] src, int width, int height) {
        int n = 0;
        int hw = width / 2;
        int hh = height / 2;
        //copy y
        for (int j = 0; j < height; j++) {
            for (int i = width - 1; i >= 0; i--) {
                des[n++] = src[width * j + i];
            }
        }

        //copy u
        for (int j = 0; j < hh; j++) {
            for (int i = hw - 1; i >= 0; i--) {
                des[n++] = src[width * height + hw * j + i];//ptemp[hw * j + i];
            }
        }

        //copy v
        for (int j = 0; j < hh; j++) {
            for (int i = hw - 1; i >= 0; i--) {
                des[n++] = src[width * height + width * height / 4 + hw * j + i];//ptemp[hw * j + i];
            }
        }
    }

    //前置摄像头需要逆时针旋转90即 270顺
    public static void yuv_rotate_270(byte[] des, byte[] src, int width, int height) {
        int n = 0;
        int hw = width / 2;
        int hh = height / 2;
        //copy y
        for (int j = width; j > 0; j--) {
            for (int i = 0; i < height; i++) {
                des[n++] = src[width * i + j];
            }
        }

        //copy u
        for (int j = hw - 1; j >= 0; j--) {
            for (int i = 0; i < hh; i++) {
                des[n++] = src[width * height + hw * i + j]; //ptemp[hw * i + j];
            }
        }

        //copy v
        for (int j = hw - 1; j >= 0; j--) {
            for (int i = 0; i < hh; i++) {
                des[n++] = src[width * height + width * height / 4 + hw * i + j];//ptemp[hw * i + j];
            }
        }
    }

    ////用户后置摄像头 将yuv420p旋转90度
    public static void yuv_rotate_90(byte[] des, byte[] src, int width, int height) {
        int n = 0;
        int hw = width / 2;//u 应该看成只有Y的1/4的小长方形
        int hh = height / 2;
        //copy y
        for (int j = 0; j < width; j++) {
            for (int i = height - 1; i >= 0; i--) {
                des[n++] = src[width * i + j];
            }
        }

        //copy u
        for (int j = 0; j < hw; j++) {
            for (int i = hh - 1; i >= 0; i--) {

                des[n++] = src[width * height + hw * i + j];//ptemp[ hw*i + j ];
            }
        }

        //copy v
        for (int j = 0; j < hw; j++) {
            for (int i = hh - 1; i >= 0; i--) {

                des[n++] = src[width * height + width * height / 4 + hw * i + j];//ptemp[hw*i + j];
            }
        }
    }


    public static byte[] test(byte[] source, int width, int height) {
        byte[] data3 = new byte[width * height * 3 / 2];
        for (int i = 0; i < width * height; i++) {
            data3[i] = source[i];
        }

        //yyyyyyyy uvuv NV12
        for (int i = 0; i < width * height / 2; i++) {
            data3[width * height + i] = source[width * height + i / 2];//存入U
            i++;
            data3[width * height + i] = source[width * height + (width * height / 4) + (i - 1) / 2];
        }

        return data3;
    }

}
