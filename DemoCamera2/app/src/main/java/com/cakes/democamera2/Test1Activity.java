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
import android.os.Environment;
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

// https://www.cnblogs.com/guanxinjing/p/11009192.html
// https://cloud.tencent.com/developer/article/1650043

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
        try {
            mCameraCaptureSession.stopRepeating();//停止重复   取消任何正在进行的重复捕获集 在这里就是停止画面预览，拍照结束后画面定格，不会再预览

            /*  mCameraCaptureSession.abortCaptures(); //终止获取   尽可能快地放弃当前挂起和正在进行的所有捕获。
             * 这里有一个坑,其实这个并不能随便调用(我是看到别的demo这么使用,但是其实是错误的,所以就在这里备注这个坑).
             * 最好只在Activity里的onDestroy调用它,终止获取是耗时操作,需要一定时间重新打开会话通道.
             * 在这个demo里我并没有恢复预览,如果你调用了这个方法关闭了会话又拍照后恢复图像预览,会话就会频繁的开关,
             * 导致拍照图片在处理耗时缓存时你又关闭了会话.导致照片缓存不完整并且失败.
             * 所以切记不要随便使用这个方法,会话开启后并不需要关闭刷新.后续其他拍照/预览/录制视频直接操作这个会话即可
             */
            CaptureRequest.Builder captureRequestBuilder = null;
            captureRequestBuilder = cameraDevice.createCaptureRequest(cameraDevice.TEMPLATE_STILL_CAPTURE);
            // 设置自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 设置自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向,如果你的app有提供横屏和竖屏,那么就需要下面的方法来控制照片为竖立状态
            // int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // Log.e(TAG, "takePicture: 手机方向="+rotation);
            // Log.e(TAG, "takePicture: 照片方向="+ORIENTATIONS.get(rotation));
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270);//这里直接写死270度，将照片竖立

            Surface captureSurface = mImageReader.getSurface();
            captureRequestBuilder.addTarget(captureSurface);
            CaptureRequest captureRequest = captureRequestBuilder.build();

            // 经测试，capture()里面的 CaptureCallback 有没有都可以，拍照的数据在mImageReader的回调里面
            mCameraCaptureSession.capture(captureRequest, takePictureCaptureCallback, cameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback takePictureCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            LogUtil.i(TAG, "takePictureCaptureCallback -- onCaptureStarted() -- 11111111");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult
                partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            LogUtil.d(TAG, "takePictureCaptureCallback -- onCaptureProgressed() -- 11111111");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//                Log.e(TAG, "onCaptureCompleted: 触发接收数据");
            Size size = request.get(CaptureRequest.JPEG_THUMBNAIL_SIZE);

            // 预览时会一直打印
            LogUtil.w(TAG, "takePictureCaptureCallback -- onCaptureCompleted() -- 11111111, width = " + size.getHeight()
                    + ", height = " + size.getHeight());
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            LogUtil.i(TAG, "takePictureCaptureCallback -- onCaptureFailed() -- 11111111");
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            LogUtil.d(TAG, "takePictureCaptureCallback -- onCaptureSequenceCompleted() -- 11111111");
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            LogUtil.w(TAG, "takePictureCaptureCallback -- onCaptureSequenceAborted() -- 11111111");
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            LogUtil.i(TAG, "takePictureCaptureCallback -- onCaptureBufferLost() -- 11111111");
        }
    };

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
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/camera2/";
        File dirFile = new File(dirPath);
        if (null != dirFile) {
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
        }
        File file = new File(dirFile, "camera2_" + System.currentTimeMillis() + ".jpg");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            photoFileOutputStream = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initImageReader() {
        //创建图片读取器,参数为分辨率宽度和高度/图片格式/需要缓存几张图片,这里写的2意思是获取2张照片
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                LogUtil.i(TAG, "onImageAvailable() --  11111111");
//        image.acquireLatestImage();//从ImageReader的队列中获取最新的image,删除旧的
//        image.acquireNextImage();//从ImageReader的队列中获取下一个图像,如果返回null没有新图像可用
                Image image = reader.acquireNextImage();
//        这里的image.getPlanes()[0]其实是图层的意思,因为我的图片格式是JPEG只有一层所以是geiPlanes()[0],如果你是其他格式(例如png)的图片会有多个图层,就可以获取指定图层的图像数据　　　　　　　
                ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                initPhotoFile();
                if (null != photoFileOutputStream) {
                    LogUtil.i(TAG, "onImageAvailable() --  2222");
                    try {
                        LogUtil.i(TAG, "onImageAvailable() --  3333");
                        photoFileOutputStream.write(bytes);
                        photoFileOutputStream.flush();
                        photoFileOutputStream.close();
                    } catch (Exception e) {
                        LogUtil.e(TAG, "onImageAvailable() -- error: " + e.getMessage());
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

        LogUtil.e(TAG, "getMatchingSize: 选择的比例是=" + selectProportion);
        LogUtil.e(TAG, "getMatchingSize: 选择的尺寸是 宽度=" + selectSize.getWidth() + "高度=" + selectSize.getHeight());
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
                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest.build(),
                        mSessionCaptureCallback, cameraHandler);
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
            // 预览时会一直打印
//            LogUtil.i(TAG, "mSessionCaptureCallback -- onCaptureStarted() -- 11111111");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult
                partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            LogUtil.d(TAG, "mSessionCaptureCallback -- onCaptureProgressed() -- 11111111");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//                Log.e(TAG, "onCaptureCompleted: 触发接收数据");
//            Size size = request.get(CaptureRequest.JPEG_THUMBNAIL_SIZE);

            // 预览时会一直打印
//            LogUtil.w(TAG, "mSessionCaptureCallback -- onCaptureCompleted() -- 11111111, width = " + size.getHeight()
//                    + ", height = " + size.getHeight());
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            LogUtil.i(TAG, "onCaptureFailed() -- 11111111");
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            LogUtil.d(TAG, "mSessionCaptureCallback -- onCaptureSequenceCompleted() -- 11111111");
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            LogUtil.w(TAG, "mSessionCaptureCallback -- onCaptureSequenceAborted() -- 11111111");
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            LogUtil.i(TAG, "mSessionCaptureCallback -- onCaptureBufferLost() -- 11111111");
        }
    };
}