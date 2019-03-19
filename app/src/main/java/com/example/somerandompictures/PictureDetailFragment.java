package com.example.somerandompictures;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class PictureDetailFragment extends Fragment {
    private static final String EXTRA_URL = "com.example.somerandompictures.extra.url";
    private static final String EXTRA_DESCRIPTION = "com.example.somerandompictures.extra.description";
    Context myContext;

    private String url;
    private String description;
    private PictureLoaderService.MyBinder pictureLoaderBinder;
    View rootView;
    ImageView pictureView;
    TextView descriptionView;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PictureDetailFragment.this.pictureLoaderBinder = (PictureLoaderService.MyBinder) service;
            pictureLoaderBinder.setCallback(new PictureLoaderService.OnLoad() {
                @Override
                public void onLoad(Bitmap bitmap) {
                    pictureView.setImageBitmap(bitmap);
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public PictureDetailFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.myContext = context;
        if (context != null) {
            context.bindService(new Intent(context, PictureLoaderService.class), serviceConnection, 0);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(EXTRA_URL)) {
            url = getArguments().getString(EXTRA_URL);
        }
        if (getArguments().containsKey(EXTRA_DESCRIPTION)) {
            description = getArguments().getString(EXTRA_DESCRIPTION);
        }


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.picture_detail, container, false);
        pictureView = rootView.findViewById(R.id.picture_view);
        descriptionView = rootView.findViewById(R.id.description_view);
        PictureLoaderService.load(rootView.getContext(), url);
        pictureView.setContentDescription(description);
        descriptionView.setText(description);
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (myContext != null) {
            myContext.unbindService(serviceConnection);
        }
        super.onDetach();
    }

}
