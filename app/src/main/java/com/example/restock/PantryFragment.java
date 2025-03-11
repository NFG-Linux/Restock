package com.example.restock;

// PantryFragment.java
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restock.databinding.FragmentPantryBinding;

import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.PopupMenu;

import com.example.restock.placeholder.PlaceholderContent;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A fragment representing a list of Items.
 */
public class PantryFragment extends Fragment {

    private FragmentPantryBinding binding;

    public PantryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPantryBinding.inflate(inflater, container, false);

        //View view = inflater.inflate(R.layout.fragment_pantry_list, container, false);
        View view = binding.getRoot();

        // Set the adapter
        RecyclerView recyclerView = binding.pantryRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new MyItemRecyclerViewAdapter2(PlaceholderContent.ITEMS));

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load bounce animation
        final Animation bounceAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);

        // Mic Icon Click
        ImageView micIcon = binding.searchBar.findViewById(R.id.mic_icon);
        micIcon.setOnClickListener(v -> {
            micIcon.startAnimation(bounceAnim);

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your search");

            try {
                speechRecognitionLauncher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Speech Recognition Not Supported", Toast.LENGTH_SHORT).show();
            }
        });

        // Sort Filter Icon Click
        ImageView sortIcon = binding.searchBar.findViewById(R.id.sort_icon);
        sortIcon.setOnClickListener(v -> {
            v.startAnimation(bounceAnim); // Apply bounce animation

            // Open BottomSheetDialogFragment (create this next)
            SortFilterBottomSheet sortBottomSheet = new SortFilterBottomSheet();
            sortBottomSheet.show(getParentFragmentManager(), "SortFilterBottomSheet");
        });

        // Floating Action Button with Menu
        FloatingActionButton fab = binding.addButton;
        fab.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_scan_item) {
                    NavHostFragment.findNavController(PantryFragment.this)
                            .navigate(R.id.action_PantryFragment_to_BarcodeScannerFragment);
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

    private final ActivityResultLauncher<Intent> speechRecognitionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> speechResult = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (speechResult != null && !speechResult.isEmpty()) {
                        binding.searchInput.setText(speechResult.get(0));
                    }
                }
            });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}