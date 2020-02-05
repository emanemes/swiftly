package com.swiftly.ingestor.model;

import java.math.*;
import java.util.*;

public class Price {
    // regular or promotional
    PriceType type;
    BigDecimal price;
    
    public Price(PriceType type, BigDecimal price) {
        this.type = type;
        this.price = price;
    }

    public PriceType getType() { return this.type; }
    public void setType(PriceType type) { this.type = type; }

    public BigDecimal getPrice() { return this.price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getCalculatorPrice() {
        // 4 decimal points needed
        return this.price.setScale(4, RoundingMode.HALF_DOWN);
    }

    public String getDisplayPrice() {
        // TODO: ensure it's rounded to 2 decimals, half down
        // TODO: consider per weight
        return "$" + price;
    }
    
}
