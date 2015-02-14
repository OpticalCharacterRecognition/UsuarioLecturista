/*
 * Copyright (C) 2010 ZXing authors
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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.fourtails.usuariolecturista.R;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to send bitmap data for OCR.
 * <p/>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing/
 */
final class DecodeHandler extends Handler {

    private final CaptureActivity activity;
    private boolean running = true;
    private final TessBaseAPI baseApi;
    private BeepManager beepManager;
    private Bitmap bitmap;
    private static boolean isDecodePending;
    private long timeRequired;

    private int counter = 0;
    public static String addedResult = "";

    ArrayList<Integer> multipleResults = new ArrayList<>();

    DecodeHandler(CaptureActivity activity) {
        this.activity = activity;
        baseApi = activity.getBaseApi();
        beepManager = new BeepManager(activity);
        beepManager.updatePrefs();
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case R.id.ocr_continuous_decode:
                // Only request a decode if a request is not already pending.
                if (!isDecodePending) {
                    isDecodePending = true;
                    ocrContinuousDecode((byte[]) message.obj, message.arg1, message.arg2);
                }
                break;
            case R.id.ocr_decode:
                ocrDecode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    static void resetDecodeState() {
        isDecodePending = false;
    }

    /**
     * Launch an AsyncTask to perform an OCR decode for single-shot mode.
     *
     * @param data   Image data
     * @param width  Image width
     * @param height Image height
     */
    private void ocrDecode(byte[] data, int width, int height) {
        beepManager.playBeepSoundAndVibrate();
        activity.displayProgressDialog();

        // This is used for portrait mode
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width;
        width = height;
        height = tmp;

        // Launch OCR asynchronously, so we get the dialog box displayed immediately
        // rotatedData for portrait mode instead of data for landscape mode
        new OcrRecognizeAsyncTask(activity, baseApi, rotatedData, width, height).execute();
    }

    /**
     * Perform an OCR decode for realtime recognition mode.
     *
     * @param data   Image data
     * @param width  Image width
     * @param height Image height
     */
    private void ocrContinuousDecode(byte[] data, int width, int height) {
        // This is used for portrait mode
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width;
        width = height;
        height = tmp;

        PlanarYUVLuminanceSource source = activity.getCameraManager().buildLuminanceSource(rotatedData, width, height);
        if (source == null) {
            sendContinuousOcrFailMessage();
            return;
        }
        ArrayList<Bitmap> bitmaps = new ArrayList<>();


        bitmaps = source.renderCroppedGreyscaleBitmaps2();
        bitmap = bitmaps.get(0);

//        bitmap = source.renderCroppedGreyscaleBitmap();


        OcrResult ocrResult = null;
        for (Bitmap bitmap1 : bitmaps) {
            ocrResult = getOcrResult2(bitmap1);
        }


//        OcrResult ocrResult = getOcrResult();
        Handler handler = activity.getHandler();
        if (handler == null) {
            return;
        }

        if (ocrResult == null) {
            try {
                sendContinuousOcrFailMessage();
            } catch (NullPointerException e) {
                activity.stopHandler();
            } finally {
                bitmap.recycle();
                baseApi.clear();
            }
            return;
        }

        try {
            Message message = Message.obtain(handler, R.id.ocr_continuous_decode_succeeded, ocrResult);
            message.sendToTarget();
        } catch (NullPointerException e) {
            activity.stopHandler();
        } finally {
            baseApi.clear();
        }
    }

    @SuppressWarnings("unused")
    private OcrResult getOcrResult() {
        OcrResult ocrResult;
        String textResult;
        long start = System.currentTimeMillis();

        try {
            baseApi.setImage(ReadFile.readBitmap(bitmap));
            textResult = baseApi.getUTF8Text();
            timeRequired = System.currentTimeMillis() - start;

            // Check for failure to recognize text
            if (textResult == null || textResult.equals("")) {
                return null;
            }
            ocrResult = new OcrResult();
            ocrResult.setWordConfidences(baseApi.wordConfidences());
            ocrResult.setMeanConfidence(baseApi.meanConfidence());
            if (ViewfinderView.DRAW_REGION_BOXES) {
                ocrResult.setRegionBoundingBoxes(baseApi.getRegions().getBoxRects());
            }
            if (ViewfinderView.DRAW_TEXTLINE_BOXES) {
                ocrResult.setTextlineBoundingBoxes(baseApi.getTextlines().getBoxRects());
            }
            if (ViewfinderView.DRAW_STRIP_BOXES) {
                ocrResult.setStripBoundingBoxes(baseApi.getStrips().getBoxRects());
            }

            // Always get the word bounding boxes--we want it for annotating the bitmap after the user
            // presses the shutter button, in addition to maybe wanting to draw boxes/words during the
            // continuous mode recognition.
            ocrResult.setWordBoundingBoxes(baseApi.getWords().getBoxRects());

//      if (ViewfinderView.DRAW_CHARACTER_BOXES || ViewfinderView.DRAW_CHARACTER_TEXT) {
//        ocrResult.setCharacterBoundingBoxes(baseApi.getCharacters().getBoxRects());
//      }
        } catch (RuntimeException e) {
            Log.e("OcrRecognizeAsyncTask", "Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
            e.printStackTrace();
            try {
                baseApi.clear();
                activity.stopHandler();
            } catch (NullPointerException e1) {
                // Continue
            }
            return null;
        }
        timeRequired = System.currentTimeMillis() - start;
        ocrResult.setBitmap(bitmap);
        ocrResult.setText(textResult);
        ocrResult.setRecognitionTimeRequired(timeRequired);
        return ocrResult;
    }


    private OcrResult getOcrResult2(Bitmap testBitmap) {
        OcrResult ocrResult;
        String textResult;
        long start = System.currentTimeMillis();

        try {
            baseApi.setImage(ReadFile.readBitmap(testBitmap));
            textResult = baseApi.getUTF8Text();
            timeRequired = System.currentTimeMillis() - start;

            // Check for failure to recognize text
            if (textResult == null || textResult.equals("")) {
                return null;
            }
            ocrResult = new OcrResult();
            ocrResult.setWordConfidences(baseApi.wordConfidences());
            ocrResult.setMeanConfidence(baseApi.meanConfidence());
            if (ViewfinderView.DRAW_REGION_BOXES) {
                ocrResult.setRegionBoundingBoxes(baseApi.getRegions().getBoxRects());
            }
            if (ViewfinderView.DRAW_TEXTLINE_BOXES) {
                ocrResult.setTextlineBoundingBoxes(baseApi.getTextlines().getBoxRects());
            }
            if (ViewfinderView.DRAW_STRIP_BOXES) {
                ocrResult.setStripBoundingBoxes(baseApi.getStrips().getBoxRects());
            }

            // Always get the word bounding boxes--we want it for annotating the bitmap after the user
            // presses the shutter button, in addition to maybe wanting to draw boxes/words during the
            // continuous mode recognition.
            ocrResult.setWordBoundingBoxes(baseApi.getWords().getBoxRects());


//      if (ViewfinderView.DRAW_CHARACTER_BOXES || ViewfinderView.DRAW_CHARACTER_TEXT) {
//        ocrResult.setCharacterBoundingBoxes(baseApi.getCharacters().getBoxRects());
//      }
        } catch (RuntimeException e) {
            Log.e("OcrRecognizeAsyncTask", "Caught RuntimeException in request to Tesseract. Setting state to CONTINUOUS_STOPPED.");
            e.printStackTrace();
            try {
                baseApi.clear();
                activity.stopHandler();
            } catch (NullPointerException e1) {
                // Continue
            }
            return null;
        }
        timeRequired = System.currentTimeMillis() - start;
        ocrResult.setBitmap(testBitmap);
        ocrResult.setText(textResult);
        ocrResult.setRecognitionTimeRequired(timeRequired);

        evaluateOCRandTryToGetSignificantResult(ocrResult);


        return ocrResult;
    }

    /**
     * Magic!
     * Basically we scan every digit and add it into a 5 digit string which then gets evaluated
     * and set into an array, to see if we can find a significant sample with a result
     */
    public void evaluateOCRandTryToGetSignificantResult(OcrResult ocrResult) {
        String textResult = ocrResult.getText();


        if (isIntegerAndIsOneDigit(textResult) && (Integer.parseInt(textResult) <= 9) && ocrResult.getMeanConfidence() > 50) {
            addedResult = addedResult + textResult;
        } else {
            addedResult = addedResult + "E";
        }
        counter++;
        if (counter == 5) {
            if (multipleResults.size() < 20) {
                if (isInteger(addedResult)) {
                    multipleResults.add(Integer.parseInt(addedResult));
                }
            } else {
                getTheMostPopularResult();
            }
        }
        if (counter > 5) {
            counter = 0;
            addedResult = "";
        }

    }

    private void getTheMostPopularResult() {
        Map<Integer, Integer> map = new HashMap<>();
        for (Integer i : multipleResults) {
            Integer count = map.get(i);
            map.put(i, count != null ? count + 1 : 0);
        }
        Integer popular = Collections.max(map.entrySet(),
                new Comparator<Map.Entry<Integer, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }).getKey();
        int carlos = popular;
        multipleResults = new ArrayList<>();
    }

    /**
     * We only want one digit integers here
     *
     * @param s string to evaluate
     * @return true if integer with one digit
     */
    public static boolean isIntegerAndIsOneDigit(String s) {
        if (s.length() > 1) {
            return false;
        }
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    /**
     * With this we are going to try to eliminate results with Error in it
     * like 001E2
     *
     * @param s string to evaluate
     * @return true if integer
     */
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private void sendContinuousOcrFailMessage() {
        Handler handler = activity.getHandler();
        if (handler != null) {
            Message message = Message.obtain(handler, R.id.ocr_continuous_decode_failed, new OcrResultFailure(timeRequired));
            message.sendToTarget();
        }
    }

}












