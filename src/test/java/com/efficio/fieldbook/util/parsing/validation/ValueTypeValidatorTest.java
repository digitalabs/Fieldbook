
package com.efficio.fieldbook.util.parsing.validation;

import java.util.Date;

import org.generationcp.commons.parsing.validation.ValueTypeValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
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
		this.dut = new ValueTypeValidator(Integer.class);
		boolean result = this.dut.isParsedValueValid(ValueTypeValidatorTest.TEST_INTEGER_STRING, null);

		Assert.assertTrue("Validator unable to properly process integer string", result);
	}

	@Test
	public void testIntegerStringFail() {
		this.dut = new ValueTypeValidator(Integer.class);
		boolean result = this.dut.isParsedValueValid(ValueTypeValidatorTest.RANDOM_TEST_STRING, null);

		Assert.assertFalse("Validator unable to properly process integer string", result);
	}

	@Test
	public void testDoubleStringMatch() {
		this.dut = new ValueTypeValidator(Double.class);
		boolean result = this.dut.isParsedValueValid(ValueTypeValidatorTest.TEST_DOUBLE_STRING, null);

		Assert.assertTrue("Validator unable to properly process double string", result);
	}

	@Test
	public void testDoubleStringFail() {
		this.dut = new ValueTypeValidator(Double.class);
		boolean result = this.dut.isParsedValueValid(ValueTypeValidatorTest.RANDOM_TEST_STRING, null);

		Assert.assertFalse("Validator unable to properly process double string", result);
	}

	@Test
	public void testExpectDoubleIntegerInput() {
		this.dut = new ValueTypeValidator(Double.class);
		boolean result = this.dut.isParsedValueValid(ValueTypeValidatorTest.TEST_INTEGER_STRING, null);

		Assert.assertTrue("Validator must still be able to process integer strings as double", result);
	}

	@Test
	public void testDateStringMatch() {
		this.dut = new ValueTypeValidator(Date.class);
		boolean result = this.dut.isParsedValueValid(ValueTypeValidatorTest.TEST_DATE_STRING, null);

		Assert.assertTrue("Validator unable to properly process date string", result);
	}

	@Test
	public void testDateStringFail() {
		this.dut = new ValueTypeValidator(Date.class);
		boolean result = this.dut.isParsedValueValid(ValueTypeValidatorTest.RANDOM_TEST_STRING, null);

		Assert.assertFalse("Validator unable to properly process date string", result);
	}

}
