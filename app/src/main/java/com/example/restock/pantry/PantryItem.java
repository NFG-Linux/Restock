package com.example.restock.pantry;

import java.io.Serializable;

// PantryItem.java
// Data model class for individual pantry items
public class PantryItem implements Serializable {
    private String code;
    private String product_name;
    private Integer quantity;
    private String expiration_date;

    @SuppressWarnings("unused")
    public PantryItem() {}

    @SuppressWarnings("unused")
    public PantryItem(String code, String product_name, Integer quantity, String expiration_date) {
        this.code = code;
        this.product_name = product_name;
        this.quantity = quantity;
        this.expiration_date = expiration_date;
    }

    public String getCode() { return code; }
    public String getProduct_name() { return product_name; }
    public Integer getQuantity() { return quantity; }
    public String getExpiration_date() { return expiration_date; }
    @SuppressWarnings("unused")
    public void setCode(String code) { this.code = code; }
    @SuppressWarnings("unused")
    public void setProduct_name(String product_name) { this.product_name = product_name; }
    @SuppressWarnings("unused")
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setExpiration_date(String expiration_date) { this.expiration_date = expiration_date; }
}
