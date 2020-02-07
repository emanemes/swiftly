package com.swiftly.ingestor.model;

import java.math.*;

/**
 * split prices eg. 2 for $5
 */
public class SplitPriceContainer extends PriceContainer {
    static String priceFormat = "$%s for %d";
    static String priceFormatPerWeight = "$%s for %d pounds";
    int quantityForPrice;

    public SplitPriceContainer(PriceType type, BigDecimal price) {
        super(type, price);
    }

    public int getQuantityForPrice() { return this.quantityForPrice; }
    public void setQuantityForPrice(int quantityForPrice) { this.quantityForPrice = quantityForPrice; }

    public BigDecimal getCalculatorPrice() {
        // 4 decimal points needed
        return price.divide(new BigDecimal(quantityForPrice)).setScale(4, RoundingMode.HALF_DOWN);
    }

    // '$5.00 for 2' or '$10.00 for 2 pounds' for items by weight
    public String getDisplayPrice() {
        if (isPerWeight()) {
            return String.format(priceFormatPerWeight, price.setScale(2, RoundingMode.HALF_DOWN).toString(), quantityForPrice);
        } else {
            return String.format(priceFormat, price.setScale(2, RoundingMode.HALF_DOWN).toString(), quantityForPrice);
        }
    }

}
