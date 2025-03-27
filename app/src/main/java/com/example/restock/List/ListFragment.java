package com.example.restock.List;

//List Fragment.java
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.restock.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class ListFragment extends Fragment implements MyItemRecyclerViewAdapter.OnItemClickListener {


    private MyItemRecyclerViewAdapter adapter;
    private List<GroceryItem> groceryItemList;
    private CollectionReference groceryListCollection;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.listRecyclerView);
        groceryItemList = new ArrayList<>();
        adapter = new MyItemRecyclerViewAdapter(groceryItemList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            groceryListCollection = FirebaseFirestore.getInstance().collection("grocery_lists").document(userId).collection("items");

            fetchGroceryList();
            FloatingActionButton fab = view.findViewById(R.id.add_button);
            fab.setOnClickListener(this::showFabMenu);
        } else {
            // Handle the case where the user is not signed in.
            // For example, you can redirect the user to the login screen.
            Toast.makeText(getContext(), "Please sign in to view your grocery list.", Toast.LENGTH_SHORT).show();
        }
        return view;
    }
    private void showFabMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.list_fab_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add_from_pantry) {
                showAddFromPantryDialog();
                return true;
            } else if (item.getItemId() == R.id.action_clear_list) {
                clearGroceryList();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showAddFromPantryDialog() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            db.collection("pantry_items")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> pantryItemNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            pantryItemNames.add(document.getString("product_name"));
                        }

                        if (pantryItemNames.isEmpty()) {
                            Toast.makeText(getContext(), "Pantry is empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean[] checkedItems = new boolean[pantryItemNames.size()];
                        String[] items = pantryItemNames.toArray(new String[0]);

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Select Items to Add");
                        builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);
                        builder.setPositiveButton("Add", (dialog, which) -> {
                            for (int i = 0; i < checkedItems.length; i++) {
                                if (checkedItems[i]) {
                                    String itemName = pantryItemNames.get(i);
                                    createQuantityInputDialog(itemName);
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch pantry items", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "Please sign in to add from pantry.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createQuantityInputDialog(String itemName) {
        EditText quantityInput = new EditText(getContext());
        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder quantityBuilder = new AlertDialog.Builder(getContext());
        quantityBuilder.setTitle("Enter Quantity");
        quantityBuilder.setView(quantityInput);
        quantityBuilder.setPositiveButton("OK", (quantityDialog, quantityWhich) -> {
            try {
                int quantity = Integer.parseInt(quantityInput.getText().toString());
                GroceryItem item = new GroceryItem(itemName, quantity, false);
                groceryListCollection.document().set(item);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
            }
        });
        quantityBuilder.setNegativeButton("Cancel", null);
        quantityBuilder.show();
    }



    private void clearGroceryList() {
        groceryListCollection.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                document.getReference().delete();
            }
        });
    }

    private void fetchGroceryList() {
        groceryListCollection.addSnapshotListener((value, error) -> {
            if (value != null) {
                groceryItemList.clear();
                for (DocumentSnapshot document : value.getDocuments()) {
                    GroceryItem item = document.toObject(GroceryItem.class);
                    if (item != null) {
                        groceryItemList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCheckChange(GroceryItem item, boolean isChecked) {
        groceryListCollection.whereEqualTo("product_name", item.getProduct_name()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                document.getReference().update("checked", isChecked);
            }
        });
    }
    @Override
    public void onDeleteClick(GroceryItem item) {
        groceryListCollection.whereEqualTo("product_name", item.getProduct_name()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                document.getReference().delete();
            }
        });
    }
}