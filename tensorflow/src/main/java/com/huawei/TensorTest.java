package com.huawei;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Debug;
import android.os.Looper;

import com.android.dex.Dex;
import com.huawei.utils.Classifier;
import com.huawei.utils.DexUtils;
import com.huawei.utils.TensorFlowImageClassifier;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class TensorTest {
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private static final String MODEL_FILE = "assets/model/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "assets/model/imagenet_comp_graph_label_strings.txt";
    public static void main(String[] args) {
        setProcessName("test");
        if (args.length == 1) {
            Debug.waitForDebugger();
        }
        List<File> apks = DexUtils.getApks("/sdcard/tensor/");
        if (apks == null || apks.size() == 0) {
            return;
        }
        PathClassLoader pathClassLoader = (PathClassLoader) Classifier.class.getClassLoader();
        Object pathList = makePathList(pathClassLoader, apks.get(0).getAbsolutePath(), "/data/app/tensor/lib/armeabi-v7a");
        for (int i = 1; i < apks.size(); i++) {
            try {
                Object[] eles = DexUtils.getDexElements(pathList);
                DexFile dexFile = DexFile.loadDex(apks.get(i).getAbsolutePath(), null, 0);
                Object ele = DexUtils.makeDexElement(dexFile);
                Object o = DexUtils.combineArray(eles, ele);
                DexUtils.setDexElements(pathList, o);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setPathList(pathClassLoader, pathList);
        Looper.prepareMainLooper();
        Classifier classifier = TensorFlowImageClassifier.create(MODEL_FILE, LABEL_FILE, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME);
        String fileName = getPhotoCacheFolder()+File.pathSeparator+"TF_" + System.currentTimeMillis() + ".jpg";
        CameraUtil.takePhoto(fileName);
        Bitmap bitmap = BitmapFactory.decodeFile(fileName);
        Bitmap croppedBitmap = getScaleBitmap(bitmap, INPUT_SIZE);
        List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
        Looper.loop();
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

    private static void setProcessName(String name) {
        Class<?> ddm = null;
        try {
            ddm = Class.forName("android.ddm.DdmHandleAppName");
            Method m = ddm.getDeclaredMethod("setAppName", new Class[]{String.class, int.class});
            m.invoke(null, name, 0);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void setPathList(PathClassLoader classLoader, Object o) {
        try {
            Field f = BaseDexClassLoader.class.getDeclaredField("pathList");
            f.setAccessible(true);
            f.set(classLoader, o);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Object makePathList(ClassLoader cl, String dexpath, String libraryPath) {
        try {
            Class c = Class.forName("dalvik.system.DexPathList");
            Constructor constructor = c.getDeclaredConstructor(new Class[]{ClassLoader.class, String.class, String.class, File.class});
            constructor.setAccessible(true);
            return constructor.newInstance(cl, dexpath, libraryPath, null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
