package com.example.restock;

// ProfileFragment.java

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private ImageView backButton;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText ageEditText;
    private TextInputEditText familySizeEditText;
    private TextInputEditText favoriteStoreEditText;
    private TextInputEditText monthlyBudgetEditText;
    private Spinner dietaryPreferenceSpinner;
    private MaterialButton saveButton;

    private FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        backButton = rootView.findViewById(R.id.back_button);
        nameEditText = rootView.findViewById(R.id.name_edit_text);
        emailEditText = rootView.findViewById(R.id.email_edit_text);
        ageEditText = rootView.findViewById(R.id.age_edit_text);
        familySizeEditText = rootView.findViewById(R.id.family_size_edit_text);
        favoriteStoreEditText = rootView.findViewById(R.id.favorite_store_edit_text);
        monthlyBudgetEditText = rootView.findViewById(R.id.monthly_budget_edit_text);
        dietaryPreferenceSpinner = rootView.findViewById(R.id.dietary_preference_spinner);
        saveButton = rootView.findViewById(R.id.save_button);

        backButton.setOnClickListener(view -> Navigation.findNavController(view).navigateUp());

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            emailEditText.setText(user.getEmail());
            loadUserData();
        }

        saveButton.setOnClickListener(view -> saveUserData());

        return rootView;
    }

    private void loadUserData() {
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String age = documentSnapshot.getString("age");
                        String familySize = documentSnapshot.getString("familySize");
                        String favoriteStore = documentSnapshot.getString("favoriteStore");
                        String monthlyBudget = documentSnapshot.getString("monthlyBudget");
                        String dietaryPreference = documentSnapshot.getString("dietaryPreference");

                        if (name != null) nameEditText.setText(name);
                        if (age != null) ageEditText.setText(age);
                        if (familySize != null) familySizeEditText.setText(familySize);
                        if (favoriteStore != null) favoriteStoreEditText.setText(favoriteStore);
                        if (monthlyBudget != null) monthlyBudgetEditText.setText(monthlyBudget);

                        if (dietaryPreference != null) {
                            int position = getSpinnerPosition(dietaryPreference);
                            dietaryPreferenceSpinner.setSelection(position);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private int getSpinnerPosition(String value) {
        for (int i = 1; i < dietaryPreferenceSpinner.getCount(); i++) {
            if (dietaryPreferenceSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void saveUserData() {
        String name = Objects.requireNonNull(nameEditText.getText()).toString().trim();
        String age = Objects.requireNonNull(ageEditText.getText()).toString().trim();
        String familySize = Objects.requireNonNull(familySizeEditText.getText()).toString().trim();
        String favoriteStore = Objects.requireNonNull(favoriteStoreEditText.getText()).toString().trim();
        String monthlyBudget = Objects.requireNonNull(monthlyBudgetEditText.getText()).toString().trim();
        String dietaryPreference = dietaryPreferenceSpinner.getSelectedItem().toString();

        if (dietaryPreference.equals("Dietary Preference")) {
            Toast.makeText(getContext(), "Please select a dietary preference.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("age", age);
        userData.put("familySize", familySize);
        userData.put("favoriteStore", favoriteStore);
        userData.put("monthlyBudget", monthlyBudget);
        userData.put("dietaryPreference", dietaryPreference);

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
