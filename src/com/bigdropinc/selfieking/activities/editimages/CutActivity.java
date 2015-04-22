package com.bigdropinc.selfieking.activities.editimages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.customs.MyProgressDialog;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.bigdropinc.selfieking.views.CutView;
import com.bigdropinc.selfieking.views.Point;

public class CutActivity extends Activity {

    private int w;
    private int h;
    private Bitmap image;
    private Bitmap original;
    public static CutView mainImageView;
    private ProgressDialog dialog;
    private String TAG = "tag";
    private Button cutButton;
    private Button nextButton;
    private Button backButton;
    private Button clearButton;
    private Button drawButton;
    private EditImage selfieImage;
    private boolean isCrop;
    private boolean isDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);
        initViews();
        initImage();
        initListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null)
            dialog.cancel();
    }

    private void initViews() {
        mainImageView = (CutView) findViewById(R.id.mainImage);
        cutButton = (Button) findViewById(R.id.cut);
        clearButton = (Button) findViewById(R.id.clear);
        nextButton = (Button) findViewById(R.id.next);
        backButton = (Button) findViewById(R.id.cutBack);
        drawButton = (Button) findViewById(R.id.draw);
    }

    private void initImage() {
        if (getIntent() != null) {
            selfieImage = DatabaseManager.getInstance().findEditImage(getIntent().getIntExtra("id", 0));
            if (selfieImage != null) {
                // byte[] byteArray = selfieImage.getResult();
                w = getWindowManager().getDefaultDisplay().getWidth();
                // w = getIntent().getIntExtra("w", 0);
                h = getIntent().getIntExtra("h", 0);
                // if (byteArray != null) {
                try {
                    // image = getImage(byteArray);
                    image = ShareActivity.loadImageFromStorage(selfieImage.getPath());
                    image = Bitmap.createScaledBitmap(image, w, h, true);
                    rotating();
                } catch (OutOfMemoryError e) {
                    Toast.makeText(this, "Sorry, image error ", Toast.LENGTH_LONG).show();
                }

                // }
                original = image;
                mainImageView.setMyWidth(w);
                mainImageView.setMyHeight(h);
                mainImageView.setBitmap(image);
            } else {
                Toast.makeText(this, "Sorry, too big image", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "intent is null ");
        }
    }

    private Bitmap getImage(byte[] byteArray) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inJustDecodeBounds = false;
        options.inDither = true;
        // options.inSampleSize = 2;
        image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        if (w > 0 && h > 0)
            return Bitmap.createScaledBitmap(image, w, h, true);
        return image;

    }

    private void rotating() {
        Boolean rotate = getIntent().getExtras().getBoolean("rotate");
        if (getIntent().getExtras().containsKey("rotate")) {
            if (!rotate) {
                image = rotateBitmap(image, -90);
            } else {
                image = rotateBitmapSimple(image, 90);
            }
        }
    }

    private void initListeners() {
        cutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cropping();
                } catch (OutOfMemoryError e) {
                    Toast.makeText(CutActivity.this, "Sorry, image error ", Toast.LENGTH_LONG).show();
                }
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
                isCrop = false;
            }
        });
        backButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        drawButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isDraw = !isDraw;
                // mainImageView.setDrawTools(isDraw);
            }
        });
    }

    private void cropping() {
        isCrop = true;
        // mainImageView.zooming = false;
        List<Point> points = mainImageView.getPoints();
        if (points.size() > 0) {
            Bitmap resultingImage;
            if (w > 0 && h > 0)
                resultingImage = Bitmap.createBitmap(w, h, image.getConfig());
            else {
                resultingImage = image.copy(image.getConfig(), true);

            }
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
            Toast.makeText(CutActivity.this, "Draw selfie ", Toast.LENGTH_LONG).show();
        }

    }

    private void gotoMain() {
        if (isCrop) {
            dialog = ProgressDialog.show(CutActivity.this, "", "");
            dialog.setContentView(new ProgressBar(CutActivity.this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            Intent intent = new Intent(getApplicationContext(), MakeSelfieActivity.class);
            EditImage selfieImage = getEditImage();
            intent.putExtra("id", selfieImage.getId());
            intent.putExtra("w", w);
            intent.putExtra("h", h);
            startActivity(intent);
            dialog.cancel();
        } else {
            Toast.makeText(CutActivity.this, "Draw selfie and press cut button", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * save to database image for quickly work
     * 
     * @return EditImage
     */
    private EditImage getEditImage() {
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//
//        image.compress(Bitmap.CompressFormat.PNG, 0, stream);
//        // image.recycle();
//        byte[] byteArray = stream.toByteArray();
//
//        selfieImage.setResult(byteArray);
//        try {
//            stream.flush();
//            stream.close();
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        }
//        stream = null;
        selfieImage.setPath(saveToInternalSorage(image));
        DatabaseManager.getInstance().updateSelfie(selfieImage);
        return selfieImage;
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
