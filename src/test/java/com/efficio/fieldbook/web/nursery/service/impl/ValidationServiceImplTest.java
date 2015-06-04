
package com.efficio.fieldbook.web.nursery.service.impl;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.nursery.service.ValidationService;

public class ValidationServiceImplTest {

	private ValidationService validationService;

	private static final String DATA_TYPE_NUMERIC = "Numeric variable";

	@Before
	public void setUp() {

		MockitoAnnotations.initMocks(this);

		this.validationService = Mockito.spy(new ValidationServiceImpl());

	}

	@Test
	public void testisValidValueValidDefault() {

		MeasurementVariable var = new MeasurementVariable();
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "sadasd", false));

	}

	@Test
	public void testisValidValueValidValueRange() {

		MeasurementVariable var = new MeasurementVariable();
		var.setMaxRange(100d);
		var.setMinRange(1d);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "50", false));

	}

	@Test
	public void testisValidValueValidNumericValue() {

		MeasurementVariable var = new MeasurementVariable();
		var.setDataType(ValidationServiceImplTest.DATA_TYPE_NUMERIC);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "50", false));

	}

	@Test
	public void testisValidValueValidDateValue() {

		MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.DATE_VARIABLE.getId());

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", true));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, true));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "20141010", true));

	}

	@Test
	public void testisValidValueInValidValueRange() {

		MeasurementVariable var = new MeasurementVariable();
		var.setMaxRange(100d);
		var.setMinRange(1d);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertFalse("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "101", false));
		Assert.assertFalse("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "0", false));
		Assert.assertFalse("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "abc", false));

	}

	@Test
	public void testisValidValueInValidNumericValue() {

		MeasurementVariable var = new MeasurementVariable();
		var.setDataType(ValidationServiceImplTest.DATA_TYPE_NUMERIC);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertFalse("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "abc", false));

	}

	@Test
	public void testisValidValueInValidDateValue() {

		MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.DATE_VARIABLE.getId());

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", true));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, true));
		Assert.assertFalse("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "sss", true));

	}

	@Test
	public void testisValidValueIfCategorical() {

		MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());

		Assert.assertTrue("The value should always be valid since we allow out of bounds value already",
				this.validationService.isValidValue(var, "xxx", false));
	}
}
