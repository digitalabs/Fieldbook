
package com.efficio.fieldbook.web.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.data.initializer.ValueReferenceTestDataInitializer;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Property;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ExportImportStudyUtilTest {

	private static final int NO_OF_POSSIBLE_VALUES = 5;

	private static final String PROPERTY_NAME = "Property Name";

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private ContextUtil contextUtil;

	private ValueReferenceTestDataInitializer valueReferenceTestDataInitializer;

	private String fileNameWithExtension;
	private List<Location> locations;
	private Workbook workbook;
	private List<Integer> instances;
	
	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		final Property prop = Mockito.mock(Property.class);
		Mockito.doReturn(prop).when(this.ontologyService).getProperty(TermId.BREEDING_METHOD_PROP.getId());
		Mockito.doReturn(new Term(1, ExportImportStudyUtilTest.PROPERTY_NAME, "Dummy defintion")).when(prop).getTerm();

		this.locations = WorkbookDataUtil.createLocationData();
		this.fileNameWithExtension = "trial_" + new Random().nextInt(1000) + ".xls";

		// init test data initializers;
		this.valueReferenceTestDataInitializer = new ValueReferenceTestDataInitializer();
		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
	}

	@Test
	public void testGetSiteNameOfTrialInstance() throws MiddlewareQueryException {
		String siteName = ExportImportStudyUtil.getSiteNameOfTrialInstance(null, this.fieldbookMiddlewareService);
		Assert.assertTrue("The site name is '' ", "".equalsIgnoreCase(siteName));

		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, new StudyTypeDto("N"));
		final MeasurementRow trialObservationWithTrialLocation = workbook.getTrialObservations().get(0);
		siteName = ExportImportStudyUtil.getSiteNameOfTrialInstance(trialObservationWithTrialLocation, this.fieldbookMiddlewareService);
		Assert.assertFalse("The site name for nursery is not empty.", "".equalsIgnoreCase(siteName));
	}

	@Test(expected = MiddlewareQueryException.class)
	public void testGetPropertyName() throws MiddlewareQueryException {
		Mockito.when(this.ontologyService.getProperty(Matchers.anyInt())).thenThrow(new MiddlewareQueryException("error"));
		ExportImportStudyUtil.getPropertyName(this.ontologyService);
	}

	@Test
	public void testPartOfRequiredColumns() {
		Assert.assertTrue("Expecting to return true for a required termId but didn't.",
				ExportImportStudyUtil.partOfRequiredColumns(TermId.ENTRY_NO.getId()));
		Assert.assertTrue("Expecting to return true for a required termId but didn't.",
				ExportImportStudyUtil.partOfRequiredColumns(TermId.DESIG.getId()));
		Assert.assertTrue("Expecting to return true for a required termId but didn't.",
				ExportImportStudyUtil.partOfRequiredColumns(TermId.PLOT_NO.getId()));

		Assert.assertFalse("Expecting to return false for a non-required termId but didn't.",
				ExportImportStudyUtil.partOfRequiredColumns(TermId.BLOCK_NO.getId()));
	}

	@Test
	public void testIsColumnVisible() {
		int termId = TermId.CROSS.getId();
		final List<Integer> visibleColumns = this.getVisibleColumnList();
		Assert.assertTrue("Expected that the given termId is part of the visible columns but didn't.",
				ExportImportStudyUtil.isColumnVisible(termId, visibleColumns));

		termId = TermId.DESIG.getId();
		Assert.assertTrue(
				"Expected that the given required termId though not part the list of visiblColumns must still be visible but didn't.",
				ExportImportStudyUtil.isColumnVisible(termId, visibleColumns));

		termId = TermId.ENTRY_CODE.getId();
		Assert.assertFalse("Expected that the given termId is not part of the visible columns but didn't.",
				ExportImportStudyUtil.isColumnVisible(termId, visibleColumns));
	}

	private List<Integer> getVisibleColumnList() {
		final List<Integer> visibleColumns = new ArrayList<Integer>();

		visibleColumns.add(TermId.PLOT_NO.getId());
		visibleColumns.add(TermId.CROSS.getId());
		visibleColumns.add(TermId.GID.getId());

		return visibleColumns;
	}

	@Test
	public void testMeasurementVariableHasValue() {
		final MeasurementData data = this.getMeasurementData();

		Assert.assertFalse("Expected that the measurement variable of the given measurementData has no value but didn't.",
				ExportImportStudyUtil.measurementVariableHasValue(data));

		data.setMeasurementVariable(this.getMeasurementVariableForCategoricalVariable());

		Assert.assertTrue("Expected that the measurement variable of the given measurementData has value but didn't.",
				ExportImportStudyUtil.measurementVariableHasValue(data));
	}

	@Test
	public void testGetFileNamePathWithSiteAndMoreThanOneInstance() throws IOException {

		Mockito.when(this.fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_1)).thenReturn(this.locations.get(0));
		Mockito.when(this.fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_2)).thenReturn(this.locations.get(1));

		this.workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 2);
		this.instances = WorkbookDataUtil.getTrialInstances(this.workbook);

		int index = 1;
		for (final MeasurementRow row : this.workbook.getTrialObservations()) {
			final FileExportInfo exportInfo = ExportImportStudyUtil.getFileNamePath(index, row, this.instances, this.fileNameWithExtension,
					false, this.fieldbookMiddlewareService, this.contextUtil);
			
			Assert.assertTrue("Expected location in filename but did not found one.",
					exportInfo.getDownloadFileName().contains(WorkbookDataUtil.LNAME + "_" + index));
			index++;
		}
	}

	@Test
	public void testGetFileNamePathWithSiteAndOneInstance() throws IOException {

		Mockito.when(this.fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_1)).thenReturn(this.locations.get(0));

		this.workbook = WorkbookDataUtil.getTestWorkbookForTrial(10, 1);
		this.instances = WorkbookDataUtil.getTrialInstances(this.workbook);

		final FileExportInfo exportInfo =
				ExportImportStudyUtil.getFileNamePath(1, this.workbook.getTrialObservations().get(0), this.instances, this.fileNameWithExtension, false,
						this.fieldbookMiddlewareService, this.contextUtil);
		Assert.assertTrue("Expected location in filename but did not found one.", exportInfo.getDownloadFileName().contains(WorkbookDataUtil.LNAME + "_1"));
	}

	@Test
	public void testGetFileNamePathWithoutSite() throws IOException {

		Mockito.when(this.fieldbookMiddlewareService.getLocationById(WorkbookDataUtil.LOCATION_ID_1)).thenReturn(this.locations.get(0));

		final MeasurementRow trialObservation = WorkbookDataUtil.createTrialObservationWithoutSite();
		this.instances = new ArrayList<Integer>();
		this.instances.add(1);

		final FileExportInfo exportInfo = ExportImportStudyUtil.getFileNamePath(1, trialObservation, this.instances,
				this.fileNameWithExtension, false, this.fieldbookMiddlewareService, this.contextUtil);
		Assert.assertTrue("Expected filename in output filename but found none.",
				exportInfo.getDownloadFileName().contains(this.fileNameWithExtension.substring(0, this.fileNameWithExtension.lastIndexOf("."))));
		final String processedFileName = exportInfo.getDownloadFileName().substring(0, this.fileNameWithExtension.lastIndexOf("."));
		Assert.assertFalse("Expected no underscore before the file extension but found one.",
				processedFileName.charAt(processedFileName.length() - 1) == '_');
	}

	@Test
	public void testGetCategoricalCellValueWhenIdValueIsNull() {
		final List<ValueReference> possibleValues = this.valueReferenceTestDataInitializer.createValueReferenceList(NO_OF_POSSIBLE_VALUES);
		final String idValue = null;
		final String returnedValue = ExportImportStudyUtil.getCategoricalCellValue(idValue, possibleValues);
		Assert.assertNull("Expecting to return null when the id value passed is also null.", returnedValue);
	}

	@Test
	public void testGetCategoricalCellValueWhenThereIsNoPossibleValues() {
		final List<ValueReference> possibleValues = new ArrayList<ValueReference>();
		final String idValue = "1";
		final String returnedValue = ExportImportStudyUtil.getCategoricalCellValue(idValue, possibleValues);
		Assert.assertTrue("Expecting to return the exact id value when the there is no possible values.", returnedValue.equals(idValue));
	}

	@Test
	public void testGetCategoricalCellValueWhenThereIsPossibleValuesAndIdValue() {
		final List<ValueReference> possibleValues = this.valueReferenceTestDataInitializer.createValueReferenceList(NO_OF_POSSIBLE_VALUES);

		final ImmutableMap<Integer, ValueReference> possibleValuesMap =
				Maps.uniqueIndex(possibleValues, new Function<ValueReference, Integer>() {

					@Override
					public Integer apply(final ValueReference from) {
						return from.getId();
					}
				});

		final String idValue = "1";
		final String expectedValue = possibleValuesMap.get(Integer.valueOf(idValue)).getName();
		final String returnedValue = ExportImportStudyUtil.getCategoricalCellValue(idValue, possibleValues);

		Assert.assertTrue("Expecting to return the exact id value passed when the there is corresponding value from the possible values.",
				expectedValue.equals(returnedValue));
	}

	@Test
	public void testGetCategoricalCellValueWhenThereIsPossibleValuesAndIdValueWithDecimalPlace() {

		final List<ValueReference> possibleValues = this.valueReferenceTestDataInitializer.createValueReferenceList(NO_OF_POSSIBLE_VALUES);

		final String idValue = "1.1";
		final String returnedValue = ExportImportStudyUtil.getCategoricalCellValue(idValue, possibleValues);
		Assert.assertTrue(
				"Expecting to return the exact id value passed when the id value has decimal place and not part of possible values of the categorical variable.",
				idValue.equals(returnedValue));

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

	private List<ValueReference> getValueReferenceList() {
		final List<ValueReference> possibleValues = new ArrayList<ValueReference>();

		for (int i = 0; i < 5; i++) {
			final ValueReference possibleValue = new ValueReference(i, String.valueOf(i));
			possibleValues.add(possibleValue);
		}
		return possibleValues;
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
