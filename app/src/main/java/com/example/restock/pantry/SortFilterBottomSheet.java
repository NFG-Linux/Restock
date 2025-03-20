package com.example.restock.pantry;

// SortFilterBottomSheet.java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.restock.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortFilterBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pantry_bottom_sheet_sort_filter, container, false);
    }
}
