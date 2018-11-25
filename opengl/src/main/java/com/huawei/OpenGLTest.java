package com.huawei;

import android.content.res.Configuration;
import android.ddm.DdmHandleAppName;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLES30;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.view.DragEvent;
import android.view.IWindow;
import android.view.IWindowSession;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLTest {
    private static int GL_VERSION = 2;
    public static void main(String[] args) throws InterruptedException {
        DdmHandleAppName.setAppName("ssss", 0);

//        Looper.prepareMainLooper();
        W w = new W();
        int seq = 1;
        EGL10 egl = (EGL10) EGLContext.getEGL();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

//        Rect rect1 = new Rect(0, 0, 0, 0);
//        Rect rect2 = new Rect(0, 0, 0, 0);
//        Rect rect3 = new Rect(0, 0, 0, 0);
//        Rect rect4 = new Rect(0, 0, 0, 0);
//        Rect rect5 = new Rect(0, 0, 0, 0);
//        Rect rect6 = new Rect(0, 0, 0, 0);
//        Configuration configuration = new Configuration();
//        IWindowSession session = WindowManagerGlobal.getWindowSession();
        Surface surface = new Surface();
//            session.addToDisplayWithoutInputChannel(w, seq, layoutParams, 1, 0, rect1, rect2);
//            session.relayout(w, seq, layoutParams, 1, 0, 0, 0, rect1, rect2, rect3, rect4, rect5, rect6, configuration, surface);
        SurfaceSession surfaceSession = new SurfaceSession();
        SurfaceControl surfaceControl = new SurfaceControl(surfaceSession, "test", 1000, 1000, PixelFormat.RGB_888, SurfaceControl.FX_SURFACE_NORMAL);
        SurfaceControl.openTransaction();
        surfaceControl.setAlpha(0.9f);
        surfaceControl.setLayer(30000);
        SurfaceControl.closeTransaction();
        surface.copyFrom(surfaceControl);
//        GL10 gl = initializeEGL(egl, surface);
        surface.lockCanvas(null);
        surface.release();
//        gl.glFlush();
//        egl.eglSwapBuffers()
//            Canvas canvas = surface.lockHardwareCanvas();
//            RectF rectF = new RectF(100,100,500,400);
//            Paint paint_red = new Paint();
//            paint_red.setColor(Color.RED);
//            paint_red.setStrokeWidth(10);
//            System.out.println(canvas);
//            canvas.drawOval(rectF, paint_red);
//            surface.unlockCanvasAndPost(canvas);
//        new H264Player(args[0], surface).start();
//        Looper.loop();
    }

    private static GL10 initializeEGL(EGL10 egl, Surface surface) {
        EGLDisplay eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] attrib_list = {0x3098, GL_VERSION,
                EGL10.EGL_NONE };
        int[] version = new int[2];
        if(!egl.eglInitialize(eglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed");
        }
        EGLConfig eglConfig = chooseConfig(egl, eglDisplay);
        EGLContext eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        EGLSurface eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surface, null);
        boolean ret = egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        System.out.println(ret);
        return (GL10) eglContext.getGL();
    }

    private static EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int[] num_config = new int[1];
        int[] configSpec = new int[] {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 0,
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE};
        int[] mConfigSpec = filterConfigSpec(configSpec);
        if (!egl.eglChooseConfig(display, mConfigSpec, null, 0,
                num_config)) {
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException(
                    "No configs match configSpec");
        }

        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
                num_config)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }
        EGLConfig config = chooseConfig(egl, display, configs);
        if (config == null) {
            throw new IllegalArgumentException("No config chosen");
        }
        return config;
    }

    private static EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                  EGLConfig[] configs) {
        int mDepthSize = 8;
        int mRedSize = 8;
        int mGreenSize =8;
        int mBlueSize = 8;
        int mAlphaSize = 8;
        int mStencilSize = 0;
        for (EGLConfig config : configs) {
            int d = findConfigAttrib(egl, display, config,
                    EGL10.EGL_DEPTH_SIZE, 0);
            int s = findConfigAttrib(egl, display, config,
                    EGL10.EGL_STENCIL_SIZE, 0);
            if ((d >= mDepthSize) && (s >= mStencilSize)) {
                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);
                if ((r == mRedSize) && (g == mGreenSize)
                        && (b == mBlueSize) && (a == mAlphaSize)) {
                    return config;
                }
            }
        }
        return null;
    }

    private static int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                 EGLConfig config, int attribute, int defaultValue) {
        int[] value = new int[1];

        if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
            return value[0];
        }
        return defaultValue;
    }

    private static int[] filterConfigSpec(int[] configSpec) {
        if (GL_VERSION != 2 && GL_VERSION != 3) {
            return configSpec;
        }
        /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
         * And we know the configSpec is well formed.
         */
        int len = configSpec.length;
        int[] newConfigSpec = new int[len + 2];
        System.arraycopy(configSpec, 0, newConfigSpec, 0, len-1);
        newConfigSpec[len-1] = EGL10.EGL_RENDERABLE_TYPE;
        if (GL_VERSION == 2) {
            newConfigSpec[len] = EGL14.EGL_OPENGL_ES2_BIT;  /* EGL_OPENGL_ES2_BIT */
        } else {
            newConfigSpec[len] = EGLExt.EGL_OPENGL_ES3_BIT_KHR; /* EGL_OPENGL_ES3_BIT_KHR */
        }
        newConfigSpec[len+1] = EGL10.EGL_NONE;
        return newConfigSpec;
    }

    static class W extends IWindow.Stub {

        @Override
        public void closeSystemDialogs(String s) throws RemoteException {

        }

        @Override
        public void dispatchAppVisibility(boolean b) throws RemoteException {

        }

        @Override
        public void dispatchDragEvent(DragEvent dragEvent) throws RemoteException {

        }

        @Override
        public void dispatchGetNewSurface() throws RemoteException {

        }

        @Override
        public void dispatchSystemUiVisibilityChanged(int i, int i1, int i2, int i3) throws RemoteException {

        }

        @Override
        public void dispatchWallpaperCommand(String s, int i, int i1, int i2, Bundle bundle, boolean b) throws RemoteException {

        }

        @Override
        public void dispatchWallpaperOffsets(float v, float v1, float v2, float v3, boolean b) throws RemoteException {

        }

        @Override
        public void dispatchWindowShown() throws RemoteException {

        }

        @Override
        public void executeCommand(String s, String s1, ParcelFileDescriptor parcelFileDescriptor) throws RemoteException {

        }

        @Override
        public void moved(int i, int i1) throws RemoteException {

        }

        @Override
        public void onAnimationStarted(int i) throws RemoteException {

        }

        @Override
        public void onAnimationStopped() throws RemoteException {

        }

        @Override
        public void resized(Rect rect, Rect rect1, Rect rect2, Rect rect3, Rect rect4, Rect rect5, boolean b, Configuration configuration) throws RemoteException {

        }

        @Override
        public void windowFocusChanged(boolean b, boolean b1) throws RemoteException {

        }
    }
}
