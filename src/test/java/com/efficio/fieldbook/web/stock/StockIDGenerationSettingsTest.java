package com.efficio.fieldbook.web.stock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Created by Daniel Villafuerte on 5/7/2015.
 */

@RunWith(JUnit4.class)
public class StockIDGenerationSettingsTest {
    StockIDGenerationSettings dut;

    public static final String TEST_IDENTIFIER = "ABC";
    public static final String TEST_SEPARATOR = "-";

    @Before
    public void setUp() throws Exception {
        dut = new StockIDGenerationSettings();
    }

    @Test
    public void testValidateNumbersInIdentifier() {
        dut.setBreederIdentifier("AB12");
        assertEquals(StockIDGenerationSettings.NUMBERS_FOUND, dut.validateSettings());
    }

    @Test
    public void testValidateSpacesInIdentifier() {
        dut.setBreederIdentifier("AB C");
        assertEquals(StockIDGenerationSettings.SPACE_FOUND, dut.validateSettings());
    }

    @Test
    public void testValidBreederIdentifier() {
        dut.setBreederIdentifier("ABC");
        assertEquals(StockIDGenerationSettings.VALID_SETTINGS, dut.validateSettings());
    }

    @Test
    public void testCopySettings() {
        StockIDGenerationSettings copy = new StockIDGenerationSettings(TEST_IDENTIFIER, TEST_SEPARATOR);
        dut.copy(copy);

        assertEquals(TEST_IDENTIFIER, dut.getBreederIdentifier());
        assertEquals(TEST_SEPARATOR, dut.getSeparator());
    }
}
