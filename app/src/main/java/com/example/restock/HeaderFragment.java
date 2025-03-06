package com.example.restock;

// HeaderFragment.java
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class HeaderFragment extends Fragment {

    public HeaderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_header, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        ImageView profileIcon = view.findViewById(R.id.profile_icon);
        ImageView notificationIcon = view.findViewById(R.id.notification_icon);

        // Load bounce animation
        final Animation bounceAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

        // Set click listeners
        profileIcon.setOnClickListener(v -> {
            profileIcon.startAnimation(bounceAnim); // Start the bounce animation
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                    .navigate(R.id.action_global_profileFragment);
        });

        notificationIcon.setOnClickListener(v -> {
            notificationIcon.startAnimation(bounceAnim); // Start the bounce animation
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                    .navigate(R.id.action_global_notificationFragment);
        });
    }
}
