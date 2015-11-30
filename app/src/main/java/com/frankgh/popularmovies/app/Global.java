package com.frankgh.popularmovies.app;

import android.app.Application;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by francisco on 11/30/15.
 */
public class Global extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        configurePicasso();
    }

    /**
     * Configures Picasso defaults
     */
    private void configurePicasso() {
        Picasso built = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(this, Integer.MAX_VALUE))
                .build();
        //built.setIndicatorsEnabled(true);
        //built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
