package com.frankgh.popularmovies.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.frankgh.popularmovies.MovieAdapter;
import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.themoviedb.api.TheMovieDbService;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<String> mMoviesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GridView gridview = (GridView) findViewById(R.id.moviesGridView);
        gridview.setAdapter(new MovieAdapter(this, R.layout.movie_grid_item, null));


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DiscoverMoviesTask moviesTask = new DiscoverMoviesTask();
        moviesTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class DiscoverMoviesTask extends AsyncTask<String, Void, List<DiscoverMovieResult>> {

        private final String LOG_TAG = DiscoverMoviesTask.class.getSimpleName();

        @Override
        protected List<DiscoverMovieResult> doInBackground(String... params) {

            List<DiscoverMovieResult> results = null;

            try {
                results = new TheMovieDbService(getApplicationContext())
                        .discoverMovies(TheMovieDbService.SORT_BY_POPULARITY, TheMovieDbService.SORT_ORDER_DESC);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            return results;
        }
    }


}
