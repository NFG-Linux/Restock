package com.example.restock;

// MyItemRecyclerViewAdapter2(pantry).java
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.restock.placeholder.PlaceholderContent.PlaceholderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter2 extends RecyclerView.Adapter<MyItemRecyclerViewAdapter2.ViewHolder> {

    private final List<PlaceholderItem> mValues;

    public MyItemRecyclerViewAdapter2(List<PlaceholderItem> items) {
        mValues = items;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PlaceholderItem item = mValues.get(position);
        holder.mItem = item;
        holder.itemName.setText(item.content); // Name
        holder.itemQuantity.setText(holder.itemView.getContext().getString(R.string.quantity_text, Integer.parseInt(item.id))); //
        holder.itemImage.setImageResource(R.drawable.ic_img);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView itemImage;
        public final TextView itemName;
        public final TextView itemQuantity;
        public PlaceholderItem mItem;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + itemName.getText() + "'";
        }
    }
}