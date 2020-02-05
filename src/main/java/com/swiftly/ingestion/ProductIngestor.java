package com.swiftly.ingestor;

import java.io.*;
import java.math.*;
import java.util.*;

import com.swiftly.ingestor.model.*;

public class ProductIngestor {
    

    /**
     * parse a fixed-width string; separator is always 2 spaces. the field lengths are:
     * 7 58 7 7 7 7 7 7 8 8
     * at this time, the line has 10 records
     * This can be extended to any additional fields fitting the same pattern
     */
    public Product parseLine(String line) {
        int delimLength = 2;
        int countOfFields = 10;
        int[] fieldLengths = {7, 58, 7, 7, 7, 7, 7, 7, 8, 8};

        double id = parseDouble(line.substring(0, fieldLengths[0]+1));
        int cursor = fieldLengths[0]+delimLength;
        String descr = line.substring(cursor, cursor+58).trim();
        Product product = new Product(id, descr);
        
        cursor = cursor + fieldLengths[1] + delimLength;
        BigDecimal splitPriceAmount = BigDecimal.ZERO;
        BigDecimal promoSplitPriceAmount = BigDecimal.ZERO;
        
        for (int i=2; i<countOfFields; i++) {
            String s = line.substring(cursor, cursor+fieldLengths[i]+1);
            //System.out.println("PARSED: " + s + ", " + i);

            switch(i) {
            case 2:
                BigDecimal singularPriceAmount = parseCurrency(s);
                // if it's not 0, we have a price
                if (isNot0(singularPriceAmount)) {
                    Price price = new Price(PriceType.REGULAR, singularPriceAmount);
                    product.addPrice(price);
                }
                break;
            case 3:
                BigDecimal promoSingularPriceAmount = parseCurrency(s);
                if (isNot0(promoSingularPriceAmount)) {
                    Price promoPrice = new Price(PriceType.PROMOTIONAL, promoSingularPriceAmount);
                    product.addPrice(promoPrice);
                }
                break;
            case 4:
                splitPriceAmount = parseCurrency(s);
                break;
            case 5:
                promoSplitPriceAmount = parseCurrency(s);
                break;
            case 6:
                int quantityForPrice = parseInt(s);
                // the splitPrice amount should have been set
                if (isNot0(splitPriceAmount)) {
                    SplitPrice splitPrice = new SplitPrice(PriceType.REGULAR, splitPriceAmount);
                    splitPrice.setQuantityForPrice(quantityForPrice);
                    product.addPrice(splitPrice);
                    splitPriceAmount = BigDecimal.ZERO; // reset for the next product with split price
                }
                break;
            case 7:
                int promoQuantityForPrice = parseInt(s);
                if (isNot0(promoSplitPriceAmount)) {
                    SplitPrice promoSplitPrice = new SplitPrice(PriceType.PROMOTIONAL, promoSplitPriceAmount);
                    promoSplitPrice.setQuantityForPrice(promoQuantityForPrice);
                    product.addPrice(promoSplitPrice);
                    promoSplitPriceAmount = BigDecimal.ZERO; // reset for the next product with promo split price
                }
                break;
            case 8:
                product.setFlags(s.toCharArray());
                break;
            case 9:
                product.setSize(s.trim());
                break;
            default:
                // all cases ought to be covered                
            }                
            
            cursor = cursor + fieldLengths[i] + delimLength; 
        }

        return product;

    }

    BigDecimal parseCurrency(String input) {
        return new BigDecimal(Double.parseDouble(input)/100).setScale(2, RoundingMode.HALF_DOWN);
    }

    int parseInt(String input) {
        return Integer.parseInt(input);
    }

    double parseDouble(String input) {
        return Double.parseDouble(input);
    }

    // use a compare to account for decimal, eg. 0 vs 0.00
    boolean isNot0(BigDecimal input) {
        return input.compareTo(BigDecimal.ZERO) != 0;
    }

}
