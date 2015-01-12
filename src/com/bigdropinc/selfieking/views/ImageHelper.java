package com.bigdropinc.selfieking.views;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

public class ImageHelper {
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        // Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
        // bitmap.getHeight(), Config.ARGB_8888);
        // Canvas canvas = new Canvas(output);
        //
        // final int color = 0xff424242;
        // final Paint paint = new Paint();
        // final Paint paint2 = new Paint();
        // paint2.setColor(Color.WHITE);
        // final Rect rect = new Rect(0, 0, bitmap.getWidth(),
        // bitmap.getHeight());
        // final RectF rectF = new RectF(rect);
        //
        // final float roundPx = pixels;
        //
        // paint.setAntiAlias(true);
        // canvas.drawARGB(0, 0, 0, 0);
        // paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint2);
        //
        // paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        //
        // canvas.drawBitmap(bitmap, rect, rect, paint);
        // BitmapShader shader;
        // shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
        // Shader.TileMode.CLAMP);
        //
        // Paint paint = new Paint();
        // paint.setAntiAlias(true);
        // paint.setShader(shader);
        //
        // float width = bitmap.getWidth();
        // float height = bitmap.getHeight();
        // RectF rectF = new RectF(0.0f, 0.0f, width, height);
        //
        // Canvas canvas = new Canvas();
        // float radius = pixels;
        // // rect contains the bounds of the shape
        // // radius is the radius in pixels of the rounded corners
        // // paint contains the shader that will texture the shape
        // canvas.drawRoundRect(rectF, radius, radius, paint);
        // final Rect rect = new Rect(0, 0, bitmap.getWidth(),
        // bitmap.getHeight());
        // Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
        // bitmap.getHeight(), Config.ARGB_8888);
        // canvas.drawBitmap(output, rect, rectF, paint);



        return bitmap;
    }

    private static Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize, bmp.getHeight() + borderSize, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }
}
