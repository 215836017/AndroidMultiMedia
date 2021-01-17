package com.cakes.democamera2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cakes.democamera2.video.MediaRecorderManager;
import com.cakes.democamera2.video.OnVideoRecordListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Test2Activity extends AppCompatActivity {

    private final String TAG = "Test2Activity";

    private TextureView textureView;
    private Button btnRecord;

    private HandlerThread cameraThread;
    private Handler cameraHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        initCameraThread();

        textureView = findViewById(R.id.test_02_texture_view);
        textureView.setSurfaceTextureListener(surfaceTextureListener);

        btnRecord = findViewById(R.id.test_02_btn_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnToRecord();
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
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.abortCaptures();
                cameraCaptureSession.close();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            cameraCaptureSession = null;
        }
    }

    private void initCameraThread() {
        cameraThread = new HandlerThread("camera2Thread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private MediaRecorderManager mediaRecorderManager;

    private void btnToRecord() {
        if (null != mediaRecorderManager && mediaRecorderManager.isRecording()) {
            stopRecord();
            return;
        }

        if (null == cameraCaptureSession) {
            showToast("null == cameraCaptureSession");
            return;
        }

        closePreviewSession();
        if (null == mediaRecorderManager) {
            mediaRecorderManager = new MediaRecorderManager(this, onVideoRecordListener);
        }

        LogUtil.d(TAG, "btnToRecord() -- videoWidth = " + mVideoSize.getWidth()
                + ", videoHeight = " + mVideoSize.getHeight());
        mediaRecorderManager.configVideoRecorder(mVideoSize.getWidth(), mVideoSize.getHeight());

        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(surfaceTexture);
            surfaces.add(previewSurface);
            captureRequestBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface recorderSurface = mediaRecorderManager.getSurface();
            surfaces.add(recorderSurface);
            captureRequestBuilder.addTarget(recorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            cameraDevice.createCaptureSession(surfaces,
                    recordCaptureSessionStateCallback, cameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        startPreview();
        if (null != mediaRecorderManager) {
            mediaRecorderManager.stopRecordVideo();
        }
        btnRecord.setText("开始录像");
    }

    private OnVideoRecordListener onVideoRecordListener = new OnVideoRecordListener() {
        @Override
        public void onVideoRecordMaxDurationReached() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("时间到了，录像结束");
                    stopRecord();
                }
            });
        }

        @Override
        public void onVideoRecord(String videoFilePath) {
            LogUtil.d(TAG, "OnVideoRecordListener -- onVideoRecord() -- videoFilePath = " + videoFilePath);
        }

        @Override
        public void onVideoRecordError() {
            LogUtil.w(TAG, "onVideoRecordError() ...");
        }
    };
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            LogUtil.d(TAG, "onSurfaceTextureAvailable() -- width = " + width + ", height = " + height);
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
    private Size mVideoSize;

    private void selectCamera(int width, int height) {
        if (null == cameraManager) {
            initCameraManager();
        }
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            if (null == cameraIdList || cameraIdList.length <= 0) {
                showToast("not found an camera...");
                return;
            }

            CameraCharacteristics characteristics;
            for (String id : cameraIdList) {
                characteristics = cameraManager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
//                if (CameraCharacteristics.LENS_FACING_BACK == facing) { // 后置摄像头
                if (CameraCharacteristics.LENS_FACING_FRONT == facing) {
                    cameraId = id;
                    StreamConfigurationMap streamConfigurationMap = characteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    mVideoSize = chooseVideoSize(streamConfigurationMap);
                    mPreviewSize = chooseOptimalSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class),
                            width, height, mVideoSize);
                    break;
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size chooseVideoSize(StreamConfigurationMap streamConfigurationMap) {

        if (null == streamConfigurationMap) {
            LogUtil.w(TAG, "getMatchingSize() -- null == streamConfigurationMap");
            throw new RuntimeException("Cannot get available preview/video sizes");
        }
        Size[] sizes = streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888);
        if (null == sizes || sizes.length <= 0) {
            throw new RuntimeException("Cannot get available preview/video sizes");
        }

        Size[] outputSizes = streamConfigurationMap.getOutputSizes(MediaRecorder.class);
        for (Size size : outputSizes) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return outputSizes[outputSizes.length - 1];
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            LogUtil.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        if (TextUtils.isEmpty(cameraId)) {
            showToast("cameraId is null");
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
            showToast("相机打开成功");
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

    private CaptureRequest.Builder captureRequestBuilder;

    private void startPreview() {
        if (null == cameraDevice || !textureView.isAvailable()) {
            return;
        }
        closePreviewSession();
        SurfaceTexture mSurfaceTexture = textureView.getSurfaceTexture();        //surfaceTexture    需要手动释放
        assert mSurfaceTexture != null;
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(mSurfaceTexture);
            captureRequestBuilder.addTarget(previewSurface); //添加surface   实际使用中这个surface最好是全局变量 在onDestroy的时候mCaptureRequest.removeTarget(mSurface);清除,否则会内存泄露

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
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

    private CameraCaptureSession.StateCallback recordCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            LogUtil.i(TAG, "recordCaptureSessionStateCallback-- onConfigured() -- 11111");
            //摄像头完成配置，可以处理Capture请求了。
            cameraCaptureSession = session;
            updatePreview();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnRecord.setText("停止录像");
                    mediaRecorderManager.startRecordVideo();
                }
            });
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            LogUtil.w(TAG, "recordCaptureSessionStateCallback-- onConfigured() -- 11111");
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


    private void showToast(String msg) {
        LogUtil.i(TAG, "showToast() -- msg = " + msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}