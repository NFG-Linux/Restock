package com.example.restock;

//MyItemRecyclerViewAdapter(list).java
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.restock.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.restock.databinding.ItemListBinding;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<PlaceholderItem> mValues;

    public MyItemRecyclerViewAdapter(List<PlaceholderItem> items) {
        mValues = items;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(ItemListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        PlaceholderItem item = mValues.get(position);
        holder.mItem = item;
        holder.mItemName.setText(item.content);
        holder.mItemImage.setImageResource(R.drawable.ic_img);
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

        public ViewHolder(ItemListBinding binding) {
            super(binding.getRoot());
            mItemImage = binding.itemImage;
            mItemName = binding.itemName;
            mEditIcon = binding.editIcon;
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + mItemName.getText() + "'";
        }
    }
}