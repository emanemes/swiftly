package com.swiftly.ingestor.model;

import java.math.*;

/**
 * prices eg. 2 for $5
 */
public class SplitPrice extends Price {
    int quantityForPrice;

    public SplitPrice(PriceType type, BigDecimal price) {
        super(type, price);
    }

    public int getQuantityForPrice() { return this.quantityForPrice; }
    public void setQuantityForPrice(int quantityForPrice) { this.quantityForPrice = quantityForPrice; }

    public BigDecimal getCalculatorPrice() {
        // 4 decimal points needed
        return price.divide(new BigDecimal(quantityForPrice)).setScale(4, RoundingMode.HALF_DOWN);
    }

    public String getDisplayPrice() {
        // TODO: ensure it's rounded to 2 decimals, half down
        // TODO: consider per weight
        // TODO: get the size from the product
        return "$" + price + " for " + quantityForPrice;
    }

}
