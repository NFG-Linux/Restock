package com.example.restock;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ManualAddDialogHelper {

    public static void showManualAddDialog(Fragment fragment) {
        showBarcodePromptDialog(fragment);
    }

    private static void showBarcodePromptDialog(Fragment fragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext());
        builder.setTitle("Enter Barcode");

        final EditText barcodeInput = new EditText(fragment.requireContext());
        barcodeInput.setHint("Barcode");
        builder.setView(barcodeInput);

        builder.setPositiveButton("Next", (dialog, which) -> {
            String barcode = barcodeInput.getText().toString().trim();
            if (!barcode.isEmpty()) {
                handleBarcode(barcode, fragment);
            } else {
                Toast.makeText(fragment.requireContext(), "Please enter a barcode", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public static void handleBarcode(String barcode, Fragment fragment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        db.collection("imported_barcodes").document(barcode).get()
                .addOnSuccessListener(importedDoc -> {
                    if (importedDoc.exists()) {
                        String productName = importedDoc.getString("product_name");
                        String brand = importedDoc.getString("brand");
                        String category = importedDoc.getString("category");
                        String ingredients = importedDoc.getString("ingredients_text");

                        promptForQuantityAndAddToPantry(barcode, productName, brand, category, ingredients, fragment.getContext());
                    } else {
                        db.collection("user_created_barcodes").document(barcode).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String productName = userDoc.getString("product_name");
                                        String brand = userDoc.getString("brand");
                                        String category = userDoc.getString("category");
                                        String ingredients = userDoc.getString("ingredients_text");

                                        promptForQuantityAndAddToPantry(barcode, productName, brand, category, ingredients, fragment.getContext());
                                    } else {
                                        promptForNewItemDetails(barcode, fragment.getContext());
                                    }
                                });
                    }
                });
    }

    private static void promptForNewItemDetails(String barcode, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add New Item Details");

        final EditText productNameInput = new EditText(context);
        productNameInput.setHint("Product Name");

        final EditText brandInput = new EditText(context);
        brandInput.setHint("Brand");

        final EditText categoryInput = new EditText(context);
        categoryInput.setHint("Category");

        final EditText ingredientsInput = new EditText(context);
        ingredientsInput.setHint("Ingredients");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(productNameInput);
        layout.addView(brandInput);
        layout.addView(categoryInput);
        layout.addView(ingredientsInput);
        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String productName = productNameInput.getText().toString();
            String brand = brandInput.getText().toString();
            String category = categoryInput.getText().toString();
            String ingredients = ingredientsInput.getText().toString();

            Map<String, Object> itemData = new HashMap<>();
            itemData.put("code", barcode);
            itemData.put("product_name", productName);
            itemData.put("brand", brand);
            itemData.put("category", category);
            itemData.put("ingredients_text", ingredients);
            itemData.put("added_by", Objects.requireNonNull(auth.getCurrentUser()).getEmail());
            itemData.put("timestamp", FieldValue.serverTimestamp());

            db.collection("user_created_barcodes").document(barcode)
                    .set(itemData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Item added to user_created_barcodes", Toast.LENGTH_SHORT).show();
                        promptForQuantityAndAddToPantry(barcode, productName, brand, category, ingredients, context);
                        navigateToPantry(context);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to add item details", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private static void promptForQuantityAndAddToPantry(String barcode, String productName, String brand, String category, String ingredients, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String userEmail = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        String docId = userEmail.split("@")[0] + "-" + barcode;
        DocumentReference pantryRef = db.collection("pantry_items").document(docId);

        pantryRef.get().addOnSuccessListener(snapshot -> {
            AtomicReference<Long> existingQty = new AtomicReference<>(snapshot.getLong("quantity"));
            if (existingQty.get() == null) {
                existingQty.set(0L);
            }

            if (!snapshot.exists()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enter Item Details");
                builder.setMessage("How many do you want to add to your pantry?\nEnter the item's expiration date:");

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(16, 16, 16, 16);

                final EditText quantityInput = new EditText(context);
                quantityInput.setHint("Quantity");
                quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                quantityInput.setLayoutParams(params);
                layout.addView(quantityInput);

                final EditText expirationInput = new EditText(context);
                expirationInput.setHint("Expiration Date (mm/dd/yyyy)");
                expirationInput.setInputType(InputType.TYPE_CLASS_TEXT);
                expirationInput.setLayoutParams(params);
                layout.addView(expirationInput);

                builder.setView(layout);

                builder.setPositiveButton("Add", (dialog, which) -> {
                    String qtyText = quantityInput.getText().toString().trim();
                    String expText = expirationInput.getText().toString().trim();
                    if (!qtyText.isEmpty()) {
                        try {
                            int addQty = Integer.parseInt(qtyText);
                            Map<String, Object> pantryItem = new HashMap<>();
                            pantryItem.put("code", barcode);
                            pantryItem.put("product_name", productName);
                            pantryItem.put("brand", brand);
                            pantryItem.put("category", category);
                            pantryItem.put("ingredients_text", ingredients);
                            pantryItem.put("quantity", addQty);
                            pantryItem.put("user_id", auth.getCurrentUser().getUid());
                            pantryItem.put("email", userEmail);
                            if (!expText.isEmpty()) {
                                pantryItem.put("expiration_date", expText);
                            }
                            pantryItem.put("timestamp", FieldValue.serverTimestamp());

                            pantryRef.set(pantryItem)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Item added to pantry", Toast.LENGTH_SHORT).show();
                                        navigateToPantry(context);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to add item to pantry", Toast.LENGTH_SHORT).show());
                        } catch (NumberFormatException e) {
                            Toast.makeText(context, "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.show();
            } else {
                promptForExistingItems(barcode, productName, brand, category, ingredients, existingQty.get(), context);
            }
        });
    }

    private static void promptForExistingItems(String barcode, String productName, String brand, String category, String ingredients, Long existingQtyValue, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String userEmail = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        String docId = userEmail.split("@")[0] + "-" + barcode;
        DocumentReference pantryRef = db.collection("pantry_items").document(docId);

        pantryRef.get().addOnSuccessListener(snapshot -> {
            AtomicReference<Long> existingQty = new AtomicReference<>(snapshot.getLong("quantity"));
            if (existingQty.get() == null) {
                existingQty.set(0L);
            }
            String currentExp = snapshot.getString("expiration_date");
            if (currentExp == null) currentExp = "00/00/0000";

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Update Item Details");
            builder.setMessage("Current quantity: " + existingQty.get() + "\nWould you like to add or subtract from this quantity?");

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 16, 16, 16);

            // Radial button group for Add/Subtract
            final RadioGroup operationGroup = new RadioGroup(context);
            operationGroup.setOrientation(RadioGroup.VERTICAL);
            RadioButton addButton = new RadioButton(context);
            addButton.setText("Add");
            RadioButton subtractButton = new RadioButton(context);
            subtractButton.setText("Subtract");
            operationGroup.addView(addButton, params);
            operationGroup.addView(subtractButton, params);
            addButton.setChecked(true); // default to Add
            layout.addView(operationGroup);

            final EditText quantityInput = new EditText(context);
            quantityInput.setHint("Quantity change");
            quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            quantityInput.setLayoutParams(params);
            layout.addView(quantityInput);

            final EditText expirationInput = new EditText(context);
            expirationInput.setHint("mm/dd/yyyy");
            expirationInput.setInputType(InputType.TYPE_CLASS_TEXT);
            expirationInput.setLayoutParams(params);
            if (!currentExp.isEmpty()) {
                expirationInput.setText(currentExp);
            }
            layout.addView(expirationInput);

            builder.setView(layout);

            builder.setPositiveButton("Update", (dialog, which) -> {
                String qtyText = quantityInput.getText().toString().trim();
                String expText = expirationInput.getText().toString().trim();
                if (!qtyText.isEmpty()) {
                    try {
                        int changeQty = Integer.parseInt(qtyText);
                        boolean isAddition = addButton.isChecked();
                        long newQty = isAddition ? existingQty.get() + changeQty : existingQty.get() - changeQty;
                        if (newQty <= 0) {
                            pantryRef.delete()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Quantity 0, item removed from pantry", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show());
                        } else {
                            if (!expText.isEmpty()) {
                                pantryRef.update("quantity", newQty, "expiration_date", expText)
                                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Item quantity is now: " + newQty + "\nItem expiration date is: " + expText, Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show());
                            } else {
                                pantryRef.update("quantity", newQty)
                                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Item quantity is now: " + newQty, Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show());
                            }
                        }
                        navigateToPantry(context);
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        }).addOnFailureListener(e ->
                Toast.makeText(context, "Error retrieving pantry data", Toast.LENGTH_SHORT).show());
    }

    private static void navigateToPantry(Context context) {
        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            androidx.fragment.app.Fragment navHostFragment = activity.getSupportFragmentManager().findFragmentById(R.id.nav_graph);
            if (navHostFragment instanceof NavHostFragment) {
                NavController navController = ((NavHostFragment) navHostFragment).getNavController();
                navController.navigate(R.id.action_PantryRefresh);
            }
        }
    }
}
