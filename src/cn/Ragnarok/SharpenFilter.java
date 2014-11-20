package cn.Ragnarok;

import android.graphics.Bitmap;
import android.graphics.Color;

public class SharpenFilter {
	
	static {
		System.loadLibrary("AndroidImageFilter");
	}
	

	public static Bitmap changeToSharpen(Bitmap bitmap) {
		
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (pixels[y * width + x] == 0) {

					pixels[y * width + x] = Color.MAGENTA;

				}
			}
		}
		int[] returnPixels = NativeFilterFunc.sharpenFilter(pixels, width, height);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (returnPixels[y * width + x] == Color.MAGENTA) {

					returnPixels[y * width + x] = Color.argb(1, 0, 0, 0);

				}
			}
		}
		Bitmap returnBitmap = Bitmap.createBitmap(returnPixels, width, height, Bitmap.Config.ARGB_8888);
		return returnBitmap;
	}
}
