package com.example.restock.List;

//MyItemRecyclerViewAdapter(list).java

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.restock.databinding.ListItemBinding;
import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<GroceryItem> mValues;
    private final OnItemClickListener mListener;

    public MyItemRecyclerViewAdapter(List<GroceryItem> items, OnItemClickListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemBinding binding = ListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        GroceryItem item = mValues.get(position);

        holder.mItemName.setText(item.getProduct_name());
        holder.mQuantity.setText(String.valueOf(item.getQuantity()));
        holder.mCheckBox.setChecked(item.isChecked());

        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mListener != null) {
                mListener.onCheckChange(item, isChecked);
            }
        });

        holder.mDeleteIcon.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mItemName;
        public final TextView mQuantity;
        public final CheckBox mCheckBox;
        public final ImageView mDeleteIcon;
        public final View mView;

        public ViewHolder(ListItemBinding binding) {
            super(binding.getRoot());
            mItemName = binding.itemName;
            mQuantity = binding.quantity;
            mCheckBox = binding.itemCheckbox;
            mDeleteIcon = binding.deleteIcon;
            mView = binding.getRoot();
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mItemName.getText() + "'";
        }
    }

    public interface OnItemClickListener {
        void onCheckChange(GroceryItem item, boolean isChecked);
        void onDeleteClick(GroceryItem item);
    }
}