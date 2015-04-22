package com.bigdropinc.selfieking.activities.editimages;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
    private boolean fromBG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_crop);
        initViews();
        initListeners();
        initImage();
        initFromBG();

    }

    private void initFromBG() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            fromBG = getIntent().getExtras().getBoolean("fromBG");
        }
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
                if (fromBG) {
                    Intent data = new Intent();
                    int h = (int) (imageView.getHeight() - imageView.getHeight() / 3);
                    data.putExtra("w", imageView.getCroppedImage().getWidth());
                    data.putExtra("h", h);
                    data.setData(getImageUri(getApplicationContext(), imageView.getCroppedImage()));
                    CropActivity.this.setResult(MakeSelfieActivity.PIC_CROP, data);
                    CropActivity.this.finish();
                } else {
                    goToMainActivity();
                }
            }
        });
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
         int w = imageView.getWidth();
        int h = (int) (imageView.getHeight() - imageView.getHeight() / 3);
        intent.putExtra("w", imageView.getCroppedImage().getWidth());
        intent.putExtra("h", h);
        selfieImage = getEditImage();
        intent.putExtra("id", selfieImage.getId());
        return intent;
    }

    private EditImage getEditImage() {
        // ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // imageView.getCroppedImage().compress(Bitmap.CompressFormat.PNG, 0,
        // stream);

        // imageView.getCroppedImage().recycle();
        selfieImage.setPath(saveToInternalSorage(imageView.getCroppedImage()));
        // byte[] byteArray = stream.toByteArray();
        //
        // selfieImage.setResult(byteArray);
        // try {
        // stream.flush();
        // stream.close();
        // } catch (IOException e) {
        //
        // e.printStackTrace();
        // }
        // stream = null;
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
                try {
                    if (selfieImage.getPath() != null && selfieImage.getPath() != "") {
                        image = ShareActivity.loadImageFromStorage(selfieImage.getPath());
                    } else if (byteArray != null) {
                        image = getImage(byteArray);
                        rotating();
                    }
                    // image= Bitmap.createScaledBitmap(image, w, h, false);
                } catch (OutOfMemoryError e) {
                    Toast.makeText(this, "Sorry, image error ", Toast.LENGTH_LONG).show();
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
