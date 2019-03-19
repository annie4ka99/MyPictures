package com.example.somerandompictures;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Queue;


public class DescriptionLoaderService extends IntentService {
    private static final String EXTRA_URL = "com.example.somerandompictures.extra.url";
    private static final String LOG_TAG = PictureLoaderService.class.getSimpleName();
    private static final String LOG_FULL_DESC = "full description";
    private final Handler main = new Handler(Looper.getMainLooper());
    private OnLoad callback;

    private final Queue<JSONObject> responses = new LinkedList<>();

    public DescriptionLoaderService() {
        super("DescriptionLoader");
    }

    public static void load(Context context, String url) {
        Intent intent = new Intent(context, DescriptionLoaderService.class);
        intent.putExtra(EXTRA_URL, url);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String url = intent.getStringExtra(EXTRA_URL);
            if (url != null) {
                loadDescription(url);
            }
        }
    }

    private void loadDescription(String urlString) {
        JSONObject result = new JSONObject();
        try {
            URLConnection connection = (new URL(urlString)).openConnection();
            connection.connect();
            try (InputStream is = connection.getInputStream()) {
                BufferedReader descriptionReader = new BufferedReader(new InputStreamReader(is), 64);
                StringBuilder description = new StringBuilder();
                String line;
                while ((line = descriptionReader.readLine()) != null) {
                    description.append(line).append('\n');
                }
                Log.d(LOG_FULL_DESC, "*" + description.toString() + "*");
                //remove '*jsonFlickrFeed('-prefix from downloading JSONObject
                result = new JSONObject(description.substring(15, description.length() - 2));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        final JSONObject finalResult = result;
        Log.d(LOG_TAG, "loadPicture: got: url: " + urlString);
        main.post(new Runnable() {
            @Override
            public void run() {
                DescriptionLoaderService.this.deliver(finalResult);
            }
        });
    }

    private void deliver(JSONObject data) {
        if (callback != null) callback.onLoad(data);
        else
            responses.add(data);
    }

    @Nullable
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
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(LOG_TAG, "onStart: ");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
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
        private final DescriptionLoaderService service;

        MyBinder(DescriptionLoaderService descriptionLoader) {
            this.service = descriptionLoader;
        }

        void setCallback(@NonNull final OnLoad callback) {
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
        void onLoad(JSONObject data);
    }
}
