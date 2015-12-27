package com.frankgh.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/23/15.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    private void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    private void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                MoviesContract.MovieExtraEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movies table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                MoviesContract.MovieExtraEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movies Extra table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // PciProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MoviesProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MoviesProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MoviesContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MoviesContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MoviesProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
         */
    public void testGetType() {
        // content://com.frankgh.popularmovies.app/movie/
        String type = mContext.getContentResolver().getType(MoviesContract.MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.frankgh.popularmovies.app/movies
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MoviesContract.MovieEntry.CONTENT_TYPE, type);

        long movieId = 94074;
        // content://com.frankgh.popularmovies.app/movie_extra/94074
        type = mContext.getContentResolver().getType(
                MoviesContract.MovieExtraEntry.buildMovieExtraUri(movieId));
        // vnd.android.cursor.dir/com.frankgh.popularmovies.app/movie_extra
        assertEquals("Error: the MovieExtraEntry CONTENT_URI with movieId should return MovieExtraEntry.CONTENT_TYPE",
                MoviesContract.MovieExtraEntry.CONTENT_TYPE, type);
    }

    /*
       This test uses the database directly to insert and then uses the ContentProvider to
       read out the data.
    */
    public void testBasicMovieExtraQuery() {
        // insert our test records into the database
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieTestValues = TestUtilities.createMovieValues();
        long movieRowId = TestUtilities.insertTestMovieValues(mContext);

        // Fantastic.  Now that we have a movie, add extra movie data!
        ContentValues movieExtraValues = TestUtilities.createMovieExtraValues(movieRowId);

        long movieExtraRowId = db.insert(MoviesContract.MovieExtraEntry.TABLE_NAME, null, movieExtraValues);
        assertTrue("Unable to Insert Movie Extra into the Database", movieExtraRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor movieExtraCursor = mContext.getContentResolver().query(
                MoviesContract.MovieExtraEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieQuery", movieExtraCursor, movieExtraValues);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if your location queries are
        performing correctly.
     */
    public void testBasicMovieQueries() {
        // insert our test records into the database
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createMovieValues();
        long movieRowId = TestUtilities.insertTestMovieValues(mContext);

        // Test the basic content provider query
        Cursor moviesCursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieQueries, movie query", moviesCursor, testValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Movie Query did not properly set NotificationUri",
                    moviesCursor.getNotificationUri(), MoviesContract.MovieEntry.CONTENT_URI);
        }
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createMovieValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.MovieEntry.CONTENT_URI, true, tco);
        Uri accountUri = mContext.getContentResolver().insert(MoviesContract.MovieEntry.CONTENT_URI, testValues);

        // Did our content observer get called?
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(accountUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating Movie.",
                cursor, testValues);

        // Fantastic.  Now that we have an movie, add extra movie data!
        ContentValues movieExtraValues = TestUtilities.createMovieExtraValues(movieRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(MoviesContract.MovieExtraEntry.CONTENT_URI, true, tco);

        Uri movieExtraInsertUri = mContext.getContentResolver()
                .insert(MoviesContract.MovieExtraEntry.CONTENT_URI, movieExtraValues);
        assertTrue(movieExtraInsertUri != null);

        // Did our content observer get called?
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor sessionCursor = mContext.getContentResolver().query(
                MoviesContract.MovieExtraEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieExtra insert.",
                sessionCursor, movieExtraValues);
    }

    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver moviesObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.MovieEntry.CONTENT_URI, true, moviesObserver);

        // Register a content observer for our weather delete.
        TestUtilities.TestContentObserver movieExtrasObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.MovieExtraEntry.CONTENT_URI, true, movieExtrasObserver);

        deleteAllRecordsFromProvider();

        // If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        moviesObserver.waitForNotificationOrFail();
        movieExtrasObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(moviesObserver);
        mContext.getContentResolver().unregisterContentObserver(movieExtrasObserver);
    }

    /*
       This test uses the provider to insert and then update the data.
    */
    public void testUpdateMovie() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createMovieValues();

        Uri accountUri = mContext.getContentResolver().
                insert(MoviesContract.MovieEntry.CONTENT_URI, values);
        long movieRowId = ContentUris.parseId(accountUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);
        Log.d(LOG_TAG, "New row id: " + movieRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Santa's Village");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor movieCursor = mContext.getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        movieCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                MoviesContract.MovieEntry.CONTENT_URI, updatedValues, MoviesContract.MovieEntry._ID + "= ?",
                new String[]{Long.toString(movieRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        tco.waitForNotificationOrFail();

        movieCursor.unregisterContentObserver(tco);
        movieCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null,   // projection
                MoviesContract.MovieEntry._ID + " = " + movieRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateMovie.  Error validating movie entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 20;

    static ContentValues[] createBulkInsertMovieValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues testValues = new ContentValues();
            testValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, "/c2Ax8Rox5g6CneChwy1gmu4UbSb.jpg");
            testValues.put(MoviesContract.MovieEntry.COLUMN_ADULT, 0);
            testValues.put(MoviesContract.MovieEntry.COLUMN_GENRE_IDS, "[28,12,878,14]");
            testValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, (140607 + i));
            testValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, "en");
            testValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, "Star Wars: The Force Awakens");
            testValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, "Thirty years after defeating the Galactic Empire, Han Solo and his allies face a new threat from the evil Kylo Ren and his army of Stormtroopers.");
            testValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-12-18");
            testValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, "/weUSwMdQIa3NaXVzwUoIIcAi85d.jpg");
            testValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, 86.6028);
            testValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Star Wars: The Force Awakens");
            testValues.put(MoviesContract.MovieEntry.COLUMN_VIDEO, 0);
            testValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, 8.06);
            testValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, 710);
            returnContentValues[i] = testValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        // Now we can bulkInsert some movies.  In fact, we only implement BulkInsert for movie
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MoviesContract.MovieEntry.CONTENT_URI, true, movieObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, bulkInsertContentValues);

        // If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MoviesContract.MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " ASC"  // sort order == by movie id ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
