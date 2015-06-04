
package com.efficio.fieldbook.web.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class KsuFieldbookUtilTest {

	private KsuFieldbookUtil ksuFieldbookUtil;

	@Before
	public void setUp() {
		this.ksuFieldbookUtil = Mockito.spy(new KsuFieldbookUtil());
	}

	@Test
	public void testIsARequiredColumn_ReturnsTrueForPlot() {
		Assert.assertTrue("Expecting PLOT is a required column but didn't.", KsuFieldbookUtil.isARequiredColumn("PLOT"));
	}

	@Test
	public void testIsARequiredColumn_ReturnsTrueForEntryNo() {
		Assert.assertTrue("Expecting Entry_No is a required column but didn't.", KsuFieldbookUtil.isARequiredColumn("Entry_No"));
	}

	@Test
	public void testIsARequiredColumn_ReturnsTrueForGID() {
		Assert.assertTrue("Expecting GID is a required column but didn't.", KsuFieldbookUtil.isARequiredColumn("GID"));
	}

	@Test
	public void testIsARequiredColumn_ReturnsTrueForDesignation() {
		Assert.assertTrue("Expecting DESIGNATION is a required column but didn't.", KsuFieldbookUtil.isARequiredColumn("Designation"));
	}

	@Test
	public void testIsARequiredColumn_ReturnsFalseForNonRequiredColumns() {
		Assert.assertFalse("Expecting PARENTAGE is a not required column but didn't.", KsuFieldbookUtil.isARequiredColumn("Parentage"));
	}

	@Test
	public void testIsValidHeaderNames_ReturnsTrueIfAllRequiredColumnsArePresent() {
		String[] headerNames = {"PLOT", "Entry_No", "Designation", "GID"};
		Assert.assertTrue("Expecting that the headers are valid when all of the required column are present but didn't.",
				KsuFieldbookUtil.isValidHeaderNames(headerNames));
	}

	@Test
	public void testIsValidHeaderNames_ReturnsFalseIfAtLeastOneOfTheRequiredColumnsIsNotPresent() {
		String[] headerNames = {"PLOT", "Entry_No", "Designation"};
		Assert.assertFalse("Expecting that the headers are not valid if at least one of the required column is not present but didn't.",
				KsuFieldbookUtil.isValidHeaderNames(headerNames));
	}

}
