package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.StockContract;
import com.example.android.inventoryapp.data.StockContract.ProductEntry;

import static android.content.ContentValues.TAG;

public class StockCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link StockCursorAdapter}.
     *
     * @param context The context
     * @param cursor  The cursor from which to get the data.
     */
    public StockCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data to the given list item layout.
     *
     * @param view    Existing view
     * @param context app context
     * @param cursor  The cursor from which to get the data.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        //Find all the views in put into ViewHolder
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.nameTextView = view.findViewById(R.id.product_name);
        viewHolder.modelTextView = view.findViewById(R.id.product_model);
        viewHolder.priceTextView = view.findViewById(R.id.product_price);
        viewHolder.quantityTextView = view.findViewById(R.id.product_quantity);
        viewHolder.imageView = view.findViewById(R.id.product_image);
        viewHolder.buyImageBtn = view.findViewById(R.id.buy_button);
        view.setTag(viewHolder);
        ViewHolder holder = (ViewHolder) view.getTag();

        // Find the columns of product attributes
        final int productIdColumnIndex = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int modelColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_MODEL);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int photoColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        String productModel = cursor.getString(modelColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final int quantityProduct = cursor.getInt(quantityColumnIndex);
        String imageUriString = cursor.getString(photoColumnIndex);
        Uri productImageUri = Uri.parse(imageUriString);

        // If model is empty string or null, then hide it
        if (TextUtils.isEmpty(productModel)) {
            holder.modelTextView.setVisibility(View.GONE);
        }

        // Update the TextViews with the attributes for the current product
        holder.nameTextView.setText(productName);
        holder.modelTextView.setText(productModel);
        holder.priceTextView.setText(productPrice);
        holder.quantityTextView.setText(String.valueOf(quantityProduct));
        holder.imageView.setImageURI(productImageUri);

        // Set up button listener for when the user clicks to buy the item
        holder.buyImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productIdColumnIndex);
                boughtProductQuantity(context, productUri, quantityProduct);
            }
        });
    }

    /**
     * This method reduced product stock by 1 on each click
     */
    private void boughtProductQuantity(Context context, Uri productUri, int currentQuantityInStock) {
        // Subtract 1 from current value if quantity of product
        int newQuantityValue;
        if (currentQuantityInStock >= 1) newQuantityValue = currentQuantityInStock - 1;
        else newQuantityValue = 0;

        if (currentQuantityInStock == 0) {
            Toast.makeText(context.getApplicationContext(), R.string.toast_out_of_stock, Toast.LENGTH_LONG).show();
        }

        // Update table by using new value of quantity
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantityValue);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);
        if (numRowsUpdated > 0) {
            // Show error message with info about pass update.
            Log.i(TAG, context.getString(R.string.buy_msg_confirm));
        } else {
            Toast.makeText(context.getApplicationContext(), R.string.no_product_in_stock, Toast.LENGTH_LONG).show();
            // Show error message in Logs with info about fail update.
            Log.e(TAG, context.getString(R.string.error_msg_stock_update));
        }
    }

    //Create ViewHolder to refer to in binding view
    static class ViewHolder {
        TextView nameTextView;
        TextView modelTextView;
        TextView priceTextView;
        TextView quantityTextView;
        ImageView imageView;
        ImageButton buyImageBtn;
    }
}
