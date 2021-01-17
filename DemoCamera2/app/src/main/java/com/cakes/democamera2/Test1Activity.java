package com.cakes.democamera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

// https://www.cnblogs.com/guanxinjing/p/10940049.html

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Test1Activity extends AppCompatActivity {

    private final String TAG = "Test1Activity";

    private TextureView textureView;

    private HandlerThread cameraThread;
    private Handler cameraHandler;

    private CameraManager cameraManager;
    private ImageReader mImageReader;

    private String mCameraId;
    private CameraCharacteristics mCharacteristics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);

        findViewById(R.id.test_01_btn_take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        textureView = findViewById(R.id.test_01_texture_view);

        initPermission();
        startCameraThread();
        initImageReader();

        textureView.setSurfaceTextureListener(surfaceTextureListener);
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

        if (null != mSurfaceTexture) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        if (null != mSurface) {
            mSurface.release();
            mSurface = null;
        }

        if (mCameraCaptureSession != null) {
            try {
                mCameraCaptureSession.stopRepeating();
                mCameraCaptureSession.abortCaptures();
                mCameraCaptureSession.close();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCameraCaptureSession = null;
        }
    }

    private void takePicture() {

    }

    private void initPermission() {
        // todo 动态获取权限
    }

    private void startCameraThread() {
        cameraThread = new HandlerThread("cameraThread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private FileOutputStream photoFileOutputStream;

    private void initPhotoFile() {
        File path = new File(getExternalCacheDir().getPath());
        if (!path.exists()) {
            Log.e(TAG, "onImageAvailable: 路径不存在");
            path.mkdirs();
        } else {
            Log.e(TAG, "onImageAvailable: 路径存在");
        }
        File file = new File(path, "demo.jpg");
        try {
            photoFileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initImageReader() {
        //创建图片读取器,参数为分辨率宽度和高度/图片格式/需要缓存几张图片,这里写的2意思是获取2张照片
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
//        image.acquireLatestImage();//从ImageReader的队列中获取最新的image,删除旧的
//        image.acquireNextImage();//从ImageReader的队列中获取下一个图像,如果返回null没有新图像可用
                Image image = reader.acquireNextImage();
//        这里的image.getPlanes()[0]其实是图层的意思,因为我的图片格式是JPEG只有一层所以是geiPlanes()[0],如果你是其他格式(例如png)的图片会有多个图层,就可以获取指定图层的图像数据　　　　　　　
                ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                if (null != photoFileOutputStream) {
                    try {
                        photoFileOutputStream.write(bytes);
                        photoFileOutputStream.flush();
                        photoFileOutputStream.close();
                    } catch (Exception e) {
                    }
                }

                image.close();
            }
        }, cameraHandler);
    }

    private void initCameraManager() {
        if (null == cameraManager) {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        }
    }

    private void selectCamera() {
        if (null == cameraManager) {
            showToast("null = CameraManager");
            return;
        }


        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            if (null == cameraIdList || cameraIdList.length <= 0) {
                showToast("cameraIdList is empty...");
                return;
            }

            CameraCharacteristics characteristics;
            for (String cameraId : cameraIdList) {
                LogUtil.i(TAG, "selectCamera() -- cameraId = " + cameraId);
                characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (CameraCharacteristics.LENS_FACING_BACK == facing) {
                    // 后置摄像头
                    mCameraId = cameraId;
                    mCharacteristics = characteristics;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        if (TextUtils.isEmpty(mCameraId)) {
            showToast("没有找到后置相机");
            return;
        }

        try {
            cameraManager.openCamera(mCameraId, stateCallback, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice cameraDevice;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private CaptureRequest.Builder mCaptureRequest;

    private void doForOpenCamera(CameraDevice camera) {
        cameraDevice = camera;

        mSurfaceTexture = textureView.getSurfaceTexture();        //surfaceTexture    需要手动释放
        Size matchingSize = getMatchingSize();
        mSurfaceTexture.setDefaultBufferSize(matchingSize.getWidth(), matchingSize.getHeight());//设置预览的图像尺寸
        mSurface = new Surface(mSurfaceTexture);//surface最好在销毁的时候要释放,surface.release();
//                       CaptureRequest可以完全自定义拍摄参数,但是需要配置的参数太多了,所以Camera2提供了一些快速配置的参数,如下:
// 　　　　　　　　　      TEMPLATE_PREVIEW ：预览
//                       TEMPLATE_RECORD：拍摄视频
//                        TEMPLATE_STILL_CAPTURE：拍照
//                        TEMPLATE_VIDEO_SNAPSHOT：创建视视频录制时截屏的请求
//                        TEMPLATE_ZERO_SHUTTER_LAG：创建一个适用于零快门延迟的请求。在不影响预览帧率的情况下最大化图像质量。
//                        TEMPLATE_MANUAL：创建一个基本捕获请求，这种请求中所有的自动控制都是禁用的(自动曝光，自动白平衡、自动焦点)。
        try {
            mCaptureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);//创建预览请求
            mCaptureRequest.addTarget(mSurface); //添加surface   实际使用中这个surface最好是全局变量 在onDestroy的时候mCaptureRequest.removeTarget(mSurface);清除,否则会内存泄露
            mCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);//自动对焦

            /**
             * 创建获取会话
             * 这里会有一个容易忘记的坑,那就是Arrays.asList(surface, mImageReader.getSurface())这个方法
             * 这个方法需要你导入后面需要操作功能的所有surface,比如预览/拍照如果你2个都要操作那就要导入2个
             * 否则后续操作没有添加的那个功能就报错surface没有准备好,这也是我为什么先初始化ImageReader的原因,因为在这里就可以拿到ImageReader的surface了
             */
            cameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), captureSession, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private Size getMatchingSize() {
        Size selectSize = null;
        float selectProportion = 0;
        float viewProportion = (float) textureView.getWidth() / (float) textureView.getHeight();
        StreamConfigurationMap streamConfigurationMap = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] sizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
        for (int i = 0; i < sizes.length; i++) {
            Size itemSize = sizes[i];
            float itemSizeProportion = (float) itemSize.getHeight() / (float) itemSize.getWidth();
            float differenceProportion = Math.abs(viewProportion - itemSizeProportion);
            LogUtil.i(TAG, "相减差值比例=" + differenceProportion);
            if (i == 0) {
                selectSize = itemSize;
                selectProportion = differenceProportion;
                continue;
            }
            if (differenceProportion <= selectProportion) {
                if (differenceProportion == selectProportion) {
                    if (selectSize.getWidth() + selectSize.getHeight() < itemSize.getWidth() + itemSize.getHeight()) {
                        selectSize = itemSize;
                        selectProportion = differenceProportion;
                    }

                } else {
                    selectSize = itemSize;
                    selectProportion = differenceProportion;
                }
            }
        }

        Log.e(TAG, "getMatchingSize: 选择的比例是=" + selectProportion);
        Log.e(TAG, "getMatchingSize: 选择的尺寸是 宽度=" + selectSize.getWidth() + "高度=" + selectSize.getHeight());
        return selectSize;
    }

    private void showToast(String msg) {
        LogUtil.i(TAG, "showToast() -- msg = " + msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            LogUtil.d(TAG, "onSurfaceTextureAvailable() -- width = " + width + ", height = " + height);
            initCameraManager();
            selectCamera();
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            LogUtil.d(TAG, "onSurfaceTextureSizeChanged() -- width = " + width + ", height = " + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            showToast("相机打开成功");

            doForOpenCamera(camera);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    private CameraCaptureSession mCameraCaptureSession;  //获取数据会话类

    /**
     * 摄像头获取会话状态回调
     */
    private CameraCaptureSession.StateCallback captureSession = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            //摄像头完成配置，可以处理Capture请求了。

            mCameraCaptureSession = session;
            //注意这里使用的是 setRepeatingRequest() 请求通过此捕获会话无休止地重复捕获图像。用它来一直请求预览图像
            try {
                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest.build(), mSessionCaptureCallback, cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    };

    /**
     * 摄像头获取会话数据回调
     */
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult
                partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//                Log.e(TAG, "onCaptureCompleted: 触发接收数据");
//                Size size = request.get(CaptureRequest.JPEG_THUMBNAIL_SIZE);

        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
        }
    };
}