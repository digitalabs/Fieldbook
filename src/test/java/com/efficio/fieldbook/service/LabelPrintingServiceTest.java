package com.efficio.fieldbook.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;

import com.lowagie.text.pdf.PdfReader;

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
    	UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(true);
    	String labels = "";
    	String fileName = "";
    	try {
    		fileName = labelPrintingService.generatePDFLabels(trialInstances, userLabelPrinting, baos);
    		
    		PdfReader reader = new PdfReader(fileName);
    		byte[] streamBytes = reader.getPageContent(1);
    	    labels = new String(streamBytes);
    	} catch (LabelPrintingException e) {
    		LOG.error(e.getMessage(), e);
    	} catch (MiddlewareQueryException e) {
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
    	UserLabelPrinting userLabelPrinting = LabelPrintingDataUtil.createUserLabelPrinting(false);
    	String labels = "";
    	String fileName = "";
    	try {
    		fileName = labelPrintingService.generateXlSLabels(trialInstances, userLabelPrinting, baos);
    		org.apache.poi.ss.usermodel.Workbook xlsBook = parseFile(fileName);
			
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
    	} catch (FileNotFoundException e) {
    		LOG.error(e.getMessage(), e);
    	} catch (IOException e) {
    		LOG.error(e.getMessage(), e);
    	} catch (Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    	
    	Assert.assertTrue(areFieldsinGeneratedLabels(labels, false));
    }
    
    private Workbook parseFile(String filename) throws Exception {
		Workbook readWorkbook = null;
		try{
			HSSFWorkbook xlsBook = new HSSFWorkbook(new FileInputStream(new File(filename))); //WorkbookFactory.create(new FileInputStream(new File(filename)));
			readWorkbook = xlsBook;
		}catch(OfficeXmlFileException officeException){
			try {
				XSSFWorkbook xlsxBook = new XSSFWorkbook(new FileInputStream(new File(filename)));
				readWorkbook = xlsxBook;
			} catch (FileNotFoundException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			} 
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return readWorkbook;
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
}
