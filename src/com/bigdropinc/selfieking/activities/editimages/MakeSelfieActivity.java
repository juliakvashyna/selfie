package com.bigdropinc.selfieking.activities.editimages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.Ragnarok.BitmapFilter;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.adapters.BottomMenuAdapter;
import com.bigdropinc.selfieking.adapters.MenuItem;
import com.bigdropinc.selfieking.controller.managers.FileManager;
import com.bigdropinc.selfieking.model.constants.BackGroundConstants;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.bigdropinc.selfieking.views.ImageHelper;
import com.devsmart.android.ui.HorizontalListView;

public class MakeSelfieActivity extends Activity implements OnTouchListener {

    private final String TAG = "tag";
    public static final String EXTRA_IMAGE = "image";
    private static final int RESULT_GALLERY = 11;
    private static final int REQUEST_IMAGE_CAPTURE = 12;
    private Bitmap image;
    private ImageView resultImageView;
    private ImageView backImageView;
    private HorizontalListView horizontalListViewCurrent;
    private BottomMenuAdapter adapterCurrent;
    private List<MenuItem> menuListCurrent;
    private ProgressDialog dialog;
    private Button doneButton;
    private Button backButton;
    private ImageButton galleryButton;
    private ImageButton cameraButton;
    private EditImage selfieImage;
    // private Button selectBackgroundButton;
    // private Button selectfilterButton;
    // These matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    private int mode = NONE;

    // Remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private int w;
    private int h;
    private float d;
    private float[] lastEvent;
    private float newRot;
    private Uri selectedImage;
    private String mCurrentPhotoPath;
    private File photoFile;
    private Uri mImageUri;
    final public static int PIC_CROP = 13;
    private int backHeight;
    private int backWidth;
    private Uri outputFileUri;
    private Uri mCropImagedUri;
    private int dimension;
    private boolean notcrop;

    // private IabHelper mHelper;

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null)
            dialog.cancel();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ScaleType.MATRIX);
        new PointF();
        new PointF();
        dumpEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            down(event);
            break;
        case MotionEvent.ACTION_POINTER_DOWN:
            pointerDown(event);
            break;
        case MotionEvent.ACTION_POINTER_UP:
            pointerUp();
            break;
        case MotionEvent.ACTION_MOVE:
            move(event, view);
            break;
        default:
            break;
        }
        view.setImageMatrix(matrix);
        BitmapFilter.matrix = matrix;
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // if (mHelper != null)
        // mHelper.dispose();
        // mHelper = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initImage();
        initListeners();
        initMenu();
        initIabHelper();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

        case RESULT_GALLERY:
            if (resultCode == Activity.RESULT_OK) {
                startCropIntent(data);
            }
            break;
        case REQUEST_IMAGE_CAPTURE:
            if (resultCode == Activity.RESULT_OK) {
                Intent newdata= new Intent();
                newdata.setData(outputFileUri);
                startCropIntent(newdata);
            }
            break;
        case PIC_CROP: {
            if (data != null) {
                Uri imageUri = data.getData();
                w = getWindowManager().getDefaultDisplay().getWidth();
                h = getIntent().getIntExtra("h", 0);
                Bitmap back = null;
                if (imageUri != null)
                    back = getBitmap(imageUri);
                if (back != null) {
                    back = Bitmap.createScaledBitmap(back, w, h, false);
                }
                selfieImage.setBackground(back);
                backImageView.setImageBitmap(back);
                getApplicationContext().getContentResolver().delete(imageUri, null, null);
            }
            break;
        }
        default:
            break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width,
        // respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap
        // will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our
        // new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        // If the bitmap is wider than it is tall
        // use the height as the square crop dimension
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            dimension = bitmap.getHeight();
        }
        // If the bitmap is taller than it is wide
        // use the width as the square crop dimension
        else {
            dimension = bitmap.getWidth();
        }
        return dimension;
    }

    // private void startCropIntent(Intent data) {
    // Intent cropIntent = new Intent("com.android.camera.action.CROP");
    // cropIntent.setClassName("com.android.camera",
    // "com.android.camera.CropImage");
    // // indicate image type and Uri
    // // cropIntent.setDataAndType(data.getData(), "image/*");
    // // set crop properties
    // cropIntent.putExtra("crop", "true");
    // // indicate aspect of desired crop
    // cropIntent.putExtra("aspectX", 1);
    // cropIntent.putExtra("aspectY", 1);
    // // indicate output X and Y
    // cropIntent.putExtra("outputX", 360);
    // cropIntent.putExtra("outputY", 360);
    // cropIntent.putExtra("scale", true);
    // cropIntent.putExtra("return-data", false);
    // // cropIntent.setDataAndType(outputFileUri, "image/*");
    // // photoFile.delete();
    // // try {
    // // photoFile = File.createTempFile("crop1", "png",
    // // Environment.getExternalStorageDirectory());
    // // } catch (IOException e) {
    // // // TODO Auto-generated catch block
    // // e.printStackTrace();
    // // }
    // if (data == null || data.getData() == null) {
    // data = new Intent();
    // data.setData(outputFileUri);
    // } else
    // mCropImagedUri = data.getData();
    // cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCropImagedUri);
    // // start the activity - we handle returning in onActivityResult
    // try {
    // startActivityForResult(cropIntent, PIC_CROP);
    // } catch (Exception e) {
    // notcrop = true;
    // onActivityResult(PIC_CROP, 1, data);
    // }
    // // photoFile.delete();
    // }
    private void startCropIntent(Intent data) {
        if (data != null) {
            Intent intent = new Intent(getApplicationContext(), CropActivity.class);
            Uri mCropImagedUri = data.getData();
            intent.putExtra(SelectImageActivity.EXTRA_IMAGE_URI, mCropImagedUri);
            intent.putExtra("fromBG", true);
            startActivityForResult(intent, PIC_CROP);
        }
    }

    private void initIabHelper() {
        // String base64EncodedPublicKey =
        // "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAppnnGKdUh/6nuk2BL1LZuyN9r60w0q/Zqx3Hkmw6IKZLj/MhpN/+KygSSlje5IlasacAd5r1fw9uQWdL3VlmWHgJ16RAkAyeqGqXT+MH43zGEdiHrKCfUJZGOvbo6jTe/rnzAvpdZl1BCLZ0G2CuY/tr2VDIUcOCTk4AHkj8V13zqekqQBK8TuNP2Eq86B17fwQtqlrLRIQOIyVcHVphGVIaDYy5VqkyH5Erfb5oMh+KLFlCUXz72mHxPINp1yYFJ/Xp4hCuHxpmCK9F+mqnSPA8+7zWaWXbWLneMDXa+AZnnKi5XGEjjHAat0fXZR76Hj6/NDyKUq03ll5V+YvJOwIDAQAB";
        // mHelper = new IabHelper(this, base64EncodedPublicKey);
        // mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
        // public void onIabSetupFinished(IabResult result) {
        // if (!result.isSuccess()) {
        // // Oh noes, there was a problem.
        // Log.d(TAG, "Problem setting up In-app Billing: " + result);
        // } else {
        // Log.d(TAG, "Setting up In-app Billing: SUCCESS DONE");
        // }
        // Hooray, IAB is fully set up!
        // }
        // });
    }

    private void init() {
        resultImageView = (ImageView) findViewById(R.id.resultImage);
        resultImageView.setOnTouchListener(this);
        backImageView = (ImageView) findViewById(R.id.backImage);
        horizontalListViewCurrent = (HorizontalListView) findViewById(R.id.bottomMenuCurrent);
        doneButton = (Button) findViewById(R.id.btnMainNext);
        backButton = (Button) findViewById(R.id.btnMainBack);
        galleryButton = (ImageButton) findViewById(R.id.bgGaleria);
        cameraButton = (ImageButton) findViewById(R.id.bgCamera);
        selfieImage = new EditImage();
    }

    private void initImage() {
        if (getIntent() != null) {
            selfieImage = DatabaseManager.getInstance().findEditImage(getIntent().getIntExtra("id", 0));
            // byte[] data = selfieImage.getResult();
            w = getIntent().getIntExtra("w", 0);
            h = getIntent().getIntExtra("h", 0);
            // if (data != null) {
            try {
                // BitmapFactory.Options options = new BitmapFactory.Options();
                // options.inPurgeable = true;
                // options.inJustDecodeBounds = false;
                // // options.inPreferredConfig = Config.RGB_565;
                // options.inDither = true;
                // // options.inSampleSize = 2;
                // image = BitmapFactory.decodeByteArray(data, 0, data.length,
                // options);
                image = ShareActivity.loadImageFromStorage(selfieImage.getPath());
                image = Bitmap.createScaledBitmap(image, w, h, false);
            } catch (OutOfMemoryError e) {
                Toast.makeText(this, "Sorry, image error ", Toast.LENGTH_LONG).show();
            }
            // } else {
            // Log.d(TAG, "data is null");
            // }
            resultImageView.setScaleType(ScaleType.MATRIX);
            // image = Bitmap.createScaledBitmap(image, w, h, true);

            resultImageView.setImageBitmap(image);
            selfieImage.setOriginalImage(image);
            selfieImage.setCroppedBitmap(image);
            selfieImage.setWidth(w);
            selfieImage.setHeight(h);
        } else {
            Log.e(TAG, "intent is null ");
        }
    }

    private void initMenu() {
        menuListCurrent = new ArrayList<MenuItem>();
        adapterCurrent = new BottomMenuAdapter(this, R.layout.bottom_item, menuListCurrent);
        adapterCurrent.setRadius(100);
        horizontalListViewCurrent.setAdapter(adapterCurrent);
        try {
            initOnItemClick();
        } catch (OutOfMemoryError e) {
            Log.d(TAG, "OutOfMemoryError initOnItemClick");
        }
        initBackgroundMenu();
    }

    private void initOnItemClick() {
        horizontalListViewCurrent.setOnItemClickListener(new OnItemClickListener() {
            MenuItem res;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                res = menuListCurrent.get(position);
                if (res != null) {
                    setBack(res.getBitmap());
                }
            }
        });
    }

    private void initListeners() {

        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFeed();
            }
        });
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        galleryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        cameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }

    private void openGallery() {
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = "test";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // try {
        // photoFile = File.createTempFile("crop", "png",
        // Environment.getExternalStorageDirectory());
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // outputFileUri = Uri.fromFile(photoFile);
        i.setType("image/*");
        // i.putExtra("crop", true);
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(i, RESULT_GALLERY);
    }

    private void openCamera() {
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = "test";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // File outputDir = new File(Environment.getExternalStorageDirectory(),
        // "Selfies");
        // File outputDir = new File(getContentResolver().g, "Selfies");
        // if (!outputDir.exists())
        // outputDir.mkdirs(); // <----
        // File outputFile = new File(outputDir, "image_001.jpg");
        // try {
        // photoFile = File.createTempFile("crop", "png",
        // Environment.getExternalStorageDirectory());
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // outputFileUri = Uri.fromFile(photoFile);

        takePictureIntent.putExtra("crop", true);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void gotoFeed() {
        if (selfieImage.getBackground() == null) {
            Toast.makeText(this, "Please choose background", Toast.LENGTH_LONG).show();

        } else {

            new AsyncTask<Void, Void, Intent>() {
                protected void onPreExecute() {
                    dialog = ProgressDialog.show(MakeSelfieActivity.this, "", "");
                    dialog.setContentView(new ProgressBar(MakeSelfieActivity.this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                };

                @Override
                protected Intent doInBackground(Void... params) {
                    Intent intent = getAddFilterActivityIntent();
                    return intent;
                }

                protected void onPostExecute(Intent result) {
                    startActivity(result);
                    if (dialog != null) {
                        dialog.cancel();
                    }
                };
            }.execute();
        }
    }

    private Intent getAddFilterActivityIntent() {
        Intent intent = new Intent(getApplicationContext(), AddFilterActivity.class);
        selfieImage.setMatrix(matrix);
        selfieImage.setPath(saveToInternalSorage(selfieImage.getSelfieWithBackground()));
        DatabaseManager.getInstance().updateSelfie(selfieImage);
        intent.putExtra("id", selfieImage.getId());
        return intent;
    }

    public String saveToInternalSorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to
            // the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    private void initBackgroundMenu() {
        Bitmap back = BitmapFactory.decodeResource(getResources(), R.drawable.easter_island);
        backHeight = (int) (back.getHeight());
        backWidth = (int) (back.getWidth());

        menuListCurrent.add(new MenuItem(BackGroundConstants.b1, ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.colosseum), 100)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b2, back));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b3, BitmapFactory.decodeResource(getResources(), R.drawable.moon)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b4, BitmapFactory.decodeResource(getResources(), R.drawable.paris)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b5, BitmapFactory.decodeResource(getResources(), R.drawable.petra)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b6, BitmapFactory.decodeResource(getResources(), R.drawable.pyramids)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b7, BitmapFactory.decodeResource(getResources(), R.drawable.statue_of_liberty)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b8, BitmapFactory.decodeResource(getResources(), R.drawable.stonehenge)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b9, BitmapFactory.decodeResource(getResources(), R.drawable.rhino)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b10, BitmapFactory.decodeResource(getResources(), R.drawable.taj_mahal)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b11, BitmapFactory.decodeResource(getResources(), R.drawable.the_great_wall)));
        adapterCurrent.notifyDataSetChanged();
    }

    private void setBack(Bitmap bitmap) {
        Bitmap back = bitmap;
        try {
            back = Bitmap.createScaledBitmap(back, backWidth, backHeight, false);
        } catch (OutOfMemoryError e) {
            Log.d("tag", "OutOfMemoryError");
        }
        selfieImage.setBackground(back);
        backImageView.setImageBitmap(selfieImage.getBackground());
    }

    private void pointerUp() {
        mode = NONE;
        lastEvent = null;
        Log.d(TAG, "mode=NONE");
    }

    private void down(MotionEvent event) {
        savedMatrix.set(matrix);
        start.set(event.getX(), event.getY());
        getAngle(event.getX(), event.getY());
        Log.d(TAG, "mode=DRAG");
        mode = DRAG;
        lastEvent = null;
    }

    private void pointerDown(MotionEvent event) {
        oldDist = spacing(event);
        Log.d(TAG, "oldDist=" + oldDist);
        if (oldDist > 10f) {
            savedMatrix.set(matrix);
            midPoint(mid, event);
            mode = ZOOM;
            Log.d(TAG, "mode=ZOOM");
        }
        lastEvent = new float[4];
        lastEvent[0] = event.getX(0);
        lastEvent[1] = event.getX(1);
        lastEvent[2] = event.getY(0);
        lastEvent[3] = event.getY(1);
        d = rotation(event);
    }

    private void move(MotionEvent event, ImageView view) {
        if (mode == DRAG) {
            matrix.set(savedMatrix);
            matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
        } else if (mode == ZOOM) {
            float newDist = spacing(event);
            if (newDist > 10f) {
                matrix.set(savedMatrix);
                float scale = newDist / oldDist;
                matrix.postScale(scale, scale, mid.x, mid.y);
            }
            if (lastEvent != null && event.getPointerCount() == 2) {
                Log.d(TAG, "mode=rotate");
                newRot = rotation(event);
                float r = newRot - d;
                float[] values = new float[9];
                matrix.getValues(values);
                float tx = values[2];
                float ty = values[5];
                float sx = values[0];
                float xc = (view.getWidth() / 2) * sx;
                float yc = (view.getHeight() / 2) * sx;
                matrix.postRotate(r, tx + xc, ty + yc);
            }
        }
    }

    /** Show an event in the LogCat view, for debugging */
    @SuppressWarnings("deprecation")
    private void dumpEvent(MotionEvent event) {
        String[] names = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount()) {
                sb.append(";");
            }
        }
        sb.append("]");
        Log.d(TAG, sb.toString());
    }

    /** Determine the space between the first two fingers */
    @SuppressLint("FloatMath")
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private double getAngle(double xTouch, double yTouch) {
        double x = xTouch - (w / 2d);
        double y = h - yTouch - (h / 2d);

        switch (getQuadrant(x, y)) {
        case 1:
            return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
        case 2:
            return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
        case 3:
            return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
        case 4:
            return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
        default:
            return 0;
        }
    }

    /**
     * @return The selected quadrant.
     */
    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private Bitmap getBitmap(Uri uri) {
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inPurgeable = true;
            o.inPreferredConfig = Config.RGB_565;
            o.inDither = true;
            BitmapFactory.decodeStream(in, null, o);

            in.close();

            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d(TAG, "scale = " + scale + ", orig-width: " + o.outWidth + ",         orig-height: " + o.outHeight);

            Bitmap b = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d(TAG, "1th scale operation dimenions - width: " + width + ",               height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            // Log.d(TAG, "bitmap size - width: " + b.getWidth() + ", height: "
            // + b.getHeight());
            return b;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    private boolean deleteLastFromDCIM() {

        boolean success = false;
        try {
            File[] images = new File(Environment.getExternalStorageDirectory() + File.separator + "Camera").listFiles();
            File latestSavedImage = images[0];
            for (int i = 1; i < images.length; ++i) {
                if (images[i].lastModified() > latestSavedImage.lastModified()) {
                    latestSavedImage = images[i];
                }
            }

            // OR JUST Use success = latestSavedImage.delete();
            success = new File(Environment.getExternalStorageDirectory() + File.separator + "Camera/" + latestSavedImage.getAbsoluteFile()).delete();
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return success;
        }

    }

}
