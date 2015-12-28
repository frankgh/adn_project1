package com.frankgh.popularmovies.app;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Fragment that loads Movie data from the api and loads it into the gridview.
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
    static final int COL_MOVIE_BACKDROP_PATH = 3;
    static final int COL_MOVIE_POSTER_PATH = 4;
    private static final int MOVIE_LOADER = 0;
    // For the movie view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_COLUMNS = {
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
    };
    private static final String SELECTED_KEY = "selected_position";
    private final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private final String MOVIE_LIST_KEY = "MovieListFragment_Movie_Data";
    private final String SORT_PREFERENCE_KEY = "sort_by_pref";
    @Bind(R.id.gridview_movies)
    GridView mGridView;
    @Bind(R.id.pull_to_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private int mPosition = GridView.INVALID_POSITION;
    //private MovieGridAdapter mMovieGridAdapter;
    private MovieAdapter mMovieAdapter;

    public static Fragment newInstance() {
        return new MovieListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Handle menu events

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
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
//        // the adapter for the movies
//        mMovieGridAdapter = new MovieGridAdapter(
//                getActivity(), // context
//                R.layout.grid_item_movie, // the layout id for the movie view
//                mMovieList // the movie data
//        );

        mGridView.setAdapter(mMovieAdapter);
        //mGridView.setAdapter(mMovieGridAdapter); // Attach the adapter to the gridview
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
//                if (cursor != null) {
//                    String locationSetting = Utility.getPreferredLocation(getActivity());
//                    ((Callback) getActivity())
//                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//                }
                mPosition = position;


                //DiscoverMovieResult movieData = mMovieList.get(position);
                //Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class);
                //detailIntent.putExtra(MovieDetailActivity.MOVIE_DETAIL_KEY, movieData);
                //startActivity(detailIntent);
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
        Log.d(LOG_TAG, "Reloading movies");
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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            String currentValue = getSortingPreference(prefs);
            String newValue = String.format("%s.%s", sortBy, sortOrder);

            if (!TextUtils.equals(currentValue, newValue)) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString(SORT_PREFERENCE_KEY, newValue); // Save new value
                edit.commit();

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
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
//        DiscoverMoviesTask discoverMoviesTask = new DiscoverMoviesTask();
//        discoverMoviesTask.execute();
    }

    private String getSortingPreference(SharedPreferences sharedPreferences) {
        String defaultValue = String.format("%s.%s", TheMovieDbService.SORT_BY_POPULARITY, TheMovieDbService.SORT_ORDER_DESC);
        return sharedPreferences.getString(SORT_PREFERENCE_KEY, defaultValue);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //SharedPreferences sharedPreferences = PreferenceManager
        //        .getDefaultSharedPreferences(getContext());
        //String sortOrder = getSortingPreference(sharedPreferences);

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
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mSwipeRefreshLayout.setRefreshing(false);
        mMovieAdapter.swapCursor(null);
    }

//    public class DiscoverMoviesTask extends AsyncTask<String, Void, List<DiscoverMovieResult>> {
//
//        private final String LOG_TAG = DiscoverMoviesTask.class.getSimpleName();
//
//        @Override
//        protected List<DiscoverMovieResult> doInBackground(String... params) {
//            SharedPreferences sharedPreferences = PreferenceManager
//                    .getDefaultSharedPreferences(getContext());
//
//            try {
//                Response<DiscoverMovieResponse> response = TheMovieDbServiceFactory.getService()
//                        .discoverMovies(getSortingPreference(sharedPreferences)).execute();
//
//                if (response != null && response.isSuccess() && response.body() != null) {
//                    return response.body().getResults();
//                }
//            } catch (Exception e) {
//                Log.e(LOG_TAG, e.getMessage(), e);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(List<DiscoverMovieResult> results) {
////            mMovieList = results;
////            if (mMovieList != null) {
////                mMovieGridAdapter.swapData(mMovieList);
////            }
//            mSwipeRefreshLayout.setRefreshing(false);
//        }
//    }
}
