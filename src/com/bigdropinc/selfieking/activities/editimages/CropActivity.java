package com.bigdropinc.selfieking.activities.editimages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.ColorDrawable;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
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
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.edmodo.cropper.CropImageView;

public class CropActivity extends Activity {

    private static final String TAG = null;
    private Button button;
    private Button back;
    private CropImageView imageView;
    private Bitmap image;
    private EditImage selfieImage;
    private ProgressDialog dialog;
    private int w;
    private int h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_crop);
        initViews();
        initListeners();
        initImage();
      
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null)
            dialog.cancel();
    }

    private void initListeners() {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainActivity();
            }
        });
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void goToMainActivity() {
        new AsyncTask<Void, Void, Intent>() {
            protected void onPreExecute() {
                dialog = ProgressDialog.show(CropActivity.this, "", "");
                dialog.setContentView(new ProgressBar(CropActivity.this), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            };

            @Override
            protected Intent doInBackground(Void... params) {
                Intent intent = getCutActivityIntent();
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

    private Intent getCutActivityIntent() {
        Intent intent = new Intent(getApplicationContext(), CutActivity.class);
        // int w = imageView.getWidth();
        int h = (int) (imageView.getHeight() - imageView.getHeight() / 3);
        intent.putExtra("w", imageView.getCroppedImage().getWidth());
        intent.putExtra("h", h);
        selfieImage = getEditImage();
        intent.putExtra("id", selfieImage.getId());
        return intent;
    }

    private EditImage getEditImage() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        imageView.getCroppedImage().compress(Bitmap.CompressFormat.PNG, 0, stream);

        imageView.getCroppedImage().recycle();
        byte[] byteArray = stream.toByteArray();

        selfieImage.setResult(byteArray);
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        stream = null;
        DatabaseManager.getInstance().updateSelfie(selfieImage);
        return selfieImage;
    }

    private void initViews() {
        imageView = (CropImageView) findViewById(R.id.bigcropImageView);
        button = (Button) findViewById(R.id.next);
        back = (Button) findViewById(R.id.cropBack);
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

            Log.d(TAG, "bitmap size - width: " + b.getWidth() + ", height: " + b.getHeight());
            return b;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    private void initImage() {
        if (getIntent() != null) {
            Uri uri = getIntent().getExtras().getParcelable(SelectImageActivity.EXTRA_IMAGE_URI);
            if (uri != null) {
                image = getBitmap(uri);
                selfieImage = new EditImage();
                selfieImage = DatabaseManager.getInstance().addSelfie(selfieImage);
            } else {
                selfieImage = DatabaseManager.getInstance().findEditImage(getIntent().getIntExtra("id", 0));
                byte[] byteArray = selfieImage.getResult();
                w = getIntent().getIntExtra("w", 0);
                h = getIntent().getIntExtra("h", 0);
                if (byteArray != null) {
                    try {
                        image = getImage(byteArray);
                        rotating();
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(this, "Sorry, image error ", Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
        imageView.setImageBitmap(image);
        imageView.setFixedAspectRatio(true);
        imageView.setGuidelines(2);
    }

    private void rotating() {
        Boolean rotate = getIntent().getExtras().getBoolean("rotate");
        if (getIntent().getExtras().containsKey("rotate") && image != null) {

            if (!rotate) {
                image = rotateBitmap(image, -90);
            } else {
                image = rotateBitmapSimple(image, 90);
            }
        }
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

    private Bitmap getImage(byte[] byteArray) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        // options.inSampleSize = 2;
        options.inDither = true;
        // options.inPreferredConfig = Config.RGB_565;
        image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        if (w > 0 && h > 0 && image != null)
            return Bitmap.createScaledBitmap(image, w, h, true);
        return image;
    }
}
