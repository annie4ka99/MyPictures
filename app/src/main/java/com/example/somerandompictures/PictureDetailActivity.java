package com.example.somerandompictures;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

/**
 * An activity representing a single Picture detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PictureListActivity}.
 */
public class PictureDetailActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "com.example.somerandompictures.extra.url";
    private static final String EXTRA_DESCRIPTION = "com.example.somerandompictures.extra.description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(EXTRA_URL,
                    getIntent().getStringExtra(EXTRA_URL));
            arguments.putString(EXTRA_DESCRIPTION,
                    getIntent().getStringExtra(EXTRA_DESCRIPTION));
            PictureDetailFragment fragment = new PictureDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.picture_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            navigateUpTo(new Intent(this, PictureListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
