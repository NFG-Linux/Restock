package com.example.restock.pantry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.restock.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

// EditPantryItemActivity.java
public class EditPantryItemActivity extends AppCompatActivity {

    PantryItem item;
    private EditText nameEditText, quantityEditText;
    Button saveButton;
    private FirebaseFirestore db;

    public EditPantryItemActivity() {}

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantry_edit_item_activity);

        db = FirebaseFirestore.getInstance();

        // Get item passed from BottomSheet
        item = (PantryItem) getIntent().getSerializableExtra("pantryItem");

        // Bind views
        nameEditText = findViewById(R.id.edit_item_name);
        quantityEditText = findViewById(R.id.edit_item_quantity);
        saveButton = findViewById(R.id.save_button);

        // Set initial values
        if (item != null) {
            nameEditText.setText(item.getProduct_name());
            quantityEditText.setText(String.valueOf(item.getQuantity()));
        }

        // Save button functionality
        saveButton.setOnClickListener(v -> {
            String updatedName = nameEditText.getText().toString().trim();
            String updatedQuantityStr = quantityEditText.getText().toString().trim();

            if (updatedName.isEmpty() || updatedQuantityStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int updatedQuantity;
            try {
                updatedQuantity = Integer.parseInt(updatedQuantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create updated data map
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("product_name", updatedName);
            updatedData.put("quantity", updatedQuantity);

            // Get user email (assuming user is authenticated)
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            // Update Fire store
            db.collection("pantry_items")
                    .whereEqualTo("code", item.getCode())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            String documentId = document.getId();  // Get the document ID

                            // Update the item data
                            db.collection("pantry_items").document(documentId)
                                    .update(updatedData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error getting item", Toast.LENGTH_SHORT).show();
                    });
        });

    }


}
