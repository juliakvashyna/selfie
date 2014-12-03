package com.bigdropinc.selfieking.activities.editimages;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import cn.Ragnarok.BitmapFilter;

import com.bidgropinc.biling.util.IabHelper;
import com.bidgropinc.biling.util.IabResult;
import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.adapters.BottomMenuAdapter;
import com.bigdropinc.selfieking.adapters.MenuItem;
import com.bigdropinc.selfieking.controller.managers.FileManager;
import com.bigdropinc.selfieking.model.constants.BackGroundConstants;
import com.bigdropinc.selfieking.model.constants.FilterConstants;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.devsmart.android.ui.HorizontalListView;

public class MakeSelfieActivity extends Activity implements OnTouchListener {
    private static final int LIGHT_FIOLET = R.color.light_fiolet;
    private static final int DARK_FIOLET = R.color.dark_fiolet;
    private static final int FIOLET = R.color.fiolet;
    private static final int OHRA = R.color.ohra;
    private static final int OHRA2 = R.color.ohra2;
    private static final int GREY = R.color.grey;
    private static final int GREYF = R.color.greyf;
    private static final int ROSY = R.color.rosy;
    private static final int GREYF2 = R.color.greyf2;
    private static final int ROSY2 = R.color.rosy2;
    private static final int GREYF3 = R.color.greyf3;
    private static final int YELLOW = R.color.yellow;
    private static final int ROSY_BROWN = R.color.rosy_brown;
    private static final int FIOLET2 = R.color.fiolet2;
    private final String TAG = "tag";
    public static final String EXTRA_IMAGE = "image";

    private Bitmap image;
    private ImageView resultImageView;
    private ImageView backImageView;
    private HorizontalListView horizontalListViewCurrent;
    private BottomMenuAdapter adapterCurrent;
    private List<MenuItem> menuListCurrent;
    private Button okButton;
    private Button doneButton;
    private Button backButton;
    private EditImage selfieImage;
    private Button selectBackgroundButton;
    private Button selectfilterButton;
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

    private IabHelper mHelper;

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
        if (mHelper != null)
            mHelper.dispose();
        mHelper = null;
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
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAppnnGKdUh/6nuk2BL1LZuyN9r60w0q/Zqx3Hkmw6IKZLj/MhpN/+KygSSlje5IlasacAd5r1fw9uQWdL3VlmWHgJ16RAkAyeqGqXT+MH43zGEdiHrKCfUJZGOvbo6jTe/rnzAvpdZl1BCLZ0G2CuY/tr2VDIUcOCTk4AHkj8V13zqekqQBK8TuNP2Eq86B17fwQtqlrLRIQOIyVcHVphGVIaDYy5VqkyH5Erfb5oMh+KLFlCUXz72mHxPINp1yYFJ/Xp4hCuHxpmCK9F+mqnSPA8+7zWaWXbWLneMDXa+AZnnKi5XGEjjHAat0fXZR76Hj6/NDyKUq03ll5V+YvJOwIDAQAB";
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                } else {
                    Log.d(TAG, "Setting up In-app Billing: SUCCESS DONE");
                }
                // Hooray, IAB is fully set up!
            }
        });
    }

    private void init() {
        resultImageView = (ImageView) findViewById(R.id.resultImage);
        resultImageView.setOnTouchListener(this);
        backImageView = (ImageView) findViewById(R.id.backImage);
        selectBackgroundButton = (Button) findViewById(R.id.selectBackground);
        selectfilterButton = (Button) findViewById(R.id.selectFilter);

        horizontalListViewCurrent = (HorizontalListView) findViewById(R.id.bottomMenuCurrent);
        okButton = (Button) findViewById(R.id.btnMainOK);
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
                image = BitmapFactory.decodeByteArray(data, 0, data.length);
            } else {
                Log.d(TAG, "data is null");
            }
            resultImageView.setScaleType(ScaleType.MATRIX);
            image = Bitmap.createScaledBitmap(image, w, h, true);

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
        initBackAndFiltersButtons();

        horizontalListViewCurrent.setAdapter(adapterCurrent);
        try {
            initOnItemClick();
        } catch (OutOfMemoryError e) {
            Log.d(TAG, "OutOfMemoryError initOnItemClick");
        }
        initBackgroundMenu();
        okVisible();
    }

    private void initBackAndFiltersButtons() {
        selectBackgroundButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackgroundClick();
            }
        });
        selectfilterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onFilterClick();
            }
        });
    }

    private void initOnItemClick() {

        horizontalListViewCurrent.setOnItemClickListener(new OnItemClickListener() {
            MenuItem res;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                res = menuListCurrent.get(position);
                if (res != null)
                    setBack(res.getImageres());
            }
        });
    }

    private void initListeners() {
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                okButtonClick();
            }
        });
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

    private void okButtonClick() {
        initBackAndFiltersButtons();
        nextVisible();

    }

    private void createImage() {
        image = selfieImage.getSelfieWithOutBackground();
        resultImageView.setImageBitmap(image);
    }

    private void okVisible() {
        doneButton.setVisibility(View.INVISIBLE);
        okButton.setVisibility(View.VISIBLE);
    }

    private void gotoFeed() {
        new AsyncTask<Void, Void, Intent>() {
            @Override
            protected Intent doInBackground(Void... params) {
                Intent intent = getShareActivityIntent();
                return intent;
            }
            protected void onPostExecute(Intent result) {
                startActivity(result);
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

    private void onFilterClick() {
        selectBackgroundButton.setBackgroundResource(R.drawable.icon_bg);
        selectfilterButton.setBackgroundResource(R.drawable.icon_filter_active);
        initFilteMenu();
        selfieImage.setFilterclick(true);
        resultImageView.setImageBitmap(selfieImage.getSelfieWithOutBackground());
        okVisible();
        resultImageView.setVisibility(View.VISIBLE);
    }

    private void initFilteMenu() {
        menuListCurrent.clear();
        menuListCurrent.add(new MenuItem(FilterConstants.F1, R.string.f1, selfieImage.getSelfieWithBackgroundWithOutFilter()));
        menuListCurrent.add(new MenuItem(FilterConstants.f2, R.string.f2, getFilterImage(LIGHT_FIOLET)));
        menuListCurrent.add(new MenuItem(FilterConstants.f3, R.string.f3, getFilterImage(DARK_FIOLET)));
        menuListCurrent.add(new MenuItem(FilterConstants.f4, R.string.f4, getFilterImage(FIOLET)));
        menuListCurrent.add(new MenuItem(FilterConstants.f5, R.string.f5, getFilterImage(OHRA)));
        menuListCurrent.add(new MenuItem(FilterConstants.f6, R.string.f6, getFilterImage(OHRA2)));
        menuListCurrent.add(new MenuItem(FilterConstants.f7, R.string.f7, getFilterImage(GREY)));
        menuListCurrent.add(new MenuItem(FilterConstants.f8, R.string.f8, getFilterImage(GREYF)));
        menuListCurrent.add(new MenuItem(FilterConstants.f9, R.string.f9, getFilterImage(ROSY)));
        menuListCurrent.add(new MenuItem(FilterConstants.f10, R.string.f10, getFilterImage(GREYF2)));
        menuListCurrent.add(new MenuItem(FilterConstants.f11, R.string.f11, getFilterImage(ROSY2)));
        menuListCurrent.add(new MenuItem(FilterConstants.f12, R.string.f12, getFilterImage(GREYF3)));
        menuListCurrent.add(new MenuItem(FilterConstants.f13, R.string.f13, getFilterImage(YELLOW)));
        menuListCurrent.add(new MenuItem(FilterConstants.f14, R.string.f14, getFilterImage(ROSY_BROWN)));
        menuListCurrent.add(new MenuItem(FilterConstants.f15, R.string.f15, getFilterImage(FIOLET2)));

        adapterCurrent.notifyDataSetChanged();
    }

    private Bitmap getFilterImage(int highlightColor) {
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(highlightColor, Mode.DST_IN);
        Bitmap bitmap = selfieImage.getSelfieWithBackgroundWithOutFilter();

        Canvas canvas = new Canvas();
        Bitmap result = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(result);
        Paint paint = new Paint();
        // paint.setFilterBitmap(false);
        // Color

        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        // paint.setColorFilter(null);
        // //
        // paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        // canvas.drawBitmap(bitmap, 0, 0, paint);
        // paint.setXfermode(null);
        return result;
    }

    private void onBackgroundClick() {
        selectBackgroundButton.setBackgroundResource(R.drawable.icon_bg_active);
        selectfilterButton.setBackgroundResource(R.drawable.icon_filter);
        initBackgroundMenu();
        okVisible();
        if (selfieImage.getColorFilter() != null)
            createImage();
        resultImageView.setVisibility(View.VISIBLE);
        resultImageView.setImageBitmap(image);
    }

    private void initBackgroundMenu() {
        menuListCurrent.add(new MenuItem(BackGroundConstants.b1, R.drawable.colosseum, "colosseum_55.jpg"));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b2, R.drawable.easter_island, "easter_island_55.jpg"));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b3, R.drawable.moon, "moon_55.jpg"));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b4, R.drawable.paris, "paris_55.jpg"));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b5, R.drawable.petra, "petra_55.jpg"));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b6, R.drawable.pyramids, "pyramids_55.jpg"));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b7, R.drawable.statue_of_liberty, "statue_of_liberty_55.jpg"));
        menuListCurrent.add(new MenuItem(BackGroundConstants.b8, R.drawable.stonehenge, "stonehenge_55.jpg"));

        adapterCurrent.notifyDataSetChanged();
    }

    private void nextVisible() {
        doneButton.setVisibility(View.VISIBLE);
        okButton.setVisibility(View.INVISIBLE);
    }

    private void setBack(String filePath) {
        Bitmap back = FileManager.getBitmapFromAsset(filePath);
        selfieImage.setBackground(back);
        backImageView.setImageBitmap(selfieImage.getBackground());
    }

    private void setBack(int res) {
        Bitmap back = BitmapFactory.decodeResource(getResources(), res);
        selfieImage.setBackground(back);
        backImageView.setImageBitmap(selfieImage.getBackground());
    }

    private void dofilter(int highlightColor) {
        if (highlightColor == 0) {
            selfieImage.setColorFilter(null);
        } else {
            PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(highlightColor, Mode.MULTIPLY);
            selfieImage.setColorFilter(colorFilter);
        }

        resultImageView.setImageBitmap(selfieImage.getSelfieWithOutBackground());
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
