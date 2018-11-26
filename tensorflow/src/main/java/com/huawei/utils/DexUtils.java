package com.huawei.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;

/**
 * Created by michael on 18-11-26.
 */

public class DexUtils {
    public static Object getPathList(BaseDexClassLoader classLoader) {
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

    public static Object[] getDexElements(Object pathList) {
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

    public static Object makeDexElement(DexFile file) {
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

    public static Object combineArray(Object[] objects, Object object) {
        int size = objects.length;
        Object array =  Array.newInstance(objects.getClass().getComponentType(), size+1);
        Array.set(array, 0, object);
        for (int i = 1; i < size+1; i++) {
            Array.set(array, i, objects[i-1]);
        }
        return array;
    }

    public static void setDexElements(Object pathList, Object array) {
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

    public static List<File> getApks(String root) {
        File r = new File(root);
        if (!r.exists()) {
            return null;
        }
        List<File> results = new ArrayList<>();
        if (r.isDirectory()) {
            File[] files = r.listFiles();
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".apk")) {
                    results.add(f);
                }
            }
            Collections.sort(results, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    if (file.lastModified() > t1.lastModified()) {
                        return 1;
                    } else if (file.lastModified() < t1.lastModified()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        } else {
            results.add(r);
        }
        return results;
    }
}
