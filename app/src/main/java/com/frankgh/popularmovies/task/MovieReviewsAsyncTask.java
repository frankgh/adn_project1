package com.frankgh.popularmovies.task;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
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
public class MovieReviewsAsyncTask extends AsyncTask<Void, Void, Void> {

    private final String LOG_TAG = MovieReviewsAsyncTask.class.getSimpleName();

    private long mMovieId;
    private Context mContext;

    public MovieReviewsAsyncTask(Context context, long movieId) {
        mMovieId = movieId;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {

        MovieReviewsResponse response = loadMovieReviews();
        if (response != null) {
            persistMovieReviews(response.getResults());
        }

        return null;
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

        mContext.getContentResolver().delete(
                MoviesContract.MovieExtraEntry.CONTENT_URI,
                MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY + " = ? AND " +
                        MoviesContract.MovieExtraEntry.COLUMN_EXTRA_NAME + " = ?",
                new String[]{Long.toString(mMovieId), MoviesContract.MovieExtraEntry.NAME_REVIEWS}
        );

        mContext.getContentResolver().insert(
                MoviesContract.MovieExtraEntry.CONTENT_URI,
                values
        );
    }
}