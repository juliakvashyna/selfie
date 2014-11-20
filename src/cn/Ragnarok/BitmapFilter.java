package cn.Ragnarok;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;

import com.bigdropinc.selfieking.views.CutView;
import com.bigdropinc.selfieking.views.Point;
import com.jabistudio.androidjhlabs.filter.ExposureFilter;
import com.jabistudio.androidjhlabs.filter.GainFilter;
import com.jabistudio.androidjhlabs.filter.GammaFilter;
import com.jabistudio.androidjhlabs.filter.PosterizeFilter;
import com.jabistudio.androidjhlabs.filter.RescaleFilter;
import com.jabistudio.androidjhlabs.filter.SmearFilter;
import com.jabistudio.androidjhlabs.filter.util.AndroidUtils;

public class BitmapFilter {
    /**
     * filter style id;
     */
    public static final int WILLOW = 1; // gray scale

    public static final int RELIEF_STYLE = 2; // relief
    public static final int AVERAGE_BLUR_STYLE = 3; // average blur
    public static final int OIL_STYLE = 4; // oil painting
    public static final int NEON_STYLE = 5; // neon
    public static final int PIXELATE_STYLE = 6; // pixelate
    public static final int TV_STYLE = 7; // Old TV
    public static final int BLOCK_STYLE = 9; // engraving
    public static final int OLD_STYLE = 10; // old photo
    public static final int SHARPEN_STYLE = 11; // sharpen
    public static final int LIGHT_STYLE = 12; // light
    public static final int LOMO_STYLE = 13; // lomo
    public static final int HDR_STYLE = 14; // HDR
    public static final int GAUSSIAN_BLUR_STYLE = 15; // gaussian blur
    public static final int SOFT_GLOW_STYLE = 16; // soft glow
    public static final int SKETCH_STYLE = 17; // sketch style
    public static final int MOTION_BLUR_STYLE = 18; // motion blur
    public static final int GOTHAM_STYLE = 19; // gotham style
    public static final int NORMAL = 20;
    public static final int GAMMA_1 = 21;
    public static final int GAMMA_2 = 22;
    public static final int GAMMA_3 = 23;
    public static final int HUDSON = 24;
    public static final int COLOR_RED = 25;
    public static final int COLOR_GREEN = 26;
    public static final int GAMMA = 27;
    public static final int SIERRA = 28;
    public static final int SEPIA_GREEN = 29;

    public static final int EARLYBIRD = 30;
    public static final int SYTRO = 31;
    public static final int TOASTER = 32;
    public static final int BRANNA = 33;
    public static final int AMARO = 34;
    public static final int MAYFAI = 35;
    public static final int VALENCI = 36;
    public static final int HERE = 37;
    public static final int BOOST_GREEN = 38;
    public static final int LOFI = 39;
    public static final int RISE = 40;
    public static final int EARLY = 41;
    public static final int TOTAL_FILTER_NUM = RISE;
    public static CutView mainImageView;
    public static Bitmap selfie;
    public static Matrix matrix;
    public static List<Point> points;

    public static int XPRO = 42;

    /**
     * change bitmap filter style
     * 
     * @param bitmap
     *            , filter style id
     */
    public static Bitmap changeStyle(Bitmap bitmap, int styleNo) {
        if (styleNo == WILLOW) {
            return GrayFilter.changeToGray(bitmap);
        } else if (styleNo == AMARO) {

            GainFilter filter = new GainFilter();
            filter.setBias((float) 0.6);
            filter.setGain(0.7f);
            int[] src = AndroidUtils.bitmapToIntArray(bitmap);
            src = filter.filter(src, bitmap.getWidth(), bitmap.getHeight());
            return Bitmap.createBitmap(src, bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        } else if (styleNo == EARLY) {
            final EarlyBird filter = new EarlyBird();
            bitmap = filter.transform(bitmap);
            return bitmap;
        } else if (styleNo == MAYFAI) {
            return doColorFilter(bitmap, 0.5, 0.3, 0.4);
        } else if (styleNo == XPRO) {
            return doColorFilter(bitmap, 0.1, 0.3, 0.5);
        } else if (styleNo == VALENCI) {
            return doPosterizeFilter(bitmap, 15);
        } else if (styleNo == RISE) {
            return doPosterizeFilter(bitmap, 35);
        } else if (styleNo == HERE) {
            return doExposureFilter(bitmap);
        } else if (styleNo == BOOST_GREEN) {
            return boost(bitmap, 2, 150);
        } else if (styleNo == LOFI) {
            return doInvertFilter(bitmap);
        } else if (styleNo == GAMMA_1) {
            return doGamma(bitmap, 0.6, 0.6, 0.6);
        } else if (styleNo == GAMMA_2) {
            return doGamma(bitmap, 1.8, 1.8, 1.8);
        } else if (styleNo == GAMMA_3) {
            return doGamma(bitmap, 0.3, 0.3, 0.3);
        } else if (styleNo == HUDSON) {
            GainFilter filter = new GainFilter();
            filter.setBias((float) 0.3);
            filter.setGain(0.9f);
            int[] src = AndroidUtils.bitmapToIntArray(bitmap);
            src = filter.filter(src, bitmap.getWidth(), bitmap.getHeight());
            return Bitmap.createBitmap(src, bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        } else if (styleNo == COLOR_RED) {
            return doColorFilter(bitmap, 0.5, 0.2, 0);
        } else if (styleNo == COLOR_GREEN) {
            return doColorFilter(bitmap, 0.2, 0.4, 0.2);
        } else if (styleNo == GAMMA) {
            return doGammaFilter(bitmap);
        } else if (styleNo == EARLYBIRD) {
            return createSepiaToningEffect(bitmap, 150, 0.6, 0.3, 0.1);
        } else if (styleNo == SEPIA_GREEN) {
            return createSepiaToningEffect(bitmap, 150, 0.2, 0.6, 0.3);
        } else if (styleNo == SIERRA) {
            return createSepiaToningEffect(bitmap, 150, 0.2, 0.2, 0.6);
        } else if (styleNo == SYTRO) {
            return doBrightness(bitmap, -30);
        } else if (styleNo == TOASTER) {
            return doBrightness(bitmap, 80);
        } else if (styleNo == BRANNA) {
            return doRescaleFilter(bitmap);
        }
        return bitmap;
    }

    public static Bitmap doFilter(Bitmap bitmap, boolean iscropped, final EarlyBird filter1) {
        int width = mainImageView.getMyWidth();
        int height = mainImageView.getMyHeight();

        if (iscropped) {
            bitmap = filter1.transform(selfie);
            List<Point> points = BitmapFilter.points;
            Bitmap resultingImage = Bitmap.createBitmap(width, height, bitmap.getConfig());

            Canvas canvas = new Canvas(resultingImage);
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            Path path = new Path();
            for (int i = 0; i < points.size(); i++) {
                path.lineTo(points.get(i).x, points.get(i).y);
            }
            canvas.drawPath(path, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, 0, 0, paint);
            bitmap = resultingImage;
        } else {
            bitmap = filter1.transform(bitmap);
        }
        return bitmap;
    }

    private static Bitmap doGammaFilter(Bitmap bitmap) {
        GammaFilter filter = new GammaFilter();
        filter.setGamma(5);
        int[] src = AndroidUtils.bitmapToIntArray(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // int[] dest = invertFilter.filter(src, width, height);
        int[] dest = filter.filter(src, width, height);

        Bitmap destBitmap = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);
        return destBitmap;
    }

    // lines
    private static Bitmap doPosterizeFilter(Bitmap bitmap, int level) {
        PosterizeFilter filter = new PosterizeFilter();
        filter.setNumLevels(level);
        int[] src = AndroidUtils.bitmapToIntArray(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // int[] dest = invertFilter.filter(src, width, height);
        int[] dest = filter.filter(src, width, height);

        Bitmap destBitmap = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);
        return destBitmap;
    }

    // bright
    private static Bitmap doRescaleFilter(Bitmap bitmap) {
        RescaleFilter filter = new RescaleFilter();
        filter.setScale(4);
        int[] src = AndroidUtils.bitmapToIntArray(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // int[] dest = invertFilter.filter(src, width, height);
        int[] dest = filter.filter(src, width, height);

        Bitmap destBitmap = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);
        return destBitmap;
    }

    // light
    private static Bitmap doExposureFilter(Bitmap bitmap) {
        ExposureFilter filter = new ExposureFilter();
        filter.setExposure(5);
        int[] src = AndroidUtils.bitmapToIntArray(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // int[] dest = invertFilter.filter(src, width, height);
        int[] dest = filter.filter(src, width, height);

        Bitmap destBitmap = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);
        return destBitmap;
    }

    private static Bitmap doInvertFilter(Bitmap bitmap) {

        SmearFilter filter = new SmearFilter();

        int[] src = AndroidUtils.bitmapToIntArray(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // int[] dest = invertFilter.filter(src, width, height);
        int[] dest = filter.filter(src, width, height);

        Bitmap destBitmap = Bitmap.createBitmap(dest, width, height, Config.ARGB_8888);
        return destBitmap;

    }

    private static Bitmap boost(Bitmap src, int type, float percent) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        int A, R, G, B;
        int pixel;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                if (type == 1) {
                    R = (int) (R * (1 + percent));
                    if (R > 255) {
                        R = 255;
                    }
                } else if (type == 2) {
                    G = (int) (G * (1 + percent));
                    if (G > 255) {
                        G = 255;
                    }
                } else if (type == 3) {
                    B = (int) (B * (1 + percent));
                    if (B > 255) {
                        B = 255;
                    }
                }
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bmOut;
    }

    public static Bitmap doGamma(Bitmap src, double red, double green, double blue) {
        // create output image
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();
        // color information
        int A, R, G, B;
        int pixel;
        // constant value curve
        final int MAX_SIZE = 256;
        final double MAX_VALUE_DBL = 255.0;
        final int MAX_VALUE_INT = 255;
        final double REVERSE = 1.0;

        // gamma arrays
        int[] gammaR = new int[MAX_SIZE];
        int[] gammaG = new int[MAX_SIZE];
        int[] gammaB = new int[MAX_SIZE];

        // setting values for every gamma channels
        for (int i = 0; i < MAX_SIZE; ++i) {
            gammaR[i] = (int) Math.min(MAX_VALUE_INT, (int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / red)) + 0.5));
            gammaG[i] = (int) Math.min(MAX_VALUE_INT, (int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / green)) + 0.5));
            gammaB[i] = (int) Math.min(MAX_VALUE_INT, (int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / blue)) + 0.5));
        }

        // apply gamma table
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // look up gamma
                R = gammaR[Color.red(pixel)];
                G = gammaG[Color.green(pixel)];
                B = gammaB[Color.blue(pixel)];
                // set new color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    private static Bitmap doColorFilter(Bitmap src, double red, double green, double blue) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                // apply filtering on each channel R, G, B
                A = Color.alpha(pixel);
                R = (int) (Color.red(pixel) * red);
                G = (int) (Color.green(pixel) * green);
                B = (int) (Color.blue(pixel) * blue);
                // set new color pixel to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    private static Bitmap createSepiaToningEffect(Bitmap src, int depth, double red, double green, double blue) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // constant grayscale
        final double GS_RED = 0.3;
        final double GS_GREEN = 0.59;
        final double GS_BLUE = 0.11;
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                // get color on each channel
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // apply grayscale sample
                B = G = R = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);

                // apply intensity level for sepid-toning on each channel
                R += (depth * red);
                if (R > 255) {
                    R = 255;
                }

                G += (depth * green);
                if (G > 255) {
                    G = 255;
                }

                B += (depth * blue);
                if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output image
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    private static Bitmap doBrightness(Bitmap src, int value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += value;
                if (R > 255) {
                    R = 255;
                } else if (R < 0) {
                    R = 0;
                }

                G += value;
                if (G > 255) {
                    G = 255;
                } else if (G < 0) {
                    G = 0;
                }

                B += value;
                if (B > 255) {
                    B = 255;
                } else if (B < 0) {
                    B = 0;
                }

                // apply new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    // private static Bitmap sharpen(Bitmap src, double weight) {
    // double[][] SharpConfig = new double[][] { { 0, -2, 0 }, { -2, weight, -2
    // }, { 0, -2, 0 } };
    // ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
    // convMatrix.applyConfig(SharpConfig);
    // convMatrix.Factor = weight - 8;
    // return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    // }

    private static class ConvolutionMatrix {
        public static final int SIZE = 3;

        public double[][] Matrix;
        public double Factor = 1;
        public double Offset = 1;

        public ConvolutionMatrix(int size) {
            Matrix = new double[size][size];
        }

        private void setAll(double value) {
            for (int x = 0; x < SIZE; ++x) {
                for (int y = 0; y < SIZE; ++y) {
                    Matrix[x][y] = value;
                }
            }
        }

        public void applyConfig(double[][] config) {
            for (int x = 0; x < SIZE; ++x) {
                for (int y = 0; y < SIZE; ++y) {
                    Matrix[x][y] = config[x][y];
                }
            }
        }

        static Bitmap computeConvolution3x3(Bitmap src, ConvolutionMatrix matrix) {
            int width = src.getWidth();
            int height = src.getHeight();
            Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());

            int A, R, G, B;
            int sumR, sumG, sumB;
            int[][] pixels = new int[SIZE][SIZE];

            for (int y = 0; y < height - 2; ++y) {
                for (int x = 0; x < width - 2; ++x) {

                    // get pixel matrix
                    for (int i = 0; i < SIZE; ++i) {
                        for (int j = 0; j < SIZE; ++j) {
                            pixels[i][j] = src.getPixel(x + i, y + j);
                        }
                    }

                    // get alpha of center pixel
                    A = Color.alpha(pixels[1][1]);

                    // init color sum
                    sumR = sumG = sumB = 0;

                    // get sum of RGB on matrix
                    for (int i = 0; i < SIZE; ++i) {
                        for (int j = 0; j < SIZE; ++j) {
                            sumR += (Color.red(pixels[i][j]) * matrix.Matrix[i][j]);
                            sumG += (Color.green(pixels[i][j]) * matrix.Matrix[i][j]);
                            sumB += (Color.blue(pixels[i][j]) * matrix.Matrix[i][j]);
                        }
                    }

                    // get final Red
                    R = (int) (sumR / matrix.Factor + matrix.Offset);
                    if (R < 0) {
                        R = 0;
                    } else if (R > 255) {
                        R = 255;
                    }

                    // get final Green
                    G = (int) (sumG / matrix.Factor + matrix.Offset);
                    if (G < 0) {
                        G = 0;
                    } else if (G > 255) {
                        G = 255;
                    }

                    // get final Blue
                    B = (int) (sumB / matrix.Factor + matrix.Offset);
                    if (B < 0) {
                        B = 0;
                    } else if (B > 255) {
                        B = 255;
                    }

                    // apply new pixel
                    result.setPixel(x + 1, y + 1, Color.argb(A, R, G, B));
                }
            }

            // final image
            return result;
        }
    }

    public static final double PI = 3.14159d;
    public static final double FULL_CIRCLE_DEGREE = 360d;
    public static final double HALF_CIRCLE_DEGREE = 180d;
    public static final double RANGE = 256d;

    public static Bitmap applyMeanRemoval(Bitmap src) {
        double[][] MeanRemovalConfig = new double[][] { { -1, -1, -1 }, { -1, 9, -1 }, { -1, -1, -1 } };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(MeanRemovalConfig);
        convMatrix.Factor = 1;
        convMatrix.Offset = 0;
        return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }

    private static Bitmap tintImage(Bitmap src, int degree) {

        int width = src.getWidth();
        int height = src.getHeight();

        int[] pix = new int[width * height];
        src.getPixels(pix, 0, width, 0, 0, width, height);

        int RY, GY, BY, RYY, GYY, BYY, R, G, B, Y;
        double angle = (PI * (double) degree) / HALF_CIRCLE_DEGREE;

        int S = (int) (RANGE * Math.sin(angle));
        int C = (int) (RANGE * Math.cos(angle));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (pix[y * width + x] != 0) {
                    int index = y * width + x;
                    int r = (pix[index] >> 16) & 0xff;
                    int g = (pix[index] >> 8) & 0xff;
                    int b = pix[index] & 0xff;
                    RY = (70 * r - 59 * g - 11 * b) / 100;
                    GY = (-30 * r + 41 * g - 11 * b) / 100;
                    BY = (-30 * r - 59 * g + 89 * b) / 100;
                    Y = (30 * r + 59 * g + 11 * b) / 100;
                    RYY = (S * BY + C * RY) / 256;
                    BYY = (C * BY - S * RY) / 256;
                    GYY = (-51 * RYY - 19 * BYY) / 100;
                    R = Y + RYY;
                    R = (R < 0) ? 0 : ((R > 255) ? 255 : R);
                    G = Y + GYY;
                    G = (G < 0) ? 0 : ((G > 255) ? 255 : G);
                    B = Y + BYY;
                    B = (B < 0) ? 0 : ((B > 255) ? 255 : B);
                    pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;
                }
            }
        }

        Bitmap outBitmap = Bitmap.createBitmap(width, height, src.getConfig());
        outBitmap.setPixels(pix, 0, width, 0, 0, width, height);

        pix = null;

        return outBitmap;
    }
}
