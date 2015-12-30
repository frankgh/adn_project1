package com.frankgh.popularmovies.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.themoviedb.api.TheMovieDbService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/28/15.
 */
public class Utility {

    public static final String SORT_PREFERENCE_KEY = "sort_by_pref";
    private static final String LOG_TAG = Utility.class.getSimpleName();
    private static final String THE_MOVIE_DB_BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";

    public static String getSortingPreference(@NonNull Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultValue = String.format("%s.%s", TheMovieDbService.SORT_BY_POPULARITY, TheMovieDbService.SORT_ORDER_DESC);
        return prefs.getString(SORT_PREFERENCE_KEY, defaultValue);
    }

    public static void updateSortingPreference(Context context, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(SORT_PREFERENCE_KEY, value); // Save new value
        edit.apply();
    }


    /**
     * Returns the Absolute Path to the poster image with a default size
     *
     * @return the absolute path
     */
    public static String getPosterAbsolutePath(String posterPath) {
        return getPosterAbsolutePath(posterPath, "w185");
    }

    /**
     * Returns the Absolute Path to the poster image. Possible sizes are:
     * w92, w154, w185, w342", "w500", "w780", or "original"
     *
     * @param imageSize one of the possible sizes: w92, w154, w185, w342, w500, w780, or original
     * @return the absolute path for the given imageSize
     */
    public static String getPosterAbsolutePath(String posterPath, String imageSize) {
        if (TextUtils.isEmpty(posterPath)) {
            return null;
        }
        return THE_MOVIE_DB_BASE_IMAGE_URL + imageSize + posterPath;
    }

    /**
     * Returns the Absolut Path the the backdrop image with a default size
     *
     * @return the absolute path
     */
    public static String getBackDropAbsolutePath(String backdropPath) {
        return getBackDropAbsolutePath(backdropPath, "w300");
    }

    /**
     * Returns the Absolute Path to the backdrop image. Possible sizes are:
     * w92, w154, w185, w342", "w500", "w780", or "original"
     *
     * @param imageSize one of the possible sizes: w92, w154, w185, w342, w500, w780, or original
     * @return the absolute path for the given imageSize
     */
    public static String getBackDropAbsolutePath(String backdropPath, String imageSize) {
        if (TextUtils.isEmpty(backdropPath)) {
            return null;
        }
        return THE_MOVIE_DB_BASE_IMAGE_URL + imageSize + backdropPath;
    }

    public static String getFormattedReleaseDate(String releaseDateString) {
        if (!TextUtils.isEmpty(releaseDateString)) {
            try {
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date releaseDate = inputDateFormat.parse(releaseDateString);

                SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
                return outputDateFormat.format(releaseDate);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Unable to parse date '" + releaseDateString + "'", e);
            }
        }
        return releaseDateString;
    }

    public static String getFormattedVoteAverage(Context context, Double voteAverage) {
        if (voteAverage != null) {
            try {
                return context.getString(R.string.format_vote_average, voteAverage);
//                NumberFormat voteAvgFormatter = new DecimalFormat("#0.0");
//                return voteAvgFormatter.format(voteAverage);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Unable to parse decimal number '" + voteAverage + "'", e);
            }
        }

        return "";
    }
}
