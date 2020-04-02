package com.example.scanmeasuredemo1.scan;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.scanmeasuredemo1.R;
import com.example.scanmeasuredemo1.measure.MeasureActivity;
import com.example.scanmeasuredemo1.measure.Source;
import com.example.scanmeasuredemo1.vision.processor.BaseProcessor;
import com.example.scanmeasuredemo1.vision.processor.FootDrawOnPaperProcessor;
import com.example.scanmeasuredemo1.vision.processor.FootRealOnPaperProcessor;
import com.example.scanmeasuredemo1.vision.processor.WindowProcessor;
import com.example.scanmeasuredemo1.vision.utils.OcvUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Mhd Wajeeh Ajajeh
 * @since 5/3/2018.
 */

public class ScanPresenter implements SurfaceHolder.Callback, Camera.PictureCallback, Camera.PreviewCallback {

    private static final String TAG = ScanPresenter.class.toString();
    private ScanActivity mActivity;
    private Camera mCamera = null;
    private ScanPreview mScanPreview;
    private SurfaceHolder mSurfaceHolder;
    private ExecutorService executor;
    private Scheduler processingScheduler;
    private long frameCnt = 0;

    // RxJava Disposables container
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    // current used Image processing (processor object)
    private BaseProcessor currentProcessor = new WindowProcessor();

    public ScanPresenter(ScanActivity mActivity) {
        this.mActivity = mActivity;
        init();
    }

    private void init() {
        mScanPreview = mActivity.getScanPreview();
        mSurfaceHolder = mScanPreview.getHolder();
        mSurfaceHolder.addCallback(this);

        // initialize scheduler object for image processing Async calls
        executor = Executors.newSingleThreadExecutor();
        processingScheduler = Schedulers.from(executor);

    }

    public void start() {
        try {
            // start the Camera Frame Preview
            mCamera.startPreview();
        } catch (Exception e) {
            Log.i(TAG, "camera object is null");
        }
    }

    public void stop() {
        try {
            // stop the Camera Frame Preview
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.i(TAG, "camera object is null");
        }

    }

    public void exit() {

        // dump RxJava image processing Async calls
        if (this.compositeDisposable != null && !this.compositeDisposable.isDisposed()) {
            this.compositeDisposable.clear();
        }
    }

    public void shut() {

        // check if any object detected
        if (!Source.currentContours.isEmpty()) {
            // launch measure Activity
            Intent intent = new Intent(mActivity, MeasureActivity.class);
            mActivity.startActivity(intent);
        } else
            Toast.makeText(mActivity, "No object Detected !", Toast.LENGTH_SHORT).show();
    }

    // update the Camera object preview
    public void updateCamera() {
        if (mCamera == null) return;
        mCamera.stopPreview();
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
    }


    // initialize Camera object parameters
    public void initCamera() {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "cannot open camera, please grant camera", Toast.LENGTH_SHORT).show();
            return;
        }

        Camera.Parameters param = mCamera.getParameters();
        Size size = getCameraMaxResolution();
        param.setPreviewSize(size != null ? size.width : 1920, size != null ? size.height : 1080);
        Point point = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getRealSize(point);
        int displayWidth = Math.min(point.x, point.y);
        int displayHeight = Math.max(point.x, point.y);
        float displayRatio = ((float) displayWidth) / displayHeight;
        float previewRatio = size != null ? (float) size.height / size.width : displayRatio;
        if (displayRatio > previewRatio) {
            ViewGroup.LayoutParams surfaceParams = mScanPreview.getLayoutParams();
            surfaceParams.height = (int) (point.y / displayRatio * previewRatio);
            mScanPreview.setLayoutParams(surfaceParams);
        }

        List<Size> supportPicSize = mCamera.getParameters().getSupportedPictureSizes();
        Collections.sort(supportPicSize, (o1, o2) -> {
            int x1 = o1.width * o1.height;
            int x2 = o2.width * o2.height;

            return (x2 - x1);
        });

        Size pictureSize = null;
        for (Size s : supportPicSize) {
            if (((float) s.height / s.width) - previewRatio < 0.01)
                pictureSize = s;
        }

        param.setPictureSize(pictureSize != null ? pictureSize.width : 1080
                , pictureSize != null ? pictureSize.height : 1920);

        PackageManager pm = mActivity.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            Log.d(TAG, "enabling autofocus");
        } else {
            Log.d(TAG, "autofocus not available");
        }

        param.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);

        mCamera.setParameters(param);
        mCamera.setDisplayOrientation(90);

    }

    // get max Camera resolution Size
    private Size getCameraMaxResolution() {
        if (mCamera != null)
            return Collections.max(mCamera.getParameters().getSupportedPreviewSizes()
                    , (o1, o2) -> o1.width - o2.width);
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        updateCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (this) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        // processing frame out of 3 for RealTime results preview
        frameCnt++;
        if (frameCnt % 3 != 0) return;

        // process Image Frame to find Contours
        processFrame(data);

    }

    private void processFrame(byte[] data) {
        // get frame size (width,height)
        Camera.Parameters parameters = mCamera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        // Image processing Async Call
        compositeDisposable.add(
                Observable.just(currentProcessor.processImage(data, parameters.getPreviewFormat(), width, height))
                        .subscribeOn(processingScheduler)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(contours -> {
                            // draw new detected contours inside the Scan Preview
                            mScanPreview.setCurrentContours(contours);

                            // set the global variables
                            Source.currentContours = contours;
                            Source.currentBitmap =
                                    OcvUtils.byteImageToBitmap(data, parameters.getPreviewFormat(), width, height, 90);


                        })
        );
    }

    public void radioButtonChecked(View view) {
        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_window:
                currentProcessor = new WindowProcessor();
                break;
            case R.id.radio_foot_draw:
                currentProcessor = new FootDrawOnPaperProcessor();
                break;

            case R.id.radio_foot_real:
                currentProcessor = new FootRealOnPaperProcessor();
                break;
        }
    }
}
