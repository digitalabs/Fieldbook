
package com.efficio.fieldbook.web.util;

import org.generationcp.middleware.data.initializer.MeasurementTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Assert;
import org.junit.Test;

public class KsuFieldbookUtilTest {

	@Test
	public void testIsValidHeaderNames_ReturnsTrueIfAllRequiredColumnsArePresent() {
		String[] headerNames = {"plot", "ENTRY_NO", "DESIGNATION", "GID", "PLOT_ID"};
		Assert.assertTrue("Expecting that the headers are valid when all of the required column are present but didn't.",
				KsuFieldbookUtil.isValidHeaderNames(headerNames));
	}

	@Test
	public void testIsValidHeaderNames_ReturnsFalseIfAtLeastOneOfTheRequiredColumnsIsNotPresent() {
		String[] headerNames = {"plot", "ENTRY_NO", "DESIGNATION"};
		Assert.assertFalse("Expecting that the headers are not valid if at least one of the required column is not present but didn't.",
				KsuFieldbookUtil.isValidHeaderNames(headerNames));
	}
	
	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdPresentInEnum () {
		MeasurementTestDataInitializer  measurementTestDataInitializer = new MeasurementTestDataInitializer();
		MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(TermId.ENTRY_NO.getId(), 1);
		String label = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(mVar);
		Assert.assertEquals("The label should be ENTRY_NO","ENTRY_NO", label);
		
	}
	
	@Test
	public void testGetLabelFromKsuRequiredColumnTermIdNotPresentInEnum () {
		MeasurementTestDataInitializer  measurementTestDataInitializer = new MeasurementTestDataInitializer();
		MeasurementVariable mVar = measurementTestDataInitializer.createMeasurementVariable(1, 1);
		String label = KsuFieldbookUtil.getLabelFromKsuRequiredColumn(mVar);
		Assert.assertEquals("The label should be " + mVar.getName(), mVar.getName(), label);
		
	}
}
