package com.swiftly.ingestor.model;

import java.math.*;
import java.util.*;

/**
 * Prices have some complexity:
 * - they can be regular or promotional (defined as type)
 * - single or split ('$5' vs '$9 for 2'); see class SplitPrice
 * - have multiple 'manifestations', aside from the raw price: 
 *   - the display price, for human consumption ('$5 each')
 *   - the calculator price, with 4 digits; same as the raw price for single prices but 
 *     divided by quantity for split prices 
 */
public class PriceContainer {
    static String priceFormat = "$%s each";
    static String priceFormatPerWeight = "$%s per pound";
    
    // regular or promotional
    PriceType type;
    BigDecimal price;
    boolean isPerWeight = false;
    
    public PriceContainer(PriceType type, BigDecimal price) {
        this.type = type;
        this.price = price;
    }

    public PriceType getType() { return this.type; }
    public void setType(PriceType type) { this.type = type; }

    public BigDecimal getPrice() { return this.price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isPerWeight() { return isPerWeight; }
    public void setIsPerWeight(boolean isPerWeight) { this.isPerWeight = isPerWeight; }
    
    public BigDecimal getCalculatorPrice() {
        // 4 decimal points needed
        return this.price.setScale(4, RoundingMode.HALF_DOWN);
    }

    // '$5.25 each' or '$5.25 per pound' for items by weight
    public String getDisplayPrice() {
        if (isPerWeight()) {
            return String.format(priceFormatPerWeight, price.setScale(2, RoundingMode.HALF_DOWN).toString());
        } else {
            return String.format(priceFormat, price.setScale(2, RoundingMode.HALF_DOWN).toString());
        }
    }
}
