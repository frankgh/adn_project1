package com.frankgh.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.data.MoviesContract;
import com.frankgh.popularmovies.themoviedb.api.TheMovieDbServiceFactory;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResponse;
import com.frankgh.popularmovies.themoviedb.model.Movie;
import com.frankgh.popularmovies.util.Utility;
import com.google.gson.Gson;

import java.util.List;

import retrofit.Response;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 1/6/16.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 60 (seconds) * 24 hours * 7 days = 1 week
    public static final int SYNC_INTERVAL = 60 * 60 * 24 * 7;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 2;

    public final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        String sortingPreference = Utility.getSortingPreference(getContext());
        Response<DiscoverMovieResponse> discoverMovieResponse = null;

        try {
            discoverMovieResponse = TheMovieDbServiceFactory.getService()
                    .discoverMovies(sortingPreference).execute();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Unable to discover movies", e);
        }

        if (discoverMovieResponse != null && discoverMovieResponse.isSuccess()) {
            DiscoverMovieResponse response = discoverMovieResponse.body();
            persistMoviesToContentProvider(response.getResults());
        } else {
            Log.d(LOG_TAG, "Response from tmdb service is not valid");
        }
    }

    private void persistMoviesToContentProvider(List<Movie> movieList) {
        if (movieList == null || movieList.isEmpty()) {
            Log.d(LOG_TAG, "The movie list is empty");
            return;
        }

        Gson gson = new Gson();
        ContentValues[] movieContentValues = new ContentValues[movieList.size()];
        ContentValues[] displayedMovieContentValues = new ContentValues[movieList.size()];

        for (int i = 0; i < movieList.size(); i++) {
            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, movieList.get(i).getBackdropPath());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_ADULT, movieList.get(i).getAdult());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_GENRE_IDS, gson.toJson(movieList.get(i).getGenreIds()));
            movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movieList.get(i).getMovieId());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, movieList.get(i).getOriginalLanguage());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movieList.get(i).getOriginalTitle());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, movieList.get(i).getOverview());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, movieList.get(i).getReleaseDate());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, movieList.get(i).getPosterPath());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, movieList.get(i).getPopularity());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, movieList.get(i).getTitle());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VIDEO, movieList.get(i).getVideo());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, movieList.get(i).getVoteAverage());
            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, movieList.get(i).getVoteCount());
            movieContentValues[i] = movieValues;

            ContentValues displayedValues = new ContentValues();
            displayedValues.put(MoviesContract.DisplayedMovieEntry.COLUMN_MOVIE_KEY, movieList.get(i).getMovieId());
            displayedValues.put(MoviesContract.DisplayedMovieEntry.COLUMN_DATE, System.currentTimeMillis());
            displayedMovieContentValues[i] = displayedValues;
        }

        ContentResolver resolver = getContext().getContentResolver();

        if (resolver != null) {
            resolver.delete(MoviesContract.DisplayedMovieEntry.CONTENT_URI, null, null);
            resolver.bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, movieContentValues);
            resolver.bulkInsert(MoviesContract.DisplayedMovieEntry.CONTENT_URI, displayedMovieContentValues);
            Log.d(LOG_TAG, "Sync Complete. " + movieContentValues.length + " Movies Inserted");
        }
    }
}
