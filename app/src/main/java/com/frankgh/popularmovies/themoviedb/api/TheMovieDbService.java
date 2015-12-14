package com.frankgh.popularmovies.themoviedb.api;

import com.frankgh.popularmovies.themoviedb.model.DiscoverMovieResult;

import java.util.List;

import retrofit.Call;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by francisco on 11/24/15.
 */
public interface TheMovieDbService {

    @POST("/discover/movie")
    Call<List<DiscoverMovieResult>> login(@Query("api_key") String apiKey, @Query("sort_by") String sortBy);

}
//public class TheMovieDbService {
//
//    public final static String SORT_BY_POPULARITY = "popularity";
//    public final static String SORT_BY_VOTE_AVERAGE = "vote_average";
//    public final static String SORT_BY_RELEASE_DATE = "release_date";
//    public final static String SORT_BY_REVENUE = "revenue";
//    public final static String SORT_BY_PRIMARY_RELEASE_DATE = "primary_release_date";
//    public final static String SORT_BY_ORIGINAL_TITLE = "original_title";
//    public final static String SORT_BY_VOTE_COUNT = "vote_count";
//
//    public final static String SORT_ORDER_ASC = "asc";
//    public final static String SORT_ORDER_DESC = "desc";
//    private final String LOG_TAG = TheMovieDbService.class.getSimpleName();
//    private Context mContext;
//
//    public TheMovieDbService(Context context) {
//        mContext = context;
//    }
//
//    public List<DiscoverMovieResult> discoverMovies(String sortBy) {
//        String discoverMoviesUriString = mContext.getString(R.string.tmdb_discover_api_url);
//
//        Uri discoverMoviesUri = buildUri(discoverMoviesUriString, sortBy);
//        String responseText = getResponseTextFromUri(discoverMoviesUri);
//        DiscoverMovieResponse response = null;
//
//        try {
//            response = new Gson().fromJson(responseText, DiscoverMovieResponse.class);
//        } catch (Exception e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//        }
//
//        if (response != null) {
//            return response.getResults();
//        }
//
//        return null;
//    }
//
//    private String getResponseTextFromUri(Uri builtUri) {
//        // These two need to be declared outside the try/catch
//        // so that they can be closed in the finally block.
//        HttpURLConnection urlConnection = null;
//        BufferedReader reader = null;
//
//        // Will contain the raw JSON response as a string.
//        String responseText = null;
//
//        try {
//            URL url = new URL(builtUri.toString());
//
//            // Create the request to TheMovieDb Api, and open the connection
//            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.connect();
//
//            // Read the input stream into a String
//            InputStream inputStream = urlConnection.getInputStream();
//            StringBuffer buffer = new StringBuffer();
//            if (inputStream == null) {
//                return null; // Nothing to do.
//            }
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                buffer.append(line);
//            }
//
//            if (buffer.length() == 0) {
//                return null; // Stream was empty.  No point in parsing.
//            }
//            return buffer.toString();
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error ", e);
//            return null;
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (final IOException e) {
//                    Log.e(LOG_TAG, "Error closing stream", e);
//                }
//            }
//        }
//    }
//
//    private Uri buildUri(String uriString, String sortBy) {
//        final String SORT_BY_PARAM = "sort_by";
//        final String API_KEY_PARAM = "api_key";
//        final String RELEASE_DATE_LTE_PARAM = "release_date.lte";
//
//        Uri.Builder builder = Uri.parse(uriString).buildUpon()
//                .appendQueryParameter(API_KEY_PARAM, mContext.getString(R.string.the_movie_db_api_key));
//
//        if (!TextUtils.isEmpty(sortBy)) {
//            builder = builder.appendQueryParameter(SORT_BY_PARAM, sortBy);
//
//            if (sortBy.indexOf(SORT_BY_RELEASE_DATE) == 0) {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
//                builder = builder.appendQueryParameter(RELEASE_DATE_LTE_PARAM, sdf.format(new Date()));
//            }
//        }
//
//        return builder.build();
//    }
//}
