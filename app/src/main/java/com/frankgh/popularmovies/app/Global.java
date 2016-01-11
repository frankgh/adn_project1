package com.frankgh.popularmovies.app;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Application defaults
 *
 * Created by francisco on 11/30/15.
 */
public class Global extends Application {

    private static final String LOG_TAG = Global.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        configureStetho(); // Stetho debugging
        configurePicasso(); // Picasso defaults
    }

    /**
     * Configures Picasso defaults
     */
    private void configurePicasso() {
        Log.d(LOG_TAG, "configurePicasso() invoked");
        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(this, Integer.MAX_VALUE))
                .build();
        // Enable the following two lines for debugging
        //picasso.setIndicatorsEnabled(true);
        //picasso.setLoggingEnabled(true);
        Picasso.setSingletonInstance(picasso);
    }

    /**
     * Configures Stetho defaults
     */
    private void configureStetho() {
        Log.d(LOG_TAG, "configureStetho() invoked");
        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());
    }
}
