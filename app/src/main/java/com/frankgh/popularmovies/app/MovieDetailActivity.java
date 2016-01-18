package com.frankgh.popularmovies.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.frankgh.popularmovies.R;

import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity {

    public final static String MOVIE_DETAIL_KEY = "MOVIE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(MovieDetailFragment.DETAIL_URI, getIntent().getData());

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_movie_detail, fragment)
                    .commit();
        }
    }

}
