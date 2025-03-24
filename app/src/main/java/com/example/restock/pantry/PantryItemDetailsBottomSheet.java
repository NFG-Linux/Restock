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
        Button editButton = view.findViewById(R.id.edit_button);

        // Set values
        nameValue.setText(item.getProduct_name());
        quantityValue.setText(String.valueOf(item.getQuantity()));

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
