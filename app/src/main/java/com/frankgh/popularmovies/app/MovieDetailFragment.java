package com.frankgh.popularmovies.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.data.MoviesContract;
import com.frankgh.popularmovies.util.Utility;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // These indices are tied to MOVIE_DETAIL_COLUMNS.  If MOVIE_DETAIL_COLUMNS changes, these
    // must change.
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_VOTE_AVERAGE = 2;
    static final int COL_MOVIE_BACKDROP_PATH = 3;
    static final int COL_MOVIE_POSTER_PATH = 4;
    static final int COL_MOVIE_RELEASE_DATE = 5;
    static final int COL_MOVIE_OVERVIEW = 6;
    static final int COL_SAVED_MOVIE_ID = 7;

    static final String DETAIL_URI = "URI";
    private static final int MOVIE_DETAIL_LOADER = 0;
    private static final int MOVIE_EXTRA_LOADER = 10;
    // For the movie view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_DETAIL_COLUMNS = {
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.SavedMovieEntry.TABLE_NAME + "." + MoviesContract.SavedMovieEntry._ID
    };

    private final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    @Bind(R.id.posterImageView)
    ImageView mPosterImageView;

    @Bind(R.id.backdrop_image_view)
    ImageView mBackdropImageView;

    @Bind(R.id.movieReleaseDateTextView)
    TextView mMovieReleaseDateTextView;

    @Bind(R.id.grid_item_movie_vote_average)
    TextView mMovieVoteAverageTextView;

    @Bind(R.id.movieOverviewTextView)
    TextView mMovieOverviewTextView;

    private Uri mSelectedMovieUri;
    private Integer mMovieId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);
        getActivity().setTitle("");

        Bundle arguments = getArguments();
        if (arguments != null) {
            mSelectedMovieUri = arguments.getParcelable(MovieDetailFragment.DETAIL_URI);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader()");

        switch (id) {
            case MOVIE_DETAIL_LOADER:
                if (mSelectedMovieUri != null) {
                    return new CursorLoader(
                            getActivity(),
                            mSelectedMovieUri,
                            MOVIE_DETAIL_COLUMNS,
                            null,
                            null,
                            null
                    );
                }
                break;

            case MOVIE_EXTRA_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MoviesContract.MovieExtraEntry.buildMovieExtraUri(mMovieId),
                        null,
                        null,
                        null,
                        null
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished()");

        switch (loader.getId()) {
            case MOVIE_DETAIL_LOADER:
                bindMovieDetail(loader, data);
                break;

            case MOVIE_EXTRA_LOADER:
                bindMovieExtra(loader, data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void bindMovieDetail(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            getActivity().onBackPressed(); // Invalid movie
            return;
        }

        mMovieId = data.getInt(COL_MOVIE_ID);
        mMovieOverviewTextView.setText(data.getString(COL_MOVIE_OVERVIEW));
        mMovieReleaseDateTextView.setText(Utility.getFormattedReleaseDate(data.getString(COL_MOVIE_RELEASE_DATE)));
        mMovieVoteAverageTextView.setText(Utility.getFormattedVoteAverage(getActivity(), data.getDouble(COL_MOVIE_VOTE_AVERAGE)));

        // Load movie extra data
        getLoaderManager().initLoader(MOVIE_EXTRA_LOADER, null, this);

        bindImageToView(Utility.getPosterAbsolutePath(data.getString(COL_MOVIE_POSTER_PATH)), mPosterImageView);
        bindImageToView(Utility.getBackDropAbsolutePath(data.getString(COL_MOVIE_BACKDROP_PATH)), mBackdropImageView);
    }

    private void bindMovieExtra(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return; // no data available
        }

        //Gson gson = new Gson();


    }

    private void bindImageToView(final String path, final ImageView imageView) {

        if (TextUtils.isEmpty(path)) {
            return;
        }

        Picasso.with(getContext())
                .load(path)
                .networkPolicy(NetworkPolicy.OFFLINE) // loads cached version
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        //Try again online if cache failed
                        Picasso.with(getContext())
                                .load(path)
                                .error(R.drawable.ic_error_24dp)
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError() {
                                        Log.v(LOG_TAG, "Could not fetch image from " + path);
                                    }
                                });
                    }
                });
    }

    private static class MovieExtraLoader extends AsyncTaskLoader<List<String>> {
        public MovieExtraLoader(Context context) {
            super(context);
        }

        @Override
        public List<String> loadInBackground() {
            return null;
        }

        @Override
        public void deliverResult(List<String> data) {
            super.deliverResult(data);
        }
    }
}
