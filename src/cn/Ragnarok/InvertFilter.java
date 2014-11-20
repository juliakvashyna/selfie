package cn.Ragnarok;

import android.graphics.Bitmap;
import android.graphics.Color;

public class InvertFilter {

	public static Bitmap chageToInvert(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Bitmap returnBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		int[] colorArray = new int[width * height];
		int r, g, b;
		bitmap.getPixels(colorArray, 0, width, 0, 0, width, height);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				r = 255 - Color.red(colorArray[y * width + x]);
				g = 255 - Color.green(colorArray[y * width + x]);
				b = 255 - Color.blue(colorArray[y * width + x]);
				if (colorArray[y * width + x] != 0) {
					colorArray[y * width + x] = Color.rgb(r, g, b);
				} else {
					colorArray[y * width + x] = Color.argb(1, 0, 0, 0);
				}
				returnBitmap.setPixel(x, y, colorArray[y * width + x]);

			}
		}

		return returnBitmap;

	}
}
