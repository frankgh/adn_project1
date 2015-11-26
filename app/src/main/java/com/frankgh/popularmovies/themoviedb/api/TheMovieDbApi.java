package com.frankgh.popularmovies.themoviedb.api;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.frankgh.popularmovies.R;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResponse;
import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by francisco on 11/24/15.
 */
public class TheMovieDbApi {

    public final static String SORT_BY_POPULARITY = "popularity";
    public final static String SORT_BY_RANKING = "ranking";
    public final static String SORT_ORDER_ASC = "asc";
    public final static String SORT_ORDER_DESC = "desc";
    private final String LOG_TAG = TheMovieDbApi.class.getSimpleName();
    private Context mContext;

    public TheMovieDbApi(Context context) {
        mContext = context;
    }

    public List<DiscoverMovieResult> discoverMovies(String orderBy, String sortOrder) {
        String discoverMoviesUriString = mContext.getString(R.string.tmdb_discover_api_url);

        Uri discoverMoviesUri = buildUri(discoverMoviesUriString, orderBy, sortOrder);
        String responseText = getResponseTextFromUri(discoverMoviesUri);
        DiscoverMovieResponse response = null;

        try {
            response = new Gson().fromJson(responseText, DiscoverMovieResponse.class);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        if (response != null) {
            return response.getResults();
        }

        return null;
    }

    private String getResponseTextFromUri(Uri builtUri) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String responseText = null;

        try {
            URL url = new URL(builtUri.toString());

            // Create the request to TheMovieDb Api, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null; // Nothing to do.
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                return null; // Stream was empty.  No point in parsing.
            }
            return buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private Uri buildUri(String uriString, String sortBy, String sortOrder) {
        final String SORT_BY_PARAM = "sort_by";
        final String API_KEY_PARAM = "api_key";

        Uri.Builder builder = Uri.parse(uriString).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, mContext.getString(R.string.the_movie_db_api_key));

        if (!TextUtils.isEmpty(sortBy)) {
            if (TextUtils.isEmpty(sortOrder)) {
                sortOrder = SORT_ORDER_DESC; // Set the default sort order
            }

            builder = builder.appendQueryParameter(SORT_BY_PARAM, String.format("%s.%s", sortBy, sortOrder));
        }

        return builder.build();
    }
}
