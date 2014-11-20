package com.bigdropinc.selfieking.activities.editimages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.bigdropinc.selfieking.views.CameraPreview;

public class SelectImageActivity extends Activity implements OnClickListener {
    /**
     * Log.
     */
    public static final String TAG = "tag";
    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_IMAGE_BYTE = "image_byte";

    private static final int RESULT_GALLERY = 11;
    public static final String EXTRA_IMAGE_BYTE_ORIGINAL = "image_byte_original";

    private Button photoButton;
    private Button galleryButton;
    private Button rotateCameraButton;

    // private ImageView selectedImageView;
    private Uri selectedImage;
    private CameraPreview mPreview;
    private Camera mCamera;
    private FrameLayout preview;
    private int currentCameraId;

    private Boolean rotate;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.photo:
            if (mCamera != null && mPreview != null && mPreview.getHolder() != null) {
                mPreview.getHolder().removeCallback(mPreview);
                mCamera.setPreviewCallback(null);
                mCamera.takePicture(null, null, mPicture);
            }
            break;
        case R.id.cameraRotate:
            changeCamera();
            break;
        case R.id.gallery:
            openGallery();
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        initView();
        initListeners();
        preview = (FrameLayout) findViewById(R.id.camera_preview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    @Override
    protected void onPause() {
        stopCamera();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
        case RESULT_GALLERY:
            if (resultCode == RESULT_OK) {
                selectedImage = data.getData();
                save();
            }
            break;
        default:
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public int getCurrentCameraId() {
        return currentCameraId;
    }

    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, final Camera camera) {
            String picturePath = saveToInternalSorage(data);
            goToCutActivity(data, picturePath);
        }
    };

    private String saveToInternalSorage(byte[] data) {
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "test.jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(data);

            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f.getAbsolutePath();
    }

    private void initCamera() {
        mCamera = getCameraInstance();
        if (mCamera != null) {
            mPreview = new CameraPreview(this, mCamera);
            preview.removeAllViews();
            preview.addView(mPreview);
            setBackOrientation();

        }
    }

    /**
     * Set right orientation of camera.
     */
    private void changeCamera() {
        if (rotate == null) {
            rotate = Boolean.valueOf(false);
        }
        rotate = !rotate;
        setFrontOrientation();
    }

    private void setFrontOrientation() {
        setCurrentCameraId();
        stopCamera();
        mCamera = Camera.open(currentCameraId);
        int result = 90;
        if (rotate) {
            result = getResult();
        }
        setOrientationStart(result);
        Log.d(TAG, "currentCameraId " + currentCameraId);
    }

    private void setOrientationStart(int result) {
        if (mCamera != null) {
            mCamera.setDisplayOrientation(result);
            try {
                mCamera.setPreviewDisplay(mPreview.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
        } else {
            Log.d(TAG, "setOrientationStart mCamera is null ");
        }
    }

    private int getResult() {
        int result;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
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
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror

        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    private void setCurrentCameraId() {
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
    }

    private void stopCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        } else {
            Log.d(TAG, "stopCamera mCamera is null ");
        }
    }

    /**
     * Set right orientation of camera.
     */
    private int setBackOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
        case Surface.ROTATION_0:
            degrees = 0;
            break; // Natural orientation
        case Surface.ROTATION_90:
            degrees = 90;
            break; // Landscape left
        case Surface.ROTATION_180:
            degrees = 180;
            break; // Upside down
        case Surface.ROTATION_270:
            degrees = 270;
            break; // Landscape right
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
        // STEP #2: Set the 'rotation' parameter
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(result);
        mCamera.setParameters(params);
        return result;
    }

    /**
     * Open back camera.
     * 
     * @return camera instance
     */
    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private void initView() {
        photoButton = (Button) findViewById(R.id.photo);
        galleryButton = (Button) findViewById(R.id.gallery);
        rotateCameraButton = (Button) findViewById(R.id.cameraRotate);
        // selectedImageView = (ImageView) findViewById(R.id.selectedImage);
    }

    private void initListeners() {
        photoButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);
        rotateCameraButton.setOnClickListener(this);
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_GALLERY);
    }

    private void save() {
        Intent intent = new Intent(getApplicationContext(), CropActivity.class);
        intent.putExtra(EXTRA_IMAGE_URI, selectedImage);
        startActivity(intent);
    }

    private void goToCutActivity(byte[] data, String picturePath) {
        Intent intent = new Intent(getApplicationContext(), CutActivity.class);
        int w = mPreview.getWidth();
        int h = mPreview.getHeight();

        EditImage selfieImage = new EditImage();
        selfieImage.setResult(data);
        selfieImage = DatabaseManager.getInstance().addSelfie(selfieImage);
        intent.putExtra("id", selfieImage.getId());
        intent.putExtra("w", w);
        intent.putExtra("h", h);
        if (rotate != null) {
            intent.putExtra("camera", rotate);
        }
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
