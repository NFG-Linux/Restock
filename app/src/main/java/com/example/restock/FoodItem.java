package com.example.restock;

public class FoodItem {
    private String name;       // Name of the food item
    private int quantity;      // Quantity of the food item
    private int imageResId;    // Resource ID for the image

    // Constructor
    public FoodItem(String name, int quantity, int imageResId) {
        this.name = name;
        this.quantity = quantity;
        this.imageResId = imageResId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getImageResId() {
        return imageResId;
    }

    // setters if i need to modify the data after creation
    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
