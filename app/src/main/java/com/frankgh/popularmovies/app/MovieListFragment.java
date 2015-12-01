package com.frankgh.popularmovies.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
public class MovieListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private final String LOG_TAG = MovieListFragment.class.getSimpleName();
    private final String MOVIE_LIST_KEY = "MovieListFragment_Movie_Data";
    private final String SORT_PREFERENCE_KEY = "sort_by_pref";

    @Bind(R.id.gridview_movies)
    GridView mGridView;

    @Bind(R.id.pull_to_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private MovieGridAdapter mMovieGridAdapter;
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
        mMovieGridAdapter = new MovieGridAdapter(
                getActivity(), // context
                R.layout.grid_item_movie, // the layout id for the movie view
                mMovieList // the movie data
        );

        mGridView.setAdapter(mMovieGridAdapter); // Attach the adapter to the gridview
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DiscoverMovieResult movieData = mMovieList.get(position);
                Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class);
                detailIntent.putExtra(MovieDetailActivity.MOVIE_DETAIL_KEY, movieData);
                startActivity(detailIntent);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onRefresh() {
        Log.d(LOG_TAG, "Reloading movies");
        updateMovies();
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
        DiscoverMoviesTask discoverMoviesTask = new DiscoverMoviesTask();
        discoverMoviesTask.execute();
    }

    private String getSortingPreference(SharedPreferences sharedPreferences) {
        String defaultValue = String.format("%s.%s", TheMovieDbService.SORT_BY_POPULARITY, TheMovieDbService.SORT_ORDER_DESC);
        return sharedPreferences.getString(SORT_PREFERENCE_KEY, defaultValue);
    }

    public class DiscoverMoviesTask extends AsyncTask<String, Void, List<DiscoverMovieResult>> {

        private final String LOG_TAG = DiscoverMoviesTask.class.getSimpleName();

        @Override
        protected List<DiscoverMovieResult> doInBackground(String... params) {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());

            try {
                return new TheMovieDbService(getContext())
                        .discoverMovies(getSortingPreference(sharedPreferences));
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<DiscoverMovieResult> results) {
            mMovieList = results;
            if (mMovieList != null) {
                mMovieGridAdapter.swapData(mMovieList);
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
