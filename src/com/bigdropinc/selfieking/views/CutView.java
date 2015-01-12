package com.bigdropinc.selfieking.views;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class CutView extends ImageView implements OnTouchListener {
    private Paint paint;
    private Path path;
    private List<Point> points;

    private boolean flgPathDraw = true;
    private Point mfirstpoint = null;
    private boolean bfirstpoint = false;
    private Point mlastpoint = null;
    private Bitmap bitmap;
    private int width;
    private int height;
    PointF zoomPos = new PointF(0, 0);
    private boolean zooming;
    private Matrix matrix = new Matrix();
    private Shader shader;
    private Paint shaderPaint = new Paint();

    public int getMyWidth() {
        return width;
    }

    public void setMyWidth(int width) {
        this.width = width;
    }

    public int getMyHeight() {
        return height;
    }

    public void setMyHeight(int height) {
        this.height = height;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    @SuppressLint("ClickableViewAccessibility")
    public CutView(Context c) {
        super(c);
        setFocusable(true);
        setFocusableInTouchMode(true);
        initPaint();
        this.setOnTouchListener(this);
        points = new ArrayList<Point>();
        bfirstpoint = false;

    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.WHITE);
    }

    public void clear() {
        initPaint();
        flgPathDraw = true;

        mlastpoint = null;
        path = new Path();
        bfirstpoint = false;
        points = new ArrayList<Point>();
        invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        shader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
    }

    @SuppressLint("ClickableViewAccessibility")
    public CutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.WHITE);

        this.setOnTouchListener(this);
        points = new ArrayList<Point>();
        bfirstpoint = false;

    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @SuppressLint("DrawAllocation")
    public void onDraw(Canvas canvas) {
        if (bitmap != null) {
            if (width > 0 && height > 0) {
                try {
                    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                } catch (OutOfMemoryError error) {
                    Log.d("tag", "OutOfMemoryError onDraw");
                }
            }

            canvas.drawBitmap(bitmap, 0, 0, null);

        }

        path = new Path();
        boolean first = true;

        for (int i = 0; i < points.size(); i += 2) {
            Point point = points.get(i);

            if (first) {
                first = false;
                path.moveTo(point.x, point.y);
            } else if (i < points.size() - 1) {
                Point next = points.get(i + 1);
                path.quadTo(point.x, point.y, next.x, next.y);
            } else {
                mlastpoint = points.get(i);
                path.lineTo(point.x, point.y);
            }
        }
        if (zooming) {
            matrix.reset();
            matrix.postScale(2f, 2f, zoomPos.x, zoomPos.y);
            shaderPaint.setShader(shader);
            shader.setLocalMatrix(matrix);
            Paint paintWhite = new Paint();
            paintWhite.setColor(Color.WHITE);
            canvas.drawCircle(zoomPos.x - 150, zoomPos.y - 150, 205, paintWhite);
            canvas.drawCircle(zoomPos.x - 150, zoomPos.y - 150, 200, shaderPaint);
            Path pathC = new Path();
            pathC.lineTo(zoomPos.x - 50, zoomPos.y - 50);

            canvas.drawPath(pathC, paintWhite);
        }

        canvas.drawPath(path, paint);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent event) {
        Point point = new Point();

        point.x = (int) event.getX();
        point.y = (int) event.getY();
        if (flgPathDraw) {
            if (bfirstpoint) {
                if (comparepoint(mfirstpoint, point)) {
                    // points.add(point);
                    points.add(mfirstpoint);
                    flgPathDraw = false;
                } else {
                    points.add(point);
                }
            } else {
                points.add(point);
            }
            if (!(bfirstpoint)) {
                mfirstpoint = point;
                bfirstpoint = true;
            }
        }

        invalidate();
        Log.e("Hi  ==>", "Size: " + point.x + " " + point.y);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d("Action up*******~~~~~~~>>>>", "called");
            mlastpoint = point;
            if (flgPathDraw) {
                if (points.size() > 12) {
                    if (!comparepoint(mfirstpoint, mlastpoint)) {
                        flgPathDraw = false;
                        points.add(mfirstpoint);
                    }
                }
            }
        }
        int action = event.getAction();

        zoomPos.x = event.getX();
        zoomPos.y = event.getY();
        matrix.reset();
        matrix.postScale(2f, 2f);
        matrix.postTranslate(-zoomPos.x, -zoomPos.y);
        shader.setLocalMatrix(matrix);

        switch (action) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
            zooming = true;
            this.invalidate();
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            zooming = false;
            this.invalidate();
            break;

        default:
            break;
        }
        return true;
    }

    /**
     * Compare points.
     * 
     * @param first
     * @param current
     * @return equal points
     */
    private boolean comparepoint(final Point first, final Point current) {
        final int padding = 3;
        int leftRangeX = (int) (current.x - padding);
        int leftRangeY = (int) (current.y - padding);

        int rightRangeX = (int) (current.x + padding);
        int rightRangeY = (int) (current.y + padding);

        if ((leftRangeX < first.x && first.x < rightRangeX) && (leftRangeY < first.y && first.y < rightRangeY)) {
            return !(points.size() < 10);
        } else {
            return false;
        }

    }

    public void fillinPartofPath() {
        Point point = new Point();
        point.x = points.get(0).x;
        point.y = points.get(0).y;

        points.add(point);
        invalidate();
    }

    public void resetView() {
        points.clear();
        paint.setColor(Color.WHITE);
        flgPathDraw = true;
        invalidate();
    }

}
