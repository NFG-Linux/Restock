package com.example.restock;

// FooterFragment.java
import android.os.Bundle;
import android.view.LayoutInflater;
// import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class FooterFragment extends Fragment {

    public FooterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_footer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        ImageView pantryButton = view.findViewById(R.id.footer_pantry);
        ImageView listButton = view.findViewById(R.id.footer_list);
        ImageView storeButton = view.findViewById(R.id.footer_store);

        // Load bounce animation
        final Animation bounceAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

        // Set click listeners
        pantryButton.setOnClickListener(v -> {
            pantryButton.startAnimation(bounceAnim); // Start the bounce animation
            pantryButton.setSelected(true); // Set selected state to true
            listButton.setSelected(false); // Set other buttons to unselected
            storeButton.setSelected(false);
            navigateTo(R.id.action_global_pantryFragment);
        });

        listButton.setOnClickListener(v -> {
            listButton.startAnimation(bounceAnim); // Start the bounce animation
            listButton.setSelected(true); // Set selected state to true
            pantryButton.setSelected(false); // Set other buttons to unselected
            storeButton.setSelected(false);
            navigateTo(R.id.action_global_listFragment);
        });

        storeButton.setOnClickListener(v -> {
            storeButton.startAnimation(bounceAnim); // Start the bounce animation
            storeButton.setSelected(true); // Set selected state to true
            pantryButton.setSelected(false); // Set other buttons to unselected
            listButton.setSelected(false);
            navigateTo(R.id.action_global_storeFragment);
        });
    }

    private void navigateTo(int actionId) {
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main)
                .navigate(actionId);
    }
}
