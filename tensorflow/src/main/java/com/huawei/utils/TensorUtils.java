package com.huawei.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Debug;

import com.huawei.CameraUtil;

import java.io.File;
import java.util.List;

/**
 * Created by michael on 18-11-27.
 */

public class TensorUtils {
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private static final String MODEL_FILE = "assets/model/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "assets/model/imagenet_comp_graph_label_strings.txt";
    public static void start() {
        Classifier classifier = TensorFlowImageClassifier.create(MODEL_FILE, LABEL_FILE, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME);
        String fileName = getPhotoCacheFolder()+File.pathSeparator+"TF_" + System.currentTimeMillis() + ".jpg";
        CameraUtil.takePhoto(fileName);
        Bitmap bitmap = BitmapFactory.decodeFile(fileName);
        Bitmap croppedBitmap = getScaleBitmap(bitmap, INPUT_SIZE);
        Debug.startMethodTracing("/data/data/tensor.trace");
        List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
        Debug.stopMethodTracing();
        if (results != null && results.size() != 0) {
            System.out.println(results.get(0));
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private static Bitmap getScaleBitmap(Bitmap bitmap, int size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) size) / width;
        float scaleHeight = ((float) size) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }


    public static String getPhotoCacheFolder() {
        return "/sdcard/opengl";
    }
}
