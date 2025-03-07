package com.example.restock;

// PantryFragment.java
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restock.databinding.FragmentPantryBinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.PopupMenu;

import com.example.restock.placeholder.PlaceholderContent;

/**
 * A fragment representing a list of Items.
 */
public class PantryFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 3;
    private FragmentPantryBinding binding;

    public PantryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPantryBinding.inflate(inflater, container, false);

        //View view = inflater.inflate(R.layout.fragment_pantry_list, container, false);
        View view = binding.getRoot();

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyItemRecyclerViewAdapter2(PlaceholderContent.ITEMS));
        }
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Floating Action Button with Menu
        FloatingActionButton fab = binding.addButton;
        fab.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_scan_item) {
                    NavHostFragment.findNavController(PantryFragment.this)
                            .navigate(R.id.action_PantryFragment_to_BarcodeScannerFragment);
                    return true;
                } else if (item.getItemId() == R.id.menu_add_item) {
                    // Placeholder for manual item addition pop-up
                    // Show manual add modal here in the next step
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}