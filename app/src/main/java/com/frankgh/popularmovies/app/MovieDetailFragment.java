package com.frankgh.popularmovies.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
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

    static final String DETAIL_URI = "URI";
    private static final int MOVIE_DETAIL_LOADER = 0;
    // For the movie view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_DETAIL_COLUMNS = {
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW
    };

//    private static final String SELECTED_MOVIE_URI_KEY = "selected_movie_uri";
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

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
////        if (savedInstanceState == null || !savedInstanceState.containsKey(SELECTED_MOVIE_URI_KEY)) {
////            Intent intent = getActivity().getIntent();
////            if (intent == null) {
////                getActivity().onBackPressed(); // Invalid movie
////                return;
////            }
////
////            mSelectedMovieUri = intent.getData();
////        } else {
////            mSelectedMovieUri = savedInstanceState.getParcelable(SELECTED_MOVIE_URI_KEY);
////        }
//    }

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

        if (mSelectedMovieUri == null) {
            getActivity().onBackPressed(); // Invalid movie
            return null;
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

        return new CursorLoader(
                getActivity(),
                mSelectedMovieUri,
                MOVIE_DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
////        outState.putParcelable(SELECTED_MOVIE_URI_KEY, mSelectedMovieUri);
//        super.onSaveInstanceState(outState);
//    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished()");

        if (!data.moveToFirst()) {
            getActivity().onBackPressed(); // Invalid movie
            return;
        }

        mMovieOverviewTextView.setText(data.getString(COL_MOVIE_OVERVIEW));
        mMovieReleaseDateTextView.setText(Utility.getFormattedReleaseDate(data.getString(COL_MOVIE_RELEASE_DATE)));
        mMovieVoteAverageTextView.setText(Utility.getFormattedVoteAverage(getActivity(), data.getDouble(COL_MOVIE_VOTE_AVERAGE)));

        bindImageToView(Utility.getPosterAbsolutePath(data.getString(COL_MOVIE_POSTER_PATH)), mPosterImageView);
        bindImageToView(Utility.getBackDropAbsolutePath(data.getString(COL_MOVIE_BACKDROP_PATH)), mBackdropImageView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
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
}
