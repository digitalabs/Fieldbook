
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.Operation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.service.api.FieldbookService;

@RunWith(MockitoJUnitRunner.class)
public class ExpDesignUtilTest {

	private static final int TEST_STANDARD_VARIABLE_TERMID = 1;
	private static final int TEST_PROPERTY_TERMID = 1234;
	private static final int TEST_SCALE_TERMID = 4321;
	private static final int TEST_METHOD_TERMID = 3333;
	private static final int TEST_DATATYPE_TERMID = 4444;

	private static final String TEST_DATATYPE_DESCRIPTION = "TEST DATATYPE";
	private static final String TEST_METHOD_NAME = "TEST METHOD";
	private static final String TEST_SCALE_NAME = "TEST SCALE";
	private static final String TEST_PROPERTY_NAME = "TEST PROPERTY";
	private static final String TEST_VARIABLE_DESCRIPTION = "TEST DESCRIPTION";
	private static final String TEST_VARIABLE_NAME = "TEST VARIABLE";

	@Mock
	private FieldbookService fieldbookService;

	@Test
	public void testConvertStandardVariableToMeasurementVariable() {

		Mockito.when(this.fieldbookService.getAllPossibleValues(TEST_STANDARD_VARIABLE_TERMID)).thenReturn(new ArrayList<ValueReference>());

		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(TEST_STANDARD_VARIABLE_TERMID);
		standardVariable.setName(TEST_VARIABLE_NAME);
		standardVariable.setDescription(TEST_VARIABLE_DESCRIPTION);
		standardVariable.setProperty(new Term(TEST_PROPERTY_TERMID, TEST_PROPERTY_NAME, ""));
		standardVariable.setScale(new Term(TEST_SCALE_TERMID, TEST_SCALE_NAME, ""));
		standardVariable.setMethod(new Term(TEST_METHOD_TERMID, TEST_METHOD_NAME, ""));
		standardVariable.setDataType(new Term(TEST_DATATYPE_TERMID, TEST_DATATYPE_DESCRIPTION, ""));

		final MeasurementVariable measurementVariable =
				ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, this.fieldbookService);

		Assert.assertEquals(standardVariable.getId(), measurementVariable.getTermId());
		Assert.assertEquals(standardVariable.getName(), measurementVariable.getName());
		Assert.assertEquals(standardVariable.getDescription(), measurementVariable.getDescription());
		Assert.assertEquals(standardVariable.getProperty().getName(), measurementVariable.getProperty());
		Assert.assertEquals(standardVariable.getScale().getName(), measurementVariable.getScale());
		Assert.assertEquals(standardVariable.getMethod().getName(), measurementVariable.getMethod());
		Assert.assertEquals(standardVariable.getDataType().getName(), measurementVariable.getDataType());
		Assert.assertNull(measurementVariable.getRole());
		Assert.assertEquals("", measurementVariable.getLabel());
	}

	@Test
	public void testConvertStandardVariableToMeasurementVariableStandardVariableHasPhenotypicType() {

		final FieldbookService mockFieldbookService = Mockito.mock(FieldbookService.class);

		Mockito.when(mockFieldbookService.getAllPossibleValues(TEST_STANDARD_VARIABLE_TERMID)).thenReturn(new ArrayList<ValueReference>());

		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(TEST_STANDARD_VARIABLE_TERMID);
		standardVariable.setName(TEST_VARIABLE_NAME);
		standardVariable.setDescription(TEST_VARIABLE_DESCRIPTION);
		standardVariable.setProperty(new Term(TEST_PROPERTY_TERMID, TEST_PROPERTY_NAME, ""));
		standardVariable.setScale(new Term(TEST_SCALE_TERMID, TEST_SCALE_NAME, ""));
		standardVariable.setMethod(new Term(TEST_METHOD_TERMID, TEST_METHOD_NAME, ""));
		standardVariable.setDataType(new Term(TEST_DATATYPE_TERMID, TEST_DATATYPE_DESCRIPTION, ""));
		standardVariable.setPhenotypicType(PhenotypicType.TRIAL_ENVIRONMENT);

		final MeasurementVariable measurementVariable =
				ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, mockFieldbookService);

		Assert.assertEquals(standardVariable.getId(), measurementVariable.getTermId());
		Assert.assertEquals(standardVariable.getName(), measurementVariable.getName());
		Assert.assertEquals(standardVariable.getDescription(), measurementVariable.getDescription());
		Assert.assertEquals(standardVariable.getProperty().getName(), measurementVariable.getProperty());
		Assert.assertEquals(standardVariable.getScale().getName(), measurementVariable.getScale());
		Assert.assertEquals(standardVariable.getMethod().getName(), measurementVariable.getMethod());
		Assert.assertEquals(standardVariable.getDataType().getName(), measurementVariable.getDataType());
		Assert.assertEquals(standardVariable.getPhenotypicType(), measurementVariable.getRole());
		Assert.assertEquals(PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0), measurementVariable.getLabel());
	}

}
