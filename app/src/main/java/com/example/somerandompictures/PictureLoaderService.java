package com.example.somerandompictures;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Queue;


public class PictureLoaderService extends IntentService {
    private static final String EXTRA_URL = "com.example.somerandompictures.extra.url";
    private static final String LOG_TAG = PictureLoaderService.class.getSimpleName();


    private final Handler main = new Handler(Looper.getMainLooper());
    private OnLoad callback;

    private final Queue<Bitmap> responses = new LinkedList<>();

    public PictureLoaderService() {
        super("PictureLoader");
    }

    public static void load(Context context, String url) {
        Intent intent = new Intent(context, PictureLoaderService.class);
        intent.putExtra(EXTRA_URL, url);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String url = intent.getStringExtra(EXTRA_URL);
            if (url != null) {
                loadPicture(url);
            }
        }
    }

    private void loadPicture(String urlString) {
        String cachePath = getCacheDir().getAbsolutePath().concat("/").concat(urlString);
        File file = new File(cachePath);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            byte[] res;
            try {
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                connection.connect();
                res = new byte[connection.getContentLength()];
                try (InputStream is = connection.getInputStream();
                     OutputStream out = new BufferedOutputStream(new FileOutputStream(cachePath))) {
                    int p = 0;
                    int r;
                    while ((r = is.read(res, p, res.length - p)) > 0) p += r;
                    out.write(res);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final Bitmap result = BitmapFactory.decodeFile(cachePath);

        Log.d(LOG_TAG, "loadPicture: got: url: " + urlString);
        main.post(new Runnable() {
            @Override
            public void run() {
                PictureLoaderService.this.deliver(result);
            }
        });
    }

    private void deliver(Bitmap data) {
        if (callback != null) callback.onLoad(data);
        else
            responses.add(data);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind: ");
        return new MyBinder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnbind: ");
        callback = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind: ");
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate: ");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(LOG_TAG, "onStart: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy: ");
    }

    //@AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class MyBinder extends Binder {
        private final PictureLoaderService service;

        MyBinder(PictureLoaderService pictureLoader) {
            this.service = pictureLoader;
        }

        void setCallback(final OnLoad callback) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    service.callback = callback;
                    while (!service.responses.isEmpty())
                        service.callback.onLoad(service.responses.remove());
                }
            });
        }
    }

    public interface OnLoad {
        void onLoad(Bitmap data);
    }
}