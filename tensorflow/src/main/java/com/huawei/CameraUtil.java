package com.huawei;

import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by michael on 18-11-4.
 */
public class CameraUtil {
    private static CameraDevice device;
    private static ImageReader mImageReader;
    private static Handler handler;
    private static CameraCaptureSession session;
    private static Object wait = new Object();

    public static class imageSaver implements Runnable {
        private Image mImage;
        private String path;
        public imageSaver(Image image, String path) {
            mImage = image;
            this.path = path;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            File imageFile = new File(path);
            System.out.println(imageFile.getAbsolutePath());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imageFile);
                fos.write(data, 0 ,data.length);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                imageFile = null;
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (wait) {
                    wait.notify();
                }
            }
        }
    }

    public static void takePhoto(final String path) {
        HandlerThread thread = new HandlerThread("test");
        thread.start();
        handler = new Handler(thread.getLooper());

        CameraManager cameraManager = new CameraManager();

        try {
            cameraManager.openCamera("0", new CameraDevice.StateCallback() {
                @Override
                public void onDisconnected(CameraDevice cameraDevice) {

                }

                @Override
                public void onError(CameraDevice cameraDevice, int i) {

                }

                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    device = cameraDevice;
                }
            }, handler);

            Thread.sleep(2000);

            CaptureRequest.Builder builder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mImageReader = ImageReader.newInstance(1080, 1920,
                    ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    //执行图像保存子线程
                    handler.post(new imageSaver(reader.acquireNextImage(), path));
                }
            }, handler);

            builder.addTarget(mImageReader.getSurface());

            device.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                }

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    session = cameraCaptureSession;
                }
            }, handler);
            Thread.sleep(2000);
            session.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession cameraCaptureSession, CaptureRequest captureRequest, TotalCaptureResult totalCaptureResult) {

                    System.out.println("complete");
                }
            }, handler);
            synchronized (wait) {
                wait.wait();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
