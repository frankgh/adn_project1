package com.frankgh.popularmovies.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.themoviedb.api.TheMovieDbService;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Fragment that loads Movie data from the api and loads it into the gridview.
 *
 * @author francisco <email>frank.guerrero@gmail.com</email>
 */
public class MovieListFragment extends Fragment {

    private final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private final String MOVIE_LIST_KEY = "MovieListFragment_Movie_Data";

    @Bind(R.id.gridview_movies)
    GridView mGridView;

    private MovieAdapter mMovieAdapter;
    private List<DiscoverMovieResult> mMovieList;

    private boolean mLoadData;

    public static Fragment newInstance() {
        return new MovieListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Handle menu events

        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_LIST_KEY)) {
            mMovieList = new ArrayList<DiscoverMovieResult>();
            mLoadData = true;
        } else {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        ButterKnife.bind(this, rootView);

        // the adapter for the movies
        mMovieAdapter = new MovieAdapter(
                getActivity(), // context
                R.layout.movie_grid_item, // the layout id for the movie view
                mMovieList // the movie data
        );

        // Attach the adapter to the gridview
        mGridView.setAdapter(mMovieAdapter);
        mGridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mLoadData) {
            updateMovies();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_LIST_KEY, (ArrayList<DiscoverMovieResult>) mMovieList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    private void updateMovies() {
        DiscoverMoviesTask discoverMoviesTask = new DiscoverMoviesTask();
        discoverMoviesTask.execute();
    }

    public class DiscoverMoviesTask extends AsyncTask<String, Void, List<DiscoverMovieResult>> {

        private final String LOG_TAG = DiscoverMoviesTask.class.getSimpleName();

        @Override
        protected List<DiscoverMovieResult> doInBackground(String... params) {

            try {
                return new TheMovieDbService(getContext())
                        .discoverMovies(TheMovieDbService.SORT_BY_POPULARITY, TheMovieDbService.SORT_ORDER_DESC);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<DiscoverMovieResult> results) {
            mMovieList = results;
            if (mMovieList != null) {
                mMovieAdapter.swapData(mMovieList);
            }
        }
    }
}
