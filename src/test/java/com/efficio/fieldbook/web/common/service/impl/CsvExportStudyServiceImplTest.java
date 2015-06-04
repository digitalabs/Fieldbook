
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.ExportService;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.exceptions.verification.NeverWantedButInvoked;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.nursery.service.impl.ValidationServiceImpl;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

public class CsvExportStudyServiceImplTest {

	private static final String CSV_EXT = ".csv";

	private static final String ZIP_EXT = ".zip";

	private static CsvExportStudyServiceImpl csvExportStudyService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private static Property DUMMY_PROPERTY;

	@Mock
	private static Term DUMMY_TERM;

	@Mock
	private ExportService exportService;

	private static String PROPERTY_NAME = "Property Name";
	private static String FILENAME = "TestFileName.csv";
	private static String UPLOAD_DIRECTORY = "";

	@Before
	public void setUp() throws MiddlewareQueryException, IOException {
		MockitoAnnotations.initMocks(this);

		CsvExportStudyServiceImplTest.csvExportStudyService = Mockito.spy(new CsvExportStudyServiceImpl());
		CsvExportStudyServiceImplTest.csvExportStudyService.setFieldbookProperties(this.fieldbookProperties);
		CsvExportStudyServiceImplTest.csvExportStudyService.setExportService(this.exportService);
		CsvExportStudyServiceImplTest.csvExportStudyService.setOntologyService(this.ontologyService);

		Property prop = Mockito.mock(Property.class);
		Mockito.doReturn(prop).when(this.ontologyService).getProperty(TermId.BREEDING_METHOD_PROP.getId());
		Mockito.doReturn(new Term(1, CsvExportStudyServiceImplTest.PROPERTY_NAME, "Dummy defintion")).when(prop).getTerm();
		Mockito.doReturn(Mockito.mock(File.class)).when(this.exportService)
				.generateCSVFile(Matchers.any(List.class), Matchers.any(List.class), Matchers.anyString());
		Mockito.doReturn(CsvExportStudyServiceImplTest.UPLOAD_DIRECTORY).when(this.fieldbookProperties).getUploadDirectory();
	}

	@Test
	public void testCSVStudyExportForTrial() throws IOException {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 2);
		workbook.setExportArrangedObservations(workbook.getObservations());

		List<Integer> instances = WorkbookDataUtil.getTrialInstances();

		Mockito.doReturn(workbook.getObservations()).when(CsvExportStudyServiceImplTest.csvExportStudyService)
				.getApplicableObservations(workbook, instances);

		String outputFilename =
				CsvExportStudyServiceImplTest.csvExportStudyService.export(workbook, CsvExportStudyServiceImplTest.FILENAME, instances);

		Assert.assertTrue("Expected the filename must end in .zip",
				CsvExportStudyServiceImplTest.ZIP_EXT.equalsIgnoreCase(outputFilename.substring(outputFilename.lastIndexOf("."))));
	}

	@Test
	public void testCSVStudyExportForNursery() throws IOException {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		workbook.setExportArrangedObservations(workbook.getObservations());

		List<Integer> instances = new ArrayList<Integer>();
		instances.add(1);

		Mockito.doReturn(workbook.getObservations()).when(CsvExportStudyServiceImplTest.csvExportStudyService)
				.getApplicableObservations(workbook, instances);

		String outputFilename =
				CsvExportStudyServiceImplTest.csvExportStudyService.export(workbook, CsvExportStudyServiceImplTest.FILENAME, instances);

		Assert.assertTrue("Expected the filename must end in .csv",
				CsvExportStudyServiceImplTest.CSV_EXT.equalsIgnoreCase(outputFilename.substring(outputFilename.lastIndexOf("."))));
	}

	@Test
	public void testGetFileNamePathForNursery() throws MiddlewareQueryException {
		String expectedFileName = File.separator + CsvExportStudyServiceImplTest.FILENAME;

		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		List<Integer> instances = new ArrayList<Integer>();
		instances.add(1);

		// with boolean flag for Nursery
		String filePathName =
				CsvExportStudyServiceImplTest.csvExportStudyService.getFileNamePath(1, workbook.getTrialObservations().get(0), instances,
						CsvExportStudyServiceImplTest.FILENAME, true);
		Assert.assertEquals("Expecting the return values are equals." + expectedFileName + " : " + filePathName, expectedFileName,
				filePathName);

		// with no instances
		filePathName =
				CsvExportStudyServiceImplTest.csvExportStudyService.getFileNamePath(1, workbook.getTrialObservations().get(0), null,
						CsvExportStudyServiceImplTest.FILENAME, true);
		Assert.assertEquals("Expecting the return values are equals." + expectedFileName + " : " + filePathName, expectedFileName,
				filePathName);
	}

	@Test
	public void testGetFileNamePathForTrialWithMultipleInstance() throws MiddlewareQueryException {
		String expectedFileName =
				File.separator
						+ CsvExportStudyServiceImplTest.FILENAME.substring(0, CsvExportStudyServiceImplTest.FILENAME.lastIndexOf("."))
						+ "-2_Location_1.csv";

		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 2);
		List<Integer> instances = WorkbookDataUtil.getTrialInstances();

		String filePathName =
				CsvExportStudyServiceImplTest.csvExportStudyService.getFileNamePath(2, workbook.getTrialObservations().get(0), instances,
						CsvExportStudyServiceImplTest.FILENAME, false);

		Assert.assertEquals("Expecting the return values are equals." + expectedFileName + " : " + filePathName, expectedFileName,
				filePathName);
	}

	@Test
	public void testGetFileNamePathForTrialWithSingleInstance() throws MiddlewareQueryException {
		String expectedFileName =
				CsvExportStudyServiceImplTest.FILENAME.substring(0, CsvExportStudyServiceImplTest.FILENAME.lastIndexOf("."))
						+ "-1_Location_1.csv";

		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 1);
		List<Integer> instances = WorkbookDataUtil.getTrialInstances();

		String filePathName =
				CsvExportStudyServiceImplTest.csvExportStudyService.getFileNamePath(1, workbook.getTrialObservations().get(0), instances,
						CsvExportStudyServiceImplTest.FILENAME, false);

		Assert.assertEquals("Expecting the return values are equals." + expectedFileName + " : " + filePathName, expectedFileName,
				filePathName);
	}

	@Test
	public void testGetExportColumnHeadersWhenVisibleColumnsIsNull() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnHeaders(null, variables);

		Mockito.verify(CsvExportStudyServiceImplTest.csvExportStudyService, Mockito.times(0)).getColumnsBasedOnVisibility(null,
				variables.get(0));
	}

	@Test
	public void testGetExportColumnHeadersWhenVisibleColumnsHasValues() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		List<Integer> visibleColumns = this.getVisibleColumnListWithOutRequiredColumns();
		CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnHeaders(visibleColumns, variables);

		for (MeasurementVariable variable : variables) {
			Mockito.verify(CsvExportStudyServiceImplTest.csvExportStudyService, Mockito.times(1)).getColumnsBasedOnVisibility(
					visibleColumns, variable);
		}
	}

	@Test
	public void testGetColumnsBasedOnVisibilityWhenTheColumnHeadersIncludeAllRequiredFields() {

		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		List<Integer> visibleColumns = this.getVisibleColumnListWithRequiredColumns();

		int noOfVisibleColumns = 0;
		for (MeasurementVariable variable : variables) {
			ExportColumnHeader columnHeader =
					CsvExportStudyServiceImplTest.csvExportStudyService.getColumnsBasedOnVisibility(visibleColumns, variable);

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

		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		List<Integer> visibleColumns = this.getVisibleColumnListWithSomeRequiredColumns();

		int noOfVisibleColumns = 0;
		for (MeasurementVariable variable : variables) {
			ExportColumnHeader columnHeader =
					CsvExportStudyServiceImplTest.csvExportStudyService.getColumnsBasedOnVisibility(visibleColumns, variable);

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

		int expectedNoOfColumns = visibleColumns.size() + 2;
		Assert.assertEquals("Expected that the no of visible column headers is " + expectedNoOfColumns + " but returned "
				+ noOfVisibleColumns, expectedNoOfColumns, noOfVisibleColumns);
	}

	private Object getNoOfVisibleColumns(List<ExportColumnHeader> visibleColumnHeaders) {
		int visibleColumns = 0;

		for (ExportColumnHeader column : visibleColumnHeaders) {
			if (column.isDisplay()) {
				visibleColumns++;
			}
		}
		return visibleColumns;
	}

	private List<Integer> getVisibleColumnListWithOutRequiredColumns() {
		List<Integer> visibleColumns = new ArrayList<Integer>();

		visibleColumns.add(TermId.CROSS.getId());
		visibleColumns.add(TermId.GID.getId());

		return visibleColumns;
	}

	private List<Integer> getVisibleColumnListWithSomeRequiredColumns() {
		List<Integer> visibleColumns = new ArrayList<Integer>();

		visibleColumns.add(TermId.PLOT_NO.getId());
		visibleColumns.add(TermId.CROSS.getId());
		visibleColumns.add(TermId.GID.getId());

		return visibleColumns;
	}

	private List<Integer> getVisibleColumnListWithRequiredColumns() {
		List<Integer> visibleColumns = new ArrayList<Integer>();

		visibleColumns.add(TermId.PLOT_NO.getId());
		visibleColumns.add(TermId.DESIG.getId());
		visibleColumns.add(TermId.ENTRY_NO.getId());
		visibleColumns.add(TermId.GID.getId());

		return visibleColumns;
	}

	@Test
	public void testGetExportColumnValues() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();
		List<Integer> visibleColumns = this.getVisibleColumnListWithRequiredColumns();
		List<ExportColumnHeader> columnHeaders =
				CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnHeaders(visibleColumns, variables);
		List<MeasurementRow> observations = workbook.getObservations();

		List<Map<Integer, ExportColumnValue>> columnValuesForAllRows =
				CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnValues(columnHeaders, variables, observations);

		Assert.assertEquals("Expecting the no of entries of column values equal to the original no of observatios but didn't. ",
				columnValuesForAllRows.size(), observations.size());
	}

	@Test
	public void testGetColumnValueMap() {
		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();
		List<Integer> visibleColumns = this.getVisibleColumnListWithRequiredColumns();
		List<ExportColumnHeader> columnHeaders =
				CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnHeaders(visibleColumns, variables);
		MeasurementRow row = workbook.getObservations().get(0);

		Map<Integer, ExportColumnValue> columnValuesInARow =
				CsvExportStudyServiceImplTest.csvExportStudyService.getColumnValueMap(columnHeaders, row);

		Assert.assertEquals("Expecting the number of generated column values in a row "
				+ "is equal to the number of visible column headers but didn't.", columnValuesInARow.size(),
				this.getNoOfVisibleColumns(columnHeaders));

		for (ExportColumnHeader columnHeader : columnHeaders) {
			if (columnHeader.isDisplay() && !columnValuesInARow.containsKey(columnHeader.getId())) {
				Assert.fail("Expecting that the ids of visibleColumnHeaders can be found in the generated list of column values in a row but didn't.");
			}
		}
	}

	@Test
	public void testGetColumnValue() {
		// For categorical variables
		MeasurementData data = this.getMeasurementData();
		data.setMeasurementVariable(this.getMeasurementVariableForCategoricalVariable()); // set categorical values

		ExportColumnValue columnValue = CsvExportStudyServiceImplTest.csvExportStudyService.getColumnValue(data, TermId.ENTRY_NO.getId());
		Assert.assertNotNull("Expected that there is a newly created ExportColumnValue object but didn't.", columnValue);

		// For non-categorical variables
		data.setMeasurementVariable(this.getMeasureVariableForNumericalVariable());
		columnValue = CsvExportStudyServiceImplTest.csvExportStudyService.getColumnValue(data, TermId.ENTRY_NO.getId());
		Assert.assertNotNull("Expected that there is a newly created ExportColumnValue object but didn't.", columnValue);

		try {
			Mockito.verify(CsvExportStudyServiceImplTest.csvExportStudyService, Mockito.times(1)).getCategoricalCellValue(data);
		} catch (NeverWantedButInvoked e) {
			Assert.fail(e.getMessage());
		}

		// for non-categorical variable with numeric data type
		data.setDataType(AppConstants.NUMERIC_DATA_TYPE.getString());
		data.setValue("3.1416");
		columnValue = CsvExportStudyServiceImplTest.csvExportStudyService.getColumnValue(data, TermId.ENTRY_NO.getId());
		Assert.assertNotNull("Expected that there is a newly created ExportColumnValue object but didn't.", columnValue);
	}

	@Test
	public void testGetNumericColumnValueIfMissing() {
		MeasurementData dataCell = new MeasurementData();
		dataCell.setValue(ValidationServiceImpl.MISSING_VAL);
		Integer termId = 2001;
		ExportColumnValue columnValue = CsvExportStudyServiceImplTest.csvExportStudyService.getNumericColumnValue(dataCell, termId);
		Assert.assertEquals("Value should be missing", ValidationServiceImpl.MISSING_VAL, columnValue.getValue());
	}

	@Test
	public void testGetNumericColumnValueIfNotMissing() {
		MeasurementData dataCell = new MeasurementData();
		dataCell.setValue("20");
		Integer termId = 2001;
		ExportColumnValue columnValue = CsvExportStudyServiceImplTest.csvExportStudyService.getNumericColumnValue(dataCell, termId);
		Assert.assertEquals("Value should be 20", Double.valueOf("20").toString(), columnValue.getValue());
	}

	private MeasurementData getMeasurementData() {
		return new MeasurementData(WorkbookDataUtil.ENTRY, String.valueOf(1));
	}

	private MeasurementVariable getMeasurementVariableForCategoricalVariable() {
		MeasurementVariable variable =
				new MeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL", "TRIAL NUMBER", WorkbookDataUtil.NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.CHARACTER_VARIABLE.getId());
		variable.setPossibleValues(this.getValueReferenceList());
		return variable;
	}

	private MeasurementVariable getMeasureVariableForNumericalVariable() {
		MeasurementVariable variable =
				new MeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL", "TRIAL NUMBER", WorkbookDataUtil.NUMBER,
						WorkbookDataUtil.ENUMERATED, WorkbookDataUtil.TRIAL_INSTANCE, WorkbookDataUtil.NUMERIC, "", WorkbookDataUtil.TRIAL);
		variable.setDataTypeId(TermId.NUMERIC_VARIABLE.getId());
		variable.setPossibleValues(new ArrayList<ValueReference>());
		return variable;
	}

	private List<ValueReference> getValueReferenceList() {
		List<ValueReference> possibleValues = new ArrayList<ValueReference>();

		for (int i = 0; i < 5; i++) {
			ValueReference possibleValue = new ValueReference(i, String.valueOf(i));
			possibleValues.add(possibleValue);
		}
		return possibleValues;
	}
}
