package com.swiftly.ingestor;

import java.io.*;
import java.math.*;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.swiftly.ingestor.model.*;
    
/**
 * Ingests products from a file of known format 'input.txt', spits out json objects constructed from the given data into a new file 'input_<timestamp>.txt'
 */
public class ProductIngestor {

    // for json serialization
    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Returns the name of the file storing the output
     */
    public String parseFile(String filename) throws Exception {
        File file = new File(filename);
        if (!file.exists()) {
            throw new IngestionException(ErrorCode.FILE_NOT_FOUND, "File " + filename +  " was not found");
        }
        
        String outputFileName = deriveOutputFilename(filename);

        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFileName)));
        String line;

        // serializing as a json array as we parse the input
        bw.write("[\n");
        int lineCtr = 0;
        while ((line = br.readLine()) != null) {
            // to make proper json
            if (lineCtr > 0) {                
                bw.write(",\n");
            }
            Product product = parseLine(line);
            String s = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product);
            bw.write(s);
            lineCtr++;
        }

        bw.write("]");
        br.close();
        bw.close();

        return outputFileName;
    }


    /**
     * Parse a fixed-width string; separator is always 1 space. The field lengths are:
     * 7 58 7 7 7 7 7 7 8 8
     * At this time, the line has 10 records
     * NOTE: if new fields need parsing, add their length to the fieldsLength array and process them in the switch bloc
     */
    public Product parseLine(String line) throws IngestionException {
        int delimLength = 1;
        int[] fieldLengths = {8, 59, 8, 8, 8, 8, 8, 8, 9, 9};

        int id = parseInt(line.substring(0, fieldLengths[0]));
        int cursor = fieldLengths[0]+delimLength;
        String descr = line.substring(cursor, cursor+58).trim();
        Product product = new Product(id, descr);
        
        cursor = cursor + fieldLengths[1] + delimLength;
        BigDecimal splitPriceAmount = BigDecimal.ZERO;
        BigDecimal promoSplitPriceAmount = BigDecimal.ZERO;
        
        for (int i=2; i<fieldLengths.length; i++) {
            String s = line.substring(cursor, cursor+fieldLengths[i]);

            switch(i) {
            case 2:
                BigDecimal singularPriceAmount = parseCurrency(s);
                // if it's not 0, we have a price
                if (isNot0(singularPriceAmount)) {
                    PriceContainer price = new PriceContainer(PriceType.REGULAR, singularPriceAmount);
                    product.addPrice(price);
                }
                break;
            case 3:
                BigDecimal promoSingularPriceAmount = parseCurrency(s);
                if (isNot0(promoSingularPriceAmount)) {
                    PriceContainer promoPrice = new PriceContainer(PriceType.PROMOTIONAL, promoSingularPriceAmount);
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
                    SplitPriceContainer splitPrice = new SplitPriceContainer(PriceType.REGULAR, splitPriceAmount);
                    splitPrice.setQuantityForPrice(quantityForPrice);
                    product.addPrice(splitPrice);
                    splitPriceAmount = BigDecimal.ZERO; // reset for the next product with split price
                }
                break;
            case 7:
                int promoQuantityForPrice = parseInt(s);
                if (isNot0(promoSplitPriceAmount)) {
                    SplitPriceContainer promoSplitPrice = new SplitPriceContainer(PriceType.PROMOTIONAL, promoSplitPriceAmount);
                    promoSplitPrice.setQuantityForPrice(promoQuantityForPrice);
                    product.addPrice(promoSplitPrice);
                    promoSplitPriceAmount = BigDecimal.ZERO; // reset for the next product with promo split price
                }
                break;
            case 8:
                setFlags(product, s.toCharArray());
                break;
            case 9:
                product.setSize(s.trim());
                break;
            default:
                // all cases ought to be covered, but just in case we missed any
                throw new IngestionException(ErrorCode.UNPROCESSED_FIELD, "One of the " + fieldLengths.length + " has not been processed");
            }                
            
            cursor = cursor + fieldLengths[i]+delimLength; 
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

    // use a compare to account for decimals, eg. 0 vs 0.00
    boolean isNot0(BigDecimal input) {
        return input.compareTo(BigDecimal.ZERO) != 0;
    }

    String deriveOutputFilename(String name) {
        int lastIndexOf = name.lastIndexOf(".");
        return name.substring(0, lastIndexOf) + "_" + System.currentTimeMillis() + name.substring(lastIndexOf);
    }

    // NOTE: if new flags need parsing, tackle them here
    void setFlags(Product product, char[] flags) {
        // If 3rd flag is set, this is a per-weight item
        if (flags[2] == 'Y') {
            product.setIsPerWeight(true);
            // inform prices as their display price needs this information
            for (PriceContainer price : product.getPrices()) {
                price.setIsPerWeight(true);
            }
        }
        // If 5th flag is set, the item is taxable
        if (flags[4] == 'Y') {
            product.setIsTaxable(true);
        }
    }

    public static final void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage is: ProductIngestor <data file>");
            System.exit(0);
        }

        try {
            ProductIngestor pi = new ProductIngestor();
            System.out.println("OUTPUT LOCATED AT: " + pi.parseFile(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
