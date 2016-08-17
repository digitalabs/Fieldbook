
package com.efficio.fieldbook.web.study.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementDataTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;

import junit.framework.Assert;

@RunWith(value = MockitoJUnitRunner.class)
public class ExcelImportStudyServiceImplTest {

	private static final String TRIAL_INSTANCE_NO = "1";
	private static final String CATEGORICAL_ID = ExcelImportStudyServiceImplTest.TRIAL_INSTANCE_NO;
	private static final String POSSIBLE_VALUE_NAME = "Possible Value Name";
	private static final String TEMPLATE_SECTION_CONDITION = "CONDITION";
	private static final String TEMPLATE_SECTION_FACTOR = "FACTOR";
	private static final String DUPLICATE_PSMR_ERROR_MESSAGE = "The system detected duplicate variable for CONDITION";
	private static final String NON_EXISTENT_VARIATE_ERROR_MESSAGE =
			"The system detected a new variate CONDITION that does not exist.  Please use the Ontology Manager to add a new variate.";
	private static final String EXISTING_VARIATE_ERROR_MESSAGE =
			"The system detected a new variate CONDITION that already exist in the study.";

	private static final String LABEL = "Label";
	private static final String METHOD = "Method";
	private static final String SCALE = "Scale";
	private static final String PROPERTY = "Property";
	private static final String DESCRIPTION = "Description";
	private final String testValue = "testValue";
	private final String currentValue = "currentValue";

	private MeasurementData wData;
	private int columnIndex;
	private int termId;
	private Row xlsRow;
	private Cell cell;
	private Workbook workbook;

	private Cell descriptionCell;
	private Cell propertyCell;
	private Cell scaleCell;
	private Cell methodCell;
	private Cell labelCell;
	private Cell trialInstanceCell;
	private org.apache.poi.ss.usermodel.Workbook xlsBook;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private MessageSource messageSource;

	private ExcelImportStudyServiceImpl importStudy;

	private MeasurementVariableTestDataInitializer mvarTDI;

	private MeasurementDataTestDataInitializer measurementDataTDI;

	@Before
	public void setUp() {
		this.mvarTDI = new MeasurementVariableTestDataInitializer();
		this.measurementDataTDI = new MeasurementDataTestDataInitializer();
		this.initTestVariables();
	}

	private void initTestVariables() {
		this.wData = this.measurementDataTDI.createMeasurementData(this.currentValue);
		this.columnIndex = 1;
		this.termId = 2;

		this.xlsRow = Mockito.mock(Row.class);
		this.cell = Mockito.mock(Cell.class);
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook();

		this.descriptionCell = Mockito.mock(Cell.class);
		this.propertyCell = Mockito.mock(Cell.class);
		this.scaleCell = Mockito.mock(Cell.class);
		this.methodCell = Mockito.mock(Cell.class);
		this.labelCell = Mockito.mock(Cell.class);
		this.trialInstanceCell = Mockito.mock(Cell.class);

		Mockito.doReturn(ExcelImportStudyServiceImplTest.DESCRIPTION).when(this.descriptionCell).getStringCellValue();
		Mockito.doReturn(ExcelImportStudyServiceImplTest.PROPERTY).when(this.propertyCell).getStringCellValue();
		Mockito.doReturn(ExcelImportStudyServiceImplTest.SCALE).when(this.scaleCell).getStringCellValue();
		Mockito.doReturn(ExcelImportStudyServiceImplTest.METHOD).when(this.methodCell).getStringCellValue();
		Mockito.doReturn(ExcelImportStudyServiceImplTest.LABEL).when(this.labelCell).getStringCellValue();

		Mockito.doReturn("1001").when(this.contextUtil).getCurrentProgramUUID();

		this.xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		this.importStudy = new ExcelImportStudyServiceImpl(this.workbook, "", "");
		this.importStudy.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		this.importStudy.setContextUtil(this.contextUtil);
		this.importStudy.setMessageSource(this.messageSource);
	}

	@Test
	public void testImportDataCellValuesWhenExcelCellIsNotNull() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		this.wData.setMeasurementVariable(null);

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertEquals("MeasurementData value should be set from the cell value", this.wData.getValue(),
				this.cell.getStringCellValue());
	}

	@Test
	public void testImportDataCellValuesWhenExcelCellIsNull() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(null);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		this.wData.setMeasurementVariable(null);

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertEquals("MeasurementData value should still be the same since the cell value is null", this.wData.getValue(),
				this.currentValue);
	}

	@Test
	public void testImportDataCellValuesWhenDataHasExistingValue() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		final MeasurementVariable var = this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId());

		this.wData.setMeasurementVariable(var);
		this.wData.setValue("test value");

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenDataHasExistingValueAndCellIsNumeric() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_NUMERIC);
		Mockito.when(this.cell.getNumericCellValue()).thenReturn(Double.valueOf("1.2"));

		final MeasurementVariable var = this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId());

		this.wData.setMeasurementVariable(var);
		this.wData.setValue("test value");

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenDataHasExistingValueAndCellIsNumericAndHasPossibleValues() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_NUMERIC);
		Mockito.when(this.cell.getNumericCellValue()).thenReturn(Double.valueOf("1.2"));

		final MeasurementVariable var = this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId());

		this.wData.setMeasurementVariable(var);
		this.wData.setValue("test value");

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenDataHasExistingValueAndCellIsStringAndHasPossibleValues() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		final MeasurementVariable var = this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId());

		this.wData.setMeasurementVariable(var);
		this.wData.setValue("test value");

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertTrue("Workbook flag for has existing data overwrite should be true", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenDataHasNoExistingValue() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		this.wData.setValue(null);

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertFalse("Workbook flag for has existing data overwrite should be false", this.workbook.hasExistingDataOverwrite());
	}

	@Test
	public void testImportDataCellValuesWhenExcelCellIsNotNullAcceptedFlagMustAlwaysBeFalse() {

		Mockito.when(this.xlsRow.getCell(this.columnIndex)).thenReturn(this.cell);
		Mockito.when(this.cell.getCellType()).thenReturn(Cell.CELL_TYPE_STRING);
		Mockito.when(this.cell.getStringCellValue()).thenReturn(this.testValue);

		this.wData.setMeasurementVariable(null);

		this.importStudy.importDataCellValues(this.wData, this.xlsRow, this.columnIndex, this.workbook,
				new HashMap<Integer, MeasurementVariable>());
		Assert.assertFalse("The Accepted Flag must be always set to false", this.wData.isAccepted());
	}

	@Test
	public void testHasCellValueIfNull() {
		final boolean resp = this.importStudy.hasCellValue(null);
		Assert.assertFalse("Should return false since cell is null", resp);
	}

	@Test
	public void testIsPropertyScaleMethodLabelCellNotNull_ReturnsTrueIfAllFieldsIsNotNull() {
		Assert.assertTrue("Expecting to return true if Property,Scale,Method,Label is not null but didn't.",
				this.importStudy.isPropertyScaleMethodLabelCellNotNull(this.propertyCell, this.scaleCell, this.methodCell, this.labelCell));
	}

	@Test
	public void testIsPropertyScaleMethodLabelCellNotNull_ReturnsFalseIfAtLeastOneFieldIsNull() {
		this.labelCell = null;
		Assert.assertFalse("Expecting to return false if at least 1 field from Property,Scale,Method,Label is null but didn't.",
				this.importStudy.isPropertyScaleMethodLabelCellNotNull(this.propertyCell, this.scaleCell, this.methodCell, this.labelCell));
	}

	@Test
	public void testIsPropertyScaleMethodLabelCellHasStringValue_ReturnsTrueIfAllFieldsHasStringValue() {

		Assert.assertTrue("Expecting to return true if Property,Scale,Method,Label have string value but didn't.", this.importStudy
				.isPropertyScaleMethodLabelCellHasStringValue(this.propertyCell, this.scaleCell, this.methodCell, this.labelCell));
	}

	@Test
	public void testIsPropertyScaleMethodLabelCellHasStringValue_ReturnsFalseIfAtLeastOneFromFieldsHasNoStringValue() {

		Mockito.doReturn(null).when(this.labelCell).getStringCellValue();

		Assert.assertFalse("Expecting to return false if at least one from Property,Scale,Method,Label has no string value but didn't.",
				this.importStudy.isPropertyScaleMethodLabelCellHasStringValue(this.propertyCell, this.scaleCell, this.methodCell,
						this.labelCell));
	}

	@Test
	public void testGetTrialInstanceNumber_ForNursery() throws WorkbookParserException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.N);

		final org.apache.poi.ss.usermodel.Workbook xlsBook = Mockito.mock(org.apache.poi.ss.usermodel.Workbook.class);
		Assert.assertEquals("Expecting to return 1 for the value of trialInstance in Nursery but didn't.",
				ExcelImportStudyServiceImplTest.TRIAL_INSTANCE_NO, this.importStudy.getTrialInstanceNumber(workbook, xlsBook));
	}

	@Test
	public void testGetTrialInstanceNumber_ForTrial() throws WorkbookParserException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.T);

		this.setUpXLSWorkbookTestData();
		Mockito.doReturn(TermId.TRIAL_INSTANCE_FACTOR.getId()).when(this.fieldbookMiddlewareService)
				.getStandardVariableIdByPropertyScaleMethodRole(ExcelImportStudyServiceImplTest.PROPERTY,
						ExcelImportStudyServiceImplTest.SCALE, ExcelImportStudyServiceImplTest.METHOD,
						PhenotypicType.getPhenotypicTypeForLabel(ExcelImportStudyServiceImplTest.LABEL));

		final String toBeReturned = "2";
		Mockito.doReturn(toBeReturned).when(this.trialInstanceCell).getStringCellValue();

		Assert.assertEquals("Expecting to return the value returned from the getTrialInstaceNumber method but didn't.", toBeReturned,
				this.importStudy.getTrialInstanceNumber(workbook, this.xlsBook));
	}

	private void setUpXLSWorkbookTestData() {
		final Sheet descriptionSheet = Mockito.mock(Sheet.class);
		Mockito.doReturn(descriptionSheet).when(this.xlsBook).getSheetAt(0);
		final int noOfRows = 3;
		Mockito.doReturn(noOfRows).when(descriptionSheet).getLastRowNum();

		final Row conditionRow = Mockito.mock(Row.class);
		Mockito.doReturn(conditionRow).when(descriptionSheet).getRow(1);
		final Cell conditionCell = Mockito.mock(Cell.class);
		Mockito.doReturn(conditionCell).when(conditionRow).getCell(0);
		Mockito.doReturn(ExcelImportStudyServiceImplTest.TEMPLATE_SECTION_CONDITION).when(conditionCell).getStringCellValue();

		final Row factorRow = Mockito.mock(Row.class);
		Mockito.doReturn(factorRow).when(descriptionSheet).getRow(noOfRows);
		final Cell factorCell = Mockito.mock(Cell.class);
		Mockito.doReturn(factorCell).when(factorRow).getCell(0);
		Mockito.doReturn(ExcelImportStudyServiceImplTest.TEMPLATE_SECTION_FACTOR).when(factorCell).getStringCellValue();

		Mockito.doReturn(conditionRow).when(descriptionSheet).getRow(2);
		Mockito.doReturn(this.descriptionCell).when(conditionRow).getCell(1);
		Mockito.doReturn(this.propertyCell).when(conditionRow).getCell(2);
		Mockito.doReturn(this.scaleCell).when(conditionRow).getCell(3);
		Mockito.doReturn(this.methodCell).when(conditionRow).getCell(4);
		Mockito.doReturn(this.labelCell).when(conditionRow).getCell(7);

		Mockito.doReturn(this.descriptionCell).when(factorRow).getCell(1);
		Mockito.doReturn(this.propertyCell).when(factorRow).getCell(2);
		Mockito.doReturn(this.scaleCell).when(factorRow).getCell(3);
		Mockito.doReturn(this.methodCell).when(factorRow).getCell(4);
		Mockito.doReturn(this.labelCell).when(factorRow).getCell(7);

		Mockito.doReturn(Cell.CELL_TYPE_STRING).when(this.trialInstanceCell).getCellType();
		Mockito.doReturn(this.trialInstanceCell).when(conditionRow).getCell(6);
	}

	@Test
	public void testGetTrialInstanceNumber_ForTrial_ReturnsExceptionForNullTrialInstance() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.T);

		this.setUpXLSWorkbookTestData();
		Mockito.doReturn(null).when(this.fieldbookMiddlewareService).getStandardVariableIdByPropertyScaleMethodRole(
				ExcelImportStudyServiceImplTest.PROPERTY, ExcelImportStudyServiceImplTest.SCALE, ExcelImportStudyServiceImplTest.METHOD,
				PhenotypicType.getPhenotypicTypeForLabel(ExcelImportStudyServiceImplTest.LABEL));

		try {
			this.importStudy.getTrialInstanceNumber(workbook, this.xlsBook);
			Assert.fail("Expecting to return an exception when the trial instance from the xls file is null but didn't.");
		} catch (final WorkbookParserException e) {
			// do nothing
		}
	}

	@Test
	public void testGetXlsValue_MeasurementRowIsNull() {
		final MeasurementRow temp = null;
		final MeasurementVariable var = new MeasurementVariable();
		final MeasurementVariable origVar = new MeasurementVariable();
		final MeasurementData data = new MeasurementData();

		final String expectedValue = "tempValue";
		var.setValue(expectedValue);
		Assert.assertEquals("Expecting to return the value from var when the measurement row is null but didn't.", expectedValue,
				this.importStudy.getXlsValue(var, temp, data, origVar));
	}

	@Test
	public void testGetXlsValue_OrigVarPossibleValuesIsNull() {
		final MeasurementRow temp = new MeasurementRow();
		final MeasurementVariable var = new MeasurementVariable();
		final MeasurementVariable origVar = new MeasurementVariable();
		final MeasurementData data = new MeasurementData();

		origVar.setPossibleValues(null);

		final String expectedValue = "tempValue";
		var.setValue(expectedValue);
		Assert.assertEquals("Expecting to return the value from var when the origVar's possible value is null but didn't.", expectedValue,
				this.importStudy.getXlsValue(var, temp, data, origVar));
	}

	@Test
	public void testGetXlsValue_OrigVarPossibleValuesIsEmpty() {
		final MeasurementRow temp = new MeasurementRow();
		final MeasurementVariable var = new MeasurementVariable();
		final MeasurementVariable origVar = new MeasurementVariable();
		final MeasurementData data = new MeasurementData();

		origVar.setPossibleValues(new ArrayList<ValueReference>());

		final String expectedValue = "tempValue";
		var.setValue(expectedValue);
		Assert.assertEquals("Expecting to return the value from var when the origVar's possible value is empty but didn't.", expectedValue,
				this.importStudy.getXlsValue(var, temp, data, origVar));
	}

	@Test
	public void testGetXlsValue_ReturnsXlsValueFromCategoricalVariablePossibleValues() {
		final MeasurementRow temp = new MeasurementRow();

		final MeasurementVariable var = new MeasurementVariable();
		var.setValue(ExcelImportStudyServiceImplTest.POSSIBLE_VALUE_NAME);

		final MeasurementData data = new MeasurementData();

		final MeasurementVariable origVar = this.mvarTDI.createMeasurementVariable(this.termId, TermId.CATEGORICAL_VARIABLE.getId());
		origVar.setValue(ExcelImportStudyServiceImplTest.POSSIBLE_VALUE_NAME);

		Assert.assertEquals("Expecting to return the value from getCategoricalIdCellValue() but didn't.",
				ExcelImportStudyServiceImplTest.CATEGORICAL_ID, this.importStudy.getXlsValue(var, temp, data, origVar));
	}

	@Test
	public void testIsMatchingPropertyScaleMethodLabel_ReturnsTrueIfAllFieldsValueAreMatched() {

		final MeasurementVariable var = this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId());
		final MeasurementVariable temp = this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId());
		Assert.assertTrue(
				"Expecting to return true if all values of property, scale, method and label of two measurement variables are the same.",
				this.importStudy.isMatchingPropertyScaleMethodLabel(var, temp));
	}

	@Test
	public void testIsMatchingPropertyScaleMethodLabel_ReturnsFalseIfAtLeast1FromFieldsValueAreNotMatched() {

		final MeasurementVariable var = this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId());
		final MeasurementVariable temp = this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId());
		temp.setLabel(temp.getLabel() + "deviation");

		Assert.assertFalse(
				"Expecting to return false if at least 1 value from property, scale, method and label of two measurement variables are not the same.",
				this.importStudy.isMatchingPropertyScaleMethodLabel(var, temp));
	}

	@Test
	public void testCreateMeasurementRowsMap() {
		final List<MeasurementRow> observations = this.workbook.getObservations();

		final Map<String, MeasurementRow> measurementRowsMap = this.importStudy.createMeasurementRowsMap(observations, "1", true);
		Assert.assertEquals("The number of measurements in the measurementRowsMap should be equal to the number of the observationss",
				observations.size(), measurementRowsMap.size());
	}

	@Test(expected = WorkbookParserException.class)
	public void testGetTrialInstanceNumberOfNurseryWithError() throws WorkbookParserException {
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook(10, StudyType.T);
		this.importStudy.getTrialInstanceNo(this.workbook, "filename");
	}

	@Test
	public void testGetTrialInstanceNumberOfNurseryOfTrial() throws WorkbookParserException {
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook(10, StudyType.T);
		final String trialInstanceNumber = this.importStudy.getTrialInstanceNo(this.workbook, "filename-11");
		Assert.assertEquals("The trial instance number should be 11", "11", trialInstanceNumber);
	}

	@Test
	public void testCopyConditionsAndConstantsWorkbook() {
		this.workbook = WorkbookTestDataInitializer.getTestWorkbook();
		this.importStudy.copyConditionsAndConstants(this.workbook);

		Assert.assertNotNull("Conditions copy should not be emprt after copy operation", this.workbook.getImportConditionsCopy());
		Assert.assertTrue("Unable to properly copy conditions portion of workbook",
				this.workbook.getImportConditionsCopy().size() == this.workbook.getConditions().size());
	}

	@Test
	public void testValidateVariates() {
		this.setUpXLSWorkbookTestData();
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(
				ExcelImportStudyServiceImplTest.PROPERTY, ExcelImportStudyServiceImplTest.SCALE, ExcelImportStudyServiceImplTest.METHOD,
				PhenotypicType.VARIATE, this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId()));

		try {
			List<String> addedVariates = WorkbookUtil.getAddedTraits(this.workbook.getAllVariables(), this.workbook.getObservations());
			Assert.assertTrue("The added variates should be empty", addedVariates.isEmpty());
			this.importStudy.validateVariates(this.xlsBook, this.workbook);
			addedVariates = WorkbookUtil.getAddedTraits(this.workbook.getAllVariables(), this.workbook.getObservations());
			Assert.assertFalse("The added variates should not be empty", addedVariates.isEmpty());
		} catch (final WorkbookParserException e) {
			Assert.fail("There should be no exceptions thrown.");
		}
	}

	@Test
	public void testValidateVariatesWithMiddleWareException() {
		this.setUpXLSWorkbookTestData();
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(
				ExcelImportStudyServiceImplTest.PROPERTY, ExcelImportStudyServiceImplTest.SCALE, ExcelImportStudyServiceImplTest.METHOD,
				PhenotypicType.VARIATE, this.contextUtil.getCurrentProgramUUID())).thenThrow(new MiddlewareException(""));
		Mockito.when(this.messageSource.getMessage(Matchers.eq("error.import.variate.duplicate.psmr"),
				Matchers.eq(new Object[] {ExcelImportStudyServiceImplTest.TEMPLATE_SECTION_CONDITION}),
				Matchers.eq(LocaleContextHolder.getLocale()))).thenReturn(ExcelImportStudyServiceImplTest.DUPLICATE_PSMR_ERROR_MESSAGE);
		try {
			this.importStudy.validateVariates(this.xlsBook, this.workbook);
			Assert.fail("A WorkbookParserException should be thrown.");
		} catch (final WorkbookParserException e) {
			Assert.assertEquals("The error message should be " + ExcelImportStudyServiceImplTest.DUPLICATE_PSMR_ERROR_MESSAGE,
					ExcelImportStudyServiceImplTest.DUPLICATE_PSMR_ERROR_MESSAGE, e.getMessage());
		}
	}

	@Test
	public void testValidateVariatesWithNonExistentVariate() {
		this.setUpXLSWorkbookTestData();
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(
				ExcelImportStudyServiceImplTest.PROPERTY, ExcelImportStudyServiceImplTest.SCALE, ExcelImportStudyServiceImplTest.METHOD,
				PhenotypicType.VARIATE, this.contextUtil.getCurrentProgramUUID())).thenReturn(null);
		Mockito.when(this.messageSource.getMessage(Matchers.eq("error.import.variate.does.not.exist"),
				Matchers.eq(new Object[] {ExcelImportStudyServiceImplTest.TEMPLATE_SECTION_CONDITION}),
				Matchers.eq(LocaleContextHolder.getLocale())))
				.thenReturn(ExcelImportStudyServiceImplTest.NON_EXISTENT_VARIATE_ERROR_MESSAGE);
		try {
			this.importStudy.validateVariates(this.xlsBook, this.workbook);
			Assert.fail("A WorkbookParserException should be thrown.");
		} catch (final WorkbookParserException e) {
			Assert.assertEquals("The error message should be " + ExcelImportStudyServiceImplTest.NON_EXISTENT_VARIATE_ERROR_MESSAGE,
					ExcelImportStudyServiceImplTest.NON_EXISTENT_VARIATE_ERROR_MESSAGE, e.getMessage());
		}
	}

	@Test
	public void testValidateVariatesWithAlreadyExistingVariate() {
		this.setUpXLSWorkbookTestData();
		final MeasurementVariable mvar = this.mvarTDI.createMeasurementVariable(this.termId, TermId.NUMERIC_VARIABLE.getId());
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(
				ExcelImportStudyServiceImplTest.PROPERTY, ExcelImportStudyServiceImplTest.SCALE, ExcelImportStudyServiceImplTest.METHOD,
				PhenotypicType.VARIATE, this.contextUtil.getCurrentProgramUUID())).thenReturn(mvar);
		this.workbook.setVariates(Arrays.asList(mvar));
		Mockito.when(this.messageSource.getMessage(Matchers.eq("error.import.variate.exists.in.study"),
				Matchers.eq(new Object[] {ExcelImportStudyServiceImplTest.TEMPLATE_SECTION_CONDITION}),
				Matchers.eq(LocaleContextHolder.getLocale()))).thenReturn(ExcelImportStudyServiceImplTest.EXISTING_VARIATE_ERROR_MESSAGE);
		try {
			this.importStudy.validateVariates(this.xlsBook, this.workbook);
			Assert.fail("A WorkbookParserException should be thrown.");
		} catch (final WorkbookParserException e) {
			Assert.assertEquals("The error message should be " + ExcelImportStudyServiceImplTest.EXISTING_VARIATE_ERROR_MESSAGE,
					ExcelImportStudyServiceImplTest.EXISTING_VARIATE_ERROR_MESSAGE, e.getMessage());
		}
	}
}
