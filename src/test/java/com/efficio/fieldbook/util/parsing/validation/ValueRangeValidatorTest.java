package com.efficio.fieldbook.util.parsing.validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
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

		dut = new ValueRangeValidator(validValues);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testIllegalConstructorArgumentEmpty() {
		dut = new ValueRangeValidator(new ArrayList<String>());
	}

	@Test
	public void testValueInRange() {
		assertTrue(dut.isParsedValueValid("B"));
	}

	@Test
	public void testValueNotInRange() {
		assertFalse(dut.isParsedValueValid("Z"));
	}
}
