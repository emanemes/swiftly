package com.swiftly.ingestor;

import java.math.*;
import java.util.*;
import org.testng.annotations.Test;
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

        assertEquals(product.getId(), 80000001d);
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

}
