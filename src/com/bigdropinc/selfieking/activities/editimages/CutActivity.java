package com.bigdropinc.selfieking.activities.editimages;

import java.io.ByteArrayOutputStream;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.bigdropinc.selfieking.views.CutView;
import com.bigdropinc.selfieking.views.Point;

public class CutActivity extends Activity {

    private int w;
    private int h;
    private Bitmap image;
    private Bitmap original;
    public static CutView mainImageView;
    private String TAG = "tag";
    private Button cutButton;
    private Button nextButton;
    private Button clearButton;
    private EditImage selfieImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);
        initViews();
        initImage();
        initListeners();
    }

    private void initViews() {
        mainImageView = (CutView) findViewById(R.id.mainImage);
        cutButton = (Button) findViewById(R.id.cut);
        clearButton = (Button) findViewById(R.id.clear);
        nextButton = (Button) findViewById(R.id.next);
    }

    private void initImage() {
        if (getIntent() != null) {

            selfieImage = DatabaseManager.getInstance().findEditImage(getIntent().getIntExtra("id", 0));
            byte[] byteArray = selfieImage.getResult();
            w = getIntent().getIntExtra("w", 0);
            h = getIntent().getIntExtra("h", 0);
            Boolean camera = getIntent().getExtras().getBoolean("camera");
            if (byteArray != null) {
                image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                if (getIntent().getExtras().containsKey("camera")) {
                    if (camera) {
                        image = rotateBitmap(image, -90);
                    } else {
                        image = rotateBitmapSimple(image, 90);
                    }
                }
            }
            if (w > 0 && h > 0 && image != null) {
                image = Bitmap.createScaledBitmap(image, w, h, true);
            } else {
                Log.d("tag", "CutActivity initImage image, w, h null");
            }
            original = image;
            mainImageView.setMyWidth(w);
            mainImageView.setMyHeight(h);
            mainImageView.setBitmap(image);
        } else {
            Log.e(TAG, "intent is null ");
        }
    }

    private void initListeners() {
        cutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cropping();
            }

        });
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMain();
            }
        });
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                image = original;
                mainImageView.setBitmap(image);
                mainImageView.clear();
                mainImageView.invalidate();
            }
        });
    }

    private void cropping() {
        List<Point> points = mainImageView.getPoints();
        if (points.size() > 0) {
            Bitmap resultingImage = Bitmap.createBitmap(w, h, image.getConfig());
            Canvas canvas = new Canvas(resultingImage);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Path path = new Path();
            for (int i = 0; i < points.size(); i++) {
                path.lineTo(points.get(i).x, points.get(i).y);
            }
            canvas.drawPath(path, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(image, 0, 0, paint);
            image = resultingImage;

            mainImageView.setBitmap(image);
            mainImageView.clear();
            mainImageView.invalidate();
        } else {
            Log.d(TAG, "points is null");
        }

    }

    private void gotoMain() {
        Intent intent = new Intent(getApplicationContext(), MakeSelfieActivity.class);
        EditImage selfieImage = getEditImage();
        intent.putExtra("id", selfieImage.getId());
        intent.putExtra("w", w);
        intent.putExtra("h", h);
        startActivity(intent);
    }

    /**
     * save to database image for quickly work
     * 
     * @return EditImage
     */
    private EditImage getEditImage() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 45, stream);
        byte[] byteArray = stream.toByteArray();

        selfieImage.setResult(byteArray);
        DatabaseManager.getInstance().updateSelfie(selfieImage);
        return selfieImage;
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        matrix.preScale(1.0f, -1);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap rotateBitmapSimple(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
