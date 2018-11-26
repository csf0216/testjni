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
import com.huawei.utils.TensorUtils;

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
        while (!Thread.interrupted()) {
            TensorUtils.start();
        }
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
