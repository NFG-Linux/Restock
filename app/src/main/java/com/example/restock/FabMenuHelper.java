package com.example.restock;

// FabMenuHelper.java
import android.widget.PopupMenu;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

//needed for Manual Add to work
import android.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class FabMenuHelper {

    public static void setupFabMenu(Fragment fragment, FloatingActionButton fab) {
        fab.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(fragment.requireContext(), v);
            popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_scan_item) {
                    NavHostFragment.findNavController(fragment)
                            .navigate(R.id.BarcodeScannerFragment);
                    return true;
                } else if (id == R.id.menu_add_item) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext());
                    builder.setTitle("Manually Add Item");

                    final EditText barcodeInput = new EditText(fragment.requireContext());
                    barcodeInput.setHint("Barcode");
                    barcodeInput.setInputType(InputType.TYPE_CLASS_TEXT);

                    LinearLayout layout = new LinearLayout(fragment.requireContext());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.addView(barcodeInput);
                    builder.setView(layout);

                    builder.setPositiveButton("Next", (dialog, which) -> {
                        String barcode = barcodeInput.getText().toString().trim();
                        if (barcode.isEmpty()) {
                            Toast.makeText(fragment.getContext(), "A barcode is required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ManualAddDialogHelper.handleBarcode(barcode, fragment);
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    builder.show();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }
}
