package com.bigdropinc.selfieking.model.selfie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "selfie")
public class EditImage {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    byte[] imageBytes;
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    byte[] filterImageBytes;

    private Bitmap originalImage;
    private Bitmap croppedBitmap;
    private Bitmap background;
    // private Filter filter;
    @DatabaseField
    private int width;
    @DatabaseField
    private int height;
    private boolean filterclick;
    private Matrix matrix;

    // private PorterDuffColorFilter colorFilter;

    public EditImage(int id) {
        this.id = id;
    }

    public EditImage() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getFilterImageBytes() {
        return filterImageBytes;
    }

    public void setFilterImageBytes(byte[] filterImageBytes) {
        this.filterImageBytes = filterImageBytes;
    }

    // public PorterDuffColorFilter getColorFilter() {
    // return colorFilter;
    // }
    //
    // public void setColorFilter(PorterDuffColorFilter colorFilter) {
    // this.colorFilter = colorFilter;
    // }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public boolean isFilterclick() {
        return filterclick;
    }

    public void setFilterclick(boolean filterclick) {
        this.filterclick = filterclick;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = (int) (width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = (int) (height);
    }

    @Override
    public String toString() {
        int len = 0;
        if (imageBytes != null) {
            len = imageBytes.length;
        }
        return "EditImage [id=" + id + ", imageBytes=" + len + ", originalImage=" + originalImage + ", croppedBitmap=" + croppedBitmap + ", background=" + background + ", width=" + width + ", height=" + height + ", filterclick=" + filterclick
                + ", matrix=" + matrix + "]";
    }

    private Bitmap doOverlayBackdround(Bitmap bitmap, boolean filterclick) {
        Bitmap workingBitmap = Bitmap.createBitmap(background);
        // Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888,
        // true);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        // Canvas canvas = new Canvas(bmOverlay);
        // if (!filter)
        // canvas.drawBitmap(bmp1, new Matrix(), null);
        if (matrix != null)
            canvas.drawBitmap(bitmap, matrix, null);
        else
            canvas.drawBitmap(bitmap, new Matrix(), null);
        // ByteArrayOutputStream out = new ByteArrayOutputStream();
        // BitmapFactory.Options options = new BitmapFactory.Options();
        // options.inPurgeable = true;
        // options.inJustDecodeBounds = false;
        // options.inSampleSize = 2;
        // options.inDither = true;
        // mutableBitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
        // mutableBitmap = BitmapFactory.decodeStream(new
        // ByteArrayInputStream(out.toByteArray()), null, options);
        return mutableBitmap;
    }

    public Bitmap getOriginalImage() {
        return originalImage;
    }

    public void setOriginalImage(Bitmap originalImage) {
        this.originalImage = originalImage;
    }

    public Bitmap getCroppedBitmap() {
        return croppedBitmap;
    }

    public void setCroppedBitmap(Bitmap croppedBitmap) {
        this.croppedBitmap = croppedBitmap;
    }

    public Bitmap getBackground() {
        if (background != null) {
            return createFilterImage(background);
        }
        return background;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public Bitmap createFilterImage(Bitmap image) {
        Canvas canvas = new Canvas();

        Bitmap result = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        // Color

        // paint.setColorFilter(colorFilter);
        canvas.drawBitmap(image, 0, 0, paint);
        // paint.setColorFilter(null);
        // paint.setXfermode(new PorterDuffXfermode(Mode.SRC));

        return result;
    }

    public Bitmap getSelfieWithOutBackground() {
        Bitmap bitmap = croppedBitmap;
        if (matrix == null) {
            croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, width, height, true);
        } else if (width > 0 && height > 0 && width <= croppedBitmap.getWidth() && height <= croppedBitmap.getHeight()) {
            croppedBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, width, height, matrix, false);
        }
        //
        if (croppedBitmap != null) {
            bitmap = croppedBitmap;
        }
        // else if (colorFilter != null) {
        // bitmap = createFilterImage(originalImage, colorFilter);
        // }
        return bitmap;
    }

    public Bitmap getSelfieWithBackground() {
        Bitmap bitmap = null;
        if (originalImage != null) {
            bitmap = Bitmap.createScaledBitmap(originalImage, width, height, true);
            if (background != null && croppedBitmap != null) {
                // if (colorFilter != null) {
                // bitmap = doOverlayBackdround(croppedBitmap, true);
                // bitmap = createFilterImage(bitmap, colorFilter);
                // } else {
                bitmap = doOverlayBackdround(croppedBitmap, false);
                // }

            } else {
                bitmap = getSelfieWithOutBackground();
            }
            createBytes(bitmap);
        }
        return bitmap;
    }

    public void createBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        // bitmap.recycle();
        imageBytes = stream.toByteArray();
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        stream = null;
    }
    public void createBytesFilter(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        // bitmap.recycle();
        filterImageBytes = stream.toByteArray();
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        stream = null;
    }

    private Bitmap getSelfieWithBackgroundWithOutFilter() {
        Bitmap bitmap = null;
        bitmap = Bitmap.createScaledBitmap(originalImage, width, height, true);
        if (background != null && croppedBitmap != null) {
            bitmap = doOverlayBackdround(croppedBitmap, false);
        } else {
            bitmap = getSelfieWithOutBackgroundithOutFilter();
        }
        return bitmap;
    }

    private Bitmap getSelfieWithOutBackgroundithOutFilter() {
        Bitmap bitmap = croppedBitmap;
        if (matrix == null) {
            croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, width, height, true);
        } else if (width > 0 && height > 0 && width <= croppedBitmap.getWidth() && height <= croppedBitmap.getHeight()) {
            croppedBitmap = Bitmap.createBitmap(croppedBitmap, 0, 0, width, height, matrix, false);
        }
        if (croppedBitmap != null) {
            bitmap = croppedBitmap;
        }
        return bitmap;
    }

    public byte[] getResult() {
        return imageBytes;
    }

    public void setResult(byte[] result) {
        this.imageBytes = result;
    }
}
