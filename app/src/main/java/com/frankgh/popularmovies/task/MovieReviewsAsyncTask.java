package com.frankgh.popularmovies.task;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.frankgh.popularmovies.data.MoviesContract;
import com.frankgh.popularmovies.themoviedb.api.TheMovieDbServiceFactory;
import com.frankgh.popularmovies.themoviedb.model.MovieReviewsResponse;
import com.frankgh.popularmovies.themoviedb.model.Review;
import com.google.gson.Gson;

import java.util.List;

import retrofit.Response;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 1/11/16.
 */
public class MovieReviewsAsyncTask extends AsyncTask<Void, Void, List<Review>> {

    private final String LOG_TAG = MovieReviewsAsyncTask.class.getSimpleName();

    private long mMovieId;
    private Context mContext;
    private Fragment mFragment;

    public MovieReviewsAsyncTask(@NonNull Fragment fragment, long movieId) {
        mMovieId = movieId;
        mFragment = fragment;
        mContext = fragment.getActivity();
    }

    @Override
    protected List<Review> doInBackground(Void... params) {
        MovieReviewsResponse response = loadMovieReviews();
        if (response != null) {
            persistMovieReviews(response.getResults());
            return response.getResults();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Review> reviews) {
        super.onPostExecute(reviews);

        if (mFragment != null) {
            ((Callback) mFragment).onMovieReviewsLoaded(reviews);
        }
    }

    private MovieReviewsResponse loadMovieReviews() {
        try {
            Response<MovieReviewsResponse> response =
                    TheMovieDbServiceFactory.getService()
                            .movieReviews(Long.toString(mMovieId), null, null)
                            .execute();

            if (response != null && response.isSuccess()) {
                return response.body();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Unable to load reviews for movie " + mMovieId, e);
        }

        return null;
    }

    private void persistMovieReviews(List<Review> results) {
        ContentValues values = new ContentValues();
        values.put(MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY, mMovieId);
        values.put(MoviesContract.MovieExtraEntry.COLUMN_EXTRA_NAME, MoviesContract.MovieExtraEntry.NAME_REVIEWS);
        values.put(MoviesContract.MovieExtraEntry.COLUMN_EXTRA_VALUE, new Gson().toJson(results));
        values.put(MoviesContract.MovieExtraEntry.COLUMN_ADDED_DATE, System.currentTimeMillis());

        // Delete existing reviews for this movie
        mContext.getContentResolver().delete(
                MoviesContract.MovieExtraEntry.CONTENT_URI,
                MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY + " = ? AND " +
                        MoviesContract.MovieExtraEntry.COLUMN_EXTRA_NAME + " = ?",
                new String[]{Long.toString(mMovieId), MoviesContract.MovieExtraEntry.NAME_REVIEWS}
        );

        // Add movie reviews
        mContext.getContentResolver().insert(
                MoviesContract.MovieExtraEntry.CONTENT_URI,
                values
        );
    }

    /**
     * A callback interface that all fragments containing this task can implement.
     * This mechanism allows activities to be notified when movie reviews are loaded.
     */
    public interface Callback {
        /**
         * MovieReviewsAsyncTaskCallback for when movie reviews have been loaded.
         *
         * @param reviews the movie reviews
         */
        void onMovieReviewsLoaded(List<Review> reviews);
    }
}
