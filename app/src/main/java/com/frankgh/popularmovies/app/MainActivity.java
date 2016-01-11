package com.frankgh.popularmovies.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.sync.MoviesSyncAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieListFragment.Callback {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAIL_FRAGMENT_TAG = "MOVIE_DETAIL_FRAGMENT";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
//            actionBar.setIcon(R.drawable.ic_logo);
//            //actionBar.setDisplayShowHomeEnabled(false);
//            actionBar.setHomeButtonEnabled(false);
//            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setDisplayUseLogoEnabled(false);
//            actionBar.setDisplayHomeAsUpEnabled(false);

//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
//            actionBar.setIcon(R.drawable.ic_logo); //also displays wide logo
//            actionBar.setDisplayShowTitleEnabled(false); //optional

            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_logo);


        }

        mTwoPane = findViewById(R.id.fragment_movie_detail) != null;

        if (mTwoPane && savedInstanceState == null) {
            // Add Detail Fragment on two pane layouts
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_movie_detail, new MovieDetailFragment(), DETAIL_FRAGMENT_TAG)
                    .commit();
        }

        MoviesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onMovieSelected(Uri movieUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_URI, movieUri);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_movie_detail, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class)
                    .setData(movieUri);
            startActivity(intent);
        }
    }
}
