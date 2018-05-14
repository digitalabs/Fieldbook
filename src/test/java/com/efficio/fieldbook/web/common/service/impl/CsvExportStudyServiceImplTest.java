
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.service.GermplasmExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

public class CsvExportStudyServiceImplTest {

	private static final String CSV_EXT = ".csv";

	private static final String ZIP_EXT = ".zip";


	@Mock
	private OntologyService ontologyService;

	@Mock
	private GermplasmExportService germplasmExportService;

	@Mock
	protected ContextUtil contextUtil;

	private CsvExportStudyServiceImpl csvExportStudyService;
	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	private static String PROPERTY_NAME = "Property Name";
	private static String STUDY_NAME = "Test Study";
	private static String ZIP_FILEPATH = "./someDirectory/output/TestFileName.zip";

	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);

		// Need to spy so that actual writing of CSV / ZIP file won't be performed
		this.csvExportStudyService = Mockito.spy(new CsvExportStudyServiceImpl());
		this.csvExportStudyService.setGermplasmExportService(this.germplasmExportService);
		this.csvExportStudyService.setOntologyService(this.ontologyService);
		this.csvExportStudyService.setContextUtil(this.contextUtil);

		final Property prop = Mockito.mock(Property.class);
		Mockito.doReturn(prop).when(this.ontologyService).getProperty(TermId.BREEDING_METHOD_PROP.getId());
		Mockito.doReturn(new Term(1, CsvExportStudyServiceImplTest.PROPERTY_NAME, "Dummy defintion")).when(prop).getTerm();
		Mockito.doReturn(Mockito.mock(File.class)).when(this.germplasmExportService)
				.generateCSVFile(Matchers.any(List.class), Matchers.any(List.class), Matchers.anyString());
		Mockito.doReturn(ZIP_FILEPATH).when(this.csvExportStudyService).createZipFile(Matchers.anyString(), Matchers.anyListOf(String.class));

		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
	}

	@Test
	public void testExportTrialMultipleInstances() throws IOException {
		final int numberOfInstances = 2;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, numberOfInstances);
		workbook.setExportArrangedObservations(workbook.getObservations());
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		Mockito.doReturn(workbook.getObservations()).when(this.csvExportStudyService)
				.getApplicableObservations(workbook, instances);

		final FileExportInfo exportInfo =
				this.csvExportStudyService.export(workbook, CsvExportStudyServiceImplTest.STUDY_NAME, instances);

		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(numberOfInstances, outputDirectories.size());
		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.germplasmExportService, Mockito.times(numberOfInstances)).generateCSVFile(Matchers.any(List.class),
				Matchers.any(List.class), filenameCaptor.capture());
		final List<String> filePaths = filenameCaptor.getAllValues();
		Mockito.verify(this.csvExportStudyService).createZipFile(CsvExportStudyServiceImplTest.STUDY_NAME, filePaths);
		Assert.assertEquals(numberOfInstances, filePaths.size());
		for (final String path : filePaths) {
			final File outputFile = new File(path);
			Assert.assertTrue(outputDirectories.contains(outputFile.getParentFile()));
		}
		Assert.assertEquals(CsvExportStudyServiceImplTest.STUDY_NAME + ZIP_EXT, exportInfo.getDownloadFileName());
		Assert.assertEquals(ZIP_FILEPATH, exportInfo.getFilePath());
	}

	@Test
	public void testExportTrialWith1Instance() throws IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 1);
		workbook.setExportArrangedObservations(workbook.getObservations());
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
		Mockito.doReturn(workbook.getObservations()).when(this.csvExportStudyService)
				.getApplicableObservations(workbook, instances);

		final FileExportInfo exportInfo =
				this.csvExportStudyService.export(workbook, CsvExportStudyServiceImplTest.STUDY_NAME, instances);

		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(1, outputDirectories.size());
		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.germplasmExportService).generateCSVFile(Matchers.any(List.class),
				Matchers.any(List.class), filenameCaptor.capture());
		final String filePath = filenameCaptor.getValue();
		final File outputFile = new File(filePath);
		Assert.assertTrue(outputDirectories.contains(outputFile.getParentFile()));
		Assert.assertEquals(CsvExportStudyServiceImplTest.STUDY_NAME + "-1_Location_1" + CSV_EXT, exportInfo.getDownloadFileName());
		Assert.assertEquals(filePath, exportInfo.getFilePath());
	}

	@Test
	public void testCSVStudyExportForNurseryStusy() throws IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, new StudyTypeDto("N"));
		workbook.setExportArrangedObservations(workbook.getObservations());
		final List<Integer> instances = new ArrayList<Integer>();
		instances.add(1);

		Mockito.doReturn(workbook.getObservations()).when(this.csvExportStudyService)
				.getApplicableObservations(workbook, instances);

		final FileExportInfo exportInfo =
				this.csvExportStudyService.export(workbook, CsvExportStudyServiceImplTest.STUDY_NAME, instances);

		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(1, outputDirectories.size());
		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.germplasmExportService).generateCSVFile(Matchers.any(List.class),
				Matchers.any(List.class), filenameCaptor.capture());
		final String filePath = filenameCaptor.getValue();
		final File outputFile = new File(filePath);
		Assert.assertTrue(outputDirectories.contains(outputFile.getParentFile()));
		Assert.assertEquals(CsvExportStudyServiceImplTest.STUDY_NAME + "-" + instances.get(0) + "_Location_1" + CSV_EXT,
			exportInfo.getDownloadFileName());
		Assert.assertEquals(filePath, exportInfo.getFilePath());
	}

	@Test
	public void testGetExportColumnHeadersWhenVisibleColumnsIsNull() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, new StudyTypeDto("N"));
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		this.csvExportStudyService.getExportColumnHeaders(null, variables);

		Mockito.verify(this.csvExportStudyService, Mockito.times(0)).getColumnsBasedOnVisibility(null,
				variables.get(0));
	}

	@Test
	public void testGetExportColumnHeadersWhenVisibleColumnsHasValues() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, new StudyTypeDto("N"));
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		final List<Integer> visibleColumns = this.getVisibleColumnListWithOutRequiredColumns();
		this.csvExportStudyService.getExportColumnHeaders(visibleColumns, variables);

		for (final MeasurementVariable variable : variables) {
			Mockito.verify(this.csvExportStudyService, Mockito.times(1)).getColumnsBasedOnVisibility(
					visibleColumns, variable);
		}
	}

	@Test
	public void testGetColumnsBasedOnVisibilityWhenTheColumnHeadersIncludeAllRequiredFields() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, new StudyTypeDto("N"));
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		final List<Integer> visibleColumns = this.getVisibleColumnListWithRequiredColumns();

		int noOfVisibleColumns = 0;
		for (final MeasurementVariable variable : variables) {
			final ExportColumnHeader columnHeader =
					this.csvExportStudyService.getColumnsBasedOnVisibility(visibleColumns, variable);

			if (visibleColumns.contains(columnHeader.getId())) {
				Assert.assertTrue("Expected that the generated export column header is visible but didn't. ", columnHeader.isDisplay());
				noOfVisibleColumns++;
			} else {
				Assert.assertFalse("Expected that the generated export column header is not visible but didn't.", columnHeader.isDisplay());
			}
		}

		Assert.assertEquals("Expected that the no of visible column headers is " + visibleColumns.size() + " but returned "
				+ noOfVisibleColumns, visibleColumns.size(), noOfVisibleColumns);
	}

	@Test
	public void testGetColumnsBasedOnVisibilityWhenSomeRequiredColumnHeadersIsNotIncluded() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, new StudyTypeDto("N"));
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		final List<Integer> visibleColumns = this.getVisibleColumnListWithSomeRequiredColumns();

		int noOfVisibleColumns = 0;
		for (final MeasurementVariable variable : variables) {
			final ExportColumnHeader columnHeader =
					this.csvExportStudyService.getColumnsBasedOnVisibility(visibleColumns, variable);

			if (visibleColumns.contains(columnHeader.getId())) {
				noOfVisibleColumns++;
				Assert.assertTrue("Expected that the generated export column header is visible but didn't. ", columnHeader.isDisplay());
			} else {
				if (ExportImportStudyUtil.partOfRequiredColumns(columnHeader.getId())) {
					noOfVisibleColumns++;
					Assert.assertTrue(
							"Expected that the generated export column header for required columns are always visible but didn't.",
							columnHeader.isDisplay());
				} else {
					Assert.assertFalse("Expected that the generated export column header is not visible but didn't.",
							columnHeader.isDisplay());
				}
			}
		}

		final int expectedNoOfColumns = visibleColumns.size() + 2;
		Assert.assertEquals("Expected that the no of visible column headers is " + expectedNoOfColumns + " but returned "
				+ noOfVisibleColumns, expectedNoOfColumns, noOfVisibleColumns);
	}

	private Object getNoOfVisibleColumns(final List<ExportColumnHeader> visibleColumnHeaders) {
		int visibleColumns = 0;

		for (final ExportColumnHeader column : visibleColumnHeaders) {
			if (column.isDisplay()) {
				visibleColumns++;
			}
		}
		return visibleColumns;
	}

	private List<Integer> getVisibleColumnListWithOutRequiredColumns() {
		final List<Integer> visibleColumns = new ArrayList<Integer>();

		visibleColumns.add(TermId.CROSS.getId());
		visibleColumns.add(TermId.GID.getId());

		return visibleColumns;
	}

	private List<Integer> getVisibleColumnListWithSomeRequiredColumns() {
		final List<Integer> visibleColumns = new ArrayList<Integer>();

		visibleColumns.add(TermId.PLOT_NO.getId());
		visibleColumns.add(TermId.CROSS.getId());
		visibleColumns.add(TermId.GID.getId());

		return visibleColumns;
	}

	private List<Integer> getVisibleColumnListWithRequiredColumns() {
		final List<Integer> visibleColumns = new ArrayList<Integer>();

		visibleColumns.add(TermId.PLOT_NO.getId());
		visibleColumns.add(TermId.DESIG.getId());
		visibleColumns.add(TermId.ENTRY_NO.getId());
		visibleColumns.add(TermId.GID.getId());

		return visibleColumns;
	}

	@Test
	public void testGetExportColumnValues() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, new StudyTypeDto("N"));
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();
		final List<Integer> visibleColumns = this.getVisibleColumnListWithRequiredColumns();
		final List<ExportColumnHeader> columnHeaders =
				this.csvExportStudyService.getExportColumnHeaders(visibleColumns, variables);
		final List<MeasurementRow> observations = workbook.getObservations();

		final List<Map<Integer, ExportColumnValue>> columnValuesForAllRows =
				this.csvExportStudyService.getExportColumnValues(columnHeaders, observations);

		Assert.assertEquals("Expecting the no of entries of column values equal to the original no of observatios but didn't. ",
				columnValuesForAllRows.size(), observations.size());
	}

	@Test
	public void testGetColumnValueMap() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, new StudyTypeDto("N"));
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();
		final List<Integer> visibleColumns = this.getVisibleColumnListWithRequiredColumns();
		final List<ExportColumnHeader> columnHeaders =
				this.csvExportStudyService.getExportColumnHeaders(visibleColumns, variables);
		final MeasurementRow row = workbook.getObservations().get(0);

		final Map<Integer, ExportColumnValue> columnValuesInARow =
				this.csvExportStudyService.getColumnValueMap(columnHeaders, row);

		Assert.assertEquals("Expecting the number of generated column values in a row "
				+ "is equal to the number of visible column headers but didn't.", columnValuesInARow.size(),
				this.getNoOfVisibleColumns(columnHeaders));

		for (final ExportColumnHeader columnHeader : columnHeaders) {
			if (columnHeader.isDisplay() && !columnValuesInARow.containsKey(columnHeader.getId())) {
				Assert.fail("Expecting that the ids of visibleColumnHeaders can be found in the generated list of column values in a row but didn't.");
			}
		}
	}

	@Test
	public void testGetColumnValue() {
		// For categorical variables
		final MeasurementData data = this.getMeasurementData();
		data.setMeasurementVariable(this.getMeasurementVariableForCategoricalVariable()); // set categorical values

		ExportColumnValue columnValue = this.csvExportStudyService.getColumnValue(data, TermId.ENTRY_NO.getId());
		Assert.assertNotNull("Expected that there is a newly created ExportColumnValue object but didn't.", columnValue);

		// For non-categorical variables
		data.setMeasurementVariable(this.getMeasureVariableForNumericalVariable());
		columnValue = this.csvExportStudyService.getColumnValue(data, TermId.ENTRY_NO.getId());
		Assert.assertNotNull("Expected that there is a newly created ExportColumnValue object but didn't.", columnValue);

		try {
			Mockito.verify(this.csvExportStudyService, Mockito.times(1)).getCategoricalCellValue(data);
		} catch (final NeverWantedButInvoked e) {
			Assert.fail(e.getMessage());
		}

		// for non-categorical variable with numeric data type
		data.setDataType(AppConstants.NUMERIC_DATA_TYPE.getString());
		data.setValue("3.1416");
		columnValue = this.csvExportStudyService.getColumnValue(data, TermId.ENTRY_NO.getId());
		Assert.assertNotNull("Expected that there is a newly created ExportColumnValue object but didn't.", columnValue);
	}

	@Test
	public void testGetNumericColumnValueIfMissing() {
		final MeasurementData dataCell = new MeasurementData();
		dataCell.setValue(MeasurementData.MISSING_VALUE);
		final Integer termId = 2001;
		final ExportColumnValue columnValue = this.csvExportStudyService.getNumericColumnValue(dataCell, termId);
		Assert.assertEquals("Value should be missing", MeasurementData.MISSING_VALUE, columnValue.getValue());
	}

	@Test
	public void testGetNumericColumnValueIfNotMissing() {
		final MeasurementData dataCell = new MeasurementData();
		dataCell.setValue("20");
		final Integer termId = 2001;
		final ExportColumnValue columnValue = this.csvExportStudyService.getNumericColumnValue(dataCell, termId);
		Assert.assertEquals("Value should be 20", Double.valueOf("20").toString(), columnValue.getValue());
	}

	@Test
	public void testGetFileExtension() {
		Assert.assertEquals(AppConstants.EXPORT_CSV_SUFFIX.getString(), this.csvExportStudyService.getFileExtension());
	}

	private MeasurementData getMeasurementData() {
		return new MeasurementData(WorkbookDataUtil.ENTRY, String.valueOf(1));
	}

	private MeasurementVariable getMeasurementVariableForCategoricalVariable() {
		final MeasurementVariable variable =
				new MeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL", "TRIAL NUMBER", WorkbookDataUtil.NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		variable.setPossibleValues(this.getValueReferenceList());
		return variable;
	}

	private MeasurementVariable getMeasureVariableForNumericalVariable() {
		final MeasurementVariable variable =
				new MeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL", "TRIAL NUMBER", WorkbookDataUtil.NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		variable.setPossibleValues(new ArrayList<ValueReference>());
		return variable;
	}

	private List<ValueReference> getValueReferenceList() {
		final List<ValueReference> possibleValues = new ArrayList<ValueReference>();

		for (int i = 0; i < 5; i++) {
			final ValueReference possibleValue = new ValueReference(i, String.valueOf(i));
			possibleValues.add(possibleValue);
		}
		return possibleValues;
	}

	private List<File> getTempOutputDirectoriesGenerated() {
		final String genericOutputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final String toolDirectory = genericOutputDirectoryPath.substring(0, genericOutputDirectoryPath.indexOf(InstallationDirectoryUtil.OUTPUT));
		File toolDirectoryFile = new File(toolDirectory);
		Assert.assertTrue(toolDirectoryFile.exists());
		List<File> outputDirectoryFiles = new ArrayList<>();
		for (final File file : toolDirectoryFile.listFiles()) {
			if (file.getName().startsWith("output") && file.getName() != InstallationDirectoryUtil.OUTPUT && file.isDirectory()) {
				outputDirectoryFiles.add(file);
			}
		}
		return outputDirectoryFiles;
	}

	@After
	public void cleanup() {
		this.deleteTestInstallationDirectory();
	}

	private void deleteTestInstallationDirectory() {
		// Delete test installation directory and its contents as part of cleanup
		final File testInstallationDirectory = new File(InstallationDirectoryUtil.WORKSPACE_DIR);
		this.installationDirectoryUtil.recursiveFileDelete(testInstallationDirectory);
	}
}
