package com.example.restock.pantry;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import com.example.restock.placeholder.PlaceholderContent.PlaceholderItem;

//Firebase imports
import com.example.restock.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

// PantryAdapter.java
// Adapter class to bind pantry item data to the pantry list UI in PantryFragment
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    private final List<PantryItem> pantryItemList;
    private final FirebaseFirestore db;

    public PantryAdapter(List<PantryItem> items) {

        this.pantryItemList = items;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pantry_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PantryItem item = pantryItemList.get(position);

        holder.itemName.setText(item.getProduct_name());
        holder.itemQuantity.setText(holder.itemView.getContext().getString(R.string.quantity_text, Integer.parseInt(String.valueOf(item.getQuantity()))));
        holder.itemImage.setImageResource(R.drawable.img_placeholder);

        // long press listener
        holder.itemView.setOnLongClickListener(view -> {
            fetchAndShowItemDetails(view.getContext(), item.getCode(), item.getProduct_name());
            return true;
        });
    }

    /**
     * Fetches item details from either 'imported_barcodes' or 'user_created_barcodes' and displays them in a dialog.
     */
    private void fetchAndShowItemDetails(Context context, String barcode, String productName) {
        db.collection("imported_barcodes").document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        showItemDetails(context, documentSnapshot);
                    } else {
                        db.collection("user_created_barcodes").document(barcode).get()
                                .addOnSuccessListener(userCreatedSnapshot -> {
                                    if (userCreatedSnapshot.exists()) {
                                        showItemDetails(context, userCreatedSnapshot);
                                    } else {
                                        showBasicItemDetails(context, barcode, productName);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> showBasicItemDetails(context, barcode, productName));
    }

    // Method to handle long press action
    private void showItemDetails(Context context, DocumentSnapshot document) {
        String code = document.getString("code");
        String productName = document.getString("product_name");
        String brand = document.getString("brand");
        String ingredients = document.getString("ingredients_text");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Item Details")
                .setMessage("Barcode: " + code + "\nName: " + productName + "\nBrand: " + brand + "\nIngredients: " + ingredients)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Fallback if item details are missing from Fire store.
     */
    private void showBasicItemDetails(Context context, String barcode, String productName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Item Details")
                .setMessage("Barcode: " + barcode + "\nName: " + productName)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public int getItemCount() {
        return pantryItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView itemImage;
        public final TextView itemName;
        public final TextView itemQuantity;
        public final ImageView itemStatus;
        public final TextView itemExpiration;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            itemStatus = itemView.findViewById(R.id.item_status_icon);
            itemExpiration = itemView.findViewById(R.id.item_expiration_date);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + itemName.getText() + "'";
        }
    }
}
