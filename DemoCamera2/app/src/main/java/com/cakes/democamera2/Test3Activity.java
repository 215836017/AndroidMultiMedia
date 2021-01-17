package com.cakes.democamera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.cakes.democamera2.codec.AvcEncoder;
import com.cakes.democamera2.codec.VideoConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Test3Activity extends BaseActivity {

    private final String TAG = "Test3Activity";

    private TextureView textureView;
    private Button btnEncoder;

    private HandlerThread cameraThread;
    private Handler cameraHandler;

    private ImageReader mImageReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test3);

        startCameraThread();

        textureView = findViewById(R.id.test_03_texture_view);
        btnEncoder = findViewById(R.id.test_03_btn_codec);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        btnEncoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEncoderClick();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != cameraThread) {
            cameraThread.quitSafely();
            try {
                cameraThread.join();
                cameraThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (null != cameraHandler) {
            cameraHandler.removeCallbacksAndMessages(null);
            cameraHandler = null;
        }

        if (cameraCaptureSession != null) {
            try {
//                cameraCaptureSession.abortCaptures();
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.close();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            cameraCaptureSession = null;
        }
    }

    private void startCameraThread() {
        cameraThread = new HandlerThread("camera2_codec");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private AvcEncoder avcEncoder;
    private boolean isEncoded;

    private void btnEncoderClick() {
        if (isEncoded) {
            isEncoded = false;
            btnEncoder.setText("开始编码");

            if (null != avcEncoder) {
                avcEncoder.stopThread();
            }
            return;

        } else {
            initFile();
            initVideoEncoder();
            avcEncoder.startEncoderThread(cameraIdForEncode);
            isEncoded = true;
            btnEncoder.setText("停止编码");
        }
    }

    private void initVideoEncoder() {
        if (null == mPreviewSize) {
            return;
        }
        if (null == avcEncoder) {
            avcEncoder = new AvcEncoder();
        }
        VideoConfiguration.Builder builder = new VideoConfiguration.Builder();
        builder.setSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        avcEncoder.setVideoConfiguration(builder.build());
    }

    private FileOutputStream yuvFos;

    private void initFile() {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testCamera2";
        File yuvFile = new File(dir + "/camera2_" + System.currentTimeMillis() + ".yuv");
        if (null != yuvFile) {
            File parentFile = yuvFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!yuvFile.exists()) {
                try {
                    yuvFile.createNewFile();
                    yuvFos = new FileOutputStream(yuvFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            initCameraManager();
            selectCamera(width, height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };

    private CameraManager cameraManager;

    private void initCameraManager() {
        if (null == cameraManager) {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        }
    }

    private String cameraId;
    private int WIDTH_DEFAULT = 640;
    private int HEIGHT_DEFAULT = 480;
    private Size mPreviewSize;
    public static final int CAMERA_ID_BACK = 0;
    public static final int CAMERA_ID_FRONT = 1;
    private int cameraIdForEncode;

    private void selectCamera(int width, int height) {
        if (null == cameraManager) {
            initCameraManager();
        }
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            if (null == cameraIdList || cameraIdList.length <= 0) {
                showToast(TAG, "not found an camera...");
                return;
            }

            CameraCharacteristics characteristics;
            for (String id : cameraIdList) {
                characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
//                if (CameraCharacteristics.LENS_FACING_BACK == facing) { // 后置摄像头
                if (CameraCharacteristics.LENS_FACING_FRONT == facing) {
                    cameraIdForEncode = CAMERA_ID_FRONT;
                    cameraId = id;
                    StreamConfigurationMap streamConfigurationMap = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mPreviewSize = chooseOptimalSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class));
                    break;
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size chooseOptimalSize(Size[] choices) {
        if (null == choices || choices.length <= 0) {
            return new Size(640, 480);
        }

        for (Size size : choices) {
            LogUtil.d(TAG, "chooseOptimalSize() width = " + size.getWidth()
                    + ", height = " + size.getHeight());
            if (size.getWidth() == 640 && size.getHeight() == 480) {
                return size;
            }
        }

        LogUtil.d(TAG, "chooseOptimalSize() -- width = " + choices[0].getWidth()
                + ", height = " + choices[0].getHeight());
        return choices[0];
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        if (TextUtils.isEmpty(cameraId)) {
            showToast(TAG, "cameraId is null");
            return;
        }

        try {
            cameraManager.openCamera(cameraId, openCameraStateCallback, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice cameraDevice;
    private CameraDevice.StateCallback openCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            showToast(TAG, "相机打开成功");
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    private CaptureRequest.Builder captureRequestBuilder;

    private void closePreviewSession() {
        if (null != cameraCaptureSession) {
            try {
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
    }

    private void initImageReader() {
        //创建图片读取器,参数为分辨率宽度和高度/图片格式/需要缓存几张图片,这里写的2意思是获取2张照片
        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
                ImageFormat.YUV_420_888, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                LogUtil.i(TAG, "onImageAvailable() --  11111111");
                processImage(reader);
            }
        }, cameraHandler);
    }

    private void processImage(ImageReader reader) {
        Image image = reader.acquireNextImage();
        if (!isEncoded) {
            LogUtil.i(TAG, "processImage() --  11111111, width = " + image.getWidth()
                    + ", height = " + image.getHeight());
            image.close();
            return;
        }
        LogUtil.i(TAG, "processImage() --  22222222222");
        //YUV_420_888格式 ---->获取到的三个通道分别对应YUV
//        int width = image.getWidth();
//        int height = image.getHeight();
//        Image.Plane[] planes = image.getPlanes();
//        int format = image.getFormat();

        // 1. 获取YUV
        byte[] data = getDataFromImage(image, COLOR_FormatI420);
        image.close();

//        if (isEncoded) {
//            try {
//                yuvFos.write(data);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        // 2. 旋转角度
        // 3. 转换为NV12
        // 4. YUV编码H264
        // 旋转，转换和编码都集中在AvcEncoder中了
        if (isEncoded) {
            avcEncoder.inputYUVToQueue(data);
        }
    }

    private void startPreview() {
        if (null == cameraDevice || !textureView.isAvailable()) {
            return;
        }
        closePreviewSession();

        showToast(TAG, "预览width：" + mPreviewSize.getWidth() + ", height：" + mPreviewSize.getHeight());
        SurfaceTexture mSurfaceTexture = textureView.getSurfaceTexture();        //surfaceTexture    需要手动释放
        assert mSurfaceTexture != null;
        // 设置预览大小
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        initImageReader();

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //  captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation); // 设置方向

            Surface previewSurface = new Surface(mSurfaceTexture);
            captureRequestBuilder.addTarget(previewSurface); //添加surface   实际使用中这个surface最好是全局变量 在onDestroy的时候mCaptureRequest.removeTarget(mSurface);清除,否则会内存泄露
            Surface yuvSurface = mImageReader.getSurface();
            captureRequestBuilder.addTarget(yuvSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, yuvSurface),
                    previewCaptureSessionStateCallback, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession cameraCaptureSession;

    private CameraCaptureSession.StateCallback previewCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            LogUtil.d(TAG, "previewCaptureSessionStateCallback-- onConfigured() -- 11111");
            //摄像头完成配置，可以处理Capture请求了。
            cameraCaptureSession = session;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            LogUtil.w(TAG, "previewCaptureSessionStateCallback-- onConfigured() -- 11111");
        }
    };

    private void updatePreview() {
        if (null == cameraDevice) {
            return;
        }
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                    null, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;
    private static Boolean VERBOSE = false;

    private boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888://是指yuv420这一系列中的某一个
            case ImageFormat.NV21:
            case ImageFormat.YV12://就是指NV12
                return true;
        }
        return false;
    }

    private byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }

        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        int totaolLength = ImageFormat.getBitsPerPixel(format);//12
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];//yuv一帧数据
        byte[] rowData = new byte[planes[0].getRowStride()];//一行数据
        if (VERBOSE) Log.v(TAG, "get data from " + planes.length + " planes");
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();//1440
            int pixelStride = planes[i].getPixelStride();//Y-->1 U/V-->2
            if (VERBOSE) {
                LogUtil.d(TAG, "pixelStride " + pixelStride);
                LogUtil.d(TAG, "rowStride " + rowStride);
                LogUtil.d(TAG, "width " + width);
                LogUtil.d(TAG, "height " + height);
                LogUtil.d(TAG, "buffer size " + buffer.remaining());
            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;//720
            int h = height >> shift;//540
            int position = rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift);//0
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));//buff位置从0开始
            //一行一行添加像素
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    //存入Y数据
                    length = w;//一行数据的长度
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;//1439 偶数位都是像素 所以只要到长度的最后一个字节即可
                    buffer.get(rowData, 0, length);//保存这一行数据 取了lenght长后 下次取数据的时候将从lenht位置开始
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];//也就是说每隔一个字节才是像素  也就是偶数位置的都是像素
                        channelOffset += outputStride;//下一个像素
                    }
                }
                if (row < h - 1) {
                    int position2 = buffer.position();
                    //下一次取出数据从那个位置开始
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            if (VERBOSE) Log.v(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }


}