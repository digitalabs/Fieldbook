/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package com.efficio.fieldbook.utils.test;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;

import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.util.AppConstants;



/**
 * The Class SettingsUtil.
 */
public class LabelPrintingDataUtil {
    
    //data for TrialInstanceInfo
    private static final String BLOCK_NAME = "block";
    private static final int ENTRY_COUNT = 1;
    private static final boolean HAS_FIELDMAP = true;
    private static final String TRIAL_INSTANCE_NO = "1";
    private static final String FIELD_NAME = "field";
    
    private static final String BLOCK_NAME_2 = "block 2";
    private static final String TRIAL_INSTANCE_NO_2 = "2";
    private static final String FIELD_NAME_2 = "field 2"; 
    
    //data for FieldMapInfo
    private static final int DATASET_ID = 1;
    private static final int BLOCK_NO = 1;
    private static final int COLUMN = 1;
    private static final int ENTRY_NUMBER = 1;
    private static final String GERMPLASM_NAME = "CIMCAL1";
    private static final String PLOT_COORDINATE = "col 1 range 1";
    private static final int RANGE = 1;
    private static final String STUDY_NAME = "labelPrintingTest";
    
    //data for FieldMapDatasetInfo
    private static final String DATASET_NAME = "labelPrintingTest-PLOT";
    
    //data for UserLabelPrinting
    private static final String BARCODE_NEEDED = "1";
    private static final String LABEL_SHEET_SIZE = "1";
    private static final String LABEL_PER_ROW = "3";
    private static final String ROWS_PER_PAGE = "7";
    private static final String LEFT_LABEL_FIELDS = AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt() 
    		+ "," + AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt();
    private static final String RIGHT_LABEL_FIELDS = String.valueOf(AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt());
    private static final String FIRST_BARCODE_FIELD = "1";
    private static final String SECOND_BARCODE_FIELD = "";
    private static final String THIRD_BARCODE_FIELD = "";
    private static final String FILE_NAME = "labelPrintingTest";
    private static final String GENERATE_TYPE = "1";
    private static final String FILE_NAME_DL_PDF = FILE_NAME + ".pdf";
    private static final String FILE_NAME_DL_XLS = FILE_NAME + ".xls";
    private static final String FILE_NAME_DDL_PDF = System.getProperty( "user.home" ) + "/" + FILE_NAME_DL_PDF;
    private static final String FILE_NAME_DDL_XLS = System.getProperty( "user.home" ) + "/" + FILE_NAME_DL_XLS;
    
    //data for StudyTrialInstanceInfo
    private static final String FIELDBOOK_NAME = "test fieldbook";
    
    private static final int FIELDBOOK_ID = 100;
    	
    public static List<FieldMapInfo> createFieldMapInfoList(boolean isTrial) {
    	List<FieldMapInfo> fieldMapInfoList = new ArrayList<FieldMapInfo>();
		FieldMapInfo fieldMapInfo = new FieldMapInfo();
		
		ArrayList<FieldMapDatasetInfo> datasets = createFieldMapDatasetInfo(isTrial);
		fieldMapInfo.setDatasets(datasets);
		fieldMapInfo.setFieldbookId(FIELDBOOK_ID);
		fieldMapInfo.setFieldbookName(FIELDBOOK_NAME);
		fieldMapInfo.setTrial(isTrial);
		fieldMapInfoList.add(fieldMapInfo);
		
		return fieldMapInfoList;
	}
    
    public static UserLabelPrinting createUserLabelPrinting(boolean isPdf) {
    	UserLabelPrinting userLabelPrinting = new UserLabelPrinting();
    	
    	FieldMapInfo fieldMapInfo = new FieldMapInfo();
    	ArrayList<FieldMapDatasetInfo> datasets = createFieldMapDatasetInfo(false);
    	
    	fieldMapInfo.setDatasets(datasets);
    	
    	userLabelPrinting.setBarcodeNeeded(BARCODE_NEEDED);
    	userLabelPrinting.setFilename(FILE_NAME);
    	userLabelPrinting.setFirstBarcodeField(FIRST_BARCODE_FIELD);
    	userLabelPrinting.setSecondBarcodeField(SECOND_BARCODE_FIELD);
    	userLabelPrinting.setThirdBarcodeField(THIRD_BARCODE_FIELD);
    	userLabelPrinting.setGenerateType(GENERATE_TYPE);
    	userLabelPrinting.setLeftSelectedLabelFields(LEFT_LABEL_FIELDS);
    	userLabelPrinting.setRightSelectedLabelFields(RIGHT_LABEL_FIELDS);
    	userLabelPrinting.setNumberOfLabelPerRow(LABEL_PER_ROW);
    	userLabelPrinting.setNumberOfRowsPerPageOfLabel(ROWS_PER_PAGE);
    	userLabelPrinting.setSizeOfLabelSheet(LABEL_SHEET_SIZE);
    	
    	if (isPdf) {
	    	userLabelPrinting.setFilenameDL(FILE_NAME_DL_PDF);
	    	userLabelPrinting.setFilenameDLLocation(FILE_NAME_DDL_PDF);
    	} else {
    		userLabelPrinting.setFilenameDL(FILE_NAME_DL_XLS);
	    	userLabelPrinting.setFilenameDLLocation(FILE_NAME_DDL_XLS);
    	}
    	
    	userLabelPrinting.setFieldMapInfo(fieldMapInfo);
    	
    	return userLabelPrinting;
    }
    
    public static ArrayList<FieldMapDatasetInfo> createFieldMapDatasetInfo(boolean isTrial) {
    	ArrayList<FieldMapDatasetInfo> datasets = new ArrayList<FieldMapDatasetInfo>();
    	
    	FieldMapDatasetInfo dataset = new FieldMapDatasetInfo();
    	
    	ArrayList<FieldMapTrialInstanceInfo> trialInstances =  new ArrayList<FieldMapTrialInstanceInfo>();
    	trialInstances.add(createFieldMapTrialInstanceInfo());
    	if (isTrial) {
    		trialInstances.add(createFieldMapSecondTrialInstanceInfo());
    	}
    	
    	dataset.setDatasetId(DATASET_ID);
    	dataset.setDatasetName(DATASET_NAME);
    	dataset.setTrialInstances(trialInstances);
    	datasets.add(dataset);
    	
    	return datasets;
    }
    
    public static FieldMapTrialInstanceInfo createFieldMapTrialInstanceInfo() {
    	FieldMapTrialInstanceInfo trialInstanceInfo = new FieldMapTrialInstanceInfo();
    	
    	trialInstanceInfo.setBlockName(BLOCK_NAME);
    	trialInstanceInfo.setEntryCount(ENTRY_COUNT);
    	trialInstanceInfo.setFieldMapLabels(createFieldMapLabels());
    	trialInstanceInfo.setHasFieldMap(HAS_FIELDMAP);
    	trialInstanceInfo.setTrialInstanceNo(TRIAL_INSTANCE_NO);
    	trialInstanceInfo.setFieldName(FIELD_NAME);
    	
    	return trialInstanceInfo;
    }
    
    private static FieldMapTrialInstanceInfo createFieldMapSecondTrialInstanceInfo() {
    	FieldMapTrialInstanceInfo trialInstanceInfo = new FieldMapTrialInstanceInfo();
    	
    	trialInstanceInfo.setBlockName(BLOCK_NAME_2);
    	trialInstanceInfo.setEntryCount(ENTRY_COUNT);
    	trialInstanceInfo.setFieldMapLabels(createFieldMapLabels());
    	trialInstanceInfo.setHasFieldMap(HAS_FIELDMAP);
    	trialInstanceInfo.setTrialInstanceNo(TRIAL_INSTANCE_NO_2);
    	trialInstanceInfo.setFieldName(FIELD_NAME_2);
    	
    	return trialInstanceInfo;
    }
    
    private static List<FieldMapLabel> createFieldMapLabels() {
    	List<FieldMapLabel> labels = new ArrayList<FieldMapLabel>();
    	
    	FieldMapLabel label = new FieldMapLabel();
    	label.setBlockNo(BLOCK_NO);
    	label.setColumn(COLUMN);
    	label.setDatasetId(DATASET_ID);
    	label.setEntryNumber(ENTRY_NUMBER);
    	label.setGermplasmName(GERMPLASM_NAME);
    	label.setPlotCoordinate(PLOT_COORDINATE);
    	label.setRange(RANGE);
    	label.setStudyName(STUDY_NAME);
    	labels.add(label);
    	
    	return labels;
    }
    
    public static List<StudyTrialInstanceInfo> createStudyTrialInstanceInfo() {
    	List<StudyTrialInstanceInfo> trialInstances = new ArrayList<StudyTrialInstanceInfo>();
    	
    	StudyTrialInstanceInfo trialInstance = new StudyTrialInstanceInfo(createFieldMapTrialInstanceInfo(), FIELDBOOK_NAME);
    	trialInstances.add(trialInstance);
    	
    	return trialInstances;
    }
}
