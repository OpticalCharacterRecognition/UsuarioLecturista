/*
 * Copyright 2009 ZXing authors
 * Copyright 2011 Robert Theis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fourtails.usuariolecturista.ocr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import com.fourtails.usuariolecturista.MainActivity;
import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;

import java.util.ArrayList;

import Catalano.Imaging.Concurrent.Filters.Grayscale;
import Catalano.Imaging.Concurrent.Filters.Threshold;
import Catalano.Imaging.FastBitmap;

/**
 * This object extends LuminanceSource around an array of YUV data returned from the camera driver,
 * with the option to crop to a rectangle within the full data. This can be used to exclude
 * superfluous pixels around the perimeter and speed up decoding.
 * <p/>
 * It works for any pixel format where the Y channel is planar and appears first, including
 * YCbCr_420_SP and YCbCr_422_SP.
 * <p/>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
public final class PlanarYUVLuminanceSource extends LuminanceSource {

    public static String methodName;
    private final byte[] yuvData;
    private final int dataWidth;
    private final int dataHeight;
    private final int left;
    private final int top;

    public static Bitmap letsDoThis;

    public PlanarYUVLuminanceSource(byte[] yuvData,
                                    int dataWidth,
                                    int dataHeight,
                                    int left,
                                    int top,
                                    int width,
                                    int height,
                                    boolean reverseHorizontal) {
        super(width, height);

        if (left + width > dataWidth || top + height > dataHeight) {
            throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
        }

        this.yuvData = yuvData;
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        this.left = left;
        this.top = top;
        if (reverseHorizontal) {
            reverseHorizontal(width, height);
        }
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }
        int offset = (y + top) * dataWidth + left;
        System.arraycopy(yuvData, offset, row, 0, width);
        return row;
    }

    @Override
    public byte[] getMatrix() {
        int width = getWidth();
        int height = getHeight();

        // If the caller asks for the entire underlying image, save the copy and give them the
        // original data. The docs specifically warn that result.length must be ignored.
        if (width == dataWidth && height == dataHeight) {
            return yuvData;
        }

        int area = width * height;
        byte[] matrix = new byte[area];
        int inputOffset = top * dataWidth + left;

        // If the width matches the full width of the underlying data, perform a single copy.
        if (width == dataWidth) {
            System.arraycopy(yuvData, inputOffset, matrix, 0, area);
            return matrix;
        }

        // Otherwise copy one cropped row at a time.
        byte[] yuv = yuvData;
        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
            inputOffset += dataWidth;
        }
        return matrix;
    }

    @Override
    public boolean isCropSupported() {
        return true;
    }

    @Override
    public LuminanceSource crop(int left, int top, int width, int height) {
        return new PlanarYUVLuminanceSource(yuvData,
                dataWidth,
                dataHeight,
                this.left + left,
                this.top + top,
                width,
                height,
                false);
    }

    public Bitmap renderCroppedGreyscaleBitmap() {
        int width = getWidth();
        int height = getHeight();
        int[] pixels = new int[width * height];
        byte[] yuv = yuvData;
        int inputOffset = top * dataWidth + left;

        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            for (int x = 0; x < width; x++) {
                int grey = yuv[inputOffset + x] & 0xff;
                pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
            }
            inputOffset += dataWidth;
        }


        int var = CaptureActivity.seek;
        int var2 = CaptureActivity.seek1;


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        FastBitmap fastBitmap = new FastBitmap(bitmap);


        Grayscale grayscale = new Grayscale();
        grayscale.applyInPlace(fastBitmap);


//        BrightnessCorrection brightnessCorrection = new BrightnessCorrection(CaptureActivity.brightness);
//        brightnessCorrection.applyInPlace(fastBitmap);


//        LevelsLinear levelsLinear = new LevelsLinear();
//        levelsLinear.applyInPlace(fastBitmap);
//
//        ConservativeSmoothing conservativeSmoothing = new ConservativeSmoothing();
//        conservativeSmoothing.applyInPlace(fastBitmap);

        // if we want threshold or not
        if (CaptureActivity.isSwitchChecked) {
            Threshold threshold = new Threshold(var2);
            threshold.applyInPlace(fastBitmap);
        }


//        GaussianBlur gaussianBlur = new GaussianBlur(var);
//        gaussianBlur.applyInPlace(fastBitmap);


//
//        Invert invert = new Invert();
//        invert.applyInPlace(fastBitmap);


//        //CaptureActivity.seek1
//        HysteresisThreshold threshold = new HysteresisThreshold();
//        threshold.applyInPlace(fastBitmap);

//        ArtifactsRemoval artifactsRemoval = new ArtifactsRemoval();
//        artifactsRemoval.applyInPlace(fastBitmap);
//
//        HistogramEqualization histogramEqualization = new HistogramEqualization();
//        histogramEqualization.applyInPlace(fastBitmap);


//        HomogenityEdgeDetector homogenityEdgeDetector = new HomogenityEdgeDetector();
//        homogenityEdgeDetector.applyInPlace(fastBitmap);

//        CannyEdgeDetector cannyEdgeDetector = new CannyEdgeDetector(0, 40);
//        cannyEdgeDetector.applyInPlace(fastBitmap);

//        Dilatation dilatation = new Dilatation(var);
//        dilatation.applyInPlace(fastBitmap);


//
//        RosinThreshold threshold = new RosinThreshold();
//        threshold.applyInPlace(fastBitmap);
////

//
//        GaussianBlur gaussianBlur = new GaussianBlur();
//        gaussianBlur.applyInPlace(fastBitmap);


        methodName = "threshold/gaussianBlur";

//        BradleyLocalThreshold bradleyLocalThreshold = new BradleyLocalThreshold();
//        bradleyLocalThreshold.applyInPlace(fastBitmap);


//        bitmap = ConvertToNegative(bitmap);
        //bitmap = changeBitmapContrastBrightness(bitmap, CaptureActivity.contrast, CaptureActivity.brightness);

        // this one works better with black text and white background, is recommended to do the conversion to negative first and a brightness of -100
//        Pix thresholdedImage = Binarize.sauvolaBinarizeTiled(ReadFile.readBitmap(bitmap), 7, 0.35f, 20, 20);
//        Log.e("OcrRecognizeAsyncTask", "thresholding completed. converting to bmp. size:" + bitmap.getWidth() + "x" + bitmap.getHeight());
//        bitmap = WriteFile.writeBitmap(thresholdedImage);
//        bitmap = ConvertToNegative(bitmap);
        // this one works better with white text and black background... i think and about -130 brightness
//        Pix thresholdedImage2 = Binarize.otsuAdaptiveThreshold(ReadFile.readBitmap(bitmap), 48, 48, 29, 29, 0.3F);
//        Log.e("OcrRecognizeAsyncTask", "thresholding completed. converting to bmp. size:" + bitmap.getWidth() + "x" + bitmap.getHeight());
//        bitmap = WriteFile.writeBitmap(thresholdedImage2);

        letsDoThis = fastBitmap.toBitmap();
        MainActivity.savedBitmap = fastBitmap.toBitmap();


        return letsDoThis;
    }


    public ArrayList<Bitmap> renderCroppedGreyscaleBitmaps() {
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        int width = getWidth();
        int height = getHeight();
        int[] pixels = new int[width * height];
        byte[] yuv = yuvData;
        int inputOffset = top * dataWidth + left;

        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            for (int x = 0; x < width; x++) {
                int grey = yuv[inputOffset + x] & 0xff;
                pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
            }
            inputOffset += dataWidth;
        }

        int dividedX = dataWidth / 7;
        int constantX = dataWidth / 7;


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i > 6; i++) {
            int xAdded = 0;
            Bitmap b1 = Bitmap.createBitmap(bitmap, xAdded, 0, constantX, dataHeight);
            b1 = changeBitmapContrastBrightness(b1, CaptureActivity.contrast, CaptureActivity.brightness);
            Pix thresholdedImage2 = Binarize.otsuAdaptiveThreshold(ReadFile.readBitmap(b1), 48, 48, 29, 29, 0.3F);
            Log.e("OcrRecognizeAsyncTask", "thresholding completed. converting to bmp. size:" + b1.getWidth() + "x" + b1.getHeight());
            b1 = WriteFile.writeBitmap(thresholdedImage2);

            dividedX = dividedX + dividedX;
            xAdded = xAdded + dividedX;

            bitmaps.add(b1);
        }


//        bitmap = ConvertToNegative(bitmap);

        // this one works better with black text and white background, is recommended to do the conversion to negative first and a brightness of -100
//        Pix thresholdedImage = Binarize.sauvolaBinarizeTiled(ReadFile.readBitmap(bitmap), 7, 0.35f, 20, 20);
//        Log.e("OcrRecognizeAsyncTask", "thresholding completed. converting to bmp. size:" + bitmap.getWidth() + "x" + bitmap.getHeight());
//        bitmap = WriteFile.writeBitmap(thresholdedImage);
//        bitmap = ConvertToNegative(bitmap);
        // this one works better with white text and black background... i think and about -130 brightness


        letsDoThis = bitmap;

        return bitmaps;
    }

    /**
     * test an array of bitmaps
     *
     * @return array of bitmaps
     */
    public ArrayList<Bitmap> renderCroppedGreyscaleBitmaps2() {
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        int width = getWidth();
        int height = getHeight();
        int[] pixels = new int[width * height];
        byte[] yuv = yuvData;
        int inputOffset = top * dataWidth + left;

        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            for (int x = 0; x < width; x++) {
                int grey = yuv[inputOffset + x] & 0xff;
                pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
            }
            inputOffset += dataWidth;
        }

        // Number of digits on the meter
        int numberOfDigits = 5;

        int dividedX = width / numberOfDigits;
        int constantX = width / numberOfDigits;


        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        FastBitmap fastBitmap = new FastBitmap(bitmap);

        Grayscale grayscale = new Grayscale();
        grayscale.applyInPlace(fastBitmap);

        int var = CaptureActivity.seek;
        int var2 = CaptureActivity.seek1;
        int brightness = CaptureActivity.brightness;

//        BrightnessCorrection brightnessCorrection = new BrightnessCorrection(brightness);
//        brightnessCorrection.applyInPlace(fastBitmap);

//        LevelsLinear levelsLinear = new LevelsLinear();
//        levelsLinear.applyInPlace(fastBitmap);
//
//        ConservativeSmoothing conservativeSmoothing = new ConservativeSmoothing();
//        conservativeSmoothing.applyInPlace(fastBitmap);

        //        Invert invert = new Invert();
//        invert.applyInPlace(fastBitmap);

//
//        NiblackThreshold threshold = new NiblackThreshold();
//        threshold.applyInPlace(fastBitmap);
//

        // if we want threshold or not
        if (CaptureActivity.isSwitchChecked) {
            Threshold threshold = new Threshold(var2);
            threshold.applyInPlace(fastBitmap);
        }
//        GaussianBlur gaussianBlur = new GaussianBlur();
//        gaussianBlur.applyInPlace(fastBitmap);

//        BernsenThreshold bernsenThreshold = new BernsenThreshold();
//        bernsenThreshold.applyInPlace(fastBitmap);


        //methodName = "GaussianBlur/BernsenThreshold";


        Bitmap segmentThisBitmap = fastBitmap.toBitmap();

        // we have 5 digits
        int xAdded = 0;

        for (int i = 0; i < 5; i++) {
            Bitmap b1 = Bitmap.createBitmap(segmentThisBitmap, xAdded, 0, constantX, height);

            dividedX = dividedX + constantX;
            xAdded = xAdded + constantX;

            bitmaps.add(b1);
        }


        letsDoThis = segmentThisBitmap;

        return bitmaps;
    }

    //TODO merge these 2 methods

    public Bitmap ConvertToNegative(Bitmap sampleBitmap) {
        ColorMatrix negativeMatrix = new ColorMatrix();
        float[] negMat = {-1, 0, 0, 0, 255, 0, -1, 0, 0, 255, 0, 0, -1, 0, 255, 0, 0, 0, 1, 0};
        negativeMatrix.set(negMat);
        final ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(negativeMatrix);
        Bitmap rBitmap = sampleBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        paint.setColorFilter(colorFilter);
        Canvas myCanvas = new Canvas(rBitmap);
        myCanvas.drawBitmap(rBitmap, 0, 0, paint);
        return rBitmap;
    }

    /**
     * @param bmp        input bitmap
     * @param contrast   0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    private void reverseHorizontal(int width, int height) {
        byte[] yuvData = this.yuvData;
        for (int y = 0, rowStart = top * dataWidth + left; y < height; y++, rowStart += dataWidth) {
            int middle = rowStart + width / 2;
            for (int x1 = rowStart, x2 = rowStart + width - 1; x1 < middle; x1++, x2--) {
                byte temp = yuvData[x1];
                yuvData[x1] = yuvData[x2];
                yuvData[x2] = temp;
            }
        }
    }

}
