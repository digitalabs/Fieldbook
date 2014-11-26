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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Location;

import com.efficio.fieldbook.web.util.AppConstants;

/**
 * The Class SettingsUtil.
 */
public class WorkbookDataUtil {

	public static final String FILE_NAME = "sample_file";
	public static final int NUMBER_OF_OBSERVATIONS = 12000;

	private static final String NURSERY_NAME = "Nursery_";
	private static final String TRIAL_NAME = "Trial_";

	// STUDY DETAILS
	private static final String TITLE = "Nursery Workbook";
	private static final String OBJECTIVE = "To evaluate the Population 114";
	private static final String START_DATE = "20130805";
	private static final String END_DATE = "20130805";
	private static final int FOLDER_ID = 1;

	// PROPERTIES
	private static final String PERSON = "PERSON";
	public static final String TRIAL_INSTANCE = "TRIAL INSTANCE";
	private static final String LOCATION = "LOCATION";
	private static final String GERMPLASM_ENTRY = "GERMPLASM ENTRY";
	private static final String GERMPLASM_ID = "GERMPLASM ID";
	private static final String CROSS_HISTORY = "CROSS HISTORY";
	private static final String SEED_SOURCE = "SEED SOURCE";
	private static final String FIELD_PLOT = "FIELD PLOT";
	private static final String REPLICATION = "REPLICATION";
	private static final String REPLICATION_FACTOR = "REPLICATION FACTOR";
	private static final String BLOCKING_FACTOR = "BLOCKING FACTOR";
	private static final String PLANTING_DATE = "Planting Date";
	private static final String SOIL_ACIDITY = "Soil Acidity";
	private static final String YIELD = "Yield";
	private static final String PLANTS_SELECTED = "Plants Selected";

	// SCALES
	private static final String DBCV = "DBCV";
	private static final String DBID = "DBID";
	public static final String NUMBER = "NUMBER";
	private static final String PEDIGREE_STRING = "PEDIGREE STRING";
	private static final String NAME = "NAME";
	private static final String NESTED_NUMBER = "NESTED NUMBER";
	private static final String DATE = "Date (yyyymmdd)";
	private static final String KG_HA = "kg/ha";
	private static final String PH = "ph";

	// METHODS
	private static final String ASSIGNED = "ASSIGNED";
	public static final String ENUMERATED = "ENUMERATED";
	private static final String CONDUCTED = "CONDUCTED";
	private static final String APPLIED = "APPLIED";
	private static final String SELECTED = "SELECTED";
	private static final String PH_METER = "PH Meter";
	private static final String DRY_AND_WEIGH = "Dry and Weigh";
	private static final String COUNTED = "Counted";

	// LABELS
	private static final String STUDY = "STUDY";
	public static final String TRIAL = "TRIAL";
	public static final String ENTRY = "ENTRY";
	private static final String PLOT = "PLOT";

	// DATA TYPES
	private static final String CHAR = "C";
	public static final String NUMERIC = "N";

	// FACTORS
	private static final String GID = "GID";
	private static final String DESIG = "DESIG";
	private static final String CROSS = "CROSS";
	private static final String SOURCE = "SOURCE";
	private static final String BLOCK = "BLOCK";
	private static final String REP = "REP";

	// CONSTANTS
	private static final int DATE_SEEDED_ID = 20810;
	private static final int SOILPH_ID = 22531;

	// VARIATES
	private static final String GYLD = "GYLD";
	private static final String CHALK_PCT = "CHALK_PCT";
	private static final int GYLD_ID = 18000;
	private static final int CHALK_PCT_ID = 22768;

	// CONDITIONS
	private static final String PI_NAME = "PI Name";
	private static final String PI_ID = "PI Id";
	private static final String COOPERATOR = "Cooperator";
	private static final String COOPERATOR_ID = "Cooperator Id";
	private static final String SITE = "Site";
	public static final String SITE_ID = "Site Id";

	private static final String GERMPLASM_NAME = "TIANDOUGOU-9";
	private static final String NUMERIC_VALUE = "1";
	
	public static final Integer LOCATION_ID_1 = 1;
	public static final Integer LOCATION_ID_2 = 2;
	private static final Integer LTYPE = 1;
	private static final Integer NLLP = 1;
	public static final String LNAME = "Location";
	private static final String LABBR = "LOC2";
	private static final Integer SNL3ID = 1;
	private static final Integer SNL2ID = 1;
	private static final Integer SNL1ID = 1;
	private static final Integer CNTRYID = 1;
	private static final Integer LRPLCE = 1;

	private static Workbook workbook;

	public static Workbook getTestWorkbook(int noOfObservations, StudyType studyType) {
		if (WorkbookDataUtil.workbook == null) {
			WorkbookDataUtil.createTestWorkbook(noOfObservations, studyType);
		}
		return WorkbookDataUtil.workbook;
	}
	
	public static Workbook getTestWorkbookForTrial(int noOfObservations, int noOfInstance) {
		if (WorkbookDataUtil.workbook == null) {
			WorkbookDataUtil.createTestWorkbook(noOfObservations, StudyType.T, noOfInstance);
		}
		return WorkbookDataUtil.workbook;
	}

	public static void setTestWorkbook(Workbook workbook) {
		WorkbookDataUtil.workbook = workbook;
	}

	private static void createTestWorkbook(int noOfObservations, StudyType studyType) {
		createTestWorkbook(noOfObservations, studyType, 2);
	}
	
	private static void createTestWorkbook(int noOfObservations, StudyType studyType,
			int noOfInstance) {
		WorkbookDataUtil.workbook = new Workbook();

		WorkbookDataUtil.createStudyDetails(studyType);
		WorkbookDataUtil.createConditions();
		WorkbookDataUtil.createFactors();
		WorkbookDataUtil.createConstants();
		WorkbookDataUtil.createVariates();
		WorkbookDataUtil.createObservations(noOfObservations, studyType.equals(StudyType.N) ? 1 : noOfInstance);
		WorkbookDataUtil.createTrialObservations(studyType.equals(StudyType.N) ? 1 : noOfInstance);
		workbook.setMeasurementDatesetId(-2);
		workbook.setTrialDatasetId(-3);
	}

	private static void createStudyDetails(StudyType studyType) {
		StudyDetails details = new StudyDetails();
		details.setStudyName((studyType.equals(StudyType.N) ? NURSERY_NAME : TRIAL_NAME) + new Random().nextInt(10000));
		details.setTitle(WorkbookDataUtil.TITLE);
		details.setObjective(WorkbookDataUtil.OBJECTIVE);
		details.setStartDate(WorkbookDataUtil.START_DATE);
		details.setEndDate(WorkbookDataUtil.END_DATE);
		details.setParentFolderId(WorkbookDataUtil.FOLDER_ID);
		details.setStudyType(studyType);
		details.setId(-1);

		WorkbookDataUtil.workbook.setStudyDetails(details);
	}

	private static void createConditions() {
		//Create measurement variables and set its dataTypeId
		List<MeasurementVariable> conditions = new ArrayList<MeasurementVariable>();

		MeasurementVariable variable = new MeasurementVariable(
				TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL", "TRIAL NUMBER",
				WorkbookDataUtil.NUMBER, WorkbookDataUtil.ENUMERATED,
				WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC, "",
				WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		variable = new MeasurementVariable(TermId.PI_NAME.getId(), "PI Name",
				"Name of Principal Investigator", WorkbookDataUtil.DBCV, WorkbookDataUtil.ASSIGNED,
				WorkbookDataUtil.PERSON, WorkbookDataUtil.CHAR, "", WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		variable = new MeasurementVariable(TermId.PI_ID.getId(), "PI ID",
				"ID of Principal Investigator", WorkbookDataUtil.DBID, WorkbookDataUtil.ASSIGNED,
				WorkbookDataUtil.PERSON, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
				WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		conditions.add(variable);

		variable = new MeasurementVariable(AppConstants.COOPERATOR_NAME.getInt(), "COOPERATOR",
				"COOPERATOR NAME", WorkbookDataUtil.DBCV, WorkbookDataUtil.CONDUCTED,
				WorkbookDataUtil.PERSON, WorkbookDataUtil.CHAR, "John Smith",
				WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		variable = new MeasurementVariable(AppConstants.COOPERATOR_ID.getInt(), "COOPERATOR ID",
				"COOPERATOR ID", WorkbookDataUtil.DBID, WorkbookDataUtil.CONDUCTED,
				WorkbookDataUtil.PERSON, WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE,
				WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		conditions.add(variable);

		variable = new MeasurementVariable(TermId.TRIAL_LOCATION.getId(), "SITE", "TRIAL SITE NAME",
				WorkbookDataUtil.DBCV, WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION,
				WorkbookDataUtil.CHAR, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		conditions.add(variable);

		variable = new MeasurementVariable(TermId.LOCATION_ID.getId(), "SITE ID", "TRIAL SITE ID",
				WorkbookDataUtil.DBID, WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION,
				WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		conditions.add(variable);

		WorkbookDataUtil.workbook.setConditions(conditions);
	}
	
	public static void addCheckConditions() {
		MeasurementVariable variable = new MeasurementVariable(TermId.CHECK_START.getId(), "CHECK_START", "CHECK_START",
				WorkbookDataUtil.DBID, WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION,
				WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		WorkbookDataUtil.workbook.getConditions().add(variable);
		
		variable = new MeasurementVariable(TermId.CHECK_INTERVAL.getId(), "CHECK_INTERVAL", "CHECK_INTERVAL",
				WorkbookDataUtil.DBID, WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION,
				WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		WorkbookDataUtil.workbook.getConditions().add(variable);
		
		variable = new MeasurementVariable(TermId.CHECK_PLAN.getId(), "CHECK_PLAN", "CHECK_PLAN",
				WorkbookDataUtil.DBID, WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.LOCATION,
				WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CATEGORICAL_VARIABLE.getId());
		WorkbookDataUtil.workbook.getConditions().add(variable);
	}

	private static void createFactors() {
		//Create measurement variables and set its dataTypeId
		List<MeasurementVariable> factors = new ArrayList<MeasurementVariable>();
		
		// Entry Factors
		MeasurementVariable variable = new MeasurementVariable(TermId.ENTRY_NO.getId(),
				WorkbookDataUtil.ENTRY, "The germplasm entry number", WorkbookDataUtil.NUMBER,
				WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.GERMPLASM_ENTRY,
				WorkbookDataUtil.NUMERIC, WorkbookDataUtil.STUDY, WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		factors.add(variable);

		variable = new MeasurementVariable(TermId.GID.getId(), WorkbookDataUtil.GID,
				"The GID of the germplasm", WorkbookDataUtil.DBID, WorkbookDataUtil.ASSIGNED,
				WorkbookDataUtil.GERMPLASM_ID, WorkbookDataUtil.NUMERIC,
				WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		factors.add(variable);

		variable = new MeasurementVariable(TermId.DESIG.getId(), WorkbookDataUtil.DESIG,
				"The name of the germplasm", WorkbookDataUtil.DBCV, WorkbookDataUtil.ASSIGNED,
				WorkbookDataUtil.GERMPLASM_ID, WorkbookDataUtil.CHAR, WorkbookDataUtil.STUDY,
				WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		factors.add(variable);

		variable = new MeasurementVariable(TermId.CROSS.getId(), WorkbookDataUtil.CROSS,
				"The pedigree string of the germplasm", WorkbookDataUtil.PEDIGREE_STRING,
				WorkbookDataUtil.ASSIGNED, WorkbookDataUtil.CROSS_HISTORY, WorkbookDataUtil.CHAR,
				WorkbookDataUtil.STUDY, WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		factors.add(variable);

		variable = new MeasurementVariable(TermId.SEED_SOURCE.getId(),
				WorkbookDataUtil.SEED_SOURCE, "The seed source of the germplasm",
				WorkbookDataUtil.NAME, WorkbookDataUtil.SELECTED, WorkbookDataUtil.SEED_SOURCE,
				WorkbookDataUtil.CHAR, WorkbookDataUtil.STUDY, WorkbookDataUtil.ENTRY);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		factors.add(variable);

		variable = new MeasurementVariable(TermId.PLOT_NO.getId(), WorkbookDataUtil.PLOT,
				"Plot number ", WorkbookDataUtil.NESTED_NUMBER, WorkbookDataUtil.ENUMERATED,
				WorkbookDataUtil.FIELD_PLOT, WorkbookDataUtil.NUMERIC,
				WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.PLOT);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		factors.add(variable);

		// Plot Factors
		variable = new MeasurementVariable(TermId.BLOCK_NO.getId(), WorkbookDataUtil.BLOCK,
				"INCOMPLETE BLOCK", WorkbookDataUtil.NUMBER, WorkbookDataUtil.ENUMERATED,
				WorkbookDataUtil.BLOCKING_FACTOR, WorkbookDataUtil.NUMERIC,
				WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.PLOT);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		factors.add(variable);

		variable = new MeasurementVariable(TermId.REP_NO.getId(), WorkbookDataUtil.REP,
				WorkbookDataUtil.REPLICATION, WorkbookDataUtil.NUMBER, WorkbookDataUtil.ENUMERATED,
				WorkbookDataUtil.REPLICATION_FACTOR, WorkbookDataUtil.NUMERIC,
				WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.PLOT);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		factors.add(variable);

		WorkbookDataUtil.workbook.setFactors(factors);
	}

	private static void createConstants() {
		//Create measurement variables and set its dataTypeId
		List<MeasurementVariable> constants = new ArrayList<MeasurementVariable>();

		MeasurementVariable variable = new MeasurementVariable(WorkbookDataUtil.DATE_SEEDED_ID,
				"DATE_SEEDED", "Date Seeded", WorkbookDataUtil.DATE, WorkbookDataUtil.APPLIED,
				WorkbookDataUtil.PLANTING_DATE, WorkbookDataUtil.NUMERIC,
				WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		constants.add(variable);

		variable = new MeasurementVariable(WorkbookDataUtil.SOILPH_ID, "SOILPH", "Soil pH",
				WorkbookDataUtil.PH, WorkbookDataUtil.PH_METER, WorkbookDataUtil.SOIL_ACIDITY,
				WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		constants.add(variable);

		WorkbookDataUtil.workbook.setConstants(constants);
	}

	private static void createVariates() {
		//Create measurement variables and set its dataTypeId
		List<MeasurementVariable> variates = new ArrayList<MeasurementVariable>();

		MeasurementVariable variable = new MeasurementVariable(WorkbookDataUtil.GYLD_ID,
				WorkbookDataUtil.GYLD, "Grain yield -dry and weigh (kg/ha)",
				WorkbookDataUtil.KG_HA, WorkbookDataUtil.DRY_AND_WEIGH, WorkbookDataUtil.YIELD,
				WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		variates.add(variable);

		variable = new MeasurementVariable(WorkbookDataUtil.CHALK_PCT_ID,
				WorkbookDataUtil.CHALK_PCT, "NUMBER OF PLANTS SELECTED", WorkbookDataUtil.NUMBER,
				WorkbookDataUtil.COUNTED, WorkbookDataUtil.PLANTS_SELECTED,
				WorkbookDataUtil.NUMERIC, WorkbookDataUtil.NUMERIC_VALUE, WorkbookDataUtil.STUDY);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		variates.add(variable);
		WorkbookDataUtil.workbook.setVariates(variates);
	}

	private static void createObservations(int noOfObservations, int trialInstances) {
		List<MeasurementRow> observations = new ArrayList<MeasurementRow>();

		MeasurementRow row;
		List<MeasurementData> dataList;
		Random random = new Random();
		DecimalFormat fmt = new DecimalFormat("#.##");

		//Create n number of observation rows
		for (int j = 0; j < trialInstances; j++) {
			for (int i = 0; i < noOfObservations; i++) {
				row = new MeasurementRow();
				dataList = new ArrayList<MeasurementData>();
	
				MeasurementData data = new MeasurementData(WorkbookDataUtil.ENTRY, String.valueOf(i));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						TermId.ENTRY_NO.getId(), WorkbookDataUtil.workbook.getFactors()));
				dataList.add(data);
	
				data = new MeasurementData(WorkbookDataUtil.GID, WorkbookDataUtil.computeGID(i));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(TermId.GID.getId(),
						WorkbookDataUtil.workbook.getFactors()));
				dataList.add(data);
	
				data = new MeasurementData(WorkbookDataUtil.DESIG, WorkbookDataUtil.GERMPLASM_NAME
						+ new Random().nextInt(10000));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						TermId.DESIG.getId(), WorkbookDataUtil.workbook.getFactors()));
				dataList.add(data);
	
				data = new MeasurementData(WorkbookDataUtil.CROSS, "-");
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						TermId.CROSS.getId(), WorkbookDataUtil.workbook.getFactors()));
				dataList.add(data);
	
				data = new MeasurementData(WorkbookDataUtil.SOURCE, "-");
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						TermId.SEED_SOURCE.getId(), WorkbookDataUtil.workbook.getFactors()));
				dataList.add(data);
	
				data = new MeasurementData(WorkbookDataUtil.PLOT, String.valueOf(i));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						TermId.PLOT_NO.getId(), WorkbookDataUtil.workbook.getFactors()));
				dataList.add(data);
	
				data = new MeasurementData(WorkbookDataUtil.BLOCK, "");
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						TermId.BLOCK_NO.getId(), WorkbookDataUtil.workbook.getFactors()));
				dataList.add(data);
	
				data = new MeasurementData(WorkbookDataUtil.REP, "");
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						TermId.REP_NO.getId(), WorkbookDataUtil.workbook.getFactors()));
				dataList.add(data);
	
				data = new MeasurementData(WorkbookDataUtil.GYLD, WorkbookDataUtil.randomizeValue(
						random, fmt, 5000));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						WorkbookDataUtil.GYLD_ID, WorkbookDataUtil.workbook.getVariates()));
				dataList.add(data);
	
				data = new MeasurementData(WorkbookDataUtil.CHALK_PCT, String.valueOf(i));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						WorkbookDataUtil.CHALK_PCT_ID, WorkbookDataUtil.workbook.getVariates()));
				dataList.add(data);
				
				data = new MeasurementData(WorkbookDataUtil.TRIAL_INSTANCE,
						String.valueOf(j + 1));
				data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
						TermId.TRIAL_INSTANCE_FACTOR.getId(), WorkbookDataUtil.workbook.getConditions()));
				dataList.add(data);
	
				row.setDataList(dataList);
				observations.add(row);
			}
		}

		WorkbookDataUtil.workbook.setObservations(observations);
	}
	
	public static List<MeasurementRow> createNewObservations(int noOfObservations) {
		List<MeasurementRow> observations = new ArrayList<MeasurementRow>();

		MeasurementRow row;
		List<MeasurementData> dataList;
		Random random = new Random();
		DecimalFormat fmt = new DecimalFormat("#.##");

		//Create n number of observation rows
		for (int i = 0; i < noOfObservations; i++) {
			row = new MeasurementRow();
			dataList = new ArrayList<MeasurementData>();

			MeasurementData data = new MeasurementData(WorkbookDataUtil.ENTRY, String.valueOf(i));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.GID, WorkbookDataUtil.computeGID(i));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.DESIG, WorkbookDataUtil.GERMPLASM_NAME
					+ new Random().nextInt(10000));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.CROSS, "-");
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.SOURCE, "-");
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.PLOT, String.valueOf(i));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.BLOCK, "");
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.REP, "");
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.GYLD, WorkbookDataUtil.randomizeValue(
					random, fmt, 5000));
			dataList.add(data);

			data = new MeasurementData(WorkbookDataUtil.CHALK_PCT, String.valueOf(i));
			dataList.add(data);

			row.setDataList(dataList);
			observations.add(row);
		}
		
		return observations;
	}

	public static void createTrialObservations(int noOfTrialInstances) {
		List<MeasurementRow> trialObservations = new ArrayList<MeasurementRow>();

		MeasurementRow row;
		List<MeasurementData> dataList;

		for (int i = 0; i < noOfTrialInstances; i++) {
			row = new MeasurementRow();
			dataList = new ArrayList<MeasurementData>();

			MeasurementData data = new MeasurementData(WorkbookDataUtil.TRIAL_INSTANCE,
					String.valueOf(i + 1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					TermId.TRIAL_INSTANCE_FACTOR.getId(), WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.PI_NAME, "");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					TermId.PI_NAME.getId(), WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.PI_ID, "");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					TermId.PI_ID.getId(), WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.COOPERATOR, "");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					AppConstants.COOPERATOR_NAME.getInt(),
					WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.COOPERATOR_ID, "");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					AppConstants.COOPERATOR_ID.getInt(), WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.SITE, LNAME + "_" + (i+1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					TermId.TRIAL_LOCATION.getId(), WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookDataUtil.SITE_ID, String.valueOf(i+1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					TermId.LOCATION_ID.getId(), WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);
			
			//Check variables
			data = new MeasurementData("CHECK_START", String.valueOf(i+1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					TermId.CHECK_START.getId(), WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData("CHECK_INTERVAL", String.valueOf(i+1));
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					TermId.CHECK_PLAN.getId(), WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData("CHECK_PLAN", "1");
			data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
					TermId.CHECK_INTERVAL.getId(), WorkbookDataUtil.workbook.getConditions()));
			dataList.add(data);

			row.setDataList(dataList);
			trialObservations.add(row);
		}

		WorkbookDataUtil.workbook.setTrialObservations(trialObservations);
	}

	public static MeasurementVariable getMeasurementVariable(int termId,
			List<MeasurementVariable> variables) {
		if (variables != null) {
			//get matching MeasurementVariable object given the term id
			for (MeasurementVariable var : variables) {
				if (var.getTermId() == termId) {
					return var;
				}
			}
		}
		return null;
	}

	private static String computeGID(int i) {
		int gid = 1000000;
		gid += i;
		return String.valueOf(gid);
	}

	private static String randomizeValue(Random random, DecimalFormat fmt, int base) {
		double value = random.nextDouble() * base;
		return fmt.format(value);
	}
	
	public static List<Location> createLocationData() {
		List<Location> locations = new ArrayList<Location>();
		locations.add(new Location(LOCATION_ID_1, LTYPE, NLLP, LNAME + " 1", LABBR, SNL3ID, SNL2ID,
	            SNL1ID, CNTRYID, LRPLCE));
		locations.add(new Location(LOCATION_ID_2, LTYPE, NLLP, LNAME + " 2", LABBR, SNL3ID, SNL2ID,
	            SNL1ID, CNTRYID, LRPLCE));
		return locations;
	}
	
	public static MeasurementRow createTrialObservationWithoutSite() {
		WorkbookDataUtil.workbook = new Workbook();

		WorkbookDataUtil.createStudyDetails(StudyType.T);
		WorkbookDataUtil.createConditions();
		
		MeasurementRow row = new MeasurementRow();
		List<MeasurementData> dataList = new ArrayList<MeasurementData>();

		MeasurementData data = new MeasurementData(WorkbookDataUtil.TRIAL_INSTANCE,
				NUMERIC_VALUE);
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
				TermId.TRIAL_INSTANCE_FACTOR.getId(), WorkbookDataUtil.workbook.getConditions()));
		dataList.add(data);
		data = new MeasurementData(WorkbookDataUtil.PI_NAME, "");
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
				TermId.PI_NAME.getId(), WorkbookDataUtil.workbook.getConditions()));
		dataList.add(data);
		data = new MeasurementData(WorkbookDataUtil.PI_ID, "");
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
				TermId.PI_ID.getId(), WorkbookDataUtil.workbook.getConditions()));
		dataList.add(data);
		data = new MeasurementData(WorkbookDataUtil.COOPERATOR, "");
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
				AppConstants.COOPERATOR_NAME.getInt(),
				WorkbookDataUtil.workbook.getConditions()));
		dataList.add(data);
		data = new MeasurementData(WorkbookDataUtil.COOPERATOR_ID, "");
		data.setMeasurementVariable(WorkbookDataUtil.getMeasurementVariable(
				AppConstants.COOPERATOR_ID.getInt(), WorkbookDataUtil.workbook.getConditions()));
		dataList.add(data);

		row.setDataList(dataList);
		return row;
	}
	
	public static List<Integer> getTrialInstances(){
		List<Integer> instances = new ArrayList<Integer>();
		for (MeasurementRow row : workbook.getTrialObservations()) {
			if (row.getDataList() != null) {
				instances.add(getTrialInstanceNo(row.getDataList()));
			}
		}
		return instances;
	}
	
	private static int getTrialInstanceNo(List<MeasurementData> dataList) {
		for (MeasurementData data : dataList) {
			if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
				return Integer.valueOf(data.getValue());
			} 
		}
		return 0;
	}

}
