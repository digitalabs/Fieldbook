
package com.efficio.fieldbook.util.parsing.validation;

import org.generationcp.commons.parsing.validation.NonEmptyValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 3/2/2015 Time: 9:14 AM
 */

@RunWith(BlockJUnit4ClassRunner.class)
public class NonEmptyValidatorTest {

	public static final String EMPTY_STRING = "";
	public static final String RANDOM_TEST_STRING = "abcdef";

	private NonEmptyValidator dut;

	@Before
	public void setUp() throws Exception {
		this.dut = new NonEmptyValidator();
	}

	@Test
	public void testNonEmptyPass() {
		Assert.assertTrue(this.dut.isParsedValueValid(NonEmptyValidatorTest.RANDOM_TEST_STRING, null));
	}

	@Test
	public void testEmptyString() {
		Assert.assertFalse(this.dut.isParsedValueValid(NonEmptyValidatorTest.EMPTY_STRING, null));
	}

	@Test
	public void testNullString() {
		Assert.assertFalse(this.dut.isParsedValueValid(null, null));
	}
}
