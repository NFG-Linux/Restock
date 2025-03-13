package com.example.restock;

// FabMenuHelper.java
import android.widget.PopupMenu;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
                    // Placeholder for manual item addition pop-up
                    // Show manual add modal here
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }
}
