package com.example.restock.List;

//MyItemRecyclerViewAdapter(list).java
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.restock.R;
import com.example.restock.databinding.ListItemBinding;
import com.example.restock.placeholder.PlaceholderContent.PlaceholderItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<PlaceholderItem> mValues;
    private final Set<Integer> selectedItems = new HashSet<>();

    public MyItemRecyclerViewAdapter(List<PlaceholderItem> items) {
        mValues = items;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ListItemBinding binding = ListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PlaceholderItem item = mValues.get(position);

        holder.mItemName.setText(item.content);
        holder.mItem = item;
        holder.mItemName.setText(item.content);
        holder.mItemImage.setImageResource(R.drawable.img_placeholder);

        // Apply strikethrough if selected
        if (selectedItems.contains(position)) {
            holder.mItemName.setPaintFlags(holder.mItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.mItemName.setTextColor(Color.RED); // Set strikethrough text color to red
        } else {
            holder.mItemName.setPaintFlags(holder.mItemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.mItemName.setTextColor(Color.BLACK); // Set regular text color to black
        }

        // Handle item click to toggle strikethrough
        holder.mView.setOnClickListener(v -> {
            if (selectedItems.contains(position)) {
                selectedItems.remove(position);
            } else {
                selectedItems.add(position);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mItemImage;
        public final TextView mItemName;
        public final ImageView mEditIcon;
        public PlaceholderItem mItem;
        public final View mView;

        public ViewHolder(ListItemBinding binding) {
            super(binding.getRoot());
            mItemImage = binding.itemImage;
            mItemName = binding.itemName;
            mEditIcon = binding.editIcon;
            mView = binding.getRoot();
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + mItemName.getText() + "'";
        }
    }
}