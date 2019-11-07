/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.utils.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import org.generationcp.commons.constant.AppConstants;
import com.google.common.collect.Maps;

/**
 * The Class SettingsUtil.
 */
public class LabelPrintingDataUtil {

	// data for TrialInstanceInfo
	private static final String BLOCK_NAME = "block";
	private static final int ENTRY_COUNT = 1;
	private static final boolean HAS_FIELDMAP = true;
	private static final String TRIAL_INSTANCE_NO = "1";
	private static final String FIELD_NAME = "field";

	private static final String BLOCK_NAME_2 = "block 2";
	private static final String TRIAL_INSTANCE_NO_2 = "2";
	private static final String FIELD_NAME_2 = "field 2";

	// data for FieldMapInfo
	private static final int DATASET_ID = 1;
	private static final int BLOCK_NO = 1;
	private static final int COLUMN = 1;
	private static final int ENTRY_NUMBER = 1;
	private static final String GERMPLASM_NAME = "CIMCAL1";
	private static final String PLOT_COORDINATE = "col 1 range 1";
	private static final int RANGE = 1;
	private static final String STUDY_NAME = "labelPrintingTest";
	public static final int SAMPLE_EXPERIMENT_NO = -2;
	public static final String SAMPLE_OBS_UNIT_ID = "CROPPRANDOM";
	public static final int SAMPLE_EXPERIMENT_NO_2 = 0;

	// data for FieldMapDatasetInfo
	private static final String DATASET_NAME = "labelPrintingTest-PLOT";

	// data for UserLabelPrinting
	private static final String BARCODE_NEEDED = "1";
	private static final String AUTOMATIC_BARCODE = "1";
	private static final String LABEL_SHEET_SIZE = "1";
	private static final String LABEL_PER_ROW = "3";
	private static final String ROWS_PER_PAGE = "7";
	private static final String LEFT_LABEL_FIELDS =
			AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt() + "," + AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt();
	private static final String RIGHT_LABEL_FIELDS = String.valueOf(AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt());
	private static final String MAIN_LABEL_FIELDS =
			AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt() + "," + AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()
					+ "," + String.valueOf(AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt());
	private static final String FIRST_BARCODE_FIELD = "1";
	private static final String SECOND_BARCODE_FIELD = "";
	private static final String THIRD_BARCODE_FIELD = "";
	public static final String FILE_NAME = "labelPrintingTest";
	public static final String FILE_NAME_DL_PDF = LabelPrintingDataUtil.FILE_NAME + ".pdf";
	public static final String FILE_NAME_DL_XLS = LabelPrintingDataUtil.FILE_NAME + ".xls";
	public static final String FILE_NAME_DL_CSV = LabelPrintingDataUtil.FILE_NAME + ".csv";
	private static final String FILE_NAME_DDL_PDF = System.getProperty("user.home") + "/" + LabelPrintingDataUtil.FILE_NAME_DL_PDF;
	private static final String FILE_NAME_DDL_XLS = System.getProperty("user.home") + "/" + LabelPrintingDataUtil.FILE_NAME_DL_XLS;
	private static final String FILE_NAME_DDL_CSV = System.getProperty("user.home") + "/" + LabelPrintingDataUtil.FILE_NAME_DL_CSV;

	// data for StudyTrialInstanceInfo
	private static final String FIELDBOOK_NAME = "test fieldbook";

	private static final int FIELDBOOK_ID = 100;

	public static List<FieldMapInfo> createFieldMapInfoList() {
		final List<FieldMapInfo> fieldMapInfoList = new ArrayList<FieldMapInfo>();
		final FieldMapInfo fieldMapInfo = new FieldMapInfo();

		final ArrayList<FieldMapDatasetInfo> datasets = LabelPrintingDataUtil.createFieldMapDatasetInfo();
		fieldMapInfo.setDatasets(datasets);
		fieldMapInfo.setFieldbookId(LabelPrintingDataUtil.FIELDBOOK_ID);
		fieldMapInfo.setFieldbookName(LabelPrintingDataUtil.FIELDBOOK_NAME);
		fieldMapInfoList.add(fieldMapInfo);

		return fieldMapInfoList;
	}
	
	public static UserLabelPrinting createUserLabelPrinting(final String type) {
		final UserLabelPrinting userLabelPrinting = new UserLabelPrinting();

		final FieldMapInfo fieldMapInfo = new FieldMapInfo();
		final ArrayList<FieldMapDatasetInfo> datasets = LabelPrintingDataUtil.createFieldMapDatasetInfo();

		fieldMapInfo.setDatasets(datasets);

		userLabelPrinting.setBarcodeNeeded(LabelPrintingDataUtil.BARCODE_NEEDED);
		userLabelPrinting.setBarcodeGeneratedAutomatically(LabelPrintingDataUtil.AUTOMATIC_BARCODE);
		userLabelPrinting.setFilename(LabelPrintingDataUtil.FILE_NAME);
		userLabelPrinting.setFirstBarcodeField(LabelPrintingDataUtil.FIRST_BARCODE_FIELD);
		userLabelPrinting.setSecondBarcodeField(LabelPrintingDataUtil.SECOND_BARCODE_FIELD);
		userLabelPrinting.setThirdBarcodeField(LabelPrintingDataUtil.THIRD_BARCODE_FIELD);
		userLabelPrinting.setGenerateType(type);

		userLabelPrinting.setNumberOfLabelPerRow(LabelPrintingDataUtil.LABEL_PER_ROW);
		userLabelPrinting.setNumberOfRowsPerPageOfLabel(LabelPrintingDataUtil.ROWS_PER_PAGE);
		userLabelPrinting.setSizeOfLabelSheet(LabelPrintingDataUtil.LABEL_SHEET_SIZE);

		if (type.equals(AppConstants.LABEL_PRINTING_PDF.getString())) {
			userLabelPrinting.setLeftSelectedLabelFields(LabelPrintingDataUtil.LEFT_LABEL_FIELDS);
			userLabelPrinting.setRightSelectedLabelFields(LabelPrintingDataUtil.RIGHT_LABEL_FIELDS);
			userLabelPrinting.setMainSelectedLabelFields("");

			userLabelPrinting.setFilenameWithExtension(LabelPrintingDataUtil.FILE_NAME_DL_PDF);
			userLabelPrinting.setFilenameDLLocation(LabelPrintingDataUtil.FILE_NAME_DDL_PDF);
		} else if (type.equals(AppConstants.LABEL_PRINTING_EXCEL.getString())) {
			userLabelPrinting.setLeftSelectedLabelFields("");
			userLabelPrinting.setRightSelectedLabelFields("");
			userLabelPrinting.setMainSelectedLabelFields(LabelPrintingDataUtil.MAIN_LABEL_FIELDS);

			userLabelPrinting.setFilenameWithExtension(LabelPrintingDataUtil.FILE_NAME_DL_XLS);
			userLabelPrinting.setFilenameDLLocation(LabelPrintingDataUtil.FILE_NAME_DDL_XLS);
		} else {
			userLabelPrinting.setLeftSelectedLabelFields("");
			userLabelPrinting.setRightSelectedLabelFields("");
			userLabelPrinting.setMainSelectedLabelFields(LabelPrintingDataUtil.MAIN_LABEL_FIELDS);

			userLabelPrinting.setFilenameWithExtension(LabelPrintingDataUtil.FILE_NAME_DL_CSV);
			userLabelPrinting.setFilenameDLLocation(LabelPrintingDataUtil.FILE_NAME_DDL_CSV);
		}

		userLabelPrinting.setIncludeColumnHeadinginNonPdf(LabelPrintingServiceImpl.INCLUDE_NON_PDF_HEADERS);

		userLabelPrinting.setFieldMapInfo(fieldMapInfo);

		return userLabelPrinting;
	}

	public static ArrayList<FieldMapDatasetInfo> createFieldMapDatasetInfo() {
		final ArrayList<FieldMapDatasetInfo> datasets = new ArrayList<FieldMapDatasetInfo>();

		final FieldMapDatasetInfo dataset = new FieldMapDatasetInfo();

		final ArrayList<FieldMapTrialInstanceInfo> trialInstances = new ArrayList<FieldMapTrialInstanceInfo>();
		trialInstances.add(LabelPrintingDataUtil.createFieldMapTrialInstanceInfo());
		trialInstances.add(LabelPrintingDataUtil.createFieldMapSecondTrialInstanceInfo());

		dataset.setDatasetId(LabelPrintingDataUtil.DATASET_ID);
		dataset.setDatasetName(LabelPrintingDataUtil.DATASET_NAME);
		dataset.setTrialInstances(trialInstances);
		datasets.add(dataset);

		return datasets;
	}

	public static FieldMapTrialInstanceInfo createFieldMapTrialInstanceInfo() {
		final FieldMapTrialInstanceInfo trialInstanceInfo = new FieldMapTrialInstanceInfo();

		trialInstanceInfo.setBlockName(LabelPrintingDataUtil.BLOCK_NAME);
		trialInstanceInfo.setEntryCount(LabelPrintingDataUtil.ENTRY_COUNT);
		trialInstanceInfo.setFieldMapLabels(LabelPrintingDataUtil.createFieldMapLabels());
		trialInstanceInfo.setHasFieldMap(LabelPrintingDataUtil.HAS_FIELDMAP);
		trialInstanceInfo.setTrialInstanceNo(LabelPrintingDataUtil.TRIAL_INSTANCE_NO);
		trialInstanceInfo.setFieldName(LabelPrintingDataUtil.FIELD_NAME);

		return trialInstanceInfo;
	}


	private static FieldMapTrialInstanceInfo createFieldMapSecondTrialInstanceInfo() {
		final FieldMapTrialInstanceInfo trialInstanceInfo = new FieldMapTrialInstanceInfo();

		trialInstanceInfo.setBlockName(LabelPrintingDataUtil.BLOCK_NAME_2);
		trialInstanceInfo.setEntryCount(LabelPrintingDataUtil.ENTRY_COUNT);
		trialInstanceInfo.setFieldMapLabels(LabelPrintingDataUtil.createFieldMapLabels());
		trialInstanceInfo.setHasFieldMap(LabelPrintingDataUtil.HAS_FIELDMAP);
		trialInstanceInfo.setTrialInstanceNo(LabelPrintingDataUtil.TRIAL_INSTANCE_NO_2);
		trialInstanceInfo.setFieldName(LabelPrintingDataUtil.FIELD_NAME_2);

		return trialInstanceInfo;
	}

	public static FieldMapTrialInstanceInfo createFieldMapThirdTrialInstanceInfo() {
		final FieldMapTrialInstanceInfo trialInstanceInfo = new FieldMapTrialInstanceInfo();

		trialInstanceInfo.setBlockName(LabelPrintingDataUtil.BLOCK_NAME);
		trialInstanceInfo.setEntryCount(LabelPrintingDataUtil.ENTRY_COUNT);
		trialInstanceInfo.setFieldMapLabels(LabelPrintingDataUtil.createFieldMapSecondLabels());
		trialInstanceInfo.setHasFieldMap(LabelPrintingDataUtil.HAS_FIELDMAP);
		trialInstanceInfo.setTrialInstanceNo(LabelPrintingDataUtil.TRIAL_INSTANCE_NO);
		trialInstanceInfo.setFieldName(LabelPrintingDataUtil.FIELD_NAME);

		return trialInstanceInfo;
	}

	private static List<FieldMapLabel> createFieldMapLabels() {
		final List<FieldMapLabel> labels = new ArrayList<FieldMapLabel>();

		final FieldMapLabel label = new FieldMapLabel();
		label.setBlockNo(LabelPrintingDataUtil.BLOCK_NO);
		label.setColumn(LabelPrintingDataUtil.COLUMN);
		label.setDatasetId(LabelPrintingDataUtil.DATASET_ID);
		label.setEntryNumber(LabelPrintingDataUtil.ENTRY_NUMBER);
		label.setGermplasmName(LabelPrintingDataUtil.GERMPLASM_NAME);
		label.setPlotCoordinate(LabelPrintingDataUtil.PLOT_COORDINATE);
		label.setRange(LabelPrintingDataUtil.RANGE);
		label.setStudyName(LabelPrintingDataUtil.STUDY_NAME);
		label.setExperimentId(LabelPrintingDataUtil.SAMPLE_EXPERIMENT_NO);
		label.setObsUnitId(LabelPrintingDataUtil.SAMPLE_OBS_UNIT_ID);
		labels.add(label);

		return labels;
	}

	private static List<FieldMapLabel> createFieldMapSecondLabels() {
		final List<FieldMapLabel> labels = new ArrayList<FieldMapLabel>();

		final FieldMapLabel label = new FieldMapLabel();
		label.setBlockNo(LabelPrintingDataUtil.BLOCK_NO);
		label.setColumn(LabelPrintingDataUtil.COLUMN);
		label.setDatasetId(LabelPrintingDataUtil.DATASET_ID);
		label.setEntryNumber(LabelPrintingDataUtil.ENTRY_NUMBER);
		label.setGermplasmName(LabelPrintingDataUtil.GERMPLASM_NAME);
		label.setPlotCoordinate(LabelPrintingDataUtil.PLOT_COORDINATE);
		label.setRange(LabelPrintingDataUtil.RANGE);
		label.setStudyName(LabelPrintingDataUtil.STUDY_NAME);
		label.setExperimentId(LabelPrintingDataUtil.SAMPLE_EXPERIMENT_NO_2);
		label.setObsUnitId(LabelPrintingDataUtil.SAMPLE_OBS_UNIT_ID);
		labels.add(label);

		return labels;
	}

	public static List<StudyTrialInstanceInfo> createStudyTrialInstanceInfo() {
		final List<StudyTrialInstanceInfo> trialInstances = new ArrayList<StudyTrialInstanceInfo>();

		final StudyTrialInstanceInfo trialInstance =
				new StudyTrialInstanceInfo(LabelPrintingDataUtil.createFieldMapTrialInstanceInfo(), LabelPrintingDataUtil.FIELDBOOK_NAME);
		final StudyTrialInstanceInfo trialInstance2 = new StudyTrialInstanceInfo(LabelPrintingDataUtil.createFieldMapSecondTrialInstanceInfo(), LabelPrintingDataUtil.FIELDBOOK_NAME);

		trialInstances.add(trialInstance);
		trialInstances.add(trialInstance2);

		return trialInstances;
	}

	public static Map<Integer, String> createLabelHeadersForStudyStock() {
		final Map<Integer, String> labelHeaders = Maps.newHashMap();
		labelHeaders.put(8170, "TRIAL_INSTANCE");
		labelHeaders.put(8210, "REP_NO");
		labelHeaders.put(1718,"TOTAL");
		labelHeaders.put(1719, "UNITS");
		labelHeaders.put(1722, "AMOUNT");
		return labelHeaders;
	}
}
