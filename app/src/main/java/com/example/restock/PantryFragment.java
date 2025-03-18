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

//firebase imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A fragment representing a list of Items.
 */
public class PantryFragment extends Fragment {

    private FragmentPantryBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private MyItemRecyclerViewAdapter2 adapter2;
    private List<PantryItem> pantryItemList;

    public PantryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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
        pantryItemList = new ArrayList<>();
        adapter2 = new MyItemRecyclerViewAdapter2(pantryItemList);
        recyclerView.setAdapter(adapter2);

        loadUserPantryItems();

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
            v.startAnimation(bounceAnim); // bounce animation

            // Open BottomSheetDialogFragment
            SortFilterBottomSheet sortBottomSheet = new SortFilterBottomSheet();
            sortBottomSheet.show(getParentFragmentManager(), "SortFilterBottomSheet");
        });

        // FAB
        FabMenuHelper.setupFabMenu(this, binding.addButton);

    }

    private void loadUserPantryItems() {
        String userEmail = auth.getCurrentUser().getEmail();

        db.collection("pantry_items")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pantryItemList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PantryItem item = document.toObject(PantryItem.class);
                        pantryItemList.add(item);
                    }

                    if (pantryItemList.isEmpty()) {
                        Toast.makeText(getContext(), "No items in pantry yet", Toast.LENGTH_SHORT).show();
                    }

                    adapter2.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Couldnt load pantry items", Toast.LENGTH_SHORT).show();
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