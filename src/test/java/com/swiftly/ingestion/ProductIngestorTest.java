package com.swiftly.ingestor;

import java.io.*;
import java.math.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import org.apache.commons.lang3.RandomStringUtils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.swiftly.ingestor.model.*;

public class ProductIngestorTest {
    ProductIngestor pi = new ProductIngestor();

    @Test
    public void testParseLineRegPrice() throws Exception {
        Product product = pi.parseLine("80000001 Kimchi-flavored white rice                                  00000567 00000000 00000000 00000000 00000000 00000000 NNNNNNNNN      18oz");

        assertEquals(product.getId(), 80000001);
        assertEquals(product.getDescription(), "Kimchi-flavored white rice");
        assertEquals(product.getSize(), "18oz");
        assertEquals(product.getUnitOfMeasure(), Product.EACH);

        List<Price> prices = product.getPrices();
        assertEquals(prices.size(), 1);
        assertEquals(prices.get(0).getType(), PriceType.REGULAR);
        assertEquals(prices.get(0).getDisplayPrice(), "$5.67");
        assertEquals(prices.get(0).getCalculatorPrice(), new BigDecimal("5.6700"));
    }

    @Test
    public void testParseLinePromoAndSplitPrice() throws Exception {
        Product product = pi.parseLine("14963801 Generic Soda 12-pack                                        00000000 00000549 00001300 00000000 00000002 00000000 NNNNYNNNN   12x12oz");
        assertEquals(product.getSize(), "12x12oz");
        assertTrue(product.isTaxable());
        List<Price> prices = product.getPrices();
        assertEquals(prices.size(), 2);

        Price promoPrice = prices.get(0);
        assertEquals(promoPrice.getType(), PriceType.PROMOTIONAL);
        assertEquals(promoPrice.getDisplayPrice(), "$5.49");
        assertEquals(promoPrice.getCalculatorPrice(), new BigDecimal("5.4900"));

        Price splitPrice = (SplitPrice)prices.get(1);
        assertEquals(splitPrice.getType(), PriceType.REGULAR);
        assertEquals(splitPrice.getDisplayPrice(), "$13.00 for 2");
        assertEquals(splitPrice.getCalculatorPrice(), new BigDecimal("6.5000"));
    }

    @Test
    public void testParseLinePromoAndSplitPriceNegative() throws Exception {
        Product product = pi.parseLine("14963801 Generic Soda 12-pack                                        00000000 -0000549 -0001300 00000000 00000002 00000000 NNNNYNNNN   12x12oz");
        List<Price> prices = product.getPrices();
        assertEquals(prices.size(), 2);
        
        Price promoPrice = prices.get(0);
        assertEquals(promoPrice.getDisplayPrice(), "$-5.49");
        
        Price splitPrice = (SplitPrice)prices.get(1);
        assertEquals(splitPrice.getDisplayPrice(), "$-13.00 for 2");
        assertEquals(splitPrice.getCalculatorPrice(), new BigDecimal("-6.5000"));
    }
    

    @Test
    public void testParseLineRegularAndPromoPrice() throws Exception {
        // TODO: hmmm. there is a flag in first position, and not flagged as taxable, which seems wrong
        Product product = pi.parseLine("40123401 Marlboro Cigarettes                                         00001000 00000549 00000000 00000000 00000000 00000000 YNNNNNNNN          ");
        assertEquals(product.getSize(), "");
        
        List<Price> prices = product.getPrices();
        assertEquals(prices.size(), 2);

        Price regularPrice = prices.get(0);
        assertEquals(regularPrice.getType(), PriceType.REGULAR);
        assertEquals(regularPrice.getDisplayPrice(), "$10.00");

        Price promoPrice = prices.get(1);
        assertEquals(promoPrice.getType(), PriceType.PROMOTIONAL);
        assertEquals(promoPrice.getDisplayPrice(), "$5.49");
    }

    @Test
    public void testParseLineByWeight() throws Exception {
        Product product = pi.parseLine("50133333 Fuji Apples (Organic)                                       00000349 00000000 00000000 00000000 00000000 00000000 NNYNNNNNN        lb");
        assertEquals(product.getUnitOfMeasure(), Product.POUND);
        assertTrue(product.isPerWeight());

        List<Price> prices = product.getPrices();
        assertEquals(prices.size(), 1);
        // TODO: should the display price be '$3.49 per pound' rather than just '$3.49' ?
    }

    @Test
    public void testCanParseNegativeCurrency() throws Exception {
        BigDecimal price = pi.parseCurrency("-0000567");
        assertTrue(price.doubleValue() < 0);
    }

    @Test
    public void testBigDecimalIs0() throws Exception {
        BigDecimal input = pi.parseCurrency("0000000");
        assertFalse(pi.isNot0(input));

        input = pi.parseCurrency("0000555");
        assertTrue(pi.isNot0(input));
    }

    @Test
    public void testParseFile() throws Exception {
        String output = pi.parseFile("src/test/resources/input-sample.txt");
        File file = new File(output);

        ObjectMapper mapper = new ObjectMapper();
        Object[] products = mapper.readValue(file, Object[].class);
        assertEquals(products.length, 4);

        // TODO: remove output file
    }

    @Test(description = "Poor man's perf test; assuming no update file is larger than 1mil, ensure we can process in less than 30 seconds")
    public void testParseLargeFile() throws Exception {
        String filename = "test-input-1mil.txt";
        generateLargeFile(filename, 100000);
        long l1 = System.currentTimeMillis();        
        String output = pi.parseFile(filename);
        long l2 = System.currentTimeMillis();
        assertTrue((l2-l1) < 30000);

        // delete files
        File file = new File(filename);
        file.delete();
        File outputFile = new File(output);
        outputFile.delete();
    }

    void generateLargeFile(String name, int max) throws Exception {
        int min = 0;
        Random rand = new Random();

        BufferedWriter bw = new BufferedWriter(new FileWriter(name));
        for (int i=0; i<max; i++) {
            int productId = rand.nextInt((max - min) + 1) + min;
            String s = String.valueOf(productId);
            int length = s.length();
            // pad with 0s if necessary
            if (length < 8) {
                StringBuffer sb = new StringBuffer();
                for (int j=0; j<8-length; j++) {
                    sb.append("0");
                }
                sb.append(s);
                s = sb.toString();
            }
            // write the random product id and the random product description
            bw.write(s + " " + RandomStringUtils.randomAlphabetic(59) + " 00000349 00000000 00000000 00000000 00000000 00000000 NNYNNNNNN        lb\n");
        }
    
        bw.close();

    }
}
