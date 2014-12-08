package com.efficio.fieldbook.web.nursery.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.nursery.service.ValidationService;

public class ValidationServiceImplTest {

private ValidationService validationService;

	private static final String DATA_TYPE_NUMERIC = "Numeric variable";
	
	@Before
	public void setUp() {
		
		MockitoAnnotations.initMocks(this);
		
		validationService = spy(new ValidationServiceImpl());
		
	}
	
	@Test
	public void testisValidValueValidDefault(){
		
		MeasurementVariable var = new MeasurementVariable();
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "sadasd", false));

	}
	
	@Test
	public void testisValidValueValidValueRange(){
		
		MeasurementVariable var = new MeasurementVariable();
		var.setMaxRange(100d);
		var.setMinRange(1d);
		
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "", false));
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, null, false));
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "50", false));

	}
	
	@Test
	public void testisValidValueValidNumericValue(){
		
		MeasurementVariable var = new MeasurementVariable();
		var.setDataType(DATA_TYPE_NUMERIC);
		
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "", false));
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, null, false));
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "50", false));

	}
	
	@Test
	public void testisValidValueValidDateValue(){
		
		MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.DATE_VARIABLE.getId());
		
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "", true));
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, null, true));
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "20141010", true));

	}
	
	@Test
	public void testisValidValueInValidValueRange(){
		
		MeasurementVariable var = new MeasurementVariable();
		var.setMaxRange(100d);
		var.setMinRange(1d);
		
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "", false));
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, null, false));
		assertFalse("The value is valid therefore it must be true.", validationService.isValidValue(var, "101", false));
		assertFalse("The value is valid therefore it must be true.", validationService.isValidValue(var, "0", false));
		assertFalse("The value is valid therefore it must be true.", validationService.isValidValue(var, "abc", false));

	}
	
	@Test
	public void testisValidValueInValidNumericValue(){
		
		MeasurementVariable var = new MeasurementVariable();
		var.setDataType(DATA_TYPE_NUMERIC);
		
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "", false));
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, null, false));
		assertFalse("The value is valid therefore it must be true.", validationService.isValidValue(var, "abc", false));

	}
	
	@Test
	public void testisValidValueInValidDateValue(){
		
		MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.DATE_VARIABLE.getId());
		
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, "", true));
		assertTrue("The value is valid therefore it must be true.", validationService.isValidValue(var, null, true));
		assertFalse("The value is valid therefore it must be true.", validationService.isValidValue(var, "sss", true));

	}
	
	@Test
	public void testisValidValueIfCategorical(){
		
		MeasurementVariable var = new MeasurementVariable();
		var.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		
		assertTrue("The value should always be valid since we allow out of bounds value already", validationService.isValidValue(var, "xxx", false));
	}
}
