package com.frankgh.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * @author Francisco Guerrero <email>me@frankgh.com</email> on 12/21/15.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MoviesDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(MoviesContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MoviesContract.MovieExtraEntry.TABLE_NAME);

        deleteTheDatabase();
        SQLiteDatabase db = new MoviesDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without the billing address entry, " +
                        "the account entry, and the session entry tables",
                tableNameHashSet.isEmpty());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<>();
        movieColumnHashSet.add(MoviesContract.MovieEntry._ID);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_ADULT);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_GENRE_IDS);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_OVERVIEW);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_POSTER_PATH);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_POPULARITY);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_TITLE);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VIDEO);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        movieColumnHashSet.add(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT);

        validateColumnsInTable(db, MoviesContract.MovieEntry.TABLE_NAME, movieColumnHashSet);

        final HashSet<String> accountColumnHashSet = new HashSet<>();
        accountColumnHashSet.add(MoviesContract.MovieExtraEntry._ID);
        accountColumnHashSet.add(MoviesContract.MovieExtraEntry.COLUMN_MOVIE_KEY);
        accountColumnHashSet.add(MoviesContract.MovieExtraEntry.COLUMN_EXTRA_NAME);
        accountColumnHashSet.add(MoviesContract.MovieExtraEntry.COLUMN_EXTRA_VALUE);
        accountColumnHashSet.add(MoviesContract.MovieExtraEntry.COLUMN_ADDED_DATE);

        validateColumnsInTable(db, MoviesContract.MovieExtraEntry.TABLE_NAME, accountColumnHashSet);

        db.close();
    }

    public void testMovieTable() {
        insertMovie();
    }

    public void testMovieExtraTable() {

        long movieRowId = insertMovie();

        assertFalse("Error: Movie Not Inserted Correctly", movieRowId == -1L);

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (session): Create movie extra values
        ContentValues movieExtraValues = TestUtilities.createMovieExtraValues(movieRowId);

        // Third Step (session): Insert ContentValues into database and get a row ID back
        long movieExtraRowId = db.insert(MoviesContract.MovieExtraEntry.TABLE_NAME, null, movieExtraValues);
        assertTrue(movieExtraRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor movieExtraCursor = db.query(
                MoviesContract.MovieExtraEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from Movie Extra query", movieExtraCursor.moveToFirst());

        // Fifth Step: Validate the Movie Extra Query
        TestUtilities.validateCurrentRecord("testInsertReadDb MovieExtraEntry failed to validate",
                movieExtraCursor, movieExtraValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from session query",
                movieExtraCursor.moveToNext());

        // Sixth Step: Close cursor and database
        movieExtraCursor.close();
        dbHelper.close();
    }

    private long insertMovie() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MoviesDbHelper dbHelper = new MoviesDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createMovieValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long movieRowId;
        movieRowId = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                MoviesContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue("Error: No Records returned from account query", cursor.moveToFirst());

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Movie Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from account query",
                cursor.moveToNext());

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return movieRowId;
    }

    private void validateColumnsInTable(SQLiteDatabase db, String tableName, HashSet<String> columnHashSet) {

        // now, do our tables contain the correct columns?
        Cursor c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required columns
        assertTrue("Error: The database doesn't contain all of the required columns for table " + tableName,
                columnHashSet.isEmpty());
    }
}
