package com.bigdropinc.selfieking.activities.editimages;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
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

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.adapters.BottomMenuAdapter;
import com.bigdropinc.selfieking.adapters.MenuItem;
import com.bigdropinc.selfieking.controller.managers.FileManager;
import com.bigdropinc.selfieking.model.constants.BackGroundConstants;
import com.bigdropinc.selfieking.model.constants.FilterConstants;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.bigdropinc.selfieking.views.CutView;
import com.bigdropinc.selfieking.views.Point;
import com.devsmart.android.ui.HorizontalListView;

public class MakeSelfieActivity extends Activity implements OnTouchListener {
    private static final int ID_FILTER = 3;
    private static final int ID_BACK = 2;
    private final String TAG = "tag";
    public static final String EXTRA_IMAGE = "image";

    private Bitmap image;
    private ImageView resultImageView;
    private ImageView backImageView;
    private HorizontalListView horizontalListView;
    private BottomMenuAdapter adapter;
    private List<MenuItem> menuList;
    private Button okButton;
    private Button doneButton;
    private EditImage selfieImage;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initImage();
        initListeners();
        initMenu();
    }

    private void init() {
        resultImageView = (ImageView) findViewById(R.id.resultImage);
        resultImageView.setOnTouchListener(this);
        backImageView = (ImageView) findViewById(R.id.backImage);
        horizontalListView = (HorizontalListView) findViewById(R.id.bottomMenu);
        okButton = (Button) findViewById(R.id.btnMainOK);
        doneButton = (Button) findViewById(R.id.btnMainNext);
        selfieImage = new EditImage();
    }

    private void initImage() {
        if (getIntent() != null) {
            byte[] data = DatabaseManager.getInstance().findEditImage(getIntent().getIntExtra("id", 0)).getResult();
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
        menuList = new ArrayList<MenuItem>();
        adapter = new BottomMenuAdapter(this, R.layout.bottom_item, menuList);
        initMenuList();
        horizontalListView.setAdapter(adapter);
        try {
            initOnItemClick();
        } catch (OutOfMemoryError e) {
            Log.d(TAG, "OutOfMemoryError initOnItemClick");
        }
    }

    private void initMenuList() {
        menuList.clear();
        menuList.add(new MenuItem(ID_BACK, R.drawable.backgrounds));
        menuList.add(new MenuItem(ID_FILTER, R.drawable.filters));
        menuList.add(new MenuItem(R.drawable.buy));

        adapter.notifyDataSetChanged();
    }

    private void initOnItemClick() {
        horizontalListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean crop = false;
                switch (((MenuItem) parent.getItemAtPosition(position)).getId()) {
                case ID_FILTER:
                    onFilterClick(crop);
                    break;
                case ID_BACK:
                    onBackgroundClick();
                    break;
                case BackGroundConstants.b1:
                    setBack("1.jpg");
                    break;
                case BackGroundConstants.b2:
                    setBack("2.jpg");
                    break;
                case BackGroundConstants.b3:
                    setBack("3.jpg");
                    break;
                case BackGroundConstants.b4:
                    setBack("4.jpg");
                    break;
                case BackGroundConstants.b5:
                    setBack("5.jpg");
                    break;
                case BackGroundConstants.b6:
                    setBack("6.jpg");
                    break;
                case FilterConstants.F1:
                    dofilter(0);
                    break;
                case FilterConstants.f2:
                    dofilter(getResources().getColor(R.color.light_fiolet));
                    break;
                case FilterConstants.f3:
                    dofilter(getResources().getColor(R.color.dark_fiolet));
                    break;
                case FilterConstants.f4:
                    dofilter(getResources().getColor(R.color.fiolet));
                    break;
                case FilterConstants.f5:
                    dofilter(getResources().getColor(R.color.ohra));
                    break;
                case FilterConstants.f6:
                    dofilter(getResources().getColor(R.color.ohra2));
                    break;
                case FilterConstants.f7:
                    dofilter(getResources().getColor(R.color.ohra2));
                    break;
                case FilterConstants.f8:
                    dofilter(getResources().getColor(R.color.greyf));
                    break;
                case FilterConstants.f9:
                    dofilter(getResources().getColor(R.color.rosy));
                    break;
                case FilterConstants.f10:
                    dofilter(getResources().getColor(R.color.greyf2));
                    break;
                case FilterConstants.f11:
                    dofilter(getResources().getColor(R.color.rosy2));
                case FilterConstants.f12:
                    dofilter(getResources().getColor(R.color.greyf3));
                    break;
                case FilterConstants.f13:
                    dofilter(getResources().getColor(R.color.yellow));
                    break;
                case FilterConstants.f14:
                    dofilter(getResources().getColor(R.color.rosy_brown));
                    break;
                case FilterConstants.f15:

                    dofilter(getResources().getColor(R.color.fiolet2));
                    break;
                default:
                    break;
                }
            }
        });
    }

    private void initListeners() {
        okButton.setOnClickListener(new OnClickListener() {
            @SuppressLint("NewApi")
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
    }

    private void okButtonClick() {
        initMenuList();
        nextVisible();
        adapter.notifyDataSetChanged();
        horizontalListView.setVisibility(View.VISIBLE);
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
        Intent intent = new Intent(getApplicationContext(), ShareActivity.class);
        // ByteArrayOutputStream stream = new ByteArrayOutputStream();
        selfieImage.setMatrix(matrix);
        // selfieImage.getSelfieWithBackground().compress(Bitmap.CompressFormat.PNG,
        // 45, stream);

        // byte[] byteArray = stream.toByteArray();

        selfieImage.getSelfieWithBackground();
        selfieImage = DatabaseManager.getInstance().addSelfie(selfieImage);

        // intent.putExtra("image", byteArray);
        intent.putExtra("id", selfieImage.getId());
        return intent;
    }

    private void onFilterClick(boolean crop) {
        initFilteMenu();
        selfieImage.setFilterclick(true);
        resultImageView.setImageBitmap(selfieImage.getSelfieWithOutBackground());
        okVisible();
        if (!crop) {
            resultImageView.setVisibility(View.VISIBLE);
        }
    }

    private void initFilteMenu() {
        menuList.clear();
        menuList.add(new MenuItem(FilterConstants.F1, R.string.f1, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f2, R.string.f2, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f3, R.string.f3, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f4, R.string.f4, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f5, R.string.f5, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f6, R.string.f6, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f7, R.string.f7, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f8, R.string.f8, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f9, R.string.f9, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f10, R.string.f10, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f11, R.string.f11, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f12, R.string.f12, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f13, R.string.f13, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f14, R.string.f14, R.drawable.filters));
        menuList.add(new MenuItem(FilterConstants.f15, R.string.f15, R.drawable.filters));
        adapter.notifyDataSetChanged();
    }

    private void onBackgroundClick() {
        initBackgroundMenu();
        okVisible();
        resultImageView.setVisibility(View.VISIBLE);
        resultImageView.setImageBitmap(image);
    }

    private void initBackgroundMenu() {
        menuList.clear();
        menuList.add(new MenuItem(BackGroundConstants.b1, "1.jpg"));
        menuList.add(new MenuItem(BackGroundConstants.b2, "2.jpg"));
        menuList.add(new MenuItem(BackGroundConstants.b3, "3.jpg"));
        menuList.add(new MenuItem(BackGroundConstants.b4, "4.jpg"));
        menuList.add(new MenuItem(BackGroundConstants.b5, "5.jpg"));
        menuList.add(new MenuItem(BackGroundConstants.b5, "6.jpg"));
        adapter.notifyDataSetChanged();
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