package com.frankgh.popularmovies.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.data.MoviesContract;
import com.frankgh.popularmovies.themoviedb.api.TheMovieDbService;
import com.frankgh.popularmovies.util.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Fragment that loads Movie data from the api and loads it into the gridView.
 *
 * @author francisco <email>frank.guerrero@gmail.com</email>
 */
public class MovieListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    // These indices are tied to MOVIE_COLUMNS.  If MOVIE_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_VOTE_AVERAGE = 2;
    static final int COL_MOVIE_POSTER_PATH = 3;

    private static final int MOVIE_LOADER = 0;
    // For the movie view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_COLUMNS = {
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
    };

    private static final String SELECTED_KEY = "selected_position";
    private final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private final String MOVIE_LIST_KEY = "MovieListFragment_Movie_Data";

    @Bind(R.id.gridview_movies)
    GridView mGridView;
    @Bind(R.id.pull_to_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private int mPosition = GridView.INVALID_POSITION;
    private MovieAdapter mMovieAdapter;

    public static Fragment newInstance() {
        return new MovieListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Handle menu events

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The gridView probably hasn't even been populated yet.
            // Actually perform the swapOut in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);
        ButterKnife.bind(this, rootView);

        // The cursor adapter for the movies
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);
        mGridView.setAdapter(mMovieAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), MovieDetailActivity.class)
                            .setData(MoviesContract.MovieEntry.buildMovieUri(cursor.getLong(MovieListFragment.COL_MOVIE_ID)));
                    startActivity(intent);
                }
                mPosition = position;
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onRefresh() {
        Log.d(LOG_TAG, "onRefresh(): Reloading movies");
        updateMovies();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String sortBy = null, sortOrder = null;

        switch (item.getItemId()) {
            case R.id.action_most_popular:
                sortBy = TheMovieDbService.SORT_BY_POPULARITY;
                sortOrder = TheMovieDbService.SORT_ORDER_DESC;
                break;

            case R.id.action_highest_rated:
                sortBy = TheMovieDbService.SORT_BY_VOTE_AVERAGE;
                sortOrder = TheMovieDbService.SORT_ORDER_DESC;
                break;

            case R.id.action_recent_releases:
                sortBy = TheMovieDbService.SORT_BY_RELEASE_DATE;
                sortOrder = TheMovieDbService.SORT_ORDER_DESC;
                break;
        }

        if (!TextUtils.isEmpty(sortBy) && !TextUtils.isEmpty(sortOrder)) {
            String currentValue = Utility.getSortingPreference(getContext());
            String newValue = String.format("%s.%s", sortBy, sortOrder);

            if (!TextUtils.equals(currentValue, newValue)) {
                Utility.updateSortingPreference(getContext(), newValue);
                updateMovies();
                return true;
            }

            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    void onSortOrderChanged() {
        updateMovies();
    }

    private void updateMovies() {
        mSwipeRefreshLayout.setRefreshing(true);
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MoviesContract.MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSwipeRefreshLayout.setRefreshing(false);
        mMovieAdapter.swapCursor(data);

        if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a
            // desired position to restore to, do so now.
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }
}
