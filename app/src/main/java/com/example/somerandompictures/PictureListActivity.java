package com.example.somerandompictures;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.somerandompictures.picture.PictureContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PictureListActivity extends AppCompatActivity {
    private boolean mTwoPane;
    private SimpleItemRecyclerViewAdapter adapter;

    private static final String EXTRA_URL = "com.example.somerandompictures.extra.url";
    private static final String EXTRA_DESCRIPTION = "com.example.somerandompictures.extra.description";
    private static final String LOG_TAG_DESC = "description";
    private static final String LOG_TAG_URL = "url";

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ((DescriptionLoaderService.MyBinder) service).setCallback(new DescriptionLoaderService.OnLoad() {
                @Override
                public void onLoad(JSONObject object) {
                    adapter.setData(object);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());


        if (findViewById(R.id.picture_detail_container) != null) {
            mTwoPane = true;
        }

        RecyclerView recyclerView = findViewById(R.id.picture_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);
        DescriptionLoaderService.load(this, getString(R.string.query));
        bindService(new Intent(this, DescriptionLoaderService.class), serviceConnection, 0);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new SimpleItemRecyclerViewAdapter(this, mTwoPane);
        recyclerView.setAdapter(adapter);
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {
        private final PictureListActivity mParentActivity;
        private final ArrayList<PictureContent> mValues = new ArrayList<>();
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureContent item = (PictureContent) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(EXTRA_URL, item.getUrl());
                    arguments.putString(EXTRA_DESCRIPTION, item.getDescription());
                    PictureDetailFragment fragment = new PictureDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.picture_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PictureDetailActivity.class);
                    intent.putExtra(EXTRA_URL, item.getUrl());
                    intent.putExtra(EXTRA_DESCRIPTION, item.getDescription());
                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(PictureListActivity parent,
                                      boolean twoPane) {
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        void setData(JSONObject data) {
            if (data == null)
                return;
            try {
                JSONArray pictureObjects = data.getJSONArray("items");
                JSONObject pictureObject;
                String description, url;
                for (int i = 0; i < pictureObjects.length(); i++) {
                    pictureObject = pictureObjects.getJSONObject(i);
                    description = pictureObject.getString("title");
                    Log.d(LOG_TAG_DESC, description);
                    String pureUrl = pictureObject.getJSONObject("media").getString("m");
                    Log.d(LOG_TAG_URL, pureUrl);
                    //changing size of downloading picture in url from small to normal
                    url = pureUrl.substring(0, pureUrl.length() - 5) + "c.jpg";
                    mValues.add(new PictureContent(String.valueOf(i + 1), url, description));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            notifyDataSetChanged();
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.picture_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).getId());
            holder.mContentView.setText(mValues.get(position).getDescription());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = view.findViewById(R.id.id_text);
                mContentView = view.findViewById(R.id.content);
            }
        }
    }

    public void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

}
