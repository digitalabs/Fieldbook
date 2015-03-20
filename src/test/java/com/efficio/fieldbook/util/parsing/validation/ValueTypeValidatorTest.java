package com.efficio.fieldbook.util.parsing.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */

@RunWith(BlockJUnit4ClassRunner.class)
public class ValueTypeValidatorTest {
	private ValueTypeValidator dut;
	public static final String TEST_INTEGER_STRING = "1";
	public static final String TEST_DOUBLE_STRING = "1.0";
	public static final String TEST_DATE_STRING = "20150212";
	public static final String RANDOM_TEST_STRING = "abcdef";

	@Test
	public void testIntegerStringMatch() {
		dut = new ValueTypeValidator(Integer.class);
		boolean result = dut.isParsedValueValid(TEST_INTEGER_STRING);

		assertTrue("Validator unable to properly process integer string", result);
	}

	@Test
	public void testIntegerStringFail() {
		dut = new ValueTypeValidator(Integer.class);
		boolean result = dut.isParsedValueValid(RANDOM_TEST_STRING);

		assertFalse("Validator unable to properly process integer string", result);
	}

	@Test
	public void testDoubleStringMatch() {
		dut = new ValueTypeValidator(Double.class);
		boolean result = dut.isParsedValueValid(TEST_DOUBLE_STRING);

		assertTrue("Validator unable to properly process double string", result);
	}

	@Test
	public void testDoubleStringFail() {
		dut = new ValueTypeValidator(Double.class);
		boolean result = dut.isParsedValueValid(RANDOM_TEST_STRING);

		assertFalse("Validator unable to properly process double string", result);
	}

	@Test
	public void testExpectDoubleIntegerInput() {
		dut = new ValueTypeValidator(Double.class);
		boolean result = dut.isParsedValueValid(TEST_INTEGER_STRING);

		assertTrue("Validator must still be able to process integer strings as double", result);
	}

	@Test
	public void testDateStringMatch() {
		dut = new ValueTypeValidator(Date.class);
		boolean result = dut.isParsedValueValid(TEST_DATE_STRING);

		assertTrue("Validator unable to properly process date string", result);
	}

	@Test
	public void testDateStringFail() {
		dut = new ValueTypeValidator(Date.class);
		boolean result = dut.isParsedValueValid(RANDOM_TEST_STRING);

		assertFalse("Validator unable to properly process date string", result);
	}

}
