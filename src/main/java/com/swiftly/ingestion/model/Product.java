package com.swiftly.ingestor.model;

import java.util.*;

public class Product {
    int id;
    String description;
    List<Price> prices = new ArrayList<Price>();
    char[] flags;
    boolean isPerWeight = false;
    boolean isTaxable = false;
    // lb, oz, etc 
    String size = "";

    static final char Y = 'Y';
    static final char N = 'N';
    public static final String EACH = "Each";
    public static final String POUND = "Pound";
        
    public Product(int id, String description) {
        this.id = id;
        this.description = description;        
    }

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public void addPrice(Price price) {
        prices.add(price);
    }
    public List<Price> getPrices() { return this.prices; }

    public String getUnitOfMeasure() {
        if (isPerWeight) {
            return POUND;
        } else {
            return EACH;
        }
    }

    public char[] getFlags() {
        return this.flags;
    }
    public void setFlags(char[] flags) {
        this.flags = flags;
        // we are guaranteed 7 entries in the array, won't check
        // If 3rd flag is set, this is a per-weight item
        if (flags[2] == Y) {
            isPerWeight = true;
        }
        // If 5th flag is set, the item is taxable
        if (flags[4] == Y) {
            isTaxable = true;
        }

        // TODO: one record in the sample input has a Y in the 1st position ?
    }
    
    public String getSize() { return this.size; }
    public void setSize(String size) { this.size = size; }

    public boolean isTaxable() { return isTaxable; }
    public boolean isPerWeight() { return isPerWeight; }

}
