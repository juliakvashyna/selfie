package com.bigdropinc.selfieking.activities.editimages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private Bitmap image;
    private ImageView resultImageView;
    private ImageView backImageView;
    private HorizontalListView horizontalListViewCurrent;
    private BottomMenuAdapter adapterCurrent;
    private List<MenuItem> menuListCurrent;
    private ProgressDialog dialog;
    private Button doneButton;
    private Button backButton;
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
        selfieImage = new EditImage();
    }

    private void initImage() {
        if (getIntent() != null) {
            selfieImage = DatabaseManager.getInstance().findEditImage(getIntent().getIntExtra("id", 0));
            byte[] data = selfieImage.getResult();
            w = getIntent().getIntExtra("w", 0);
            h = getIntent().getIntExtra("h", 0);
            if (data != null) {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPurgeable = true;
                    options.inJustDecodeBounds = false;
                    // options.inPreferredConfig = Config.RGB_565;
                    options.inDither = true;
                    // options.inSampleSize = 2;
                    image = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                } catch (OutOfMemoryError e) {
                    Toast.makeText(this, "Sorry, image error ", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "data is null");
            }
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
                if (res != null)
                    setBack(res.getBitmap());
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
    }

    private void gotoFeed() {
        new AsyncTask<Void, Void, Intent>() {
            protected void onPreExecute() {
                dialog = ProgressDialog.show(MakeSelfieActivity.this, "", "");
                dialog.setContentView(new ProgressBar(MakeSelfieActivity.this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            };

            @Override
            protected Intent doInBackground(Void... params) {
                Intent intent = getShareActivityIntent();
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

    private Intent getShareActivityIntent() {
        Intent intent = new Intent(getApplicationContext(), AddFilterActivity.class);
        selfieImage.setMatrix(matrix);
        selfieImage.getSelfieWithBackground();
        DatabaseManager.getInstance().updateSelfie(selfieImage);
        intent.putExtra("id", selfieImage.getId());
        return intent;
    }

    private void initBackgroundMenu() {
        menuListCurrent.add(new MenuItem(BackGroundConstants.b1, ImageHelper.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.colosseum), 100)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b2, BitmapFactory.decodeResource(getResources(), R.drawable.easter_island)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b3, BitmapFactory.decodeResource(getResources(), R.drawable.moon)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b4, BitmapFactory.decodeResource(getResources(), R.drawable.paris)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b5, BitmapFactory.decodeResource(getResources(), R.drawable.petra)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b6, BitmapFactory.decodeResource(getResources(), R.drawable.pyramids)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b7, BitmapFactory.decodeResource(getResources(), R.drawable.statue_of_liberty)));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b8, BitmapFactory.decodeResource(getResources(), R.drawable.stonehenge)));
        adapterCurrent.notifyDataSetChanged();
    }

    private void setBack(String filePath) {
        Bitmap back = FileManager.getBitmapFromAsset(filePath);
        selfieImage.setBackground(back);
        backImageView.setImageBitmap(selfieImage.getBackground());
    }

    private void setBack(Bitmap bitmap) {
        Bitmap back = bitmap;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            back.compress(Bitmap.CompressFormat.JPEG, 50, out);
            Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
            back = Bitmap.createBitmap(decoded);
            // decoded.recycle();
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

}
