package com.frankgh.popularmovies.app;

import android.content.ActivityNotFoundException;
import android.content.ClipDescription;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.data.MoviesContract;
import com.frankgh.popularmovies.task.MovieReviewsAsyncTask;
import com.frankgh.popularmovies.task.MovieVideosAsyncTask;
import com.frankgh.popularmovies.themoviedb.model.Review;
import com.frankgh.popularmovies.themoviedb.model.Video;
import com.frankgh.popularmovies.util.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        MovieReviewsAsyncTask.Callback, MovieVideosAsyncTask.Callback {

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

    static final String YOU_TUBE_VIDEO_URL = "http://www.youtube.com/watch?v=";
    static final String DETAIL_URI = "URI";
    static final int COL_EXTRA_NAME = 0;
    static final int COL_EXTRA_VALUE = 1;
    private static final int MOVIE_DETAIL_LOADER = 0;
    private static final int MOVIE_EXTRA_LOADER = 10;
    // For the movie view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] MOVIE_DETAIL_COLUMNS = {
            MoviesContract.MovieEntry.TABLE_NAME + "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.SavedMovieEntry.TABLE_NAME + "." + MoviesContract.SavedMovieEntry._ID
    };
    private static final String[] MOVIE_EXTRA_COLUMNS = {
            MoviesContract.MovieExtraEntry.COLUMN_EXTRA_NAME,
            MoviesContract.MovieExtraEntry.COLUMN_EXTRA_VALUE
    };

    private final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    @Bind(R.id.posterImageView)
    @Nullable
    ImageView mPosterImageView;

    @Bind(R.id.backdrop_image_view)
    ImageView mBackdropImageView;

    @Bind(R.id.movieTitleTextView)
    TextView mMovieTitleTextView;

    @Bind(R.id.movieReleaseDateTextView)
    TextView mMovieReleaseDateTextView;

    @Bind(R.id.grid_item_movie_vote_average)
    TextView mMovieVoteAverageTextView;

    @Bind(R.id.movieOverviewTextView)
    TextView mMovieOverviewTextView;

    @Bind(R.id.favorite_fab)
    FloatingActionButton mFavoriteFab;

    @Bind(R.id.toolbar)
    @Nullable
    Toolbar toolbar;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @Bind(R.id.movie_detail_review_progress_bar)
    ProgressBar movieReviewsProgressBar;

    @Bind(R.id.movie_detail_trailer_progress_bar)
    ProgressBar movieTrailersProgressBar;

    @Bind(R.id.empty_reviews)
    TextView mEmptyReviewsTextView;

    @Bind(R.id.empty_trailers)
    TextView mEmptyTrailersTextView;

    @Bind(R.id.movie_detail_reviews_container)
    LinearLayout mReviewsContainer;

    @Bind(R.id.movie_detail_videos_container)
    LinearLayout mVideosContainer;

    @Bind(R.id.empty_detail_layout)
    LinearLayout mEmptyDetailLayout;

    @Bind(R.id.header_bar)
    View mHeaderBarView;

    @Bind(R.id.movie_detail_body)
    View mBodyView;

    private Uri mSelectedMovieUri;
    private Long mMovieId;
    private Long mSavedMovieId;
    private String mTrailerYouTubeKey;

    private MovieReviewsAsyncTask mMovieReviewsAsyncTask;
    private MovieVideosAsyncTask mMovieVideosAsyncTask;

    private LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        ButterKnife.bind(this, rootView);

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Log.d(LOG_TAG, "onCreateView");

        Bundle arguments = getArguments();
        if (arguments != null) {
            mSelectedMovieUri = arguments.getParcelable(MovieDetailFragment.DETAIL_URI);
            setHasOptionsMenu(true);
            toggleMovieDetail(true);
        } else {
            toggleMovieDetail(false);
        }

        Log.d(LOG_TAG, "onCreateView mMovieId: " + mMovieId);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inflater = getLayoutInflater(savedInstanceState);

        Log.d(LOG_TAG, "onActivityCreated");
        // Load movie detail data
        if (mSelectedMovieUri != null) {
            getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movie_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_share:
                openShareIntent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);

        if (mMovieReviewsAsyncTask != null) {
            mMovieReviewsAsyncTask.cancel(true);
            mMovieReviewsAsyncTask = null;
        }

        if (mMovieVideosAsyncTask != null) {
            mMovieVideosAsyncTask.cancel(true);
            mMovieVideosAsyncTask = null;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
                if (mMovieId != null) {
                    return new CursorLoader(
                            getActivity(),
                            MoviesContract.MovieExtraEntry.buildMovieExtraUri(mMovieId),
                            MOVIE_EXTRA_COLUMNS,
                            null,
                            null,
                            null
                    );
                }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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
        // DO NOTHING
    }

    @Override
    public void onMovieReviewsLoaded(List<Review> reviews) {
        movieReviewsProgressBar.setVisibility(View.GONE);
        mReviewsContainer.removeAllViews();

        if (reviews == null || reviews.isEmpty()) {
            mEmptyReviewsTextView.setVisibility(View.VISIBLE);
            return;
        }

        for (int i = 0; i < reviews.size(); i++) {
            addReviewView(mReviewsContainer, reviews.get(i).getAuthor(), reviews.get(i).getContent());

            if (i + 1 != reviews.size()) {
                addDividerLineView(mReviewsContainer);
            }
        }
    }

    @Override
    public void onMovieVideosLoaded(List<Video> videos) {
        movieTrailersProgressBar.setVisibility(View.GONE);
        mVideosContainer.removeAllViews();
        int videoCount = 0;

        if (videos != null) {
            for (Video video : videos) {
                if (video.getYouTubeThumbnailUrl() != null) {

                    if (TextUtils.isEmpty(mTrailerYouTubeKey)) {
                        mTrailerYouTubeKey = video.getKey();
                    }

                    addVideoView(mVideosContainer, video);
                    videoCount++;
                }
            }
        }

        if (videoCount == 0) {
            setHasOptionsMenu(false); // Hide share option menu
            mEmptyTrailersTextView.setVisibility(View.VISIBLE);
        } else {
            mVideosContainer.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.favorite_fab)
    void onFavoriteClick() {
        if (mSavedMovieId == null) {
            addMovieToFavorites();
        } else {
            removeMovieFromFavorites();
        }
    }

    private void addMovieToFavorites() {
        ContentValues values = new ContentValues();
        values.put(MoviesContract.SavedMovieEntry.COLUMN_MOVIE_KEY, mMovieId);
        values.put(MoviesContract.SavedMovieEntry.COLUMN_IS_SAVED, 1);
        values.put(MoviesContract.SavedMovieEntry.COLUMN_DATE, System.currentTimeMillis());
        values.put(MoviesContract.SavedMovieEntry.COLUMN_UPDATED_DATE, System.currentTimeMillis());

        // Save Favorited movie in Content
        Uri savedMovieUri = getContext().getContentResolver().insert(
                MoviesContract.SavedMovieEntry.CONTENT_URI,
                values
        );

        Snackbar.make(getView(), R.string.movie_detail_added_favorite, Snackbar.LENGTH_SHORT).show();
        mSavedMovieId = MoviesContract.SavedMovieEntry.getIdFromUri(savedMovieUri);
        updateFavoriteFab();
    }

    private void bindMovieDetail(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String movieTitle = data.getString(COL_MOVIE_TITLE);

        getActivity().setTitle(movieTitle);
        collapsingToolbar.setTitle(movieTitle);
        mMovieTitleTextView.setText(movieTitle);

        mMovieId = data.getLong(COL_MOVIE_ID);
        mSavedMovieId = data.isNull(COL_SAVED_MOVIE_ID) ? null : data.getLong(COL_SAVED_MOVIE_ID);
        mMovieOverviewTextView.setText(data.getString(COL_MOVIE_OVERVIEW));
        mMovieReleaseDateTextView.setText(Utility.getFormattedReleaseDate(data.getString(COL_MOVIE_RELEASE_DATE)));
        mMovieVoteAverageTextView.setText(Utility.getFormattedVoteAverage(getActivity(), data.getDouble(COL_MOVIE_VOTE_AVERAGE)));
        // Load movie extra data
        getLoaderManager().initLoader(MOVIE_EXTRA_LOADER, null, this);

        if (mPosterImageView != null) {
            bindImageToView(Utility.getPosterAbsolutePath(data.getString(COL_MOVIE_POSTER_PATH)), mPosterImageView);
        }
        bindImageToView(Utility.getBackDropAbsolutePath(data.getString(COL_MOVIE_BACKDROP_PATH)), mBackdropImageView);
        updateFavoriteFab();
    }

    private void bindMovieExtra(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            executeTasks();
            return; // no data available
        }

        Gson gson = new Gson();

        do {
            String name = data.getString(COL_EXTRA_NAME),
                    value = data.getString(COL_EXTRA_VALUE);

            if (!TextUtils.isEmpty(value)) {
                if (TextUtils.equals(name, MoviesContract.MovieExtraEntry.NAME_REVIEWS)) {

                    try {
                        List<Review> reviews = gson.fromJson(value, new TypeToken<List<Review>>() {
                        }.getType());

                        onMovieReviewsLoaded(reviews);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Unable to deserialize review data for movie with id: " + mMovieId, e);
                    }
                } else if (TextUtils.equals(name, MoviesContract.MovieExtraEntry.NAME_VIDEOS)) {

                    try {
                        List<Video> videos = gson.fromJson(value, new TypeToken<List<Video>>() {
                        }.getType());

                        onMovieVideosLoaded(videos);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Unable to deserialize video data for movie with id: " + mMovieId, e);
                    }
                }
            }
        } while (data.moveToNext());
    }

    private void updateFavoriteFab() {
        mFavoriteFab.setImageResource((mSavedMovieId == null) ?
                R.drawable.ic_favorite_outline_24dp :
                R.drawable.ic_favorite_24dp);
    }

    private void removeMovieFromFavorites() {
        // Delete movie from favorites
        if (getContext().getContentResolver().delete(
                MoviesContract.SavedMovieEntry.CONTENT_URI,
                MoviesContract.SavedMovieEntry.COLUMN_MOVIE_KEY + " = ?",
                new String[]{Long.toString(mMovieId)}
        ) != 0) {
            mSavedMovieId = null;
            Snackbar.make(getView(), R.string.movie_detail_removed_favorite, Snackbar.LENGTH_SHORT).show();
        }
        updateFavoriteFab();
    }

    private void addVideoView(LinearLayout container, final Video video) {
        final View trailerView = inflater.inflate(R.layout.movie_detail_trailer, container, false);
        ImageView thumbnailImageView = ButterKnife.findById(trailerView, R.id.movie_detail_trailer_thumbnail);
        ImageView playImageView = ButterKnife.findById(trailerView, R.id.movie_detail_play_image_view);

        playImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openYoutubeIntent(video.getKey());
            }
        });

        bindImageToView(video.getYouTubeThumbnailUrl(), thumbnailImageView);

        container.addView(trailerView);
    }

    private void toggleMovieDetail(boolean display) {

        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) mFavoriteFab.getLayoutParams();

        if (display) {
            mEmptyDetailLayout.setVisibility(View.GONE);
            mHeaderBarView.setVisibility(View.VISIBLE);
            mBodyView.setVisibility(View.VISIBLE);

            p.setAnchorId(R.id.header_bar);
            mFavoriteFab.setLayoutParams(p);
            mFavoriteFab.setVisibility(View.VISIBLE);
        } else {
            mEmptyDetailLayout.setVisibility(View.VISIBLE);
            mHeaderBarView.setVisibility(View.GONE);
            mBodyView.setVisibility(View.GONE);

            p.setAnchorId(View.NO_ID);
            mFavoriteFab.setLayoutParams(p);
            mFavoriteFab.setVisibility(View.GONE);
        }
    }

    /**
     * Answer found in http://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
     *
     * @param id
     */
    private void openYoutubeIntent(String id) {
        Intent intent = null;
        try {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        } catch (ActivityNotFoundException ex) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOU_TUBE_VIDEO_URL + id));
        }
        startActivity(intent);
    }

    private void openShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(ClipDescription.MIMETYPE_TEXT_PLAIN);
        intent.putExtra(Intent.EXTRA_TEXT, YOU_TUBE_VIDEO_URL + mTrailerYouTubeKey);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, mMovieTitleTextView.getText());
        startActivity(Intent.createChooser(intent, getActivity().getString(R.string.action_share)));
    }

    private void addReviewView(LinearLayout container, String author, String content) {
        final View reviewView = inflater.inflate(R.layout.movie_detail_review, container, false);
        TextView reviewAuthor = ButterKnife.findById(reviewView, R.id.movie_detail_review_author);
        TextView reviewContent = ButterKnife.findById(reviewView, R.id.movie_detail_review_content);
        reviewAuthor.setText(author);
        reviewContent.setText(content);
        container.addView(reviewView);
    }

    private void addDividerLineView(LinearLayout container) {
        final View dividerLineView = inflater.inflate(R.layout.divider_line, container, false);
        container.addView(dividerLineView);
    }

    /**
     * Retrieve movie reviews and videos from TheMovieDB Service asynchronously
     */
    private void executeTasks() {
        mMovieReviewsAsyncTask = new MovieReviewsAsyncTask(this, mMovieId);
        mMovieVideosAsyncTask = new MovieVideosAsyncTask(this, mMovieId);

        mMovieReviewsAsyncTask.execute();
        mMovieVideosAsyncTask.execute();
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
