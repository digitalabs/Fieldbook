
package com.efficio.fieldbook.web.stock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by Daniel Villafuerte on 5/7/2015.
 */

@RunWith(JUnit4.class)
public class StockListGenerationSettingsTest {

	StockListGenerationSettings dut;

	public static final String TEST_IDENTIFIER = "ABC";
	public static final String TEST_SEPARATOR = "-";

	@Before
	public void setUp() throws Exception {
		this.dut = new StockListGenerationSettings();
	}

	@Test
	public void testValidateNumbersInIdentifier() {
		this.dut.setBreederIdentifier("AB12");
		Assert.assertEquals(StockListGenerationSettings.NUMBERS_FOUND, this.dut.validateSettings());
	}

	@Test
	public void testValidateSpacesInIdentifier() {
		this.dut.setBreederIdentifier("AB C");
		Assert.assertEquals(StockListGenerationSettings.SPACE_FOUND, this.dut.validateSettings());
	}

	@Test
	public void testValidBreederIdentifier() {
		this.dut.setBreederIdentifier("ABC");
		Assert.assertEquals(StockListGenerationSettings.VALID_SETTINGS, this.dut.validateSettings());
	}

	@Test
	public void testCopySettings() {
		StockListGenerationSettings copy =
				new StockListGenerationSettings(StockListGenerationSettingsTest.TEST_IDENTIFIER,
						StockListGenerationSettingsTest.TEST_SEPARATOR);
		this.dut.copy(copy);

		Assert.assertEquals(StockListGenerationSettingsTest.TEST_IDENTIFIER, this.dut.getBreederIdentifier());
		Assert.assertEquals(StockListGenerationSettingsTest.TEST_SEPARATOR, this.dut.getSeparator());
	}
}
