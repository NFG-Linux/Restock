package com.example.restock.pantry;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.restock.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

// PantryItemDetailsBottomSheet.java
public class PantryItemDetailsBottomSheet extends BottomSheetDialogFragment {

    PantryItem item;
    ActivityResultLauncher<Intent> editItemLauncher;

    public PantryItemDetailsBottomSheet() {}

    public PantryItemDetailsBottomSheet(PantryItem item, ActivityResultLauncher<Intent> editItemLauncher) {
        this.item = item;
        this.editItemLauncher = editItemLauncher;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog bottomSheetDialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.pantry_item_modal_details, null);

        // Bind Views
        TextView nameValue = view.findViewById(R.id.item_name_value);
        TextView quantityValue = view.findViewById(R.id.item_quantity_value);
        TextView ingredientsValue = view.findViewById(R.id.item_ingredients_value);
        TextView dateAddedValue = view.findViewById(R.id.item_date_added_value);
        TextView barcodeValue = view.findViewById(R.id.item_barcode_value);
        Button editButton = view.findViewById(R.id.edit_button);


        // Set values
        nameValue.setText(item.getProduct_name());
        quantityValue.setText(String.valueOf(item.getQuantity()));

        // set ingredients
        if (item.getIngredientsText() != null && !item.getIngredientsText().isEmpty()) {
            ingredientsValue.setText(item.getIngredientsText());
        } else {
            ingredientsValue.setText("No ingredients available");
        }

        // set timestamp
        if (item.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            dateAddedValue.setText(sdf.format(item.getTimestamp()));
        } else {
            dateAddedValue.setText("Date added not available");
        }

        // Set barcode
        if (item.getCode() != null && !item.getCode().isEmpty()) {
            barcodeValue.setText(item.getCode());
        } else {
            barcodeValue.setText("Barcode not available");
        }

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditPantryItemActivity.class);
            intent.putExtra("pantryItem", item);
            editItemLauncher.launch(intent);
            dismiss(); // Close bottom sheet
        });

        bottomSheetDialog.setContentView(view);
        return bottomSheetDialog;
    }
}
