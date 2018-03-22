package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class StockContract {

    // Log tag used for troubleshooting
    public static final String LOG_TAG = StockContract.class.getSimpleName();
    /**
     * Build The URI
     */
    // authority part of Uri
    public static final String CONTENT_AUTHORITY = "com.example.android.products";
    // base content of URI
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // path to table name
    public static final String PATH_PRODUCT = "products";
    // Give it an empty constructor.
    private StockContract() {
    }

    /**
     * Inner class where each entry in the table represents a single product in the table.
     */
    public static abstract class ProductEntry implements BaseColumns {
        /**
         * The MIME type for a list of products.
         */
        public static final String LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * The MIME type for a single product.
         */
        public static final String ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * The content URI to access the stock data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);

        /**
         * Name of database table for stock products
         */
        public final static String TABLE_NAME = "stock";

        /**
         * Unique ID number for the product.
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME = "name";

        /**
         * Model of the product.
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_MODEL = "model";

        /**
         * Condition of the product.
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_CONDITION = "condition";

        /**
         * Quantity of the product.
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Image of the product.
         * Type: TEXT (URI for image)
         */
        public static final String COLUMN_PRODUCT_IMAGE = "image";

        /**
         * Price of the product.
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Name of the supplier.
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_NAME = "supplierName";

        /**
         * Email of the supplier.
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_EMAIL = "supplierEmail";

        /**
         * Only possible values for the condition of the product.
         */
        public static final int CONDITION_UNKNOWN = 0;
        public static final int CONDITION_NEW = 1;
        public static final int CONDITION_USED = 2;

        /**
         * Validates and returns the condition of the product
         */
        public static boolean isValidCondition(int condition) {
            if (condition == CONDITION_UNKNOWN || condition == CONDITION_NEW || condition == CONDITION_USED) {
                return true;
            }
            return false;
        }
    }

}

