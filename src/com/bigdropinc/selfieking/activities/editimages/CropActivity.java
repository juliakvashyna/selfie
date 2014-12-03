package com.bigdropinc.selfieking.activities.editimages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.bigdrop.selfieking.db.DatabaseManager;
import com.bigdropinc.selfieking.R;
import com.bigdropinc.selfieking.model.selfie.EditImage;
import com.edmodo.cropper.CropImageView;

public class CropActivity extends Activity {

    private static final String TAG = null;
    private Button button;
    private CropImageView imageView;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_crop);
        initViews();
        initListeners();
        initImage();
    }

    private void initListeners() {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainActivity();
            }
        });
    }

    private void goToMainActivity() {
        new AsyncTask<Void, Void, Intent>() {
            @Override
            protected Intent doInBackground(Void... params) {
                Intent intent = getCutActivityIntent();
                return intent;
            }

            protected void onPostExecute(Intent result) {
                startActivity(result);
            };
        }.execute();
    }

    private Intent getCutActivityIntent() {
        Intent intent = new Intent(getApplicationContext(), CutActivity.class);
        int w = imageView.getWidth();
        int h = (int) (imageView.getHeight() - imageView.getHeight() / 3);
        intent.putExtra("w", w);
        intent.putExtra("h", h);
        EditImage selfieImage = getEditImage();
        intent.putExtra("id", selfieImage.getId());
        return intent;
    }

    private EditImage getEditImage() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageView.getCroppedImage().compress(Bitmap.CompressFormat.PNG, 45, stream);
        byte[] byteArray = stream.toByteArray();
        EditImage selfieImage = new EditImage();
        selfieImage.setResult(byteArray);
        selfieImage=   DatabaseManager.getInstance().addSelfie(selfieImage);
        return selfieImage;
    }

    private void initViews() {
        imageView = (CropImageView) findViewById(R.id.bigcropImageView);
        button = (Button) findViewById(R.id.next);
    }

    private Bitmap getBitmap(Uri uri) {
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
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
            } else {
                Log.d(TAG, "uri is null");
            }
        }
        imageView.setImageBitmap(image);
        imageView.setFixedAspectRatio(true);
        imageView.setGuidelines(2);
    }
}
