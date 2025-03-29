package com.example.restock.List;

public class GroceryItem {
    private String product_name; // Match PantryItem field name
    private Integer quantity; // Match PantryItem field type
    private boolean checked;

    @SuppressWarnings("unused")
    public GroceryItem() {
        // Required empty constructor for Firestore
    }

    public GroceryItem(String product_name, Integer quantity, boolean checked) {
        this.product_name = product_name;
        this.quantity = quantity;
        this.checked = checked;
    }

    public String getProduct_name() { // Match PantryItem field name
        return product_name;
    }
    @SuppressWarnings("unused")
    public void setProduct_name(String product_name) { // Match PantryItem field name
        this.product_name = product_name;
    }

    public Integer getQuantity() { // Match PantryItem field type
        return quantity;
    }
    @SuppressWarnings("unused")
    public void setQuantity(Integer quantity) { // Match PantryItem field type
        this.quantity = quantity;
    }

    public boolean isChecked() {
        return checked;
    }
    @SuppressWarnings("unused")
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
