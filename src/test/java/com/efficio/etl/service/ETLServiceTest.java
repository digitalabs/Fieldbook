
package com.efficio.etl.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.etl.service.impl.ETLServiceImpl;
import com.efficio.etl.web.bean.IndexValueDTO;
import com.efficio.etl.web.bean.SheetDTO;
import com.efficio.etl.web.bean.UserSelection;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ETLServiceTest {

	@Resource
	private Workbook workbook;

	@Mock
	private FileService fileService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private DataImportService dataImportService;

	@Mock
	private ResourceBundleMessageSource messageSource;

	@InjectMocks
	private ETLServiceImpl etlService = new ETLServiceImpl();

	private UserSelection userSelection;

	private final static String PROGRAM_UUID = "9f2102ee-ca88-43bc-900a-09dc49a29ddb";

	private final static int ALL_OBSERVATION_ROWS = 447;
	private final static int GID_COLUMN = 1;
	private final static int OBSERVATION_HEADER_ROW = 0;
	private final static int OBSERVATION_CONTENT_ROW = 1;
	private final static int OBSERVATION_SHEET_INDEX = 1;

	private final static int COLUMN_WITH_BLANK_CELL = 12;
	private final static int CELL_COUNT_BEFORE_BLANK = 36;

	private final static String[] COLUMN_HEADERS = new String[] {"ENTRY", "GID", "DESIG", "CROSS", "SOURCE", "PLOT", "BLOCK", "REP", "ROW",
			"COL", "NBEPm2", "GYLD", "equi-Kkni", "equi-Tiand", "DTFL", "DFLF", "FDect", "GDENS", "TGW", "PERTH", "PH1", "PH2", "INTNN1",
			"INTNN2", "PEDL1", "PEDL2", "PANL1", "PANL2", "NHH", "NBGPAN", "PH", "INTNN", "PEDL", "PANL", "AleuCol_1_5"};

	private final static String SHEET_1_NAME = "Description";
	private final static String SHEET_2_NAME = "Observation";

	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String LOCATION_ID = "LOCATION_ID";
	private static final String LOCATION_NAME = "LOCATION_NAME";
	private static final String SITE_SOIL_PH = "SITE_SOIL_PH";
	private static final String ENTRY_NO = "ENTRY_NO";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String ASI = "ASI";
	private static final String ASI_MEAN = "ASI_MEAN";
	private static final String ALEU_COL_1_5 = "ALEU_COL_1_5";

	private static final int STUDY_ID = 1;
	private static final int TRIAL_DATASET_ID = 2;
	private static final int MEASUREMENT_DATASET_ID = 3;
	private static final int MEANS_DATASET_ID = 4;

	private static final int DUMMY_PROPERTY_ID = 10;
	private static final String DUMMY_PROPERTY_NAME = "PROPERTY";
	private static final String DUMMY_PROPERTY_DEF = "PROPERT-DEF";

	private static final int DUMMY_SCALE_ID = 20;
	private static final String DUMMY_SCALE_NAME = "SCALE";
	private static final String DUMMY_SCALE_DEF = "SCALE-DEF";

	private static final int DUMMY_METHOD_ID = 30;
	private static final String DUMMY_METHOD_NAME = "METHOD";
	private static final String DUMMY_METHOD_DEF = "METHOD-DEF";

	private static final int DUMMY_DATATYPE_ID = 40;
	private static final String DUMMY_DATATYPE_NAME = "DATATYPE";
	private static final String DUMMY_DATATYPE_DEF = "DATATYPE-DEF";

	@Before
	public void setUp() throws IOException {

		MockitoAnnotations.initMocks(this);
		this.userSelection = new UserSelection();
		this.fillObservationInfoOfUserSelection(this.userSelection);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);
		Mockito.when(this.fileService.retrieveWorkbook(Mockito.anyString())).thenReturn(this.workbook);
	}

	@Test
	public void testRetrieveSheetInformationPositive() {
		final List<SheetDTO> sheetDTOs = this.etlService.retrieveSheetInformation(this.workbook);

		Assert.assertEquals(2, sheetDTOs.size());

		Assert.assertEquals(ETLServiceTest.SHEET_1_NAME, sheetDTOs.get(0).getSheetName());
		Assert.assertEquals(ETLServiceTest.SHEET_2_NAME, sheetDTOs.get(1).getSheetName());
	}

	@Test
	public void testRetrieveColumnInformationPositive() {
		final List<IndexValueDTO> indexValueDTOs =
				this.etlService.retrieveColumnInformation(this.workbook, ETLServiceTest.OBSERVATION_SHEET_INDEX,
						ETLServiceTest.OBSERVATION_HEADER_ROW);

		Assert.assertEquals(ETLServiceTest.COLUMN_HEADERS.length, indexValueDTOs.size());

		for (int i = 0; i < ETLServiceTest.COLUMN_HEADERS.length; i++) {
			Assert.assertEquals(ETLServiceTest.COLUMN_HEADERS[i], indexValueDTOs.get(i).getValue());
		}
	}

	@Test
	public void testComputeObservationRowsAll() {
		Assert.assertEquals(this.etlService.calculateObservationRows(this.workbook, ETLServiceTest.OBSERVATION_SHEET_INDEX,
				ETLServiceTest.OBSERVATION_CONTENT_ROW, ETLServiceTest.GID_COLUMN), ETLServiceTest.ALL_OBSERVATION_ROWS);
	}

	@Test
	public void testComputeObservationRowsWithBlank() {
		Assert.assertTrue(this.etlService.calculateObservationRows(this.workbook, ETLServiceTest.OBSERVATION_SHEET_INDEX,
				ETLServiceTest.OBSERVATION_CONTENT_ROW, ETLServiceTest.COLUMN_WITH_BLANK_CELL) < ETLServiceTest.ALL_OBSERVATION_ROWS);

		Assert.assertEquals(this.etlService.calculateObservationRows(this.workbook, ETLServiceTest.OBSERVATION_SHEET_INDEX,
				ETLServiceTest.OBSERVATION_CONTENT_ROW, ETLServiceTest.COLUMN_WITH_BLANK_CELL), ETLServiceTest.CELL_COUNT_BEFORE_BLANK);
	}

	@Test
	public void testExtractColumnHeadersPositive() {

		Assert.assertArrayEquals(ETLServiceTest.COLUMN_HEADERS, this.etlService.retrieveColumnHeaders(this.workbook, this.userSelection, Boolean.FALSE)
				.toArray());

	}

	@Test
	public void testRetrieveAndSetProjectOntologyForPlotDataImport() {
		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, STUDY_ID);
		this.userSelection.setDatasetType(datasetType);

		final List<DataSet> plotDatasets = this.createPlotDatasetsTestData(this.userSelection.getStudyName() + "-PLOTDATA");
		Mockito.doReturn(plotDatasets).when(this.studyDataManager)
				.getDataSetsByType(this.userSelection.getStudyId(), DataSetType.PLOT_DATA);

		final List<DataSet> trialDatasets = this.createTrialDatasetsTestData(this.userSelection.getStudyName() + "-ENVIRONMENT");
		Mockito.doReturn(trialDatasets).when(this.studyDataManager)
				.getDataSetsByType(this.userSelection.getStudyId(), DataSetType.SUMMARY_DATA);

		final org.generationcp.middleware.domain.etl.Workbook workbook =
				this.etlService.retrieveAndSetProjectOntology(this.userSelection, false);

		Assert.assertNotNull(workbook);
		Assert.assertTrue("Imported type must be plot data", datasetType == workbook.getImportType());

		Assert.assertNotNull(workbook.getStudyDetails());
		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(), workbook
				.getStudyDetails().getId());
		Assert.assertEquals("Study name must be " + this.userSelection.getStudyName(), this.userSelection.getStudyName(), workbook
				.getStudyDetails().getStudyName());
		Assert.assertEquals("Study title must be " + this.userSelection.getStudyTitle(), this.userSelection.getStudyTitle(), workbook
				.getStudyDetails().getTitle());
		Assert.assertEquals("Study objective must be " + this.userSelection.getStudyObjective(), this.userSelection.getStudyObjective(),
				workbook.getStudyDetails().getObjective());
		final String expectedStartDate = ETLServiceImpl.formatDate(this.userSelection.getStudyStartDate());
		Assert.assertEquals("Study start date must be " + expectedStartDate, expectedStartDate, workbook.getStudyDetails().getStartDate());
		final String expectedEndDate = ETLServiceImpl.formatDate(this.userSelection.getStudyEndDate());
		Assert.assertEquals("Study end date must be " + expectedEndDate, expectedEndDate, workbook.getStudyDetails().getEndDate());
		Assert.assertEquals("Study type must be " + this.userSelection.getStudyType(), this.userSelection.getStudyType(), workbook
				.getStudyDetails().getStudyType().getName());

		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(), workbook
				.getStudyDetails().getId());
		Assert.assertTrue("Trial dataset id must be " + TRIAL_DATASET_ID, TRIAL_DATASET_ID == workbook.getTrialDatasetId());
		Assert.assertTrue("Measurement dataset id must be " + MEASUREMENT_DATASET_ID,
				MEASUREMENT_DATASET_ID == workbook.getMeasurementDatesetId());

		Assert.assertNotNull(workbook.getConditions());
		Assert.assertNotNull(workbook.getConstants());

		Assert.assertNotNull(workbook.getFactors());
		Assert.assertEquals("The number of factors must be 5", 5, workbook.getFactors().size());
		for (final MeasurementVariable measurementVariable : workbook.getFactors()) {
			Assert.assertTrue("A factor should either have a trial environment, germplasm or trial design role",
					measurementVariable.getRole() == PhenotypicType.TRIAL_ENVIRONMENT
							|| measurementVariable.getRole() == PhenotypicType.GERMPLASM
							|| measurementVariable.getRole() == PhenotypicType.TRIAL_DESIGN);
		}

		Assert.assertNotNull(workbook.getVariates());
		Assert.assertEquals("The number of variates must be 2", 2, workbook.getVariates().size());
		for (final MeasurementVariable measurementVariable : workbook.getVariates()) {
			Assert.assertTrue("A variate should have a variate role", measurementVariable.getRole() == PhenotypicType.VARIATE);
		}

	}

	@Test
	public void testRetrieveAndSetProjectOntologyForPlotDataImportOldDatasetNames() {
		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, STUDY_ID);
		this.userSelection.setDatasetType(datasetType);

		final List<DataSet> plotDatasets = this.createPlotDatasetsTestData("MEASUREMENT EFEC_" + this.userSelection.getStudyName());
		plotDatasets.add(this.createPlotDatasetTestData("TRIAL_" + this.userSelection.getStudyName(), true));
		Mockito.doReturn(plotDatasets).when(this.studyDataManager)
				.getDataSetsByType(this.userSelection.getStudyId(), DataSetType.PLOT_DATA);

		final org.generationcp.middleware.domain.etl.Workbook workbook =
				this.etlService.retrieveAndSetProjectOntology(this.userSelection, false);

		Mockito.verify(this.studyDataManager, Mockito.times(0)).getDataSetsByType(this.userSelection.getStudyId(), DataSetType.MEANS_DATA);

		Assert.assertNotNull(workbook);
		Assert.assertTrue("Imported type must be plot data", datasetType == workbook.getImportType());

		Assert.assertNotNull(workbook.getStudyDetails());
		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(), workbook
				.getStudyDetails().getId());
		Assert.assertEquals("Study name must be " + this.userSelection.getStudyName(), this.userSelection.getStudyName(), workbook
				.getStudyDetails().getStudyName());
		Assert.assertEquals("Study title must be " + this.userSelection.getStudyTitle(), this.userSelection.getStudyTitle(), workbook
				.getStudyDetails().getTitle());
		Assert.assertEquals("Study objective must be " + this.userSelection.getStudyObjective(), this.userSelection.getStudyObjective(),
				workbook.getStudyDetails().getObjective());
		final String expectedStartDate = ETLServiceImpl.formatDate(this.userSelection.getStudyStartDate());
		Assert.assertEquals("Study start date must be " + expectedStartDate, expectedStartDate, workbook.getStudyDetails().getStartDate());
		final String expectedEndDate = ETLServiceImpl.formatDate(this.userSelection.getStudyEndDate());
		Assert.assertEquals("Study end date must be " + expectedEndDate, expectedEndDate, workbook.getStudyDetails().getEndDate());
		Assert.assertEquals("Study type must be " + this.userSelection.getStudyType(), this.userSelection.getStudyType(), workbook
				.getStudyDetails().getStudyType().getName());

		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(), workbook
				.getStudyDetails().getId());
		Assert.assertTrue("Trial dataset id must be " + TRIAL_DATASET_ID, TRIAL_DATASET_ID == workbook.getTrialDatasetId());
		Assert.assertTrue("Measurement dataset id must be " + MEASUREMENT_DATASET_ID,
				MEASUREMENT_DATASET_ID == workbook.getMeasurementDatesetId());

		Assert.assertNotNull(workbook.getConditions());
		Assert.assertNotNull(workbook.getConstants());

		Assert.assertNotNull(workbook.getFactors());
		Assert.assertEquals("The number of factors must be 5", 5, workbook.getFactors().size());
		for (final MeasurementVariable measurementVariable : workbook.getFactors()) {
			Assert.assertTrue("A factor should either have a trial environment, germplasm or trial design role",
					measurementVariable.getRole() == PhenotypicType.TRIAL_ENVIRONMENT
							|| measurementVariable.getRole() == PhenotypicType.GERMPLASM
							|| measurementVariable.getRole() == PhenotypicType.TRIAL_DESIGN);
		}

		Assert.assertNotNull(workbook.getVariates());
		Assert.assertEquals("The number of variates must be 2", 2, workbook.getVariates().size());
		for (final MeasurementVariable measurementVariable : workbook.getVariates()) {
			Assert.assertTrue("A variate should have a variate role", measurementVariable.getRole() == PhenotypicType.VARIATE);
		}

	}

	@Test
	public void testRetrieveAndSetProjectOntologyForMeansDataImport() {
		final int datasetType = DataSetType.MEANS_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, STUDY_ID);
		this.userSelection.setDatasetType(datasetType);

		final List<DataSet> meansDatasets = this.createMeansDatasetsTestData(this.userSelection.getStudyName() + "-MEANS");
		Mockito.doReturn(meansDatasets).when(this.studyDataManager)
				.getDataSetsByType(this.userSelection.getStudyId(), DataSetType.MEANS_DATA);

		final List<DataSet> plotDatasets = this.createPlotDatasetsTestData(this.userSelection.getStudyName() + "-PLOTDATA");
		Mockito.doReturn(plotDatasets).when(this.studyDataManager)
				.getDataSetsByType(this.userSelection.getStudyId(), DataSetType.PLOT_DATA);

		final List<DataSet> trialDatasets = this.createTrialDatasetsTestData(this.userSelection.getStudyName() + "-ENVIRONMENT");
		Mockito.doReturn(trialDatasets).when(this.studyDataManager)
				.getDataSetsByType(this.userSelection.getStudyId(), DataSetType.SUMMARY_DATA);

		final org.generationcp.middleware.domain.etl.Workbook workbook =
				this.etlService.retrieveAndSetProjectOntology(this.userSelection, true);

		Assert.assertNotNull(workbook);
		Assert.assertTrue("Imported type must be means data", datasetType == workbook.getImportType());

		Assert.assertNotNull(workbook.getStudyDetails());
		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(), workbook
				.getStudyDetails().getId());
		Assert.assertEquals("Study name must be " + this.userSelection.getStudyName(), this.userSelection.getStudyName(), workbook
				.getStudyDetails().getStudyName());
		Assert.assertEquals("Study title must be " + this.userSelection.getStudyTitle(), this.userSelection.getStudyTitle(), workbook
				.getStudyDetails().getTitle());
		Assert.assertEquals("Study objective must be " + this.userSelection.getStudyObjective(), this.userSelection.getStudyObjective(),
				workbook.getStudyDetails().getObjective());
		final String expectedStartDate = ETLServiceImpl.formatDate(this.userSelection.getStudyStartDate());
		Assert.assertEquals("Study start date must be " + expectedStartDate, expectedStartDate, workbook.getStudyDetails().getStartDate());
		final String expectedEndDate = ETLServiceImpl.formatDate(this.userSelection.getStudyEndDate());
		Assert.assertEquals("Study end date must be " + expectedEndDate, expectedEndDate, workbook.getStudyDetails().getEndDate());
		Assert.assertEquals("Study type must be " + this.userSelection.getStudyType(), this.userSelection.getStudyType(), workbook
				.getStudyDetails().getStudyType().getName());

		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(), workbook
				.getStudyDetails().getId());
		Assert.assertTrue("Trial dataset id must be " + TRIAL_DATASET_ID, TRIAL_DATASET_ID == workbook.getTrialDatasetId());
		Assert.assertTrue("Means dataset id must be " + MEANS_DATASET_ID, MEANS_DATASET_ID == workbook.getMeansDatasetId());

		Assert.assertNotNull(workbook.getConditions());
		Assert.assertNotNull(workbook.getConstants());

		Assert.assertNotNull(workbook.getFactors());
		Assert.assertEquals("The number of factors must be 3", 3, workbook.getFactors().size());
		for (final MeasurementVariable measurementVariable : workbook.getFactors()) {
			Assert.assertTrue("A factor should either have a trial environment, germplasm or trial design role",
					measurementVariable.getRole() == PhenotypicType.TRIAL_ENVIRONMENT
							|| measurementVariable.getRole() == PhenotypicType.GERMPLASM
							|| measurementVariable.getRole() == PhenotypicType.TRIAL_DESIGN);
		}

		Assert.assertNotNull(workbook.getVariates());
		Assert.assertEquals("The number of variates must be 2", 2, workbook.getVariates().size());
		for (final MeasurementVariable measurementVariable : workbook.getVariates()) {
			Assert.assertTrue("A variate should have a variate role", measurementVariable.getRole() == PhenotypicType.VARIATE);
		}
	}

	@Test
	public void testCheckOutOfBoundsDataTrue() throws IOException {

		// Accept any workbook when checkForOutOfBoundsData is called. It will be captured and verified later.
		Mockito.when(
				this.dataImportService.checkForOutOfBoundsData(Mockito.any(org.generationcp.middleware.domain.etl.Workbook.class),
						Mockito.eq(PROGRAM_UUID))).thenReturn(true);

		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, STUDY_ID);
		this.userSelection.setDatasetType(datasetType);
		this.userSelection.getPhenotypicMap().putAll(this.createPhenotyicMapTestData());

		final ArgumentCaptor<org.generationcp.middleware.domain.etl.Workbook> workbookCaptor =
				ArgumentCaptor.forClass(org.generationcp.middleware.domain.etl.Workbook.class);

		Assert.assertTrue(this.etlService.checkOutOfBoundsData(this.userSelection));

		// Make sure the dataImportService.checkForOutOfBoundsData is called
		Mockito.verify(this.dataImportService, Mockito.times(1)).checkForOutOfBoundsData(
				workbookCaptor.capture(), Mockito.eq(PROGRAM_UUID));
		Assert.assertNotNull(workbookCaptor.getValue());
	}

	@Test
	public void testCheckOutOfBoundsDataFalse() throws IOException {

		// Accept any workbook when checkForOutOfBoundsData is called. It will be captured and verified later.
		Mockito.when(
				this.dataImportService.checkForOutOfBoundsData(
						Mockito.any(org.generationcp.middleware.domain.etl.Workbook.class), Mockito.eq(PROGRAM_UUID))).thenReturn(false);

		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, STUDY_ID);
		this.userSelection.setDatasetType(datasetType);
		this.userSelection.getPhenotypicMap().putAll(this.createPhenotyicMapTestData());

		final ArgumentCaptor<org.generationcp.middleware.domain.etl.Workbook> workbookCaptor =
				ArgumentCaptor.forClass(org.generationcp.middleware.domain.etl.Workbook.class);

		Assert.assertFalse(this.etlService.checkOutOfBoundsData(this.userSelection));

		// Make sure the dataImportService.checkForOutOfBoundsData is called
		Mockito.verify(this.dataImportService, Mockito.times(1)).checkForOutOfBoundsData(
				workbookCaptor.capture(), Mockito.eq(PROGRAM_UUID));
		Assert.assertNotNull(workbookCaptor.getValue());

	}

	@Test(expected = IOException.class)
	public void testCheckOutOfBoundsDataException() throws IOException {

		Mockito.when(this.fileService.retrieveWorkbook(Mockito.anyString())).thenThrow(new IOException());
		this.etlService.checkOutOfBoundsData(this.userSelection);

	}

	@Test
	public void testIsObservationOverMaximumLimitTrue() {

		List<String> errors = new ArrayList<>();
		this.etlService.setMaxRowLimit(100);
		Assert.assertTrue(this.etlService.isObservationOverMaximumLimit(this.userSelection, errors, this.workbook));
		Assert.assertTrue(!errors.isEmpty());
	}

	@Test
	public void testIsObservationOverMaximumLimitFalse() {

		List<String> errors = new ArrayList<>();

		Assert.assertFalse(this.etlService.isObservationOverMaximumLimit(this.userSelection, errors, this.workbook));
		Assert.assertTrue("If max limit for observation is not reached, there should be no error added in the list.", errors.isEmpty());
	}

	@Test
	public void testIsWorkbookHasObservationRecordsFalse() {
		List<String> errors = new ArrayList<>();
		Workbook emptyWorkbook = new HSSFWorkbook();
		emptyWorkbook.createSheet();
		emptyWorkbook.createSheet();

		Assert.assertFalse(this.etlService.isWorkbookHasObservationRecords(this.userSelection, errors, emptyWorkbook));
		Assert.assertTrue(!errors.isEmpty());
		Assert.assertEquals("error.observation.no.records", errors.get(0));
	}

	@Test
	public void testIsWorkbookHasObservationRecordsTrue() {
		List<String> errors = new ArrayList<>();

		Assert.assertTrue(this.etlService.isWorkbookHasObservationRecords(this.userSelection, errors, this.workbook));
		Assert.assertTrue(errors.isEmpty());
	}

	@Test
	public void testCreateWorkbookFromUserSelection() {

		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, STUDY_ID);
		this.userSelection.setDatasetType(datasetType);
		this.userSelection.getPhenotypicMap().putAll(this.createPhenotyicMapTestData());

		org.generationcp.middleware.domain.etl.Workbook workbook =
				this.etlService.createWorkbookFromUserSelection(this.userSelection, true);

		Assert.assertEquals(8, workbook.getFactors().size());
		Assert.assertEquals(3, workbook.getVariates().size());

	}

	@Test
	public void testExtractExcelFileDataNoInvalidValues() {

		this.userSelection.setObservationRows(1);
		org.generationcp.middleware.domain.etl.Workbook testWorkbook = this.createTestWorkbook();
		Workbook importData = this.createTestExcelWorkbookFromWorkbook(testWorkbook, false);
		List<MeasurementRow> result = this.etlService.extractExcelFileData(importData, this.userSelection, testWorkbook, false);

		Assert.assertEquals(1, result.size());

		Assert.assertEquals("1", result.get(0).getMeasurementData(TRIAL_INSTANCE).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ENTRY_NO).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(PLOT_NO).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ALEU_COL_1_5).getValue());

	}

	@Test
	public void testExtractExcelFileDataDiscardInvalidValues() {

		this.userSelection.setObservationRows(1);
		org.generationcp.middleware.domain.etl.Workbook testWorkbook = this.createTestWorkbook();
		Workbook importData = this.createTestExcelWorkbookFromWorkbook(testWorkbook, true);
		List<MeasurementRow> result = this.etlService.extractExcelFileData(importData, this.userSelection, testWorkbook, true);

		Assert.assertEquals(1, result.size());

		Assert.assertEquals("1", result.get(0).getMeasurementData(TRIAL_INSTANCE).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ENTRY_NO).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(PLOT_NO).getValue());
		Assert.assertEquals("The value must be empty since the original data is an invalid value", "",
				result.get(0).getMeasurementData(ALEU_COL_1_5).getValue());

	}

	@Test
	public void testExtractExcelFileDataKeepInvalidValues() {

		this.userSelection.setObservationRows(1);
		org.generationcp.middleware.domain.etl.Workbook testWorkbook = this.createTestWorkbook();
		Workbook importData = this.createTestExcelWorkbookFromWorkbook(testWorkbook, true);
		List<MeasurementRow> result = this.etlService.extractExcelFileData(importData, this.userSelection, testWorkbook, false);

		Assert.assertEquals(1, result.size());

		Assert.assertEquals("1", result.get(0).getMeasurementData(TRIAL_INSTANCE).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ENTRY_NO).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(PLOT_NO).getValue());
		Assert.assertEquals("The value should be 6", "6", result.get(0).getMeasurementData(ALEU_COL_1_5).getValue());

	}

	protected Map<PhenotypicType, LinkedHashMap<String, MeasurementVariable>> createPhenotyicMapTestData() {

		Map<PhenotypicType, LinkedHashMap<String, MeasurementVariable>> map = new HashMap<>();

		// Trial Environment
		LinkedHashMap<String, MeasurementVariable> trialEnvironmentsMap = new LinkedHashMap<>();
		trialEnvironmentsMap.put(TRIAL_INSTANCE, this.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), TRIAL_INSTANCE,
				PhenotypicType.TRIAL_ENVIRONMENT, DataType.NUMERIC_VARIABLE.getId()));
		map.put(PhenotypicType.TRIAL_ENVIRONMENT, trialEnvironmentsMap);

		// Trial Design
		LinkedHashMap<String, MeasurementVariable> trialDesignsMap = new LinkedHashMap<>();
		trialDesignsMap.put("REP", this.createMeasurementVariable(TermId.REP_NO.getId(), "REP", PhenotypicType.TRIAL_DESIGN,
				DataType.NUMERIC_VARIABLE.getId()));
		trialDesignsMap.put("ROW",
				this.createMeasurementVariable(TermId.ROW.getId(), "ROW", PhenotypicType.TRIAL_DESIGN, DataType.NUMERIC_VARIABLE.getId()));
		trialDesignsMap.put("COL",
				this.createMeasurementVariable(TermId.COL.getId(), "COL", PhenotypicType.TRIAL_DESIGN, DataType.NUMERIC_VARIABLE.getId()));
		map.put(PhenotypicType.TRIAL_DESIGN, trialDesignsMap);

		// Germplasm
		LinkedHashMap<String, MeasurementVariable> germplasmMap = new LinkedHashMap<>();
		germplasmMap.put("SOURCE", this.createMeasurementVariable(TermId.SOURCE.getId(), "SOURCE", PhenotypicType.GERMPLASM,
				DataType.NUMERIC_VARIABLE.getId()));
		germplasmMap.put(
				"ENTRY",
				this.createMeasurementVariable(TermId.ENTRY_NO.getId(), "ENTRY", PhenotypicType.GERMPLASM,
						DataType.NUMERIC_VARIABLE.getId()));
		germplasmMap.put("CROSS",
				this.createMeasurementVariable(TermId.CROSS.getId(), "CROSS", PhenotypicType.GERMPLASM, DataType.NUMERIC_VARIABLE.getId()));
		germplasmMap.put("GID",
				this.createMeasurementVariable(TermId.GID.getId(), "GID", PhenotypicType.GERMPLASM, DataType.NUMERIC_VARIABLE.getId()));
		map.put(PhenotypicType.GERMPLASM, germplasmMap);

		// Variate
		LinkedHashMap<String, MeasurementVariable> variatesMap = new LinkedHashMap<>();
		variatesMap.put("GYLD", this.createMeasurementVariable(18150, "GYLD", PhenotypicType.VARIATE, DataType.NUMERIC_VARIABLE.getId()));
		variatesMap.put("PH", this.createMeasurementVariable(20343, "PH", PhenotypicType.VARIATE, DataType.NUMERIC_VARIABLE.getId()));
		variatesMap.put("AleuCol_1_5",
				this.createMeasurementVariable(51547, "AleuCol_1_5", PhenotypicType.VARIATE, DataType.CATEGORICAL_VARIABLE.getId()));
		map.put(PhenotypicType.VARIATE, variatesMap);

		return map;
	}

	private List<DataSet> createTrialDatasetsTestData(final String datasetName) {
		final List<DataSet> trialDatasets = new ArrayList<>();
		trialDatasets.add(this.createTrialDatasetTestData(datasetName));
		return trialDatasets;
	}

	private DataSet createTrialDatasetTestData(final String datasetName) {
		final DataSet trialDataset = new DataSet();
		trialDataset.setName(datasetName);
		trialDataset.setDescription(datasetName);
		trialDataset.setDataSetType(DataSetType.SUMMARY_DATA);
		trialDataset.setId(TRIAL_DATASET_ID);
		trialDataset.setVariableTypes(this.createTrialVariableTypesTestData());
		return trialDataset;
	}

	private List<DataSet> createPlotDatasetsTestData(final String datasetName) {
		final List<DataSet> plotDatasets = new ArrayList<>();
		plotDatasets.add(this.createPlotDatasetTestData(datasetName, false));
		return plotDatasets;
	}

	private DataSet createPlotDatasetTestData(final String datasetName, final boolean isTrial) {
		final DataSet plotDataset = new DataSet();
		plotDataset.setName(datasetName);
		plotDataset.setDescription(datasetName);
		plotDataset.setDataSetType(DataSetType.PLOT_DATA);
		if (isTrial) {
			plotDataset.setId(TRIAL_DATASET_ID);
			plotDataset.setVariableTypes(this.createTrialVariableTypesTestData());
		} else {
			plotDataset.setVariableTypes(this.createPlotVariableTypesTestData());
			plotDataset.setId(MEASUREMENT_DATASET_ID);
		}
		return plotDataset;
	}

	private VariableTypeList createTrialVariableTypesTestData() {
		final VariableTypeList trialVariableTypeList = new VariableTypeList();
		int rank = 0;
		trialVariableTypeList.add(new DMSVariableType(TRIAL_INSTANCE, TRIAL_INSTANCE, this.createStandardVariableTestData(TRIAL_INSTANCE,
				PhenotypicType.TRIAL_ENVIRONMENT), ++rank));
		trialVariableTypeList.add(new DMSVariableType(LOCATION_ID, LOCATION_ID, this.createStandardVariableTestData(LOCATION_ID,
				PhenotypicType.TRIAL_ENVIRONMENT), ++rank));
		trialVariableTypeList.add(new DMSVariableType(LOCATION_NAME, LOCATION_NAME, this.createStandardVariableTestData(LOCATION_NAME,
				PhenotypicType.TRIAL_ENVIRONMENT), ++rank));
		trialVariableTypeList.add(new DMSVariableType(SITE_SOIL_PH, SITE_SOIL_PH, this.createStandardVariableTestData(SITE_SOIL_PH,
				PhenotypicType.VARIATE), ++rank));
		return trialVariableTypeList;
	}

	private StandardVariable createStandardVariableTestData(final String name, final PhenotypicType phenotypicType) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setName(name);
		standardVariable.setPhenotypicType(phenotypicType);
		// PSM combination should be unique but for testing this class, it is not important
		standardVariable.setProperty(new Term(DUMMY_PROPERTY_ID, DUMMY_PROPERTY_NAME, DUMMY_PROPERTY_DEF));
		standardVariable.setScale(new Term(DUMMY_SCALE_ID, DUMMY_SCALE_NAME, DUMMY_SCALE_DEF));
		standardVariable.setMethod(new Term(DUMMY_METHOD_ID, DUMMY_METHOD_NAME, DUMMY_METHOD_DEF));
		standardVariable.setDataType(new Term(DUMMY_DATATYPE_ID, DUMMY_DATATYPE_NAME, DUMMY_DATATYPE_DEF));
		return standardVariable;
	}

	private VariableTypeList createPlotVariableTypesTestData() {
		final VariableTypeList plotVariableTypeList = new VariableTypeList();
		int rank = 0;
		plotVariableTypeList.add(new DMSVariableType(TRIAL_INSTANCE, TRIAL_INSTANCE, this.createStandardVariableTestData(TRIAL_INSTANCE,
				PhenotypicType.TRIAL_ENVIRONMENT), ++rank));
		plotVariableTypeList.add(new DMSVariableType(ENTRY_NO, ENTRY_NO, this.createStandardVariableTestData(ENTRY_NO,
				PhenotypicType.GERMPLASM), ++rank));
		plotVariableTypeList.add(new DMSVariableType(PLOT_NO, PLOT_NO, this.createStandardVariableTestData(PLOT_NO,
				PhenotypicType.TRIAL_DESIGN), ++rank));
		plotVariableTypeList.add(new DMSVariableType(ASI, ASI, this.createStandardVariableTestData(ASI, PhenotypicType.VARIATE), ++rank));
		return plotVariableTypeList;
	}

	private List<DataSet> createMeansDatasetsTestData(final String datasetName) {
		final List<DataSet> meansDataset = new ArrayList<>();
		meansDataset.add(this.createMeansDatasetTestData(datasetName));
		return meansDataset;
	}

	private DataSet createMeansDatasetTestData(final String datasetName) {
		final DataSet meansDataset = new DataSet();
		meansDataset.setId(MEANS_DATASET_ID);
		meansDataset.setName(datasetName);
		meansDataset.setDescription(datasetName);
		meansDataset.setDataSetType(DataSetType.MEANS_DATA);
		meansDataset.setVariableTypes(this.createMeansVariableTypesTestData());
		return meansDataset;
	}

	private VariableTypeList createMeansVariableTypesTestData() {
		final VariableTypeList meansVariableTypeList = new VariableTypeList();
		int rank = 0;
		meansVariableTypeList.add(new DMSVariableType(TRIAL_INSTANCE, TRIAL_INSTANCE, this.createStandardVariableTestData(TRIAL_INSTANCE,
				PhenotypicType.TRIAL_ENVIRONMENT), ++rank));
		meansVariableTypeList.add(new DMSVariableType(ASI_MEAN, ASI_MEAN, this.createStandardVariableTestData(ASI_MEAN,
				PhenotypicType.VARIATE), ++rank));
		return meansVariableTypeList;
	}

	private MeasurementVariable createMeasurementVariable(int termId, String name, PhenotypicType phenotypicType, int dataTypeId) {
		StandardVariable stdvar = this.createStandardVariableTestData(name, phenotypicType);
		stdvar.setPhenotypicType(phenotypicType);
		stdvar.setId(termId);
		final MeasurementVariable var =
				new MeasurementVariable(termId, stdvar.getName(), stdvar.getDescription(), stdvar.getScale().getName(), stdvar.getMethod()
						.getName(), stdvar.getProperty().getName(), stdvar.getDataType().getName(), "", stdvar.getPhenotypicType()
						.getLabelList().get(0));
		var.setRole(phenotypicType);
		var.setDataTypeId(stdvar.getDataType().getId());
		var.setFactor(false);
		var.setOperation(null);
		return var;
	}

	private void fillObservationInfoOfUserSelection(final UserSelection userSelection) {
		userSelection.setHeaderRowIndex(ETLServiceTest.OBSERVATION_HEADER_ROW);
		userSelection.setContentRowIndex(ETLServiceTest.OBSERVATION_CONTENT_ROW);
		userSelection.setSelectedSheet(ETLServiceTest.OBSERVATION_SHEET_INDEX);
		userSelection.setObservationRows(ETLServiceTest.ALL_OBSERVATION_ROWS);
	}

	private void fillStudyDetailsOfUserSelection(final UserSelection userSelection, final Integer studyId) {
		userSelection.setStudyName("ETLStudy" + Math.random());
		userSelection.setStudyTitle("Study for testing");
		userSelection.setStudyObjective("To test the data import tool");
		userSelection.setStudyStartDate("09/01/2015");
		userSelection.setStudyEndDate("10/01/2015");
		userSelection.setStudyType(StudyType.T.getName());
		userSelection.setStudyId(studyId);
	}

	protected Workbook createTestExcelWorkbookFromWorkbook(org.generationcp.middleware.domain.etl.Workbook workbook,
			boolean withInvalidValues) {

		HSSFWorkbook excelWorkbook = new HSSFWorkbook();
		excelWorkbook.createSheet("Description");
		HSSFSheet observationSheet = excelWorkbook.createSheet("Observation");

		List<MeasurementVariable> allVariables = new LinkedList<>();
		allVariables.addAll(workbook.getFactors());
		allVariables.addAll(workbook.getVariates());

		HSSFRow row1 = observationSheet.createRow(0);
		for (int i = 0; i < allVariables.size(); i++) {
			HSSFCell cell = row1.createCell(i);
			cell.setCellValue(allVariables.get(i).getName());
		}

		HSSFRow row2 = observationSheet.createRow(1);
		for (int i = 0; i < allVariables.size(); i++) {
			HSSFCell cell = row2.createCell(i);

			if (allVariables.get(i).getDataTypeId() == DataType.CATEGORICAL_VARIABLE.getId()) {
				cell.setCellValue(withInvalidValues ? "6" : "1");
			} else {
				cell.setCellValue("1");
			}

		}

		return excelWorkbook;
	}

	protected org.generationcp.middleware.domain.etl.Workbook createTestWorkbook() {
		org.generationcp.middleware.domain.etl.Workbook workbook = new org.generationcp.middleware.domain.etl.Workbook();

		List<MeasurementVariable> factors = new LinkedList<>();
		List<MeasurementVariable> variates = new LinkedList<>();

		factors.add(new MeasurementVariable(TRIAL_INSTANCE, "", "", "", "", "", "", ""));
		factors.add(new MeasurementVariable(ENTRY_NO, "", "", "", "", "", "", ""));
		factors.add(new MeasurementVariable(PLOT_NO, "", "", "", "", "", "", ""));

		MeasurementVariable categorical = new MeasurementVariable(ALEU_COL_1_5, "", "", "", "", "", "", "");
		categorical.setPossibleValues(this.createPossibleValues());
		categorical.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		categorical.setRole(PhenotypicType.VARIATE);
		factors.add(categorical);

		workbook.setFactors(factors);
		workbook.setVariates(variates);

		return workbook;
	}

	protected List<ValueReference> createPossibleValues() {
		List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference(1, "1", ""));
		possibleValues.add(new ValueReference(2, "2", ""));
		possibleValues.add(new ValueReference(3, "3", ""));
		possibleValues.add(new ValueReference(4, "4", ""));
		possibleValues.add(new ValueReference(5, "5", ""));
		return possibleValues;
	}

}
