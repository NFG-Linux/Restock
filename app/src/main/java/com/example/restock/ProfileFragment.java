package com.example.restock;

// ProfileFragment.java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class ProfileFragment extends Fragment {

    private ImageView backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize back button
        backButton = rootView.findViewById(R.id.back_button);

        // Set click listener to navigate back
        backButton.setOnClickListener(view -> {
            Navigation.findNavController(view).navigateUp();
        });

        return rootView;
    }
}
