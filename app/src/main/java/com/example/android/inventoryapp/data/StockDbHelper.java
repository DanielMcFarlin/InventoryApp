package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public final class StockDbHelper extends SQLiteOpenHelper {

    // Log tag used for troubleshooting
    private static final String LOG_TAG = StockDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link StockDbHelper}
     *
     * @param context of the app
     */
    public StockDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " +
                StockContract.ProductEntry.TABLE_NAME + "(" +
                StockContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StockContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                StockContract.ProductEntry.COLUMN_PRODUCT_MODEL + " TEXT, " +
                StockContract.ProductEntry.COLUMN_PRODUCT_CONDITION + " INTEGER NOT NULL, " +
                StockContract.ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, " +
                StockContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER DEFAULT 0, " +
                StockContract.ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, " +
                StockContract.ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL, " +
                StockContract.ProductEntry.COLUMN_PRODUCT_IMAGE + " TEXT NOT NULL); ";
        Log.v(LOG_TAG, SQL_CREATE_STOCK_TABLE);
        //Create table
        db.execSQL(SQL_CREATE_STOCK_TABLE);
    }

    // This is called when the database needs to be updated with new info.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.delete("stock", null, null);
        db.execSQL("DELETE  FROM " + "stock");
        db.close();
        onCreate(db);
    }
}