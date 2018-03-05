
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
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.ExportImportStudyUtil;

public class CsvExportStudyServiceImplTest {

	private static final String CSV_EXT = ".csv";

	private static final String ZIP_EXT = ".zip";

	private static CsvExportStudyServiceImpl csvExportStudyService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private static Property DUMMY_PROPERTY;

	@Mock
	private static Term DUMMY_TERM;

	@Mock
	private GermplasmExportService germplasmExportService;

	private static String PROPERTY_NAME = "Property Name";
	private static String FILENAME = "TestFileName.csv";

	@Before
	public void setUp() throws MiddlewareQueryException, IOException {
		MockitoAnnotations.initMocks(this);

		CsvExportStudyServiceImplTest.csvExportStudyService = Mockito.spy(new CsvExportStudyServiceImpl());
		CsvExportStudyServiceImplTest.csvExportStudyService.setGermplasmExportService(this.germplasmExportService);
		CsvExportStudyServiceImplTest.csvExportStudyService.setOntologyService(this.ontologyService);

		final Property prop = Mockito.mock(Property.class);
		Mockito.doReturn(prop).when(this.ontologyService).getProperty(TermId.BREEDING_METHOD_PROP.getId());
		Mockito.doReturn(new Term(1, CsvExportStudyServiceImplTest.PROPERTY_NAME, "Dummy defintion")).when(prop).getTerm();
		Mockito.doReturn(Mockito.mock(File.class)).when(this.germplasmExportService)
				.generateCSVFile(Matchers.any(List.class), Matchers.any(List.class), Matchers.anyString());
	}

	@Test
	public void testCSVStudyExportForTrial() throws IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, 2);
		workbook.setExportArrangedObservations(workbook.getObservations());

		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);

		Mockito.doReturn(workbook.getObservations()).when(CsvExportStudyServiceImplTest.csvExportStudyService)
				.getApplicableObservations(workbook, instances);

		final FileExportInfo exportInfo =
				CsvExportStudyServiceImplTest.csvExportStudyService.export(workbook, CsvExportStudyServiceImplTest.FILENAME, instances);

		Assert.assertTrue("Expected the filename must end in .zip",
				CsvExportStudyServiceImplTest.ZIP_EXT.equalsIgnoreCase(exportInfo.getDownloadFileName().substring(exportInfo.getDownloadFileName().lastIndexOf("."))));
	}

	@Test
	public void testCSVStudyExportForNursery() throws IOException {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		workbook.setExportArrangedObservations(workbook.getObservations());

		final List<Integer> instances = new ArrayList<Integer>();
		instances.add(1);

		Mockito.doReturn(workbook.getObservations()).when(CsvExportStudyServiceImplTest.csvExportStudyService)
				.getApplicableObservations(workbook, instances);

		final FileExportInfo exportInfo =
				CsvExportStudyServiceImplTest.csvExportStudyService.export(workbook, CsvExportStudyServiceImplTest.FILENAME, instances);

		Assert.assertTrue("Expected the filename must end in .csv",
				CsvExportStudyServiceImplTest.CSV_EXT.equalsIgnoreCase(exportInfo.getDownloadFileName().substring(exportInfo.getDownloadFileName().lastIndexOf("."))));
	}

	@Test
	public void testGetExportColumnHeadersWhenVisibleColumnsIsNull() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnHeaders(null, variables);

		Mockito.verify(CsvExportStudyServiceImplTest.csvExportStudyService, Mockito.times(0)).getColumnsBasedOnVisibility(null,
				variables.get(0));
	}

	@Test
	public void testGetExportColumnHeadersWhenVisibleColumnsHasValues() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		final List<Integer> visibleColumns = this.getVisibleColumnListWithOutRequiredColumns();
		CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnHeaders(visibleColumns, variables);

		for (final MeasurementVariable variable : variables) {
			Mockito.verify(CsvExportStudyServiceImplTest.csvExportStudyService, Mockito.times(1)).getColumnsBasedOnVisibility(
					visibleColumns, variable);
		}
	}

	@Test
	public void testGetColumnsBasedOnVisibilityWhenTheColumnHeadersIncludeAllRequiredFields() {

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		final List<Integer> visibleColumns = this.getVisibleColumnListWithRequiredColumns();

		int noOfVisibleColumns = 0;
		for (final MeasurementVariable variable : variables) {
			final ExportColumnHeader columnHeader =
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

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();

		final List<Integer> visibleColumns = this.getVisibleColumnListWithSomeRequiredColumns();

		int noOfVisibleColumns = 0;
		for (final MeasurementVariable variable : variables) {
			final ExportColumnHeader columnHeader =
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
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();
		final List<Integer> visibleColumns = this.getVisibleColumnListWithRequiredColumns();
		final List<ExportColumnHeader> columnHeaders =
				CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnHeaders(visibleColumns, variables);
		final List<MeasurementRow> observations = workbook.getObservations();

		final List<Map<Integer, ExportColumnValue>> columnValuesForAllRows =
				CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnValues(columnHeaders, variables, observations);

		Assert.assertEquals("Expecting the no of entries of column values equal to the original no of observatios but didn't. ",
				columnValuesForAllRows.size(), observations.size());
	}

	@Test
	public void testGetColumnValueMap() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		final List<MeasurementVariable> variables = workbook.getMeasurementDatasetVariables();
		final List<Integer> visibleColumns = this.getVisibleColumnListWithRequiredColumns();
		final List<ExportColumnHeader> columnHeaders =
				CsvExportStudyServiceImplTest.csvExportStudyService.getExportColumnHeaders(visibleColumns, variables);
		final MeasurementRow row = workbook.getObservations().get(0);

		final Map<Integer, ExportColumnValue> columnValuesInARow =
				CsvExportStudyServiceImplTest.csvExportStudyService.getColumnValueMap(columnHeaders, row);

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

		ExportColumnValue columnValue = CsvExportStudyServiceImplTest.csvExportStudyService.getColumnValue(data, TermId.ENTRY_NO.getId());
		Assert.assertNotNull("Expected that there is a newly created ExportColumnValue object but didn't.", columnValue);

		// For non-categorical variables
		data.setMeasurementVariable(this.getMeasureVariableForNumericalVariable());
		columnValue = CsvExportStudyServiceImplTest.csvExportStudyService.getColumnValue(data, TermId.ENTRY_NO.getId());
		Assert.assertNotNull("Expected that there is a newly created ExportColumnValue object but didn't.", columnValue);

		try {
			Mockito.verify(CsvExportStudyServiceImplTest.csvExportStudyService, Mockito.times(1)).getCategoricalCellValue(data);
		} catch (final NeverWantedButInvoked e) {
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
		final MeasurementData dataCell = new MeasurementData();
		dataCell.setValue(MeasurementData.MISSING_VALUE);
		final Integer termId = 2001;
		final ExportColumnValue columnValue = CsvExportStudyServiceImplTest.csvExportStudyService.getNumericColumnValue(dataCell, termId);
		Assert.assertEquals("Value should be missing", MeasurementData.MISSING_VALUE, columnValue.getValue());
	}

	@Test
	public void testGetNumericColumnValueIfNotMissing() {
		final MeasurementData dataCell = new MeasurementData();
		dataCell.setValue("20");
		final Integer termId = 2001;
		final ExportColumnValue columnValue = CsvExportStudyServiceImplTest.csvExportStudyService.getNumericColumnValue(dataCell, termId);
		Assert.assertEquals("Value should be 20", Double.valueOf("20").toString(), columnValue.getValue());
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
}
