package com.frankgh.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    private DiscoverMovieResult mMovieData;

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra(MovieDetailActivity.MOVIE_DETAIL_KEY)) {
            mMovieData = intent.getParcelableExtra(MovieDetailActivity.MOVIE_DETAIL_KEY);
        }

        if (mMovieData == null) {
            getActivity().onBackPressed();
            return rootView;
        }

        //getActivity().setTitle(mMovieData.getTitle());


        return rootView;
    }
}
