
package com.efficio.fieldbook.web.nursery.service.impl;

import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.MethodTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.WorkbenchService;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private ValidationServiceImpl validationService;

	private static final String DATA_TYPE_NUMERIC = "Numeric";

	private Workbook workbook;

	private static final String WARNING_MESSAGE =
			"The value for null in the import file is invalid and will not be imported. You can change this value by editing it manually, or by uploading a corrected import file.";

	private static final String EMPTY_STRING = "";

	private MethodTestDataInitializer methodTestDataInitializer;

	private MeasurementVariableTestDataInitializer measurementVarTestDataInitializer;

	@Before
	public void setUp() {
		this.methodTestDataInitializer = new MethodTestDataInitializer();
		this.measurementVarTestDataInitializer = new MeasurementVariableTestDataInitializer();
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook();
		this.workbook.setConditions(this.measurementVarTestDataInitializer.createMeasurementVariableList());
		Mockito.when(this.fieldbookMiddlewareService.getAllBreedingMethods(Matchers.eq(false)))
				.thenReturn(this.methodTestDataInitializer.createMethodList());
	}

	@Test
	public void testisValidValueValidDefault() {

		final MeasurementVariable var = new MeasurementVariable();
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "sadasd", false));

	}

	@Test
	public void testisValidValueValidValueRange() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setMaxRange(100d);
		var.setMinRange(1d);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "50", false));

	}

	@Test
	public void testisValidValueValidNumericValue() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataType(ValidationServiceImplTest.DATA_TYPE_NUMERIC);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "50", false));

	}

	@Test
	public void testisValidValueValidDateValue() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.DATE_VARIABLE.getId());

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", true));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, true));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "20141010", true));

	}

	@Test
	public void testisValidValueInValidValueRange() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setMaxRange(100d);
		var.setMinRange(1d);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "101", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "0", false));
		Assert.assertFalse("The value is not valid therefore it must be false.", this.validationService.isValidValue(var, "abc", false));

	}

	@Test
	public void testisValidValueInValidNumericValue() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataType(ValidationServiceImplTest.DATA_TYPE_NUMERIC);

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", false));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, false));
		Assert.assertFalse("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "abc", false));

	}

	@Test
	public void testisValidValueInValidDateValue() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.DATE_VARIABLE.getId());

		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "", true));
		Assert.assertTrue("The value is valid therefore it must be true.", this.validationService.isValidValue(var, null, true));
		Assert.assertFalse("The value is valid therefore it must be true.", this.validationService.isValidValue(var, "sss", true));

	}

	@Test
	public void testisValidValueIfCategorical() {

		final MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());

		Assert.assertTrue("The value should always be valid since we allow out of bounds value already",
				this.validationService.isValidValue(var, "xxx", false));
	}

	@Test
	public void testValidatePersonIdIfPIIdHasInvalidValue() {
		Mockito.when(this.workbenchService.getPersonById(Matchers.anyInt())).thenReturn(null);
		final String warningMessage =
				this.validationService.validatePersonId(this.measurementVarTestDataInitializer.createMeasurementVariable());
		Assert.assertTrue("There should be a warning message", ValidationServiceImplTest.WARNING_MESSAGE.equals(warningMessage));
	}

	@Test
	public void testValidatePersonIdIfPIIdHasValidValue() {
		Mockito.when(this.workbenchService.getPersonById(Matchers.anyInt())).thenReturn(new Person());
		final String warningMessage =
				this.validationService.validatePersonId(this.measurementVarTestDataInitializer.createMeasurementVariable());
		Assert.assertTrue("There should be no warning message", ValidationServiceImplTest.EMPTY_STRING.equals(warningMessage));
	}

	@Test
	public void testValidateBreedingMethodCodeIfBMCodeHasValue() {
		final String warningMessage = this.validationService.validateBreedingMethodCode(
				this.measurementVarTestDataInitializer.createMeasurementVariable(TermId.BREEDING_METHOD_CODE.getId(), "PSP"));
		Assert.assertTrue("There should be no warning message", ValidationServiceImplTest.EMPTY_STRING.equals(warningMessage));
	}

	@Test
	public void testValidateBreedingMethodCodeIfBMCodeHasInvalue() {
		final String warningMessage = this.validationService.validateBreedingMethodCode(
				this.measurementVarTestDataInitializer.createMeasurementVariable(TermId.BREEDING_METHOD_CODE.getId(), "PXP"));
		Assert.assertTrue("There should be a warning message", ValidationServiceImplTest.WARNING_MESSAGE.equals(warningMessage));
	}
}
