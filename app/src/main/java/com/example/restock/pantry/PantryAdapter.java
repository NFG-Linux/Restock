package com.example.restock.pantry;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//Firebase imports
import com.example.restock.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

// PantryAdapter.java
// Adapter class to bind pantry item data to the pantry list UI in PantryFragment
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    List<PantryItem> pantryItemList;
    FirebaseFirestore db;
    FragmentManager fragmentManager;

    public PantryAdapter() {}

    public PantryAdapter(List<PantryItem> items, FragmentManager fragmentManager) {
        this.pantryItemList = items;
        this.db = FirebaseFirestore.getInstance();
        this.fragmentManager = fragmentManager;
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
        holder.itemQuantity.setText(holder.itemView.getContext().getString(R.string.quantity_text, item.getQuantity()));
        holder.itemImage.setImageResource(R.drawable.img_placeholder);

        // long press listener -> open BottomSheet directly
        holder.itemView.setOnLongClickListener(view -> {
            PantryItemDetailsBottomSheet bottomSheet = new PantryItemDetailsBottomSheet(item);
            bottomSheet.show(fragmentManager, "PantryItemDetailsBottomSheet"); // Use fragmentManager directly
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return pantryItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView itemImage;
        public final TextView itemName;
        public final TextView itemQuantity;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
        }
    }
}
