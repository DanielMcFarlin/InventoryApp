package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.StockContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Product Loader ID
     */
    private static final int EDITING_PRODUCT_LOADER = 0;
    // Using ButterKnife to reduce repetitious code
    @BindView(R.id.name_edit)
    EditText mNameEditText;
    @BindView(R.id.model_edit)
    EditText mModelEditText;
    @BindView(R.id.condition_spinner)
    Spinner mConditionSpinner;
    @BindView(R.id.image)
    ImageView mImage;
    @BindView(R.id.price_edit)
    EditText mPrice;
    @BindView(R.id.supplier_name_edit)
    EditText mSupplierName;
    @BindView(R.id.supplier_email_edit)
    EditText mSupplierEmail;
    @BindView(R.id.quantity_edit)
    EditText mQuantityEditText;
    @BindView(R.id.add_or_edit_image_hint)
    TextView mImageHint;
    @BindView(R.id.plusButton)
    Button mAddButton;
    @BindView(R.id.minusButton)
    Button mMinusButton;

    /**
     * BOOLEAN status for required fields,
     * (True if these fields have been populated already)
     */
    boolean hasAllRequirements = false;

    /**
     * Current quantity of product
     */
    private int mQuantity;

    /**
     * URI of product image
     */
    private Uri mImageUri;

    /**
     * Content URI for the existing product
     */
    private Uri mCurrentProductUri;

    /**
     * Condition of the product
     */
    private int mCondition = ProductEntry.CONDITION_UNKNOWN;

    /**
     * Boolean that keeps track of whether the product has been edited or not
     */
    private boolean mProductAltered = false;

    /**
     * OnTouchListener that listens for any user touches on a View.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductAltered = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Unbind of the ButterKnife views
        Unbinder unbinder = ButterKnife.bind(this);

        // Examine the intent that was used to launch this activity.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent doesn't contain a product content URI,
        // then add a new product
        if (mCurrentProductUri == null) {
            // Change title to "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));
            mImageHint.setText(getText(R.string.add_photo_hint_text));
            mSupplierName.setEnabled(true);
            mSupplierEmail.setEnabled(true);
            mQuantityEditText.setEnabled(true);
            mQuantityEditText.setVisibility(View.VISIBLE);
            mImage.setImageResource(R.drawable.ic_empty_inventory);
            mAddButton.setVisibility(View.INVISIBLE);
            mMinusButton.setVisibility(View.INVISIBLE);
            // Set "Delete" menu option to be hidden.
            invalidateOptionsMenu();

        } else {
            // Change title to "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));
            mImageHint.setText(getText(R.string.edit_photo_hint_text));
            mSupplierName.setEnabled(true);
            mSupplierEmail.setEnabled(true);
            mQuantityEditText.setEnabled(true);

            // Initialize loader.
            getLoaderManager().initLoader(EDITING_PRODUCT_LOADER, null, this);
        }

        // Setup OnTouchListeners on all the input fields,
        // So it can be determined if certain fields have been edited.
        mNameEditText.setOnTouchListener(mTouchListener);
        mModelEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mConditionSpinner.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierEmail.setOnTouchListener(mTouchListener);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryImageSelector();
                mProductAltered = true;
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOneToQuantity(v);
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minusOneFromQuantity(v);
            }
        });

        setupSpinner();
    }

    // Check Permission to read storage for images on phone
    public void tryImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openImageSelector();
    }

    // Check the SDK Version, then use the correct intent type accordingly
    private void openImageSelector() {
        Intent openImageIntent;
        if (Build.VERSION.SDK_INT < 19) {
            openImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            openImageIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            openImageIntent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        openImageIntent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(openImageIntent, getString(R.string.select_image)), 0);
    }

    // Results of asking permission to access external storage for the image.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] permissionGranted) {
        switch (requestCode) {
            case 1:
                if (permissionGranted.length > 0 && permissionGranted[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                }
        }
    }

    // Once the image is found set the image to that URI
    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == 0 && result == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mImage.setImageURI(mImageUri);
                mImage.invalidate();
            }
        }
    }

    /**
     * Setup the dropdown spinner to select condition of product.
     */
    private void setupSpinner() {
        // Create adapter for spinner
        ArrayAdapter conditionSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_condition, android.R.layout.simple_spinner_item);

        // Simple list view with 1 item/line
        conditionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mConditionSpinner.setAdapter(conditionSpinnerAdapter);

        // Set the integer to the constant values
        mConditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String conditionSelection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(conditionSelection)) {
                    if (conditionSelection.equals(getString(R.string.condition_new))) {
                        mCondition = ProductEntry.CONDITION_NEW;
                    } else if (conditionSelection.equals(getString(R.string.condition_used))) {
                        mCondition = ProductEntry.CONDITION_USED;
                    } else {
                        mCondition = ProductEntry.CONDITION_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class,
            // onNothingSelected must be defined as CONDITION_UNKNOWN
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCondition = ProductEntry.CONDITION_UNKNOWN;
            }
        });
    }

    /**
     * Helper method used to order more via email
     */
    public void orderMore() {
        Intent orderMoreIntent = new Intent(android.content.Intent.ACTION_SENDTO);
        orderMoreIntent.setType("text/plain");
        orderMoreIntent.setData(Uri.parse("mailto:" + mSupplierEmail.getText().toString().trim()));
        orderMoreIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New order:    " +
                mNameEditText.getText().toString().trim() +
                " " + mModelEditText.getText().toString().trim());
        String message = "We need a new order of: \n " +
                "Product - " + mNameEditText.getText().toString().trim() +
                "\n" +
                "Model# - " + mModelEditText.getText().toString().trim() +
                "\n" +
                "\n" +
                "Can you please send us ______ items of this product." +
                "\n" +
                "\n" +
                "Thank You!," +
                "\n" +
                "- Warehouse";
        orderMoreIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
        startActivity(orderMoreIntent);
    }


    /**
     * Get user input from editor and save product into database.
     */
    private boolean saveProduct() {
        // Quantity of products
        int quantity;

        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String modelString = mModelEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        String supplierNameString = mSupplierName.getText().toString().trim();
        String supplierEmailString = mSupplierEmail.getText().toString().trim();


        // Check if this is supposed to be a new product
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(modelString) &&
                TextUtils.isEmpty(quantityString) &&
                mCondition == ProductEntry.CONDITION_UNKNOWN &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierEmailString) &&
                mImageUri == null) {
            // If no fields were modified, return early
            hasAllRequirements = true;
            return true;
        }

        // Create a ContentValues object
        ContentValues contentValues = new ContentValues();

        // Validate All Info Here:
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.validation_product_name), Toast.LENGTH_LONG).show();
            return hasAllRequirements; //(False, return early)
        } else {
            contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.validation_product_quantity), Toast.LENGTH_LONG).show();
            return hasAllRequirements; //(False, return early)
        } else {
            quantity = Integer.parseInt(quantityString);
            contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.validation_product_price), Toast.LENGTH_LONG).show();
            return hasAllRequirements; //(False, return early)
        } else {
            contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        }

        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.validation_product_image), Toast.LENGTH_LONG).show();
            return hasAllRequirements; //(False, return early)
        } else {
            contentValues.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUri.toString());
        }

        // Values that can be null or empty with no errors thrown
        contentValues.put(ProductEntry.COLUMN_PRODUCT_MODEL, modelString);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_CONDITION, mCondition); // Not null exists here, will always be an int
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);


        // Determine if this is a new or existing product
        if (mCurrentProductUri == null) {
            // Insert new product into the provider
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, show error message
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Else, the insertion was successful, show success message
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            // Otherwise this is an existing product
            int rowsAffected = getContentResolver().update(mCurrentProductUri, contentValues, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, display error message
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, update was successful, show success message
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_LONG).show();
            }

        }

        hasAllRequirements = true;
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" item
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Clicked on a menu
        switch (item.getItemId()) {
            // Clicked on the "Save"
            case R.id.action_save:
                // Save product to database
                saveProduct();
                if (hasAllRequirements) {
                    // Exit activity
                    finish();
                }
                return true;

            // Clicked on the "Delete"
            case R.id.action_delete:
                // Confirmation dialog for deletion
                deleteConfirmationDialog();
                return true;

            // Clicked "Order More"
            case R.id.action_order_more:
                orderMore();
                return true;

            // Clicked on the "Up"
            case android.R.id.home:
                // If the product hasn't changed, continue to MainActivity
                if (!mProductAltered) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                DialogInterface.OnClickListener discardBtnClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                unsavedChangesDialog(discardBtnClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductAltered) {
            super.onBackPressed();
            return;
        }

        // If there are unsaved changes, setup a dialog to warn the user before exiting
        DialogInterface.OnClickListener discardBtnClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        unsavedChangesDialog(discardBtnClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //  Shows all product attributes in editor projection
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_MODEL,
                ProductEntry.COLUMN_PRODUCT_CONDITION,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_QUANTITY
        };

        // Executes on background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null);                      // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Move to the first row of the cursor, and read the data
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes.
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int modelColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_MODEL);
            int conditionColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CONDITION);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

            // Extract out the value from the Cursor
            String name = cursor.getString(nameColumnIndex);
            String model = cursor.getString(modelColumnIndex);
            int condition = cursor.getInt(conditionColumnIndex);
            String imageUriString = cursor.getString(imageColumnIndex);
            mImageUri = Uri.parse(imageUriString);
            String price = cursor.getString(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            mQuantity = quantity;

            // Update the views
            mNameEditText.setText(name);
            mModelEditText.setText(model);
            mImage.setImageURI(mImageUri);
            mPrice.setText(price);
            mSupplierName.setText(supplierName);
            mSupplierEmail.setText(supplierEmail);
            mQuantityEditText.setText(Integer.toString(quantity));

            // Condition dropdown spinner
            switch (condition) {
                case ProductEntry.CONDITION_NEW:
                    mConditionSpinner.setSelection(1);
                    break;
                case ProductEntry.CONDITION_USED:
                    mConditionSpinner.setSelection(2);
                    break;
                default:
                    mConditionSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear out all the data from the input fields on LoaderReset
        mNameEditText.setText("");
        mModelEditText.setText("");
        mImage.setImageResource(R.drawable.ic_empty_inventory);
        mPrice.setText("");
        mSupplierName.setText("");
        mSupplierEmail.setText("");
        mQuantityEditText.setText("");
        mConditionSpinner.setSelection(0); // Aka. Unknown Condition
    }

    // Discard button clickListener dialog set up
    private void unsavedChangesDialog(DialogInterface.OnClickListener discardClickListener) {
        // Create an AlertDialog.Builder and set the message.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog);
        builder.setPositiveButton(R.string.discard, discardClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, dismiss message
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Delete button clickListener dialog set up
    private void deleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, dismiss the message
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Helper Method for deletion of the product in the database.
     */
    private void deleteProduct() {
        // Check to see if it is an existing product first
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, show the error
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Else, the delete was successful, show the success
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_LONG).show();
            }
        }

        // Close the activity
        finish();
    }

    public void addOneToQuantity(View view) {
        mQuantity++;
        displayQuantity();
    }

    public void minusOneFromQuantity(View view) {
        if (mQuantity == 0) {
            Toast.makeText(this, "Can't decrease quantity anymore!", Toast.LENGTH_LONG).show();
        } else {
            mQuantity--;
            displayQuantity();
        }
    }

    public void displayQuantity() {
        mQuantityEditText.setText(String.valueOf(mQuantity));
    }
}