package com.efficio.fieldbook.service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.csvreader.CsvReader;
import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.utils.test.ExcelImportUtil;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;
import com.lowagie.text.pdf.PdfReader;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class LabelPrintingServiceTest extends AbstractBaseIntegrationTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingServiceTest.class);
    
    private static int[] fieldMapLabels = {AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt(), 
			AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt(), 
			AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()};
    
    private static final String PLOT_COORDINATES = "Plot Coordinates";

    @Resource
    private LabelPrintingService labelPrintingService;
    
    @Resource
    private ResourceBundleMessageSource messageSource;
        
    @Test
    public void testGetAvailableLabelFieldsFromTrialWithoutFieldMap() {
    	Locale locale = new Locale("en", "US");
    	List<LabelFields> labels = labelPrintingService.getAvailableLabelFields(true, false, locale);
    	Assert.assertFalse(areFieldsInLabelList(labels));
    }
    
    @Test
    public void testGetAvailableLabelFieldsFromTrialWithFieldMap() {
    	Locale locale = new Locale("en", "US");
    	List<LabelFields> labels = labelPrintingService.getAvailableLabelFields(true, true, locale);
    	Assert.assertTrue(areFieldsInLabelList(labels));
    }
    
    @Test
    public void testGetAvailableLabelFieldsFromNurseryWithoutFieldMap() {
    	Locale locale = new Locale("en", "US");
    	List<LabelFields> labels = labelPrintingService.getAvailableLabelFields(false, false, locale);
    	Assert.assertFalse(areFieldsInLabelList(labels));
    }
    
    @Test
    public void testGetAvailableLabelFieldsFromNurseryWithFieldMap() {
    	Locale locale = new Locale("en", "US");
    	List<LabelFields> labels = labelPrintingService.getAvailableLabelFields(false, true, locale);
    	Assert.assertTrue(areFieldsInLabelList(labels));
    }
    
    @Test
    public void testGetAvailableLabelFieldsFromFieldMap() {
    	Locale locale = new Locale("en", "US");
    	List<LabelFields> labels = labelPrintingService.getAvailableLabelFields(false, true, locale);
    	Assert.assertTrue(areFieldsInLabelList(labels));
    }
    
    private boolean areFieldsInLabelList(List<LabelFields> labels) {
    	int fieldMapLabelCount = 0;
    	
    	if (labels != null) {
    		for (LabelFields label : labels) {
    			for (int fieldMapLabel : fieldMapLabels) {
    				if (label.getId() == fieldMapLabel) {
    					fieldMapLabelCount++;
    				}
    			}
    		}
    		
    		if (fieldMapLabelCount == fieldMapLabels.length) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    	return false;
    }
    
    @Test
    public void testFieldmapFieldsInGeneratedPdf() {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
    	UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
    	String labels = "";
    	String fileName = "";
    	try {
    		fileName = labelPrintingService.generatePDFLabels(trialInstances, userLabelPrinting, baos);
    		
    		PdfReader reader = new PdfReader(fileName);
    		byte[] streamBytes = reader.getPageContent(1);
    	    labels = new String(streamBytes);
    	} catch (LabelPrintingException e) {
    		LOG.error(e.getMessage(), e);
    	} catch (FileNotFoundException e) {
    		LOG.error(e.getMessage(), e);
    	} catch (IOException e) {
    		LOG.error(e.getMessage(), e);
    	}
    	
    	Assert.assertTrue(areFieldsinGeneratedLabels(labels, true));
    }
    
    @Test
    public void testFieldmapFieldsInGeneratedXls() {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
    	UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_EXCEL.getString());
    	String labels = "";
    	String fileName = "";
    	try {
    		fileName = labelPrintingService.generateXlSLabels(trialInstances, userLabelPrinting, baos);
    		org.apache.poi.ss.usermodel.Workbook xlsBook = ExcelImportUtil.parseFile(fileName);
			
    		Sheet sheet = xlsBook.getSheetAt(0);
    		
    		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
    			Row row = sheet.getRow(i);
                if (row != null) {
                	for (int j = 0; j <= row.getLastCellNum(); j++) {
                		Cell cell = row.getCell(j);
                		
                		if (cell != null && cell.getStringCellValue() != null) {
                			labels = labels + " " + cell.getStringCellValue();
                		}
                	}
                }
    		}
    		
    	} catch (MiddlewareQueryException e) {
    		LOG.error(e.getMessage(), e);
    	} catch (Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	
    	Assert.assertTrue(areFieldsinGeneratedLabels(labels, false));
    }
    
    
    
    private boolean areFieldsinGeneratedLabels(String labels, boolean isPdf) {
    	boolean areFieldsInLabels = true;
    	Locale locale = new Locale("en", "US");
    	
    	for (int label : fieldMapLabels) {
    		String fieldName = "";
    		if (label == AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
    			fieldName = messageSource.getMessage(
            			"label.printing.available.fields.block.name", null, locale);
    		} else if (label == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
    			fieldName = messageSource.getMessage(
            			"label.printing.available.fields.field.name", null, locale);
    		} else if (label == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
    			fieldName = PLOT_COORDINATES;
    		}
    		
    		String[] field = labels.split(fieldName);
    		if (field.length <= 1) {
    			areFieldsInLabels = false;
    			break;
    		}
    	}
    	
    	return areFieldsInLabels;
    }
        
    @Test
    public void testFieldMapPropertiesOfNurseryWithoutFieldMap() {
    	UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
    	FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(false).get(0);
    	setFieldmapProperties(fieldMapInfoDetail, false, false);
    	boolean hasFieldMap = labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);
    	
    	Assert.assertFalse(hasFieldMap);
    	Assert.assertFalse(userLabelPrinting.isFieldMapsExisting());
    }
    
    @Test
    public void testFieldMapPropertiesOfNurseryWithFieldMap() {
    	UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
    	FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(false).get(0);
    	setFieldmapProperties(fieldMapInfoDetail, true, false);
    	boolean hasFieldMap = labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);
    	
    	Assert.assertTrue(hasFieldMap);
    	Assert.assertTrue(userLabelPrinting.isFieldMapsExisting());
    }
    
    @Test
    public void testFieldMapPropertiesOfTrialWithoutFieldMaps() {
    	UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
    	FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(true).get(0);
    	setFieldmapProperties(fieldMapInfoDetail, false, false);
    	boolean hasFieldMap = labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);
    	
    	Assert.assertFalse(hasFieldMap);
    	Assert.assertFalse(userLabelPrinting.isFieldMapsExisting());
    }
    
    @Test
    public void testFieldMapPropertiesOfTrialWithOneFieldMap() {
    	UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
    	FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(true).get(0);
    	setFieldmapProperties(fieldMapInfoDetail, false, true);
    	boolean hasFieldMap = labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);
    	
    	Assert.assertTrue(hasFieldMap);
    	Assert.assertFalse(userLabelPrinting.isFieldMapsExisting());
    }
    
    @Test
    public void testFieldMapPropertiesOfTrialWithFieldMaps() {
    	UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
    	FieldMapInfo fieldMapInfoDetail = LabelPrintingDataUtil.createFieldMapInfoList(true).get(0);
    	setFieldmapProperties(fieldMapInfoDetail, true, false);
    	boolean hasFieldMap = labelPrintingService.checkAndSetFieldmapProperties(userLabelPrinting, fieldMapInfoDetail);
    	
    	Assert.assertTrue(hasFieldMap);
    	Assert.assertTrue(userLabelPrinting.isFieldMapsExisting());
    }
    
    private void setFieldmapProperties(FieldMapInfo fieldMapInfoDetail, boolean hasFieldMap, boolean hasOneTrialInstanceWithFieldMap) {
    	//set column to null and hasFieldMap to false if study has no fieldmap at all
    	//else, don't change the values
    	for (FieldMapDatasetInfo dataset : fieldMapInfoDetail.getDatasetsWithFieldMap()) {
    		int ctr = 0;
    		for (FieldMapTrialInstanceInfo trialInstance : dataset.getTrialInstances()) {
    			if ((ctr == 0 && hasOneTrialInstanceWithFieldMap) || !hasOneTrialInstanceWithFieldMap) {
    				trialInstance.setHasFieldMap(hasFieldMap);
        			if (!hasFieldMap) {
    	    			for (FieldMapLabel label : trialInstance.getFieldMapLabels()) {
    	    				label.setColumn(null);
    	    			}
        			}
    			}
    			ctr++;
    		}
    	}
    }
    
    @Test
    public void testGenerationOfPdfLabels() {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
    	UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_PDF.getString());
    	String fileName = "";
    	try {
    		fileName = labelPrintingService.generatePDFLabels(trialInstances, userLabelPrinting, baos);
    		
    		PdfReader reader = new PdfReader(fileName);
    		Assert.assertNotNull("Expected a new pdf file was created but found none.", reader);
    		byte[] streamBytes = reader.getPageContent(1);
    		Assert.assertNotNull("Expected a file with content but found none.", streamBytes);
    	} catch (LabelPrintingException e) {
    		LOG.error(e.getMessage(), e);
    		Assert.fail("Error encountered while generating pdf file.");
    	} catch (FileNotFoundException e) {
    		LOG.error(e.getMessage(), e);
    		Assert.fail("File not found.");
    	} catch (IOException e) {
    		LOG.error(e.getMessage(), e);
    		Assert.fail("Error encountered while reading file.");
    	}
    }
    
    @Test
    public void testGenerationOfXlsLabels() {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
    	UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_EXCEL.getString());
    	String fileName = "";
    	try {
    		fileName = labelPrintingService.generateXlSLabels(trialInstances, userLabelPrinting, baos);
    		org.apache.poi.ss.usermodel.Workbook xlsBook = ExcelImportUtil.parseFile(fileName);
			
    		Assert.assertNotNull("Expected a new workbook file was created but found none.", xlsBook);
    		
    		Sheet sheet = xlsBook.getSheetAt(0);
    		
    		Assert.assertNotNull("Expecting an xls file with 1 sheet but found none.", sheet);
    		
    		Assert.assertTrue("Expected at least one row but got 0", sheet.getLastRowNum() > 0);
    	} catch (MiddlewareQueryException e) {
    		LOG.error(e.getMessage(), e);
    		Assert.fail("Encountered error while exporting to xls.");
    	} catch (Exception e) {
    		LOG.error(e.getMessage(), e);
    		Assert.fail("Excountered error while reading xls file.");
    	}
    }
    
    @Test
    public void testGenerationOfCsvLabels() {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	List<StudyTrialInstanceInfo> trialInstances = LabelPrintingDataUtil.createStudyTrialInstanceInfo();
    	UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(AppConstants.LABEL_PRINTING_CSV.getString());
    	String fileName = "";
    	try {
    		fileName = labelPrintingService.generateCSVLabels(trialInstances, userLabelPrinting, baos);
    		
    		CsvReader csvReader = new CsvReader(fileName);
    		
    		csvReader.readHeaders();
            String[] headers = csvReader.getHeaders();
			
    		Assert.assertNotNull("Expected a new csv file was created but found none.", csvReader);    		
    		Assert.assertNotNull("Expecting a csv file with headers but no header was found.", headers);
    		Assert.assertTrue("Expected all headers but only got " + headers.length, areHeadersEqual(headers, userLabelPrinting));
    		Assert.assertTrue("Expected " + " rows but got " + " instead.", areRowsEqual(csvReader, headers, userLabelPrinting));
    	} catch (IOException e) {
    		LOG.error(e.getMessage(), e);
    		Assert.fail("Excountered error while exporting/reading csv file.");
    	}
    }
    
    @Test
    public void testGenerateAddedInformationField(){
    	LabelPrintingServiceImpl labelPrintingServiceImpl = new LabelPrintingServiceImpl();
    	FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = new FieldMapTrialInstanceInfo();
    	StudyTrialInstanceInfo trialInstance = new StudyTrialInstanceInfo(fieldMapTrialInstanceInfo, "TestStudy");
    	String barCode = "testBarcode";
    	fieldMapTrialInstanceInfo.setLocationName("Loc1");
    	fieldMapTrialInstanceInfo.setBlockName("Block1");
    	fieldMapTrialInstanceInfo.setFieldName("Field1");
    	trialInstance.setFieldbookName("Fieldbook1");
    	fieldMapTrialInstanceInfo.setTrialInstanceNo("1");

    	Map<String,String> dataResults = labelPrintingServiceImpl.generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, barCode);
    	Assert.assertEquals("Should have the same location name", fieldMapTrialInstanceInfo.getLocationName(), dataResults.get("locationName"));
    	Assert.assertEquals("Should have the same Block name", fieldMapTrialInstanceInfo.getBlockName(), dataResults.get("blockName"));
    	Assert.assertEquals("Should have the same Field name", fieldMapTrialInstanceInfo.getFieldName(), dataResults.get("fieldName"));
    	Assert.assertEquals("Should have the same Fieldbook name", trialInstance.getFieldbookName(), dataResults.get("selectedName"));
    	Assert.assertEquals("Should have the same Trial Instance Number", fieldMapTrialInstanceInfo.getTrialInstanceNo(), dataResults.get("trialInstanceNumber"));
    	Assert.assertEquals("Should have the same Barcode string", barCode, dataResults.get("barcode"));
    }
    
    @Test
    public void testAppendBarcodeIfBarcodeNeededTrue(){
    	LabelPrintingServiceImpl labelPrintingServiceImpl = new LabelPrintingServiceImpl();
    	String mainSelectedFields = "";
    	String newFields = labelPrintingServiceImpl.appendBarcode(true, mainSelectedFields);
    	Assert.assertEquals("Should have the id of the Barcode fields", ","+AppConstants.AVAILABLE_LABEL_BARCODE.getInt(), newFields);
    }
    
    @Test
    public void testAppendBarcodeIfBarcodeNeededFalse(){
    	LabelPrintingServiceImpl labelPrintingServiceImpl = new LabelPrintingServiceImpl();
    	String mainSelectedFields = "";
    	String newFields = labelPrintingServiceImpl.appendBarcode(false, mainSelectedFields);
    	Assert.assertEquals("Should have the NO id of the Barcode fields", "", newFields);
    }
    
    @Test
    public void testPrintHeaderFieldsIfIncludeHeader(){
    	LabelPrintingServiceImpl labelPrintingServiceImpl = new LabelPrintingServiceImpl();
    	labelPrintingServiceImpl.setMessageSource(messageSource);
    	Row row = Mockito.mock(Row.class);
    	Cell cell = Mockito.mock(Cell.class);
    	Mockito.doReturn(cell).when(row).createCell(Mockito.anyInt());
		List<Integer> selectedFieldIDs = new ArrayList<>();
				selectedFieldIDs.add(1);
				selectedFieldIDs.add(2);
				selectedFieldIDs.add(3);
    	labelPrintingServiceImpl.printHeaderFields(true, selectedFieldIDs, row, 0, Mockito.mock(CellStyle.class));
    	Mockito.verify(cell, Mockito.times(3)).setCellValue(Mockito.anyString());
    	
    }

    @Test
    public void testPrintHeaderFieldsIfNotIncludeHeader(){
    	LabelPrintingServiceImpl labelPrintingServiceImpl = new LabelPrintingServiceImpl();
    	labelPrintingServiceImpl.setMessageSource(messageSource);
    	Row row = Mockito.mock(Row.class);
    	Cell cell = Mockito.mock(Cell.class);
    	Mockito.doReturn(cell).when(row).createCell(Mockito.anyInt());
		List<Integer> selectedFieldIDs = new ArrayList<>();
		selectedFieldIDs.add(1);
		selectedFieldIDs.add(2);
		selectedFieldIDs.add(3);
    	labelPrintingServiceImpl.printHeaderFields(false, selectedFieldIDs, row, 0, Mockito.mock(CellStyle.class));
    	Mockito.verify(cell, Mockito.never()).setCellValue(Mockito.anyString());
    	
    }
    
	private boolean areRowsEqual(CsvReader csvReader, String[] headers, UserLabelPrinting userLabelPrinting) {
		try {
			int rowNum = 0, rowNum2 = 0;
			while (csvReader.readRecord()) {
				rowNum++;
			}
			
			for (FieldMapDatasetInfo dataset : userLabelPrinting.getFieldMapInfo().getDatasets()) {
				for (FieldMapTrialInstanceInfo trialInstance : dataset.getTrialInstancesWithFieldMap()) {
					rowNum2 += trialInstance.getFieldMapLabels().size();
				}
			}
			return rowNum == rowNum2;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			Assert.fail("Error encountered while reading the file.");
			return false;
		}
	}

	private boolean areHeadersEqual(String[] headers, UserLabelPrinting userLabelPrinting) {
		int headerLength = userLabelPrinting.getLeftSelectedLabelFields().split(",").length + 
				userLabelPrinting.getRightSelectedLabelFields().split(",").length;
		return headers.length == headerLength;
	}

	@Test
	public void testPopulateUserSpecifiedLabelFieldsForNurseryEnvironmentDataOnly() {
		String testDesigValue = "123";
		LabelPrintingService printingService = new LabelPrintingServiceImpl();

		String testSelectedFields = Integer.toString(TermId.DESIG.getId()) + "," + Integer.toString(TermId.ENTRY_NO.getId());


		List<FieldMapTrialInstanceInfo> input = new ArrayList<>();
		input.add(LabelPrintingDataUtil.createFieldMapTrialInstanceInfo());

		printingService.populateUserSpecifiedLabelFields(input, setupTestWorkbook(), testSelectedFields, false);

		assertEquals(testDesigValue, input.get(0).getFieldMapLabel(LabelPrintingDataUtil.SAMPLE_EXPERIMENT_NO).getUserFields().get(TermId.DESIG.getId()));
		assertEquals("1", input.get(0).getFieldMapLabel(LabelPrintingDataUtil.SAMPLE_EXPERIMENT_NO).getUserFields().get(TermId.ENTRY_NO.getId()));
	}

	protected Workbook setupTestWorkbook() {
		Workbook workbook = mock(Workbook.class);
		doReturn(new ArrayList<MeasurementVariable>()).when(workbook).getStudyConditions();
		doReturn(new ArrayList<MeasurementVariable>()).when(workbook).getFactors();

		// prepare measurement rows simulating experiment data
		List<MeasurementRow> sampleData = new ArrayList<>();

		// add a row with measurement data for the DESIG and ENTRY_NO terms
		MeasurementRow row = new MeasurementRow();
		// experiment ID here is set to be the same as the one used when creating the sample instance data, since they need to correlate.
		row.setExperimentId(LabelPrintingDataUtil.SAMPLE_EXPERIMENT_NO);

		List<MeasurementData> dataList = new ArrayList<>();
		MeasurementData data = new MeasurementData();
		MeasurementVariable var = new MeasurementVariable();
		var.setTermId(TermId.DESIG.getId());
		data.setMeasurementVariable(var);
		data.setValue("123");
		dataList.add(data);

		data = new MeasurementData();
		var = new MeasurementVariable();
		var.setTermId(TermId.ENTRY_NO.getId());
		data.setMeasurementVariable(var);
		data.setValue("1");
		dataList.add(data);

		row.setDataList(dataList);
		sampleData.add(row);
		doReturn(sampleData).when(workbook).getObservations();

		return workbook;
	}
}
