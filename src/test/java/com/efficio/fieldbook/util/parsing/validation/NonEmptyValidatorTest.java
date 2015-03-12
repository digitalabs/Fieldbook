package com.efficio.fieldbook.util.parsing.validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.junit.Assert.*;
/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 3/2/2015
 * Time: 9:14 AM
 */

@RunWith(BlockJUnit4ClassRunner.class)
public class NonEmptyValidatorTest {

	public static final String EMPTY_STRING = "";
	public static final String RANDOM_TEST_STRING = "abcdef";


	private NonEmptyValidator dut;

	@Before
	public void setUp() throws Exception {
		dut = new NonEmptyValidator();
	}

	@Test
	public void testNonEmptyPass() {
		assertTrue(dut.isParsedValueValid(RANDOM_TEST_STRING));
	}

	@Test
	public void testEmptyString() {
		assertFalse(dut.isParsedValueValid(EMPTY_STRING));
	}

	@Test
	public void testNullString() {
		assertFalse(dut.isParsedValueValid(null));
	}
}
