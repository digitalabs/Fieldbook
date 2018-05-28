
package com.efficio.etl.service;

import com.efficio.etl.service.impl.ETLServiceImpl;
import com.efficio.etl.web.bean.IndexValueDTO;
import com.efficio.etl.web.bean.SheetDTO;
import com.efficio.etl.web.bean.UserSelection;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.DataSetTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.ValueReferenceTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.OntologyService;
import org.generationcp.middleware.util.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	@Mock
	private OntologyService ontologyService;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@InjectMocks
	private final ETLServiceImpl etlService = new ETLServiceImpl();

	private UserSelection userSelection;

	private MeasurementDataTestDataInitializer measurementDataTestDataInitializer;

	private final static String PROGRAM_UUID = "9f2102ee-ca88-43bc-900a-09dc49a29ddb";

	private final static int ALL_OBSERVATION_ROWS = 447;
	private final static int GID_COLUMN = 1;
	private final static int OBSERVATION_HEADER_ROW = 0;
	private final static int OBSERVATION_CONTENT_ROW = 1;
	private final static int OBSERVATION_SHEET_INDEX = 1;

	private final static int COLUMN_WITH_BLANK_CELL = 12;
	private final static int CELL_COUNT_BEFORE_BLANK = 36;

	private final static String[] COLUMN_HEADERS = new String[] { "ENTRY", "GID", "DESIG", "CROSS", "SOURCE", "PLOT",
			"BLOCK", "REP", "ROW", "COL", "NBEPm2", "GYLD", "equi-Kkni", "equi-Tiand", "DTFL", "DFLF", "FDect", "GDENS",
			"TGW", "PERTH", "PH1", "PH2", "INTNN1", "INTNN2", "PEDL1", "PEDL2", "PANL1", "PANL2", "NHH", "NBGPAN", "PH",
			"INTNN", "PEDL", "PANL", "AleuCol_1_5" };

	private final static String SHEET_1_NAME = "Description";
	private final static String SHEET_2_NAME = "Observation";

	private static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	private static final String ENTRY_NO = "ENTRY_NO";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String REP_NO = "REP_NO";
	private static final String ALEU_COL_1_5 = "ALEU_COL_1_5";

	private static final int STUDY_ID = 1;
	private static final int TRIAL_DATASET_ID = 2;
	private static final int MEASUREMENT_DATASET_ID = 3;
	private static final int MEANS_DATASET_ID = 4;

	@Before
	public void setUp() throws IOException {

		MockitoAnnotations.initMocks(this);
		this.userSelection = new UserSelection();
		this.fillObservationInfoOfUserSelection(this.userSelection);

		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(ETLServiceTest.PROGRAM_UUID);
		Mockito.when(this.fileService.retrieveWorkbook(Matchers.anyString())).thenReturn(this.workbook);

		this.measurementDataTestDataInitializer = new MeasurementDataTestDataInitializer();

		final StandardVariable standardVariable = Mockito.mock(StandardVariable.class);
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_TYPE.getId(), ETLServiceTest.PROGRAM_UUID))
				.thenReturn(standardVariable);
		Mockito.when(standardVariable.getEnumerations())
				.thenReturn(Arrays.asList(new Enumeration(TermId.CHECK.getId(), TermId.CHECK.name(), "", 0)));
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
		final List<IndexValueDTO> indexValueDTOs = this.etlService.retrieveColumnInformation(this.workbook,
				ETLServiceTest.OBSERVATION_SHEET_INDEX, ETLServiceTest.OBSERVATION_HEADER_ROW);

		Assert.assertEquals(ETLServiceTest.COLUMN_HEADERS.length, indexValueDTOs.size());

		for (int i = 0; i < ETLServiceTest.COLUMN_HEADERS.length; i++) {
			Assert.assertEquals(ETLServiceTest.COLUMN_HEADERS[i], indexValueDTOs.get(i).getValue());
		}
	}

	@Test
	public void testComputeObservationRowsAll() {
		Assert.assertEquals(ETLServiceTest.ALL_OBSERVATION_ROWS,
				this.etlService.calculateObservationRows(this.workbook, ETLServiceTest.OBSERVATION_SHEET_INDEX,
						ETLServiceTest.OBSERVATION_CONTENT_ROW, ETLServiceTest.GID_COLUMN));
	}

	@Test
	public void testComputeObservationRowsWithBlank() {
		Assert.assertTrue(this.etlService.calculateObservationRows(this.workbook,
				ETLServiceTest.OBSERVATION_SHEET_INDEX, ETLServiceTest.OBSERVATION_CONTENT_ROW,
				ETLServiceTest.COLUMN_WITH_BLANK_CELL) < ETLServiceTest.ALL_OBSERVATION_ROWS);

		Assert.assertEquals(ETLServiceTest.CELL_COUNT_BEFORE_BLANK,
				this.etlService.calculateObservationRows(this.workbook, ETLServiceTest.OBSERVATION_SHEET_INDEX,
						ETLServiceTest.OBSERVATION_CONTENT_ROW, ETLServiceTest.COLUMN_WITH_BLANK_CELL));
	}

	@Test
	public void testExtractColumnHeadersPositive() {

		Assert.assertArrayEquals(ETLServiceTest.COLUMN_HEADERS,
				this.etlService.retrieveColumnHeaders(this.workbook, this.userSelection, Boolean.FALSE).toArray());

	}

	@Test
	public void testRetrieveAndSetProjectOntologyForPlotDataImport() {
		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, ETLServiceTest.STUDY_ID);
		this.userSelection.setDatasetType(datasetType);

		Mockito.doReturn(new StudyTypeDto(10010,"Trial",StudyTypeDto.TRIAL_NAME)).when(this.studyDataManager).getStudyTypeByName(this.userSelection.getStudyType());
		final List<DataSet> plotDatasets = DataSetTestDataInitializer
				.createPlotDatasetsTestData(this.userSelection.getStudyName() + "-PLOTDATA");
		Mockito.doReturn(plotDatasets).when(this.studyDataManager).getDataSetsByType(this.userSelection.getStudyId(),
				DataSetType.PLOT_DATA);

		final List<DataSet> trialDatasets = DataSetTestDataInitializer
				.createTrialDatasetsTestData(this.userSelection.getStudyName() + "-ENVIRONMENT");
		Mockito.doReturn(trialDatasets).when(this.studyDataManager).getDataSetsByType(this.userSelection.getStudyId(),
				DataSetType.SUMMARY_DATA);

		final org.generationcp.middleware.domain.etl.Workbook workbook = this.etlService
				.retrieveAndSetProjectOntology(this.userSelection, false);

		Assert.assertNotNull(workbook);
		Assert.assertTrue("Imported type must be plot data", datasetType == workbook.getImportType());

		Assert.assertNotNull(workbook.getStudyDetails());
		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(),
				workbook.getStudyDetails().getId());
		Assert.assertEquals("Study name must be " + this.userSelection.getStudyName(),
				this.userSelection.getStudyName(), workbook.getStudyDetails().getStudyName());
		Assert.assertEquals("Study title must be " + this.userSelection.getStudyDescription(),
				this.userSelection.getStudyDescription(), workbook.getStudyDetails().getDescription());
		Assert.assertEquals("Study objective must be " + this.userSelection.getStudyObjective(),
				this.userSelection.getStudyObjective(), workbook.getStudyDetails().getObjective());
		final String expectedStartDate = ETLServiceImpl.formatDate(this.userSelection.getStudyStartDate());
		Assert.assertEquals("Study start date must be " + expectedStartDate, expectedStartDate,
				workbook.getStudyDetails().getStartDate());
		final String expectedEndDate = ETLServiceImpl.formatDate(this.userSelection.getStudyEndDate());
		Assert.assertEquals("Study end date must be " + expectedEndDate, expectedEndDate,
				workbook.getStudyDetails().getEndDate());
		Assert.assertEquals("Study type must be " + this.userSelection.getStudyType(),
				this.userSelection.getStudyType(), workbook.getStudyDetails().getStudyType().getName());

		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(),
				workbook.getStudyDetails().getId());
		Assert.assertTrue("Trial dataset id must be " + ETLServiceTest.TRIAL_DATASET_ID,
				ETLServiceTest.TRIAL_DATASET_ID == workbook.getTrialDatasetId());
		Assert.assertTrue("Measurement dataset id must be " + ETLServiceTest.MEASUREMENT_DATASET_ID,
				ETLServiceTest.MEASUREMENT_DATASET_ID == workbook.getMeasurementDatesetId());

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
			Assert.assertTrue("A variate should have a variate role",
					measurementVariable.getRole() == PhenotypicType.VARIATE);
		}

	}

	@Test
	public void testRetrieveAndSetProjectOntologyForPlotDataImportOldDatasetNames() {
		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, ETLServiceTest.STUDY_ID);//
		this.userSelection.setDatasetType(datasetType);

		Mockito.doReturn(new StudyTypeDto(10010,"Trial",StudyTypeDto.TRIAL_NAME)).when(this.studyDataManager).getStudyTypeByName(this.userSelection.getStudyType());
		final List<DataSet> plotDatasets = DataSetTestDataInitializer
				.createPlotDatasetsTestData("MEASUREMENT EFEC_" + this.userSelection.getStudyName());
		plotDatasets.add(DataSetTestDataInitializer
				.createPlotDatasetTestData("TRIAL_" + this.userSelection.getStudyName(), true));
		Mockito.doReturn(plotDatasets).when(this.studyDataManager).getDataSetsByType(this.userSelection.getStudyId(),
				DataSetType.PLOT_DATA);
		Mockito.doReturn(1).when(this.workbenchDataManager).getCurrentIbdbUserId(Long.valueOf(0), 1);

		final org.generationcp.middleware.domain.etl.Workbook workbook = this.etlService
				.retrieveAndSetProjectOntology(this.userSelection, false);

		Mockito.verify(this.studyDataManager, Mockito.times(0)).getDataSetsByType(this.userSelection.getStudyId(),
				DataSetType.MEANS_DATA);

		Assert.assertNotNull(workbook);
		Assert.assertTrue("Imported type must be plot data", datasetType == workbook.getImportType());

		Assert.assertNotNull(workbook.getStudyDetails());
		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(),
				workbook.getStudyDetails().getId());
		Assert.assertEquals("Study name must be " + this.userSelection.getStudyName(),
				this.userSelection.getStudyName(), workbook.getStudyDetails().getStudyName());
		Assert.assertEquals("Study title must be " + this.userSelection.getStudyDescription(),
				this.userSelection.getStudyDescription(), workbook.getStudyDetails().getDescription());
		Assert.assertEquals("Study objective must be " + this.userSelection.getStudyObjective(),
				this.userSelection.getStudyObjective(), workbook.getStudyDetails().getObjective());
		final String expectedStartDate = ETLServiceImpl.formatDate(this.userSelection.getStudyStartDate());
		Assert.assertEquals("Study start date must be " + expectedStartDate, expectedStartDate,
				workbook.getStudyDetails().getStartDate());
		final String expectedEndDate = ETLServiceImpl.formatDate(this.userSelection.getStudyEndDate());
		Assert.assertEquals("Study end date must be " + expectedEndDate, expectedEndDate,
				workbook.getStudyDetails().getEndDate());
		Assert.assertEquals("Study type must be " + this.userSelection.getStudyType(),
				this.userSelection.getStudyType(), workbook.getStudyDetails().getStudyType().getName());

		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(),
				workbook.getStudyDetails().getId());
		Assert.assertTrue("Trial dataset id must be " + ETLServiceTest.TRIAL_DATASET_ID,
				ETLServiceTest.TRIAL_DATASET_ID == workbook.getTrialDatasetId());
		Assert.assertTrue("Measurement dataset id must be " + ETLServiceTest.MEASUREMENT_DATASET_ID,
				ETLServiceTest.MEASUREMENT_DATASET_ID == workbook.getMeasurementDatesetId());

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
			Assert.assertTrue("A variate should have a variate role",
					measurementVariable.getRole() == PhenotypicType.VARIATE);
		}

	}

	@Test
	public void testRetrieveAndSetProjectOntologyForMeansDataImport() {
		final int datasetType = DataSetType.MEANS_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, ETLServiceTest.STUDY_ID);
		this.userSelection.setDatasetType(datasetType);

		Mockito.doReturn(new StudyTypeDto(10010,StudyTypeDto.TRIAL_LABEL,StudyTypeDto.TRIAL_NAME)).when(this.studyDataManager).getStudyTypeByName(this.userSelection.getStudyType());
		final List<DataSet> meansDatasets = DataSetTestDataInitializer
				.createMeansDatasetsTestData(this.userSelection.getStudyName() + "-MEANS");
		Mockito.doReturn(meansDatasets).when(this.studyDataManager).getDataSetsByType(this.userSelection.getStudyId(),
				DataSetType.MEANS_DATA);

		final List<DataSet> plotDatasets = DataSetTestDataInitializer
				.createPlotDatasetsTestData(this.userSelection.getStudyName() + "-PLOTDATA");
		Mockito.doReturn(plotDatasets).when(this.studyDataManager).getDataSetsByType(this.userSelection.getStudyId(),
				DataSetType.PLOT_DATA);

		final List<DataSet> trialDatasets = DataSetTestDataInitializer
				.createTrialDatasetsTestData(this.userSelection.getStudyName() + "-ENVIRONMENT");
		Mockito.doReturn(trialDatasets).when(this.studyDataManager).getDataSetsByType(this.userSelection.getStudyId(),
				DataSetType.SUMMARY_DATA);

		final org.generationcp.middleware.domain.etl.Workbook workbook = this.etlService
				.retrieveAndSetProjectOntology(this.userSelection, true);

		Assert.assertNotNull(workbook);
		Assert.assertTrue("Imported type must be means data", datasetType == workbook.getImportType());

		Assert.assertNotNull(workbook.getStudyDetails());
		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(),
				workbook.getStudyDetails().getId());
		Assert.assertEquals("Study name must be " + this.userSelection.getStudyName(),
				this.userSelection.getStudyName(), workbook.getStudyDetails().getStudyName());
		Assert.assertEquals("Study title must be " + this.userSelection.getStudyDescription(),
				this.userSelection.getStudyDescription(), workbook.getStudyDetails().getDescription());
		Assert.assertEquals("Study objective must be " + this.userSelection.getStudyObjective(),
				this.userSelection.getStudyObjective(), workbook.getStudyDetails().getObjective());
		final String expectedStartDate = ETLServiceImpl.formatDate(this.userSelection.getStudyStartDate());
		Assert.assertEquals("Study start date must be " + expectedStartDate, expectedStartDate,
				workbook.getStudyDetails().getStartDate());
		final String expectedEndDate = ETLServiceImpl.formatDate(this.userSelection.getStudyEndDate());
		Assert.assertEquals("Study end date must be " + expectedEndDate, expectedEndDate,
				workbook.getStudyDetails().getEndDate());
		Assert.assertEquals("Study type must be " + this.userSelection.getStudyType(),
				this.userSelection.getStudyType(), workbook.getStudyDetails().getStudyType().getName());

		Assert.assertEquals("Study id must be " + this.userSelection.getStudyId(), this.userSelection.getStudyId(),
				workbook.getStudyDetails().getId());
		Assert.assertTrue("Trial dataset id must be " + ETLServiceTest.TRIAL_DATASET_ID,
				ETLServiceTest.TRIAL_DATASET_ID == workbook.getTrialDatasetId());
		Assert.assertTrue("Means dataset id must be " + ETLServiceTest.MEANS_DATASET_ID,
				ETLServiceTest.MEANS_DATASET_ID == workbook.getMeansDatasetId());

		Assert.assertNotNull(workbook.getConditions());
		Assert.assertNotNull(workbook.getConstants());

		Assert.assertNotNull(workbook.getFactors());
		Assert.assertEquals("The number of factors must be 3", 3, workbook.getFactors().size());

		Assert.assertNotNull(workbook.getVariates());
		Assert.assertEquals("The number of variates must be 2", 2, workbook.getVariates().size());
		for (final MeasurementVariable measurementVariable : workbook.getVariates()) {
			Assert.assertTrue("A variate should have a variate role",
					measurementVariable.getRole() == PhenotypicType.VARIATE);
		}
	}

	@Test
	public void testConvertEntryTypeNameToID() {
		final String value = "T";
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.ENTRY_TYPE.getId(), value);
		final MeasurementData measurementData = this.measurementDataTestDataInitializer.createMeasurementData(value,
				variable);
		final Map<String, Integer> availableEntryTypes = new HashMap<>();
		availableEntryTypes.put(value, TermId.ENTRY_TYPE.getId());
		this.etlService.convertEntryTypeNameToID(variable, measurementData, availableEntryTypes);
		Assert.assertEquals("The measurement data's value should be " + TermId.ENTRY_TYPE.getId(),
				String.valueOf(TermId.ENTRY_TYPE.getId()), measurementData.getValue());
	}

	@Test
	public void testRetrieveAvailableEntryTypes() {
		final Map<String, Integer> availableEntryTypes = this.etlService
				.retrieveAvailableEntryTypes(ETLServiceTest.PROGRAM_UUID);
		Assert.assertEquals("The map should contain the id  of " + TermId.CHECK.name(),
				String.valueOf(TermId.CHECK.getId()), availableEntryTypes.get(TermId.CHECK.name()).toString());
	}

	@Test
	public void testCheckOutOfBoundsDataTrue() throws IOException {

		// Accept any workbook when checkForOutOfBoundsData is called. It will
		// be captured and verified later.
		Mockito.when(this.dataImportService.checkForOutOfBoundsData(
				Matchers.any(org.generationcp.middleware.domain.etl.Workbook.class),
				Matchers.eq(ETLServiceTest.PROGRAM_UUID))).thenReturn(true);

		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, ETLServiceTest.STUDY_ID);
		this.userSelection.setDatasetType(datasetType);
		this.userSelection.getPhenotypicMap().putAll(this.createPhenotyicMapTestData());

		final ArgumentCaptor<org.generationcp.middleware.domain.etl.Workbook> workbookCaptor = ArgumentCaptor
				.forClass(org.generationcp.middleware.domain.etl.Workbook.class);

		Assert.assertTrue(this.etlService.checkOutOfBoundsData(this.userSelection));

		// Make sure the dataImportService.checkForOutOfBoundsData is called
		Mockito.verify(this.dataImportService, Mockito.times(1)).checkForOutOfBoundsData(workbookCaptor.capture(),
				Matchers.eq(ETLServiceTest.PROGRAM_UUID));
		Assert.assertNotNull(workbookCaptor.getValue());
	}

	@Test
	public void testCheckOutOfBoundsDataFalse() throws IOException {

		// Accept any workbook when checkForOutOfBoundsData is called. It will
		// be captured and verified later.
		Mockito.when(this.dataImportService.checkForOutOfBoundsData(
				Matchers.any(org.generationcp.middleware.domain.etl.Workbook.class),
				Matchers.eq(ETLServiceTest.PROGRAM_UUID))).thenReturn(false);

		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, ETLServiceTest.STUDY_ID);
		this.userSelection.setDatasetType(datasetType);
		this.userSelection.getPhenotypicMap().putAll(this.createPhenotyicMapTestData());

		final ArgumentCaptor<org.generationcp.middleware.domain.etl.Workbook> workbookCaptor = ArgumentCaptor
				.forClass(org.generationcp.middleware.domain.etl.Workbook.class);

		Assert.assertFalse(this.etlService.checkOutOfBoundsData(this.userSelection));

		// Make sure the dataImportService.checkForOutOfBoundsData is called
		Mockito.verify(this.dataImportService, Mockito.times(1)).checkForOutOfBoundsData(workbookCaptor.capture(),
				Matchers.eq(ETLServiceTest.PROGRAM_UUID));
		Assert.assertNotNull(workbookCaptor.getValue());

	}

	@Test(expected = IOException.class)
	public void testCheckOutOfBoundsDataException() throws IOException {

		Mockito.when(this.fileService.retrieveWorkbook(Matchers.anyString())).thenThrow(new IOException());
		this.etlService.checkOutOfBoundsData(this.userSelection);

	}

	@Test
	public void testIsObservationOverMaximumLimitTrue() {

		final List<String> errors = new ArrayList<>();
		this.etlService.setMaxRowLimit(100);
		Assert.assertTrue(this.etlService.isObservationOverMaximumLimit(this.userSelection, errors, this.workbook));
		Assert.assertTrue(!errors.isEmpty());
	}

	@Test
	public void testIsObservationOverMaximumLimitFalse() {

		final List<String> errors = new ArrayList<>();

		Assert.assertFalse(this.etlService.isObservationOverMaximumLimit(this.userSelection, errors, this.workbook));
		Assert.assertTrue("If max limit for observation is not reached, there should be no error added in the list.",
				errors.isEmpty());
	}

	@Test
	public void testIsWorkbookHasObservationRecordsFalse() {
		final List<String> errors = new ArrayList<>();
		final Workbook emptyWorkbook = new HSSFWorkbook();
		emptyWorkbook.createSheet();
		emptyWorkbook.createSheet();

		Assert.assertFalse(this.etlService.isWorkbookHasObservationRecords(this.userSelection, errors, emptyWorkbook));
		Assert.assertTrue(!errors.isEmpty());
		Assert.assertEquals("error.observation.no.records", errors.get(0));
	}

	@Test
	public void testIsWorkbookHasObservationRecordsTrue() {
		final List<String> errors = new ArrayList<>();

		Assert.assertTrue(this.etlService.isWorkbookHasObservationRecords(this.userSelection, errors, this.workbook));
		Assert.assertTrue(errors.isEmpty());
	}

	@Test
	public void testCreateWorkbookFromUserSelection() {

		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, ETLServiceTest.STUDY_ID);
		this.userSelection.setDatasetType(datasetType);
		this.userSelection.getPhenotypicMap().putAll(this.createPhenotyicMapTestData());

		final org.generationcp.middleware.domain.etl.Workbook workbook = this.etlService
				.createWorkbookFromUserSelection(this.userSelection, true);

		Assert.assertEquals(8, workbook.getFactors().size());
		Assert.assertEquals(3, workbook.getVariates().size());

	}

	@Test
	public void testExtractExcelFileDataNoInvalidValues() {

		this.userSelection.setObservationRows(1);
		final org.generationcp.middleware.domain.etl.Workbook testWorkbook = this.createTestWorkbook();
		final Workbook importData = this.createTestExcelWorkbookFromWorkbook(testWorkbook, false);
		final List<MeasurementRow> result = this.etlService.extractExcelFileData(importData, this.userSelection,
				testWorkbook, false);

		Assert.assertEquals(1, result.size());

		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.TRIAL_INSTANCE).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.ENTRY_NO).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.PLOT_NO).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.ALEU_COL_1_5).getValue());

	}

	@Test
	public void testExtractExcelFileDataDiscardInvalidValues() {

		this.userSelection.setObservationRows(1);
		final org.generationcp.middleware.domain.etl.Workbook testWorkbook = this.createTestWorkbook();
		final Workbook importData = this.createTestExcelWorkbookFromWorkbook(testWorkbook, true);
		final List<MeasurementRow> result = this.etlService.extractExcelFileData(importData, this.userSelection,
				testWorkbook, true);

		Assert.assertEquals(1, result.size());

		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.TRIAL_INSTANCE).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.ENTRY_NO).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.PLOT_NO).getValue());
		Assert.assertEquals("The value must be empty since the original data is an invalid value", "",
				result.get(0).getMeasurementData(ETLServiceTest.ALEU_COL_1_5).getValue());

	}

	@Test
	public void testFillDetailsOfDatasetsInWorkbook() {
		final int datasetType = DataSetType.PLOT_DATA.getId();
		this.fillStudyDetailsOfUserSelection(this.userSelection, ETLServiceTest.STUDY_ID);
		this.userSelection.setDatasetType(datasetType);

		final List<DataSet> plotDatasets = DataSetTestDataInitializer
				.createPlotDatasetsTestData(this.userSelection.getStudyName() + "-PLOTDATA");
		Mockito.doReturn(plotDatasets).when(this.studyDataManager).getDataSetsByType(this.userSelection.getStudyId(),
				DataSetType.PLOT_DATA);

		final List<DataSet> trialDatasets = DataSetTestDataInitializer
				.createTrialDatasetsTestData(this.userSelection.getStudyName() + "-ENVIRONMENT");
		Mockito.doReturn(trialDatasets).when(this.studyDataManager).getDataSetsByType(this.userSelection.getStudyId(),
				DataSetType.SUMMARY_DATA);
		final org.generationcp.middleware.domain.etl.Workbook wb = WorkbookTestDataInitializer.getTestWorkbook();
		wb.setFactors(null);
		wb.setVariates(null);
		wb.setConditions(null);
		wb.setConstants(null);

		this.etlService.fillDetailsOfDatasetsInWorkbook(wb, this.userSelection.getStudyId(), false);

		Assert.assertNotNull(wb.getFactors());
		Assert.assertNotNull(wb.getConditions());
		Assert.assertNotNull(wb.getVariates());
		Assert.assertNotNull(wb.getConstants());
	}

	@Test
	public void testExtractExcelFileDataKeepInvalidValues() {

		this.userSelection.setObservationRows(1);
		final org.generationcp.middleware.domain.etl.Workbook testWorkbook = this.createTestWorkbook();
		final Workbook importData = this.createTestExcelWorkbookFromWorkbook(testWorkbook, true);
		final List<MeasurementRow> result = this.etlService.extractExcelFileData(importData, this.userSelection,
				testWorkbook, false);

		Assert.assertEquals(1, result.size());

		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.TRIAL_INSTANCE).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.ENTRY_NO).getValue());
		Assert.assertEquals("1", result.get(0).getMeasurementData(ETLServiceTest.PLOT_NO).getValue());
		Assert.assertEquals("The value should be 6", "6",
				result.get(0).getMeasurementData(ETLServiceTest.ALEU_COL_1_5).getValue());

	}

	@Test
	public void testHeadersContainsPlotIdFalse() {
		this.userSelection.setObservationRows(1);
		final org.generationcp.middleware.domain.etl.Workbook testWorkbook = this.createTestWorkbook();

		Assert.assertFalse(this.etlService.headersContainsPlotId(testWorkbook));
	}

	@Test
	public void testHeadersContainsPlotIdTrue() {
		this.userSelection.setObservationRows(1);
		final org.generationcp.middleware.domain.etl.Workbook testWorkbook = this.createTestWorkbook();
		final MeasurementVariable plotVariable = new MeasurementVariable();
		plotVariable.setTermId(TermId.PLOT_ID.getId());
		testWorkbook.getFactors().add(plotVariable);
		Assert.assertTrue(this.etlService.headersContainsPlotId(testWorkbook));
	}

	@Test
	public void testCheckForMismatchedHeadersHeadersMatch() {

		final List<MeasurementVariable> studyHeaders = new ArrayList<>();
		final MeasurementVariable trialInstance = new MeasurementVariable();
		trialInstance.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
		trialInstance.setName(TRIAL_INSTANCE);
		final MeasurementVariable entryNo = new MeasurementVariable();
		entryNo.setRole(PhenotypicType.GERMPLASM);
		entryNo.setName(ENTRY_NO);
		final MeasurementVariable repNo = new MeasurementVariable();
		repNo.setRole(PhenotypicType.TRIAL_DESIGN);
		repNo.setName(REP_NO);

		studyHeaders.add(trialInstance);
		studyHeaders.add(entryNo);
		studyHeaders.add(repNo);

		final List<String> fileHeaders = new ArrayList<>();
		fileHeaders.add(TRIAL_INSTANCE);
		fileHeaders.add(ENTRY_NO);
		fileHeaders.add(REP_NO);

		Map<String, List<Message>> errors = this.etlService.checkForMismatchedHeaders(fileHeaders, studyHeaders, false);
		Assert.assertTrue(errors.isEmpty());

	}

	@Test
	public void testCheckForMismatchedHeadersHeadersDoNotMatch() {

		final List<MeasurementVariable> studyHeaders = new ArrayList<>();
		final MeasurementVariable trialInstance = new MeasurementVariable();
		trialInstance.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
		trialInstance.setName(TRIAL_INSTANCE);
		final MeasurementVariable entryNo = new MeasurementVariable();
		entryNo.setRole(PhenotypicType.GERMPLASM);
		entryNo.setName(ENTRY_NO);
		final MeasurementVariable repNo = new MeasurementVariable();
		repNo.setRole(PhenotypicType.TRIAL_DESIGN);
		repNo.setName(REP_NO);

		studyHeaders.add(trialInstance);
		studyHeaders.add(entryNo);
		studyHeaders.add(repNo);

		final List<String> fileHeaders = new ArrayList<>();
		fileHeaders.add(TRIAL_INSTANCE);
		fileHeaders.add(REP_NO);

		Map<String, List<Message>> errors = this.etlService.checkForMismatchedHeaders(fileHeaders, studyHeaders, false);
		Assert.assertFalse(errors.isEmpty());

	}

	@Test
	public void testCheckForMismatchedHeadersHeadersNoTrialEnvironmentOnFileHeaders() {

		final List<MeasurementVariable> studyHeaders = new ArrayList<>();
		final MeasurementVariable trialInstance = new MeasurementVariable();
		trialInstance.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
		trialInstance.setName(TRIAL_INSTANCE);
		final MeasurementVariable entryNo = new MeasurementVariable();
		entryNo.setRole(PhenotypicType.GERMPLASM);
		entryNo.setName(ENTRY_NO);
		final MeasurementVariable repNo = new MeasurementVariable();
		repNo.setRole(PhenotypicType.TRIAL_DESIGN);
		repNo.setName(REP_NO);

		studyHeaders.add(trialInstance);
		studyHeaders.add(entryNo);
		studyHeaders.add(repNo);

		final List<String> fileHeaders = new ArrayList<>();
		fileHeaders.add(ENTRY_NO);
		fileHeaders.add(REP_NO);

		Map<String, List<Message>> errors = this.etlService.checkForMismatchedHeaders(fileHeaders, studyHeaders, false);
		Assert.assertTrue("Trial environment variables are not required in file headers, there should be no mismatch error ", errors.isEmpty());

	}

	@Test
	public void testCheckForMismatchedHeadersHeadersMeansDataImportIsTrue() {

		final List<MeasurementVariable> studyHeaders = new ArrayList<>();
		final MeasurementVariable trialInstance = new MeasurementVariable();
		trialInstance.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
		trialInstance.setName(TRIAL_INSTANCE);
		final MeasurementVariable entryNo = new MeasurementVariable();
		entryNo.setRole(PhenotypicType.GERMPLASM);
		entryNo.setName(ENTRY_NO);
		final MeasurementVariable repNo = new MeasurementVariable();
		repNo.setRole(PhenotypicType.TRIAL_DESIGN);
		repNo.setName(REP_NO);

		studyHeaders.add(trialInstance);
		studyHeaders.add(entryNo);
		studyHeaders.add(repNo);

		final List<String> fileHeaders = new ArrayList<>();
		fileHeaders.add(ENTRY_NO);

		Map<String, List<Message>> errors = this.etlService.checkForMismatchedHeaders(fileHeaders, studyHeaders, true);
		Assert.assertTrue("Trial design variables are not required in file headers if the dataset being imported is type 'Means', there should be no mismatch error ", errors.isEmpty());

	}

	protected Map<PhenotypicType, LinkedHashMap<String, MeasurementVariable>> createPhenotyicMapTestData() {

		final Map<PhenotypicType, LinkedHashMap<String, MeasurementVariable>> map = new HashMap<>();

		// Trial Environment
		final LinkedHashMap<String, MeasurementVariable> trialEnvironmentsMap = new LinkedHashMap<>();
		trialEnvironmentsMap.put(ETLServiceTest.TRIAL_INSTANCE,
				MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(),
						ETLServiceTest.TRIAL_INSTANCE, PhenotypicType.TRIAL_ENVIRONMENT,
						DataType.NUMERIC_VARIABLE.getId()));
		map.put(PhenotypicType.TRIAL_ENVIRONMENT, trialEnvironmentsMap);

		// Trial Design
		final LinkedHashMap<String, MeasurementVariable> trialDesignsMap = new LinkedHashMap<>();
		trialDesignsMap.put("REP", MeasurementVariableTestDataInitializer.createMeasurementVariable(
				TermId.REP_NO.getId(), "REP", PhenotypicType.TRIAL_DESIGN, DataType.NUMERIC_VARIABLE.getId()));
		trialDesignsMap.put("ROW", MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.ROW.getId(),
				"ROW", PhenotypicType.TRIAL_DESIGN, DataType.NUMERIC_VARIABLE.getId()));
		trialDesignsMap.put("COL", MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.COL.getId(),
				"COL", PhenotypicType.TRIAL_DESIGN, DataType.NUMERIC_VARIABLE.getId()));
		map.put(PhenotypicType.TRIAL_DESIGN, trialDesignsMap);

		// Germplasm
		final LinkedHashMap<String, MeasurementVariable> germplasmMap = new LinkedHashMap<>();
		germplasmMap.put("SOURCE", MeasurementVariableTestDataInitializer.createMeasurementVariable(
				TermId.SOURCE.getId(), "SOURCE", PhenotypicType.GERMPLASM, DataType.NUMERIC_VARIABLE.getId()));
		germplasmMap.put("ENTRY", MeasurementVariableTestDataInitializer.createMeasurementVariable(
				TermId.ENTRY_NO.getId(), "ENTRY", PhenotypicType.GERMPLASM, DataType.NUMERIC_VARIABLE.getId()));
		germplasmMap.put("CROSS", MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.CROSS.getId(),
				"CROSS", PhenotypicType.GERMPLASM, DataType.NUMERIC_VARIABLE.getId()));
		germplasmMap.put("GID", MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.GID.getId(),
				"GID", PhenotypicType.GERMPLASM, DataType.NUMERIC_VARIABLE.getId()));
		map.put(PhenotypicType.GERMPLASM, germplasmMap);

		// Variate
		final LinkedHashMap<String, MeasurementVariable> variatesMap = new LinkedHashMap<>();
		variatesMap.put("GYLD", MeasurementVariableTestDataInitializer.createMeasurementVariable(18150, "GYLD",
				PhenotypicType.VARIATE, DataType.NUMERIC_VARIABLE.getId()));
		variatesMap.put("PH", MeasurementVariableTestDataInitializer.createMeasurementVariable(20343, "PH",
				PhenotypicType.VARIATE, DataType.NUMERIC_VARIABLE.getId()));
		variatesMap.put("AleuCol_1_5", MeasurementVariableTestDataInitializer.createMeasurementVariable(51547,
				"AleuCol_1_5", PhenotypicType.VARIATE, DataType.CATEGORICAL_VARIABLE.getId()));
		map.put(PhenotypicType.VARIATE, variatesMap);

		return map;
	}

	private void fillObservationInfoOfUserSelection(final UserSelection userSelection) {
		userSelection.setHeaderRowIndex(ETLServiceTest.OBSERVATION_HEADER_ROW);
		userSelection.setContentRowIndex(ETLServiceTest.OBSERVATION_CONTENT_ROW);
		userSelection.setSelectedSheet(ETLServiceTest.OBSERVATION_SHEET_INDEX);
		userSelection.setObservationRows(ETLServiceTest.ALL_OBSERVATION_ROWS);
	}

	private void fillStudyDetailsOfUserSelection(final UserSelection userSelection, final Integer studyId) {
		userSelection.setStudyName("ETLStudy" + Math.random());
		userSelection.setStudyDescription("Study for testing");
		userSelection.setStudyObjective("To test the data import tool");
		userSelection.setStudyStartDate("09/01/2015");
		userSelection.setStudyEndDate("10/01/2015");
		userSelection.setStudyType(StudyTypeDto.TRIAL_NAME);
		userSelection.setStudyId(studyId);
	}

	protected Workbook createTestExcelWorkbookFromWorkbook(
			final org.generationcp.middleware.domain.etl.Workbook workbook, final boolean withInvalidValues) {

		final HSSFWorkbook excelWorkbook = new HSSFWorkbook();
		excelWorkbook.createSheet("Description");
		final HSSFSheet observationSheet = excelWorkbook.createSheet("Observation");

		final List<MeasurementVariable> allVariables = new LinkedList<>();
		allVariables.addAll(workbook.getFactors());
		allVariables.addAll(workbook.getVariates());

		final HSSFRow row1 = observationSheet.createRow(0);
		for (int i = 0; i < allVariables.size(); i++) {
			final HSSFCell cell = row1.createCell(i);
			cell.setCellValue(allVariables.get(i).getName());
		}

		final HSSFRow row2 = observationSheet.createRow(1);
		for (int i = 0; i < allVariables.size(); i++) {
			final HSSFCell cell = row2.createCell(i);

			if (allVariables.get(i).getDataTypeId() == DataType.CATEGORICAL_VARIABLE.getId()) {
				cell.setCellValue(withInvalidValues ? "6" : "1");
			} else {
				cell.setCellValue("1");
			}

		}

		return excelWorkbook;
	}

	protected org.generationcp.middleware.domain.etl.Workbook createTestWorkbook() {
		final org.generationcp.middleware.domain.etl.Workbook workbook = new org.generationcp.middleware.domain.etl.Workbook();

		final List<MeasurementVariable> factors = new LinkedList<>();
		final List<MeasurementVariable> variates = new LinkedList<>();

		factors.add(new MeasurementVariable(ETLServiceTest.TRIAL_INSTANCE, "", "", "", "", "", "", ""));
		factors.add(new MeasurementVariable(ETLServiceTest.ENTRY_NO, "", "", "", "", "", "", ""));
		factors.add(new MeasurementVariable(ETLServiceTest.PLOT_NO, "", "", "", "", "", "", ""));

		final MeasurementVariable categorical = new MeasurementVariable(ETLServiceTest.ALEU_COL_1_5, "", "", "", "", "",
				"", "");
		categorical.setPossibleValues(ValueReferenceTestDataInitializer.createPossibleValues());
		categorical.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		categorical.setRole(PhenotypicType.VARIATE);
		factors.add(categorical);

		workbook.setFactors(factors);
		workbook.setVariates(variates);

		return workbook;
	}

}
