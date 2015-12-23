package com.frankgh.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.frankgh.popularmovies.util.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/21/15.
 */
public class TestUtilities extends AndroidTestCase {

    public static ContentValues createMovieValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, "/c2Ax8Rox5g6CneChwy1gmu4UbSb.jpg");
        testValues.put(MoviesContract.MovieEntry.COLUMN_ADULT, 0);
        testValues.put(MoviesContract.MovieEntry.COLUMN_GENRE_IDS, "[28,12,878,14]");
        testValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, 140607);
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

        return testValues;
    }

    public static ContentValues createMovieExtraValues(long movieRowId) {

        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY, movieRowId);
        testValues.put(MoviesContract.MovieExtraEntry.COLUMN_EXTRA_NAME, MoviesContract.MovieExtraEntry.NAME_VIDEOS);
        testValues.put(MoviesContract.MovieExtraEntry.COLUMN_EXTRA_VALUE,
                "{\"id\":140607,\"results\":[{\"id\":\"55e5901992514137a00020ff\",\"iso_639_1\":\"en\",\"key\":\"OMOVFvcNfvE\",\"name\":\"Teaser\",\"site\":\"YouTube\",\"size\":720,\"type\":\"Trailer\"},{\"id\":\"5478b88b925141231c00249a\",\"iso_639_1\":\"en\",\"key\":\"erLk59H86ww\",\"name\":\"Official Teaser\",\"site\":\"YouTube\",\"size\":1080,\"type\":\"Teaser\"},{\"id\":\"553070d1c3a3680a940006ba\",\"iso_639_1\":\"en\",\"key\":\"ngElkyQ6Rhs\",\"name\":\"Official Teaser #2\",\"site\":\"YouTube\",\"size\":1080,\"type\":\"Teaser\"},{\"id\":\"5625a9a7c3a3680e0e0150c2\",\"iso_639_1\":\"en\",\"key\":\"sGbxmsDFVnE\",\"name\":\"Official Trailer #1\",\"site\":\"YouTube\",\"size\":1080,\"type\":\"Trailer\"},{\"id\":\"565f51c69251416f0b0043ba\",\"iso_639_1\":\"en\",\"key\":\"z6rNlK_Rjnc\",\"name\":\"Star Wars :The Force Awakens - ABC TGIT TV Spot Trailer #3 HD\",\"site\":\"YouTube\",\"size\":720,\"type\":\"Trailer\"}]}");
        testValues.put(MoviesContract.MovieExtraEntry.COLUMN_ADDED_DATE, "Dec 21, 2015 2:37:03 PM");
        return testValues;
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + valueCursor.getString(idx) +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    public static long insertTestMovieValues(Context mContext) {
        // insert our test records into the database
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createMovieValues();

        long movieRowId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Movie Values", movieRowId != -1);

        return movieRowId;
    }

    public static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

    /*
        The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }
}
