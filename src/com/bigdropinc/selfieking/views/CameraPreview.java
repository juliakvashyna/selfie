package com.bigdropinc.selfieking.views;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private String tag = "tag";
    private Context context;
    private int currentCameraId = 0;
    private boolean isPreviewRunning;

    public int getCurrentCameraId() {
        return currentCameraId;
    }

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.context = context;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        setCameraDisplayOrientation();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(currentCameraId, info);
        WindowManager winManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = winManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
        case Surface.ROTATION_0:
            degrees = 0;
            break;
        case Surface.ROTATION_90:
            degrees = 90;
            break;
        case Surface.ROTATION_180:
            degrees = 180;
            break;
        case Surface.ROTATION_270:
            degrees = 270;
            break;
        default:
            break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        if (mCamera != null) {
            mCamera.setDisplayOrientation(result);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the
        // preview.
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);

                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(tag, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Camera.Parameters parameters = mCamera.getParameters();
        // List<Camera.Size> previewSizes =
        // parameters.getSupportedPreviewSizes();
        //
        // // You need to choose the most appropriate previewSize for your app
        // Camera.Size previewSize = previewSizes.get(0);// .... select one of
        // // previewSizes here
        //
        // parameters.setPreviewSize(previewSize.width, previewSize.height);
        // mCamera.setParameters(parameters);
        // mCamera.startPreview();
        Camera.Parameters mParameters = mCamera.getParameters();
        Camera.Size bestSize = null;

        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
        if (sizeList != null) {
            bestSize = sizeList.get(0);

            for (int i = 1; i < sizeList.size(); i++) {
                if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
                    bestSize = sizeList.get(i);
                }
            }

            mParameters.setPreviewSize(bestSize.width, bestSize.height);
            mCamera.setParameters(mParameters);
            mCamera.startPreview();
        }
    }

    // public void surfaceChanged(SurfaceHolder holder, int format, int w, int
    // h) {
    //
    // // If your preview can change or rotate, take care of those events here.
    // // Make sure to stop the preview before resizing or reformatting it.
    //
    // if (mHolder.getSurface() == null) {
    // // preview surface does not exist
    // return;
    // }
    //
    // // stop preview before making changes
    // try {
    // mCamera.stopPreview();
    // } catch (Exception e) {
    // e.printStackTrace();
    // // ignore: tried to stop a non-existent preview
    // }
    //
    // // set preview size and make any resize, rotate or
    // // reformatting changes here
    //
    // // start preview with new settings
    // try {
    // mCamera.setPreviewDisplay(mHolder);
    // mCamera.startPreview();
    //
    // } catch (Exception e) {
    // Log.d(tag, "Error starting camera preview: " + e.getMessage());
    // }
    // }

    public void changeCamera() {
        // if (isPreviewRunning) {
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mHolder.removeCallback(this);
        mCamera.release();
        mCamera = null;
        // }

        // swap the id of the camera to be used
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mCamera = Camera.open(currentCameraId);
        try {
            // this step is critical or preview on new camera will no know where
            // to render to
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setCameraDisplayOrientation();
        mCamera.startPreview();

    }
}
