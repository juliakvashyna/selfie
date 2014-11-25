package com.bigdropinc.selfieking.model.selfie;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.bigdropinc.selfieking.controller.managers.login.LoginManagerImpl;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "selfie")
public class EditImage {
    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    @DatabaseField(generatedId = true)
    private int id;

    private Bitmap originalImage;

    private Bitmap croppedBitmap;

    private Bitmap background;
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    byte[] imageBytes;

    private int width;
    private int height;
    private boolean filterclick;
    private Matrix matrix;
    private PorterDuffColorFilter colorFilter;

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

    public PorterDuffColorFilter getColorFilter() {
        return colorFilter;
    }

    public void setColorFilter(PorterDuffColorFilter colorFilter) {
        this.colorFilter = colorFilter;
    }

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

    private Bitmap doOverlayBackdround(Bitmap bitmap, boolean filterclick) {
        return overlay(background, bitmap, filterclick);
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
        if (background != null && colorFilter != null) {
            return createFilterImage(background);
        }
        return background;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    private Bitmap createFilterCropImage(Bitmap image) {
        Canvas canvas = new Canvas();
        Bitmap cropImage = doHighlightImage(image);
        Bitmap result = Bitmap.createBitmap(cropImage.getWidth(), cropImage.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(result);
        Paint paint = new Paint();
        // paint.setFilterBitmap(false);
        // Color

        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(cropImage, 0, 0, paint);
        paint.setColorFilter(null);

        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawBitmap(cropImage, 0, 0, paint);
        paint.setXfermode(null);
        return result;
    }

    public static Bitmap doHighlightImage(Bitmap src) {
        // create new bitmap, which will be painted and becomes result image
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth() + 96, src.getHeight() + 96, Bitmap.Config.ARGB_8888);
        // setup canvas for painting
        Canvas canvas = new Canvas(bmOut);
        // setup default color
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        // create a blur paint for capturing alpha
        Paint ptBlur = new Paint();
        ptBlur.setMaskFilter(new BlurMaskFilter(15, Blur.NORMAL));
        int[] offsetXY = new int[2];
        // capture alpha into a bitmap
        Bitmap bmAlpha = src.extractAlpha(ptBlur, offsetXY);
        // create a color paint
        Paint ptAlphaColor = new Paint();
        ptAlphaColor.setColor(0xFFFFFFFF);
        // paint color for captured alpha region (bitmap)
        canvas.drawBitmap(bmAlpha, offsetXY[0], offsetXY[1], ptAlphaColor);
        // free memory
        bmAlpha.recycle();

        // paint the image source
        canvas.drawBitmap(src, 0, 0, null);

        // return out final image
        return bmOut;
    }

    private Bitmap createFilterImage(Bitmap image) {
        Canvas canvas = new Canvas();

        Bitmap result = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        // Color

        paint.setColorFilter(colorFilter);
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
        if (croppedBitmap != null && colorFilter != null) {
            bitmap = createFilterCropImage(croppedBitmap);
        } else if (croppedBitmap != null) {
            bitmap = croppedBitmap;
        } else if (colorFilter != null) {
            bitmap = createFilterImage(originalImage);
        }
        return bitmap;
    }

    public Bitmap getSelfieWithBackground() {
        Bitmap bitmap = null;

        bitmap = Bitmap.createScaledBitmap(originalImage, width, height, true);
        if (background != null && croppedBitmap != null) {
            if (colorFilter != null) {
                bitmap = doOverlayBackdround(croppedBitmap, true);
                bitmap = createFilterImage(bitmap);
            } else {
                bitmap = doOverlayBackdround(croppedBitmap, false);
            }

        } else {
            bitmap = getSelfieWithOutBackground();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 15, stream);

        // byte[] byteArray = stream.toByteArray();

        imageBytes = stream.toByteArray();

        return bitmap;
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2, boolean filter) {
        Bitmap bmOverlay = Bitmap.createScaledBitmap(bmp1, width, height, true);
        Canvas canvas = new Canvas(bmOverlay);
        // if (!filter)
        // canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, matrix, null);
        return bmOverlay;
    }

    public byte[] getResult() {
        return imageBytes;
    }

    public void setResult(byte[] result) {
        this.imageBytes = result;
    }
}
