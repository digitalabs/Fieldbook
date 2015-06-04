
package com.efficio.fieldbook.util.parsing.validation;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.parsing.validation.ValueRangeValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@RunWith(BlockJUnit4ClassRunner.class)
public class ValueRangeValidatorTest {

	private ValueRangeValidator dut;

	@Before
	public void setUp() throws Exception {
		List<String> validValues = new ArrayList<>();
		validValues.add("A");
		validValues.add("B");
		validValues.add("C");

		this.dut = new ValueRangeValidator(validValues);
	}

	@Test
	public void testValueInRange() {
		Assert.assertTrue(this.dut.isParsedValueValid("B", null));
	}

	@Test
	public void testValueNotInRange() {
		Assert.assertFalse(this.dut.isParsedValueValid("Z", null));
	}

	@Test
	public void testBlankValueAndSkipIfEmptyTrue() {
		Assert.assertTrue(this.dut.isParsedValueValid(null, null));
	}

	@Test
	public void testBlankValueAndSkipIfEmptyFalse() {
		List<String> validValues = new ArrayList<>();
		validValues.add("A");
		validValues.add("B");
		validValues.add("C");
		this.dut = new ValueRangeValidator(validValues, false);

		Assert.assertFalse(this.dut.isParsedValueValid(null, null));
	}

	@Test
	public void testValueCheckAgainstBlankValid() {
		this.dut = new ValueRangeValidator(null);

		Assert.assertTrue(this.dut.isParsedValueValid("ZZ", null));
	}
}
