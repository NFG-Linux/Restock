package com.example.restock;

// MyItemRecyclerViewAdapter2.java
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                .inflate(R.layout.fragment_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public PlaceholderItem mItem;

        public ViewHolder(View itemView) {
            super(itemView);
            mIdView = itemView.findViewById(R.id.item_number);
            mContentView = itemView.findViewById(R.id.content);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}