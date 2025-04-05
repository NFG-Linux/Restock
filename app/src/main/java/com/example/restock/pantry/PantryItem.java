package com.example.restock.pantry;

import java.io.Serializable;
import java.util.Date;

// PantryItem.java
// Data model class for individual pantry items
public class PantryItem implements Serializable {
    private String code;
    private String product_name;
    private Integer quantity;
    private String expiration_date;
    private String ingredients_text;
    private Date timestamp;

    @SuppressWarnings("unused")
    public PantryItem() {}

    @SuppressWarnings("unused")
    public PantryItem(String code, String product_name, Integer quantity, String expiration_date, Date timestamp) {
        this.code = code;
        this.product_name = product_name;
        this.quantity = quantity;
        this.expiration_date = expiration_date;
        this.timestamp = timestamp;
    }

    // Getters
    public String getCode() { return code; }
    public String getProduct_name() { return product_name; }
    public Integer getQuantity() { return quantity; }
    public String getExpiration_date() { return expiration_date; }
    @SuppressWarnings("unused")
    public String getIngredientsText() { return ingredients_text; }
    @SuppressWarnings("unused")
    public Date getTimestamp() { return timestamp; }


    // Setters
    @SuppressWarnings("unused")
    public void setCode(String code) { this.code = code; }
    @SuppressWarnings("unused")
    public void setProduct_name(String product_name) { this.product_name = product_name; }
    @SuppressWarnings("unused")
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setExpiration_date(String expiration_date) { this.expiration_date = expiration_date; }
    @SuppressWarnings("unused")
    public void setIngredientsText(String ingredients_text) { this.ingredients_text = ingredients_text; }
    @SuppressWarnings("unused")
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
