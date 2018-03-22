package com.example.android.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.StockContract.ProductEntry;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Log Tag for troubleshooting
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int PRODUCT_LOADER = 0;
    StockCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list_view);

        // Find and set empty view on the ListView
        // (only seen when ListView is empty)
        View emptyList = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyList);

        // Setup an Adapter to create a list item for each row.
        mCursorAdapter = new StockCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        // Setup item click listener for the list of products
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to EditorActivity
                Intent editIntent = new Intent(MainActivity.this, EditorActivity.class);

                // Form the content URI that represents the
                // specific product that was clicked on
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                Log.i(LOG_TAG, "Current URI: " + currentProductUri);

                // Set the URI on the data field of the intent
                editIntent.setData(currentProductUri);

                // Launch the EditorActivity to display the data for the current product
                startActivity(editIntent);
            }
        });

        // Start the loader
        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


    private void insertDummyData() {
        // Get Uri for example photo from drawable resource
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.dummy_pic)
                + '/' + getResources().getResourceTypeName(R.drawable.dummy_pic) + '/'
                + getResources().getResourceEntryName(R.drawable.dummy_pic));
        Log.i(LOG_TAG, "Dummy data photo uri: " + String.valueOf(imageUri));

        // Create a ContentValues object
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, getString(R.string.dummy_data_name));
        values.put(ProductEntry.COLUMN_PRODUCT_MODEL, getString(R.string.dummy_data_model));
        values.put(ProductEntry.COLUMN_PRODUCT_CONDITION, ProductEntry.CONDITION_NEW);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 100);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, String.valueOf(imageUri));
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, getString(R.string.dummy_data_supplier_email));
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, getString(R.string.dummy_data_supplier_name));
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 250.00);

        // Insert a new row for PlayStation 4 into the provider using the ContentResolver
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Clicked on a menu option
        switch (item.getItemId()) {
            // Clicked on the "Insert dummy data"
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            // Clicked on the "Delete all"
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection with specified columns
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_MODEL,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };
        // On background thread.
        return new CursorLoader(this,       // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null                        // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update StockCursorAdapter with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This deletes data in the CursorAdapter
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all products in the database
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from the database");
    }
}
