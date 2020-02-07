package com.swiftly.ingestor.model;

import java.util.*;

public class Product {
    int id;
    String description;
    List<PriceContainer> prices = new ArrayList<PriceContainer>();
    boolean isTaxable = false;
    boolean isPerWeight = false;
    // lb, oz, etc 
    String size = "";

    public static final String EACH = "each";
    public static final String POUND = "pound";
        
    public Product(int id, String description) {
        this.id = id;
        this.description = description;        
    }

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public void addPrice(PriceContainer price) {
        prices.add(price);
    }
    public List<PriceContainer> getPrices() { return this.prices; }

    public String getUnitOfMeasure() {
        if (isPerWeight) {
            return POUND;
        } else {
            return EACH;
        }
    }
    
    public String getSize() { return this.size; }
    public void setSize(String size) { this.size = size; }

    public boolean isTaxable() { return isTaxable; }
    public void setIsTaxable(boolean isTaxable) { this.isTaxable = isTaxable; }
    public boolean isPerWeight() { return isPerWeight; }
    public void setIsPerWeight(boolean isPerWeight) { this.isPerWeight = isPerWeight; }

}
