package com.frankgh.popularmovies.app;

import android.database.Cursor;
import android.net.Uri;
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
import com.frankgh.popularmovies.sync.MoviesSyncAdapter;
import com.frankgh.popularmovies.themoviedb.api.TheMovieDbService;
import com.frankgh.popularmovies.util.Utility;

import butterknife.Bind;
import butterknife.BindString;
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
    static final int COL_DISPLAYED_MOVIE_ID = 0;
    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_VOTE_AVERAGE = 3;
    static final int COL_MOVIE_POSTER_PATH = 4;

    private static final int MOVIE_LOADER = 0;
    // For the movie view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_COLUMNS = {
            MoviesContract.DisplayedMovieEntry.TABLE_NAME + "." + MoviesContract.DisplayedMovieEntry._ID,
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
    };

    private static final String[] FAVORITE_MOVIE_COLUMNS = {
            MoviesContract.SavedMovieEntry.TABLE_NAME + "." + MoviesContract.SavedMovieEntry._ID,
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
    @BindString(R.string.action_favorites)
    String SORT_BY_FAVORITES;
    private int mPosition = GridView.INVALID_POSITION;
    private MovieAdapter mMovieAdapter;
    private boolean mRequiresLoaderRestart;

    public static Fragment newInstance() {
        return new MovieListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Handle menu events
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
                    Uri movieDetailUri = MoviesContract.MovieEntry.buildMovieUri(cursor.getLong(MovieListFragment.COL_MOVIE_ID));
                    ((Callback) getActivity())
                            .onMovieSelected(movieDetailUri);
                }
                mPosition = position;
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The gridView probably hasn't even been populated yet.
            // Actually perform the swapOut in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

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

        if (SORT_BY_FAVORITES.equals(Utility.getSortingPreference(getActivity()))) {
            mRequiresLoaderRestart = true;
        }

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

            case R.id.action_favorites:
                sortBy = SORT_BY_FAVORITES;
                sortOrder = SORT_BY_FAVORITES;
                break;
        }

        if (!TextUtils.isEmpty(sortBy) && !TextUtils.isEmpty(sortOrder)) {
            String currentValue = Utility.getSortingPreference(getContext());
            String newValue = null;

            if (SORT_BY_FAVORITES.equals(sortBy)) {
                newValue = sortBy;
                mRequiresLoaderRestart = true;
            } else {
                newValue = String.format("%s.%s", sortBy, sortOrder);
            }

            if (!TextUtils.equals(currentValue, newValue)) {
                if (SORT_BY_FAVORITES.equals(currentValue)) {
                    mRequiresLoaderRestart = true;
                }

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

    private void updateMovies() {
        mSwipeRefreshLayout.setRefreshing(true);

        if (!SORT_BY_FAVORITES.equals(Utility.getSortingPreference(getActivity()))) {
            MoviesSyncAdapter.syncImmediately(getActivity());
        }

        if (mRequiresLoaderRestart) {
            Log.d(LOG_TAG, "Restarting loader");
            mRequiresLoaderRestart = false;
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String preference = Utility.getSortingPreference(getActivity());

        if (SORT_BY_FAVORITES.equals(preference)) {
            // Favorite movies
            return new CursorLoader(getActivity(),
                    MoviesContract.SavedMovieEntry.CONTENT_URI,
                    FAVORITE_MOVIE_COLUMNS,
                    null,
                    null,
                    null);
        } else {
            return new CursorLoader(getActivity(),
                    MoviesContract.DisplayedMovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null);
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSwipeRefreshLayout.setRefreshing(false);
        mMovieAdapter.swapCursor(data);

        if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a
            // desired position to restore to, do so now.
            mGridView.smoothScrollToPosition(mPosition);
            //mGridView.setSelection(mPosition);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         *
         * @param movieUri the movie URI
         */
        void onMovieSelected(Uri movieUri);
    }
}
