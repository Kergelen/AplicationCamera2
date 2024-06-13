package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScreenCapture {
    private MediaProjection mediaProjection;
    private MediaProjectionManager projectionManager;
    private ImageReader imageReader;
    private OkHttpClient httpClient;
    private Context context;


    public MediaProjectionManager getProjectionManager() {
        return projectionManager;
    }

    public ScreenCapture(Context context) {
        this.context = context;
        projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        httpClient = new OkHttpClient();
    }

    public void startScreenCapture(Intent data, int resultCode) {
        Log.w("MOJE", "startScreenCapture called");
        mediaProjection = projectionManager.getMediaProjection(resultCode, data);

        if (mediaProjection == null) {
            Log.w("MOJE", "mediaProjection is null");
        } else {
            Log.w("MOJE", "mediaProjection created");
        }

        // Register a callback to manage resources
        mediaProjection.registerCallback(new MediaProjection.Callback() {
            @Override
            public void onStop() {
                // Handle MediaProjection stopped event here
                // For example, you can stop and release resources here
                if (imageReader != null) {
                    imageReader.close();
                    imageReader = null;
                }
                mediaProjection.unregisterCallback(this);
                mediaProjection = null;
            }
        }, null);

        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);

        if (imageReader == null) {
            Log.w("MOJE", "imageReader is null");
        } else {
            Log.w("MOJE", "imageReader created");
        }

        mediaProjection.createVirtualDisplay("ScreenCapture",
                width, height, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), new VirtualDisplay.Callback() {
                    @Override
                    public void onPaused() {
                        Log.w("MOJE", "VirtualDisplay paused");
                    }

                    @Override
                    public void onResumed() {
                        Log.w("MOJE", "VirtualDisplay resumed");
                    }

                    @Override
                    public void onStopped() {
                        Log.w("MOJE", "VirtualDisplay stopped");
                    }
                }, null);

        Log.w("MOJE", "VirtualDisplay created");
    }

    public void stopScreenCapture() {
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }

    public void captureScreenshotAndSendToServer(String serverUrl) {
        Log.w("MOJE", "captureScreenshotAndSendToServer called");
        Image image = imageReader.acquireLatestImage();
        if (image != null) {
            Log.w("MOJE", "Image acquired");
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * image.getWidth();

            Bitmap bitmap = Bitmap.createBitmap(image.getWidth() + rowPadding / pixelStride, image.getHeight(), Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);

            // Convert the bitmap to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            // Send the byte array to server
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Void> future = executor.submit(new SendToServerTask(serverUrl, byteArray));

            image.close();
        } else {
            Log.w("MOJE", "Image is null");
        }
    }

    private class SendToServerTask implements Callable<Void> {
        private String serverUrl;
        private byte[] byteArray;

        public SendToServerTask(String serverUrl, byte[] byteArray) {
            this.serverUrl = serverUrl;
            this.byteArray = byteArray;
        }

        @Override
        public Void call() {
            // Create a RequestBody instance from the byte array
            RequestBody requestBody = RequestBody.create(byteArray, MediaType.parse("image/png"));

            // Create a multipart body with the 'image' field
            MultipartBody multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "screenshot.png", requestBody)
                    .build();

            // Build the request
            Request request = new Request.Builder()
                    .url(serverUrl)
                    .post(multipartBody)
                    .build();

            try {
                Response response = httpClient.newCall(request).execute();

                if (response.isSuccessful()) {
                    Log.w("MOJE", "Request sent successfully. Server response: " + response.body().string());
                } else {
                    Log.w("MOJE", "Failed to send request. Server response: " + response.body().string());
                }
                response.close();
            } catch (IOException e) {
                Log.w("MOJE", "Error sending request: " + e.getMessage());
            }

            return null;
        }
    }
}