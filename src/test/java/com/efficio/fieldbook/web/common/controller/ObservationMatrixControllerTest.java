package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.ModelMap;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.service.ValidationService;

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

	@Test
	public void testEditExperimentCells() throws MiddlewareQueryException{
		int termId = 2000;
		ExtendedModelMap model = new ExtendedModelMap();
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList();
		dataList.add(generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(), "TestVarName1"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList();
		dataList.add(generateTestMeasurementData(termId, "2nd", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(), "TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		observationMatrixController.setStudySelection(userSelection);
		observationMatrixController.editExperimentCells(1, termId, model);
		MeasurementData data = (MeasurementData) model.get("measurementData");
		Assert.assertEquals("Should be able to return a copy of the measurement data, so the value should be the same", "2nd", data.getValue());
		Assert.assertEquals("Should be able to return a copy of the measurement data, so the id should be the same", termId, data.getMeasurementVariable().getTermId());
	}
	
	@Test
	public void testUpdateExperimentCellDataIfNotDiscard(){
		int termId = 2000;
		String newValue = "new value";
		ExtendedModelMap model = new ExtendedModelMap();
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList();
		dataList.add(generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(), "TestVarName1"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList();
		dataList.add(generateTestMeasurementData(termId, "2nd", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(), "TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		observationMatrixController.setStudySelection(userSelection);
		observationMatrixController.setValidationService(Mockito.mock(ValidationService.class));
		Map<String, String> data = new HashMap();
		data.put("index", "1");
		data.put("termId", Integer.toString(termId));
		data.put("value", newValue);
		data.put("isNew", "1");
    	int isNew = Integer.valueOf(data.get("isNew"));
    	HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    	Mockito.when(req.getParameter("isDiscard")).thenReturn("0");
    	    	    	
		Map<String, Object> results = observationMatrixController.updateExperimentCellData(data, req);
		
		Map<String, Object> dataMap =  (Map) results.get("data");
		Assert.assertEquals("Should have the new value already",newValue, dataMap.get("TestVarName2"));		
	}
	
	@Test
	public void testUpdateExperimentCellDataIfDiscard(){
		int termId = 2000;
		String newValue = "new value";
		ExtendedModelMap model = new ExtendedModelMap();
		UserSelection userSelection = new UserSelection();
		List<MeasurementRow> measurementRowList = new ArrayList();
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList();
		dataList.add(generateTestMeasurementData(1000, "1st", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(), "TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		row = new MeasurementRow();
		dataList = new ArrayList();
		dataList.add(generateTestMeasurementData(termId, "2nd", TermId.CHARACTER_VARIABLE.getId(), new ArrayList<ValueReference>(), "TestVarName2"));
		row.setDataList(dataList);
		measurementRowList.add(row);
		userSelection.setMeasurementRowList(measurementRowList);
		userSelection.setWorkbook(Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class));
		observationMatrixController.setStudySelection(userSelection);
		observationMatrixController.setValidationService(Mockito.mock(ValidationService.class));
		Map<String, String> data = new HashMap();
		data.put("index", "1");
		data.put("termId", Integer.toString(termId));
		data.put("value", newValue);
		data.put("isNew", "1");
    	int isNew = Integer.valueOf(data.get("isNew"));
    	HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    	Mockito.when(req.getParameter("isDiscard")).thenReturn("1");
    	    	    	
		Map<String, Object> results = observationMatrixController.updateExperimentCellData(data, req);
		
		Map<String, Object> dataMap =  (Map) results.get("data");
		Assert.assertEquals("Should have the old value since we discard the saving","2nd", dataMap.get("TestVarName2"));
	}
	
	private MeasurementData generateTestMeasurementData(int termId, String value, int dataTypeId, List<ValueReference> possibleValues, String varName){
		MeasurementData emptyData = new MeasurementData();
		MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		measurementVariable.setDataTypeId(dataTypeId);
		measurementVariable.setPossibleValues(possibleValues);
		measurementVariable.setName(varName);
		emptyData.setcValueId("");
		emptyData.setDataType("");
		emptyData.setEditable(false);
		emptyData.setLabel("");
		emptyData.setPhenotypeId(0);
		emptyData.setValue(value);
		emptyData.setMeasurementVariable(measurementVariable);
		return emptyData;
	}
}
