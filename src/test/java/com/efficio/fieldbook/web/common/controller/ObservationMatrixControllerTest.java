package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;

public class ObservationMatrixControllerTest {

	private ObservationMatrixController observationMatrixController;
	
	@Before
	public void setUp() {
		
		MockitoAnnotations.initMocks(this);
		
		observationMatrixController = spy(new ObservationMatrixController());
		
	}
	
	@Test
	public void testCopyMeasurementValue(){
		
		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(generateTestDataList());
		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(generateTestDataList());
		
		observationMatrixController.copyMeasurementValue(origRow, valueRow);
		
		for (int x=0; x < origRow.getDataList().size(); x++){
			assertEquals("The origRow's measurement value must be equal to the valueRow's measurement value", origRow.getDataList().get(x).getValue(), valueRow.getDataList().get(x).getValue());
		}
		
	}
	
	@Test 
	public void testCopyMeasurementValueNullEmptyPossibleValues(){
		
		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(generateTestDataList());
		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(generateTestDataList());
		
		MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);
		
		MeasurementVariable measurementVariable = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<>();
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);
		
		origRow.getDataList().add(nullData);
		valueRow.getDataList().add(nullData);
		
		observationMatrixController.copyMeasurementValue(origRow, valueRow);
		
		for (int x=0; x < origRow.getDataList().size(); x++){
			assertEquals("The origRow's measurement value must be equal to the valueRow's measurement value", origRow.getDataList().get(x).getValue(), valueRow.getDataList().get(x).getValue());
		}
		
	}
	
	@Test 
	public void testCopyMeasurementValueNullNullPossibleValuesAndValueIsNotEmpty(){
		
		MeasurementRow origRow = new MeasurementRow();
		origRow.setDataList(generateTestDataList());
		MeasurementRow valueRow = new MeasurementRow();
		valueRow.setDataList(generateTestDataList());
		
		MeasurementData data = new MeasurementData();
		data.setcValueId("1234");
		data.setDataType(null);
		data.setEditable(false);
		data.setLabel(null);
		data.setPhenotypeId(null);
		data.setValue(null);
		
		MeasurementData data2 = new MeasurementData();
		data2.setcValueId(null);
		data2.setDataType(null);
		data2.setEditable(false);
		data2.setLabel(null);
		data2.setPhenotypeId(null);
		data2.setValue("jjasd");
		
		MeasurementVariable measurementVariable = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		data.setMeasurementVariable(measurementVariable);
		
		origRow.getDataList().add(data);
		valueRow.getDataList().add(data2);
		
		observationMatrixController.copyMeasurementValue(origRow, valueRow);
		
		for (int x=0; x < origRow.getDataList().size(); x++){
			assertEquals("The origRow's measurement value must be equal to the valueRow's measurement value", origRow.getDataList().get(x).getValue(), valueRow.getDataList().get(x).getValue());
		}
		
	}
	
	private List<MeasurementData> generateTestDataList(){
		
		List<MeasurementData> dataList = new ArrayList<>();
		
		for (int x=0; x< 10; x++){
			MeasurementData data = new MeasurementData();
			data.setcValueId(UUID.randomUUID().toString());
			data.setDataType(UUID.randomUUID().toString());
			data.setEditable(true);
			data.setLabel(UUID.randomUUID().toString());
			data.setPhenotypeId(x);
			data.setValue(UUID.randomUUID().toString());
			data.setMeasurementVariable(new MeasurementVariable());
			dataList.add(data);
		}
		
		MeasurementData nullData = new MeasurementData();
		nullData.setcValueId(null);
		nullData.setDataType(null);
		nullData.setEditable(false);
		nullData.setLabel(null);
		nullData.setPhenotypeId(null);
		nullData.setValue(null);
		
		MeasurementVariable measurementVariable = new MeasurementVariable();
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference());
		measurementVariable.setPossibleValues(possibleValues);
		nullData.setMeasurementVariable(measurementVariable);
		dataList.add(nullData);
		
		
		MeasurementData emptyData = new MeasurementData();
		emptyData.setcValueId("");
		emptyData.setDataType("");
		emptyData.setEditable(false);
		emptyData.setLabel("");
		emptyData.setPhenotypeId(0);
		emptyData.setValue("");
		emptyData.setMeasurementVariable(measurementVariable);
		dataList.add(emptyData);
		
		return dataList;
	}

}
