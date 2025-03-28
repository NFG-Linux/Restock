package com.example.restock.pantry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restock.FabMenuHelper;
import com.example.restock.R;

import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

//firebase imports
import com.example.restock.databinding.PantryFragmentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// PantryFragment.java
// Fragment class that for displays and manages the user's pantry list
public class PantryFragment extends Fragment {

    private PantryFragmentBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    PantryAdapter adapter;
    List<PantryItem> pantryItemList;

    public PantryFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = PantryFragmentBinding.inflate(inflater, container, false);

        //View view = inflater.inflate(R.layout.fragment_pantry_list, container, false);
        View view = binding.getRoot();

        // Set the adapter
        RecyclerView recyclerView = binding.pantryRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        pantryItemList = new ArrayList<>();
        adapter = new PantryAdapter(pantryItemList, getParentFragmentManager(), editItemLauncher);

        recyclerView.setAdapter(adapter);

        loadUserPantryItems();

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPantryItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

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

                        // expiration_date is retrieved
                        if (document.contains("expiration_date")) {
                            item.setExpiration_date(document.getString("expiration_date"));
                        }

                        // ingredients_text is retrieved
                        if (document.contains("ingredients_text")) {
                            item.setIngredientsText(document.getString("ingredients_text"));
                        }


                        // Retrieve timestamp field
                        if (document.contains("timestamp")) {
                            item.setTimestamp(document.getDate("timestamp")); // Store as Date object
                        }

                        pantryItemList.add(item);
                    }

                    if (pantryItemList.isEmpty()) {
                        Toast.makeText(getContext(), "No items in pantry yet", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Couldn't load pantry items", Toast.LENGTH_SHORT).show()
                );
    }


    private void searchPantryItems(String searchText) {
        String userEmail = auth.getCurrentUser().getEmail();
        if (searchText.trim().isEmpty()) {
            loadUserPantryItems();
            return;
        }
        db.collection("pantry_items")
                .whereEqualTo("email", userEmail)
                .orderBy("product_name")
                .startAt(searchText)
                .endAt(searchText + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pantryItemList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        PantryItem item = document.toObject(PantryItem.class);
                        pantryItemList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "No items found", Toast.LENGTH_SHORT).show());
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

    private final ActivityResultLauncher<Intent> editItemLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadUserPantryItems();
                }
            });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
