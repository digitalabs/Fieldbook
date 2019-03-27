package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import org.generationcp.commons.constant.AppConstants;
import junit.framework.Assert;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.context.MessageSource;
import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.LocationTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExcelExportStudyServiceImplTest {

	private static final String XLS_EXT = ".xls";
	private static final String ZIP_EXT = ".zip";
	private static final String NO_RANGE = "All values allowed";
	private static final String MAX_ONLY = " and below";
	private static final String MIN_ONLY = " and above";

	private static final int TEST_VARIABLE_TERMID = 1;
	private static final String STUDY_NAME = "Test Study";
	private static final String ZIP_FILEPATH = "./someDirectory/output/TestFileName.zip";

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;
	
	@Mock
	protected ContextUtil contextUtil;

	@Mock
	private LocationDataManager locationDataManager;

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private ExcelExportStudyServiceImpl excelExportStudyService;
	
	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(fieldbookService.getAllPossibleValues(TEST_VARIABLE_TERMID)).thenReturn(this.createPossibleValues());
		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
	}

	@Test
	public void testWriteSectionRow() {

		final int rowNumber = 1;

		final HSSFSheet sheet = new HSSFWorkbook().createSheet();

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(TEST_VARIABLE_TERMID);
		measurementVariable.setName("Sample Name");
		measurementVariable.setDescription("Sample Description");
		measurementVariable.setProperty("Sample Property");
		measurementVariable.setScale("Sample Scale");
		measurementVariable.setMethod("Sample Method");
		measurementVariable.setValue("1");
		measurementVariable.setVariableType(VariableType.STUDY_DETAIL);
		measurementVariable.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		measurementVariable.setLabel("Sample Label");

		this.excelExportStudyService.writeSectionRow(rowNumber, sheet, measurementVariable);

		final HSSFRow row = sheet.getRow(rowNumber);

		// Verify the cells and their values created
		Assert.assertEquals(measurementVariable.getName(),
				row.getCell(ExcelExportStudyServiceImpl.VARIABLE_NAME_COLUMN_INDEX).getStringCellValue());
		Assert.assertEquals(measurementVariable.getDescription(),
				row.getCell(ExcelExportStudyServiceImpl.DESCRIPTION_COLUMN_INDEX).getStringCellValue());
		Assert.assertEquals(measurementVariable.getProperty(),
				row.getCell(ExcelExportStudyServiceImpl.PROPERTY_COLUMN_INDEX).getStringCellValue());
		Assert.assertEquals(measurementVariable.getScale(),
				row.getCell(ExcelExportStudyServiceImpl.SCALE_COLUMN_INDEX).getStringCellValue());
		Assert.assertEquals(measurementVariable.getMethod(),
				row.getCell(ExcelExportStudyServiceImpl.METHOD_COLUMN_INDEX).getStringCellValue());
		Assert.assertEquals("C", row.getCell(ExcelExportStudyServiceImpl.DATATYPE_COLUMN_INDEX).getStringCellValue());
		Assert.assertEquals("A", row.getCell(ExcelExportStudyServiceImpl.VARIABLE_VALUE_COLUMN_INDEX).getStringCellValue());
		Assert.assertEquals(measurementVariable.getLabel(),
				row.getCell(ExcelExportStudyServiceImpl.LABEL_COLUMN_INDEX).getStringCellValue());

	}

	@Test
	public void testGetLabel() {

		final MeasurementVariable measurementVariable = new MeasurementVariable();

		measurementVariable.setTreatmentLabel("TreatmentLabel");
		measurementVariable.setLabel("Label");

		// If Treatment Label available, return  measurementVariable.getTreatmentLabel() value.
		Assert.assertEquals("TreatmentLabel", this.excelExportStudyService.getLabel(measurementVariable));

		measurementVariable.setLabel("Label");
		measurementVariable.setTreatmentLabel("");

		// If Treatment Label has no value, return measurementVariable.getLabel()'s value
		Assert.assertEquals("Label", this.excelExportStudyService.getLabel(measurementVariable));

	}

	@Test
	public void testSetContentOfVariableValueColumnVariableIsATrait() {

		final HSSFCell cell = Mockito.mock(HSSFCell.class);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setPossibleValues(this.createPossibleValues());
		measurementVariable.setValue("1");
		measurementVariable.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		measurementVariable.setVariableType(VariableType.TRAIT);

		this.excelExportStudyService.setContentOfVariableValueColumn(cell, measurementVariable);

		final ArgumentCaptor<String> cellValueCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(cell).setCellValue(cellValueCaptor.capture());

		Assert.assertEquals("A", cellValueCaptor.getValue());

	}

	@Test
	public void testSetContentOfVariableValueColumnVariableDefault() {

		final HSSFCell cell = Mockito.mock(HSSFCell.class);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setPossibleValues(this.createPossibleValues());
		measurementVariable.setValue("1");
		measurementVariable.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		measurementVariable.setVariableType(VariableType.STUDY_DETAIL);

		this.excelExportStudyService.setContentOfVariableValueColumn(cell, measurementVariable);

		final ArgumentCaptor<String> cellValueCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(cell).setCellValue(cellValueCaptor.capture());

		Assert.assertEquals("A", cellValueCaptor.getValue());

	}

	@Test
	public void testSetVariableValueBasedOnDataTypeDataTypeIsNumerical() {

		final HSSFCell cell = Mockito.mock(HSSFCell.class);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setValue("100.111");
		measurementVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());

		this.excelExportStudyService.setVariableValueBasedOnDataType(cell, measurementVariable);

		final ArgumentCaptor<Integer> cellTypeCaptor = ArgumentCaptor.forClass(Integer.class);
		final ArgumentCaptor<Double> cellValueCaptor = ArgumentCaptor.forClass(Double.class);

		Mockito.verify(cell).setCellType(cellTypeCaptor.capture());
		Mockito.verify(cell).setCellValue(cellValueCaptor.capture());

		// If the datatype is numeric, the variable's value must be converted to double
		// and the cell's format should be CELL_TYPE_NUMERIC
		Assert.assertEquals(Cell.CELL_TYPE_NUMERIC, cellTypeCaptor.getValue().intValue());
		Assert.assertEquals(100.111d, cellValueCaptor.getValue().doubleValue());

	}

	@Test
	public void testSetVariableValueBasedOnDataTypeDataTypeIsCategorical() {

		final HSSFCell cell = Mockito.mock(HSSFCell.class);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setPossibleValues(this.createPossibleValues());
		measurementVariable.setValue("1");
		measurementVariable.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());

		this.excelExportStudyService.setVariableValueBasedOnDataType(cell, measurementVariable);

		final ArgumentCaptor<String> cellValueCaptor = ArgumentCaptor.forClass(String.class);

		Mockito.verify(cell).setCellValue(cellValueCaptor.capture());

		// If the datatype is categorical then the variable's value (which is categorical id) must be converted to
		// categorical name it represents
		Assert.assertEquals("A", cellValueCaptor.getValue());

	}

	@Test
	public void testSetVariableValueBasedOnDataTypeDataTypeDefault() {

		final HSSFCell cell = Mockito.mock(HSSFCell.class);
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setValue("1");
		measurementVariable.setDataTypeId(DataType.CHARACTER_VARIABLE.getId());

		this.excelExportStudyService.setVariableValueBasedOnDataType(cell, measurementVariable);

		final ArgumentCaptor<String> cellValueCaptor = ArgumentCaptor.forClass(String.class);

		Mockito.verify(cell).setCellValue(cellValueCaptor.capture());

		// If the datatype is not categorical or numeruc then the variable's value should be set to the cell's value
		Assert.assertEquals("1", cellValueCaptor.getValue());

	}

	@Test
	public void testGetPossibleValueDetailAsStringBasedOnDataTypeDataTypeIsCategorical() {

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setDataTypeId(DataType.CATEGORICAL_VARIABLE.getId());
		measurementVariable.setPossibleValues(this.createPossibleValues());

		Assert.assertEquals("A/B/C", this.excelExportStudyService.getPossibleValueDetailAsStringBasedOnDataType(measurementVariable));

	}

	@Test
	public void testGetPossibleValueDetailAsStringBasedOnDataTypeDataTypeIsNumeric() {

		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setDataTypeId(DataType.NUMERIC_VARIABLE.getId());
		measurementVariable.setMinRange(1.1d);
		measurementVariable.setMaxRange(2.2d);

		Assert.assertEquals("1.1 - 2.2", this.excelExportStudyService.getPossibleValueDetailAsStringBasedOnDataType(measurementVariable));

	}

	@Test
	public void testGetPossibleValueDetailAsStringBasedOnDataTypeDataTypeDefault() {

		final String variableValue = "VariableValue";
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setDataTypeId(DataType.CHARACTER_VARIABLE.getId());
		measurementVariable.setValue(variableValue);

		Assert.assertEquals("If the data type is not numeric or categorical, the value should come from the variable's value as is.",
				variableValue, this.excelExportStudyService.getPossibleValueDetailAsStringBasedOnDataType(measurementVariable));

	}

	@Test
	public void testConvertPossibleValuesToString() {

		final List<ValueReference> possibleValues = createPossibleValues();

		Assert.assertEquals("A/B/C", this.excelExportStudyService
				.convertPossibleValuesToString(possibleValues, ExcelExportStudyServiceImpl.POSSIBLE_VALUES_AS_STRING_DELIMITER));
		Assert.assertEquals("Return blank if the possible values list is empty", "", this.excelExportStudyService
				.convertPossibleValuesToString(new ArrayList<ValueReference>(),
						ExcelExportStudyServiceImpl.POSSIBLE_VALUES_AS_STRING_DELIMITER));

	}

	@Test
	public void testConcatenateMinMaxValueIfAvailable() {

		final MeasurementVariable measurementVariable = new MeasurementVariable();

		measurementVariable.setMinRange(1.1d);
		measurementVariable.setMaxRange(2.2d);

		Assert.assertEquals("1.1 - 2.2", excelExportStudyService.concatenateMinMaxValueIfAvailable(measurementVariable));

		measurementVariable.setMinRange(null);
		measurementVariable.setMaxRange(null);
		Assert.assertEquals("If both min and max value are null, the return value should be empty", NO_RANGE,
				excelExportStudyService.concatenateMinMaxValueIfAvailable(measurementVariable));

		measurementVariable.setMinRange(1.1);
		measurementVariable.setMaxRange(null);
		Assert.assertEquals("If has min value  but max is null", "1.1" + MIN_ONLY,
				excelExportStudyService.concatenateMinMaxValueIfAvailable(measurementVariable));

		measurementVariable.setMinRange(null);
		measurementVariable.setMaxRange(2.2);
		Assert.assertEquals("If has max value  but min is null", "2.2" + MAX_ONLY,
				excelExportStudyService.concatenateMinMaxValueIfAvailable(measurementVariable));
	}

	@Test
	public void testPopulateFilteredConditionsForLocationID() {

		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.LOCATION_ID.getId(), "LOCATION_NAME", "1990");
		variable.setRole(PhenotypicType.TRIAL_ENVIRONMENT);
		final MeasurementRow trialObservation = Mockito.mock(MeasurementRow.class);
		final List<MeasurementVariable> filteredConditions = new ArrayList<>();

		final Location location = LocationTestDataInitializer.createLocation(1990, "WARDA");
		Mockito.when(trialObservation.getMeasurementDataValue(variable.getTermId())).thenReturn("1990");
		Mockito.when(this.locationDataManager.getLocationByID(1990)).thenReturn(location);
		Mockito.when(this.fieldbookService.getStandardVariable(TermId.TRIAL_LOCATION.getId())).thenReturn(
				StandardVariableTestDataInitializer.createStandardVariable());

		this.excelExportStudyService.populateFilteredConditions(trialObservation, filteredConditions, variable);
		Assert.assertEquals(2, filteredConditions.size());
		Assert.assertEquals(TermId.LOCATION_ID.getId(), filteredConditions.get(0).getTermId());
		Assert.assertEquals(TermId.LOCATION_ID.name(), filteredConditions.get(0).getName());
		Assert.assertEquals(TermId.TRIAL_LOCATION.getId(), filteredConditions.get(1).getTermId());
		Assert.assertEquals("LOCATION_NAME", filteredConditions.get(1).getName());
	}

	@Test
	public void testPopulateFilteredConditions() {
		final MeasurementRow trialObservation = Mockito.mock(MeasurementRow.class);
		final List<MeasurementVariable> filteredConditions = new ArrayList<>();
		final MeasurementVariable variable = MeasurementVariableTestDataInitializer.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL_INSTANCE", "1");
		variable.setRole(PhenotypicType.TRIAL_ENVIRONMENT);

		this.excelExportStudyService.populateFilteredConditions(trialObservation, filteredConditions, variable);
		Assert.assertEquals(1, filteredConditions.size());
		Assert.assertEquals(TermId.TRIAL_INSTANCE_FACTOR.getId(), filteredConditions.get(0).getTermId());
		Assert.assertEquals("TRIAL_INSTANCE", filteredConditions.get(0).getName());
	}
	
	@Test
	public void testExportStudyMultipleInstances() throws IOException {
		final int numberOfInstances = 2;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(20, numberOfInstances);
		workbook.setExportArrangedObservations(workbook.getObservations());
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);
	
		final ExcelExportStudyServiceImpl spyComponent = this.setupExcelExportServiceSpy();
		final FileExportInfo exportInfo =
				spyComponent.export(workbook, ExcelExportStudyServiceImplTest.STUDY_NAME, instances);

		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(numberOfInstances, outputDirectories.size());
		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(spyComponent, Mockito.times(numberOfInstances)).writeOutputFile(Mockito.any(Workbook.class), ArgumentMatchers.<List<Integer>>isNull(),
				Mockito.any(MeasurementRow.class), Mockito.anyListOf(MeasurementRow.class), filenameCaptor.capture());
		final List<String> filePaths = filenameCaptor.getAllValues();
		Assert.assertEquals(numberOfInstances, filePaths.size());
		Mockito.verify(spyComponent).createZipFile(ExcelExportStudyServiceImplTest.STUDY_NAME, filePaths);
		for (final String path : filePaths) {
			final File outputFile = new File(path);
			Assert.assertTrue(outputDirectories.contains(outputFile.getParentFile()));
		}
		Assert.assertEquals(ExcelExportStudyServiceImplTest.STUDY_NAME + ZIP_EXT, exportInfo.getDownloadFileName());
		Assert.assertEquals(ZIP_FILEPATH, exportInfo.getFilePath());
	}
	
	@Test
	public void testExportStudyWith1Instance() throws IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForStudy(20, 1);
		workbook.setExportArrangedObservations(workbook.getObservations());
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);

		final ExcelExportStudyServiceImpl spyComponent = this.setupExcelExportServiceSpy();
		Mockito.doReturn("fileName")
				.when(messageSource)
				.getMessage(Matchers.anyString(), ArgumentMatchers.<Object[]>isNull(), Matchers.any(Locale.class));

		final FileExportInfo exportInfo =
				spyComponent.export(workbook, ExcelExportStudyServiceImplTest.STUDY_NAME, instances);

		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		Assert.assertEquals(1, outputDirectories.size());
		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(spyComponent).writeOutputFile(Mockito.any(Workbook.class), ArgumentMatchers.<List<Integer>>isNull(),
				Mockito.any(MeasurementRow.class), Mockito.anyListOf(MeasurementRow.class), filenameCaptor.capture());
		final String filePath = filenameCaptor.getValue();
		final File outputFile = new File(filePath);
		Assert.assertTrue(outputDirectories.contains(outputFile.getParentFile()));
		Assert.assertEquals(ExcelExportStudyServiceImplTest.STUDY_NAME + "-1_Location_1" + XLS_EXT, exportInfo.getDownloadFileName());
		Assert.assertEquals(filePath, exportInfo.getFilePath());
	}

	private ExcelExportStudyServiceImpl setupExcelExportServiceSpy() throws IOException {
		final ExcelExportStudyServiceImpl spyComponent = Mockito.spy(this.excelExportStudyService);
		Mockito.doNothing().when(spyComponent).writeOutputFile(Mockito.any(Workbook.class), ArgumentMatchers.<List<Integer>>isNull(),
				Mockito.any(MeasurementRow.class), Mockito.anyListOf(MeasurementRow.class), Mockito.anyString());
		Mockito.doReturn(ZIP_FILEPATH).when(spyComponent).createZipFile(Matchers.anyString(), Matchers.anyListOf(String.class));
		return spyComponent;
	}
	@Test
	public void testGetFileExtension() {
		Assert.assertEquals(AppConstants.EXPORT_XLS_SUFFIX.getString(), this.excelExportStudyService.getFileExtension());
	}

	private List<ValueReference> createPossibleValues() {
		final List<ValueReference> possibleValues = new ArrayList<>();
		possibleValues.add(new ValueReference(1, "A", ""));
		possibleValues.add(new ValueReference(2, "B", ""));
		possibleValues.add(new ValueReference(3, "C", ""));
		return possibleValues;
	}
	
	private List<File> getTempOutputDirectoriesGenerated() {
		final String genericOutputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final String toolDirectory = genericOutputDirectoryPath.substring(0, genericOutputDirectoryPath.indexOf(InstallationDirectoryUtil.OUTPUT));
		final File toolDirectoryFile = new File(toolDirectory);
		Assert.assertTrue(toolDirectoryFile.exists());
		final List<File> outputDirectoryFiles = new ArrayList<>();
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
