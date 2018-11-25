package com.huawei;

import android.app.ActivityThread;
import android.app.Instrumentation;
import android.app.LoadedApk;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.res.CompatibilityInfo;
import android.ddm.DdmHandleAppName;
import android.os.Debug;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ApkTest {
    public static void main(String[] args) throws RemoteException, InterruptedException {
        if (args.length != 1 && args.length != 2) {
            System.out.println("Usage: pakage debug[1]");
            return;
        }
        if (args.length == 2) {
            if (Integer.parseInt(args[1]) == 1) {
                setProcessName("test");
                Debug.waitForDebugger();
                Thread.sleep(1000);
            }
        }
        Looper.prepareMainLooper();
        ActivityThread activityThread = createActivityThread();
        IPackageManager pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        ApplicationInfo app = pm.getApplicationInfo(args[0], 0, 0);
        LoadedApk loadedApk = new LoadedApk(activityThread, app, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null, true, true, true);
        Context context = createAppContext(activityThread, loadedApk);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.hpw.myapp", "com.hpw.myapp.ui.main.MainActivity");
        context.startActivity(intent);
        Looper.loop();
    }

    private static ActivityThread createActivityThread() {
        try {
            Constructor c = ActivityThread.class.getDeclaredConstructor();
            c.setAccessible(true);
            ActivityThread result = (ActivityThread) c.newInstance();
            Instrumentation instrumentation = new Instrumentation();
            Field mInstrumentation = ActivityThread.class.getDeclaredField("mInstrumentation");
            mInstrumentation.setAccessible(true);
            mInstrumentation.set(result, instrumentation);
            return result;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Context createAppContext(ActivityThread mainThread, LoadedApk loadedApk) {
        try {
            Class c = Class.forName("android.app.ContextImpl");
            Method m = c.getDeclaredMethod("createAppContext", new Class[]{ActivityThread.class, LoadedApk.class});
            m.setAccessible(true);
            return (Context) m.invoke(null, mainThread, loadedApk);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void setProcessName(String name) {
        DdmHandleAppName.setAppName(name, 0);
    }
}
