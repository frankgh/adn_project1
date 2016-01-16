package com.frankgh.popularmovies.task;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.frankgh.popularmovies.data.MoviesContract;
import com.frankgh.popularmovies.themoviedb.api.TheMovieDbServiceFactory;
import com.frankgh.popularmovies.themoviedb.model.MovieVideosResponse;
import com.frankgh.popularmovies.themoviedb.model.Video;
import com.google.gson.Gson;

import java.util.List;

import retrofit.Response;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 1/11/16.
 */
public class MovieVideosAsyncTask extends AsyncTask<Void, Void, List<Video>> {

    private final String LOG_TAG = MovieVideosAsyncTask.class.getSimpleName();

    private long mMovieId;
    private Context mContext;
    private Fragment mFragment;

    public MovieVideosAsyncTask(@NonNull Fragment fragment, long movieId) {
        mMovieId = movieId;
        mFragment = fragment;
        mContext = fragment.getActivity();
    }

    @Override
    protected List<Video> doInBackground(Void... params) {
        MovieVideosResponse response = loadMovieVideos();
        if (response != null) {
            persistMovieVideos(response.getResults());
            return response.getResults();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Video> videos) {
        super.onPostExecute(videos);

        if (mFragment != null) {
            ((Callback) mFragment).onMovieVideosLoaded(videos);
        }
    }

    private MovieVideosResponse loadMovieVideos() {
        try {
            Response<MovieVideosResponse> response =
                    TheMovieDbServiceFactory.getService()
                            .movieVideos(Long.toString(mMovieId))
                            .execute();

            if (response != null && response.isSuccess()) {
                return response.body();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Unable to load videos for movie " + mMovieId, e);
        }
        return null;
    }

    private void persistMovieVideos(List<Video> results) {
        ContentValues values = new ContentValues();
        values.put(MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY, mMovieId);
        values.put(MoviesContract.MovieExtraEntry.COLUMN_EXTRA_NAME, MoviesContract.MovieExtraEntry.NAME_VIDEOS);
        values.put(MoviesContract.MovieExtraEntry.COLUMN_EXTRA_VALUE, new Gson().toJson(results));
        values.put(MoviesContract.MovieExtraEntry.COLUMN_ADDED_DATE, System.currentTimeMillis());

        // Delete existing data
        mContext.getContentResolver().delete(
                MoviesContract.MovieExtraEntry.CONTENT_URI,
                MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY + " = ? AND " +
                        MoviesContract.MovieExtraEntry.COLUMN_EXTRA_NAME + " = ?",
                new String[]{Long.toString(mMovieId), MoviesContract.MovieExtraEntry.NAME_VIDEOS}
        );

        // Persist data
        mContext.getContentResolver().insert(
                MoviesContract.MovieExtraEntry.CONTENT_URI,
                values
        );
    }

    /**
     * A callback interface that all fragments containing this task can implement.
     * This mechanism allows activities to be notified when movie videos are loaded.
     */
    public interface Callback {
        /**
         * MovieVideosAsyncTaskCallback for when movie reviews have been loaded.
         *
         * @param videos the movie videos
         */
        void onMovieVideosLoaded(List<Video> videos);
    }
}
