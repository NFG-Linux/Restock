package com.example.restock.pantry;

import java.util.Date;

// PantryItem.java
// Data model class for individual pantry items
public class PantryItem {
    private String code;
    private String product_name;
    private Integer quantity;

    @SuppressWarnings("unused")
    public PantryItem() {}

    @SuppressWarnings("unused")
    public PantryItem(String code, String product_name, Integer quantity, Date expirationDate, String imageUrl, String status) {
        this.code = code;
        this.product_name = product_name;
        this.quantity = quantity;
    }

    // Getters
    public String getCode() { return code; }
    public String getProduct_name() { return product_name; }
    public Integer getQuantity() { return quantity; }

    // Setters
    @SuppressWarnings("unused")
    public void setCode(String code) { this.code = code; }
    @SuppressWarnings("unused")
    public void setProduct_name(String product_name) { this.product_name = product_name; }
    @SuppressWarnings("unused")
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
