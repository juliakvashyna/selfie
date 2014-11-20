package com.bigdropinc.selfieking.controller.managers;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;

import com.bigdropinc.selfieking.model.selfie.EditImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class FileManager {

    private static Context context;

    public static void init(Context context) {
        FileManager.context = context;
    }

    public static Bitmap createBitmapFromUri(Uri uri) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Config.RGB_565;
        options.inDither = true;
        try {
            return BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();

        }
        return null;
    }

    public static Bitmap getImageFromUri(Uri uri) {

        Bitmap image = null;
        try {
            image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // image = Bitmap.createScaledBitmap(image, 400, 400, false);
        return image;
    }

    public static Bitmap getImageFromUri(Uri uri, int reqWidth, int reqHeight) {
        if (reqWidth == 0 && reqHeight == 0) {
            return getImageFromUri(uri);
        }
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public Bitmap createImageFile(EditImage selfieImage) {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Images";
        File dir = new File(file_path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "image.png");
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            selfieImage.getSelfieWithOutBackground().compress(Bitmap.CompressFormat.PNG, 10, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Config.RGB_565;
            options.inDither = true;
            // return
            // FileManager.decodeScaledBitmapFromSdCard(file.getAbsolutePath(),
            // selfieImage.getWidth(), selfieImage.getHeight());
        } catch (OutOfMemoryError e) {
            Log.d("TAG", "OutOfMemoryError decodeFile");
        }
        return null;
    }

    public static Bitmap getBitmapFromAsset(String filePath) {

        try {
            return new DecodeAsync().execute(filePath).get();
        } catch (InterruptedException e) {

            e.printStackTrace();
        } catch (ExecutionException e) {

            e.printStackTrace();
        }
        return null;
    }

    static class DecodeAsync extends AsyncTask<String, Void, Bitmap> {
        private AssetManager assetManager = context.getAssets();

        @Override
        protected Bitmap doInBackground(String... params) {
            String filePath = params[0];
            InputStream istr = null;
            try {
                istr = assetManager.open(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return BitmapFactory.decodeStream(istr);
        }
    }

    public static Uri getUriFromiImage(Bitmap inImage) {
        Uri uri;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        uri = Uri.parse(path);
        return uri;
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Bitmap rotateIfNeed(String path, Bitmap image, Context context) {
        File imageFile = new File(path);
        ExifInterface ei = null;
        int orientation = 0;
        try {
            ei = new ExifInterface(imageFile.getAbsolutePath());
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (orientation) {
        case ExifInterface.ORIENTATION_ROTATE_90:
            return rotateBitmap(image, 90);
        case ExifInterface.ORIENTATION_ROTATE_180:
            return rotateBitmap(image, 180);
        case ExifInterface.ORIENTATION_ROTATE_270:
            return rotateBitmap(image, 270);
        default:
            break;
        }
        return image;
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
