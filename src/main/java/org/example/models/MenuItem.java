package org.example.models;

public class MenuItem {
    private Integer itemId;
    private String itemName;
    private String category;
    private Double price;
    private String description;
    private Boolean isAvailable;

    public MenuItem(Integer itemId, String itemName, String category, Double price, String description, Boolean isAvailable) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.category = category;
        this.price = price;
        this.description = description;
        this.isAvailable = isAvailable;
    }

    public Integer getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getCategory() {
        return category;
    }

    public Double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

}
