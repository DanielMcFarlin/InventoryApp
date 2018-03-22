package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


public class StockProvider extends ContentProvider {

    /**
     * Log Tag for troubleshooting
     */
    public static final String LOG_TAG = StockProvider.class.getSimpleName();

    /**
     * URI matcher code for a single product in the stock table
     */
    private static final int PRODUCT_ID = 1;

    /**
     * URI matcher code for the content URI for the stock table products
     */
    private static final int PRODUCTS = 2;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Add Uri to sUriMatcher object
    static {
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_PRODUCT, PRODUCTS);
        sUriMatcher.addURI(StockContract.CONTENT_AUTHORITY, StockContract.PATH_PRODUCT + "/#", PRODUCT_ID);
    }

    /**
     * Database helper object
     */
    private StockDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        mDbHelper = new StockDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI
     */
    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase readDatabase = mDbHelper.getReadableDatabase();

        //Initialize cursor to hold the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the full table of products in the stock table
                cursor = readDatabase.query(StockContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case PRODUCT_ID:
                // For grabbing a single product from the stock table specifically
                selection = StockContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Perform a query on the stock table with specific ID for product
                cursor = readDatabase.query(StockContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor changing
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertNewProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values
     */
    private Uri insertNewProduct(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(StockContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that the condition is valid
        Integer condition = values.getAsInteger(StockContract.ProductEntry.COLUMN_PRODUCT_CONDITION);
        if (condition == null || !StockContract.ProductEntry.isValidCondition(condition)) {
            throw new IllegalArgumentException("Product requires valid condition");
        }

        // If the quantity is provided, check that it's greater than 0
        Integer quantity = values.getAsInteger(StockContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }

        // Get "writeable" database
        SQLiteDatabase writeDatabase = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = writeDatabase.insert(StockContract.ProductEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed.
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID appended
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Update selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updatedProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = StockContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatedProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    /**
     * Update Method for products in the stock database with the given content values.
     * Return the number of rows that were successfully updated.
     */
    private int updatedProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // check that the name value is not null.
        if (values.containsKey(StockContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(StockContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // check that the condition value is valid.
        if (values.containsKey(StockContract.ProductEntry.COLUMN_PRODUCT_CONDITION)) {
            Integer condition = values.getAsInteger(StockContract.ProductEntry.COLUMN_PRODUCT_CONDITION);
            if (condition == null || !StockContract.ProductEntry.isValidCondition(condition)) {
                throw new IllegalArgumentException("Product requires valid condition");
            }
        }

        // check that the quantity value is valid.
        if (values.containsKey(StockContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(StockContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        // If there are no values to update,
        // then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get writable database to update the data
        SQLiteDatabase writeDatabase = mDbHelper.getWritableDatabase();

        // Perform the update on the database
        int rowsUpdated = writeDatabase.update(StockContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, notify all the listeners
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable writeDatabase
        SQLiteDatabase writeDatabase = mDbHelper.getWritableDatabase();

        // Track number of rows deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = writeDatabase.delete(StockContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = StockContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = writeDatabase.delete(StockContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, notify all listeners
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return StockContract.ProductEntry.LIST_TYPE;
            case PRODUCT_ID:
                return StockContract.ProductEntry.ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
