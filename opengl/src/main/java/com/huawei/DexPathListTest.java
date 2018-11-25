package com.huawei;

import android.ddm.DdmHandleAppName;
import android.os.Debug;
import android.os.RemoteException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * Created by michael on 18-11-26.
 */

public class DexPathListTest {
    public static void main(String[] args) throws RemoteException {
        DdmHandleAppName.setAppName("com.hpw.mvpframe", 0);
        System.out.println("meminfo");
        Debug.waitForDebugger();
        try {
            DexFile dexFile = DexFile.loadDex("/sdcard/test/bin/target.dex", "/sdcard/test/bin/target.odex", 0);
            PathClassLoader pathClassLoader = (PathClassLoader) DexPathListTest.class.getClassLoader();
            Object pathList = getPathList(pathClassLoader);
            Object[] objects = getDexElements(pathList);
            Object element = makeDexElement(dexFile);
            setDexElements(pathList, combineArray(objects, element));
            new HelloWorld().say();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Object getPathList(BaseDexClassLoader classLoader) {
        try {
            Field f = BaseDexClassLoader.class.getDeclaredField("pathList");
            f.setAccessible(true);
            return f.get(classLoader);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object[] getDexElements(Object pathList) {
        try {
            Field f = pathList.getClass().getDeclaredField("dexElements");
            f.setAccessible(true);
            return (Object[]) f.get(pathList);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object makeDexElement(DexFile file) {
        try {
            Class element = Class.forName("dalvik.system.DexPathList$Element");
            Constructor constructor = element.getConstructor(File.class, boolean.class, File.class, DexFile.class);
            return constructor.newInstance(new File(""), false, null, file);
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

    private static Object combineArray(Object[] objects, Object object) {
        int size = objects.length;
        Object array =  Array.newInstance(objects.getClass().getComponentType(), size+1);
        Array.set(array, 0, object);
        for (int i = 1; i < size+1; i++) {
            Array.set(array, i, objects[i-1]);
        }
        return array;
    }

    private static void setDexElements(Object pathList, Object array) {
        try {
            Field f = pathList.getClass().getDeclaredField("dexElements");
            f.setAccessible(true);
            f.set(pathList, array);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
