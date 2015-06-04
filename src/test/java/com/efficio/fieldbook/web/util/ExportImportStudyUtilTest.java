
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

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

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;

public class ExportImportStudyUtilTest {

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;

	private static String PROPERTY_NAME = "Property Name";

	@Before
	public void setUp() throws MiddlewareQueryException {
		MockitoAnnotations.initMocks(this);
		Property prop = Mockito.mock(Property.class);
		Mockito.doReturn(prop).when(this.ontologyService).getProperty(TermId.BREEDING_METHOD_PROP.getId());
		Mockito.doReturn(new Term(1, ExportImportStudyUtilTest.PROPERTY_NAME, "Dummy defintion")).when(prop).getTerm();
	}

	@Test
	public void testGetSiteNameOfTrialInstance() throws MiddlewareQueryException {
		String siteName = ExportImportStudyUtil.getSiteNameOfTrialInstance(null, this.fieldbookMiddlewareService);
		Assert.assertTrue("The site name is '' ", "".equalsIgnoreCase(siteName));

		WorkbookDataUtil.setTestWorkbook(null);
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(20, StudyType.N);
		MeasurementRow trialObservationWithTrialLocation = workbook.getTrialObservations().get(0);
		siteName = ExportImportStudyUtil.getSiteNameOfTrialInstance(trialObservationWithTrialLocation, this.fieldbookMiddlewareService);
		Assert.assertFalse("The site name for nursery is not empty.", "".equalsIgnoreCase(siteName));
	}

	@Test
	public void testGetPropertyName() throws MiddlewareQueryException {
		Assert.assertNotNull("Expecting there is a property name returned after call getPropertyName() but didn't.",
				ExportImportStudyUtil.getPropertyName(this.ontologyService));

		// returns error
		Mockito.when(this.ontologyService.getProperty(Matchers.anyInt())).thenThrow(new MiddlewareQueryException("error"));
		Assert.assertTrue("Expecting there is a property name returned after call getPropertyName() but didn't.",
				"".equalsIgnoreCase(ExportImportStudyUtil.getPropertyName(this.ontologyService)));
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
		List<Integer> visibleColumns = this.getVisibleColumnList();
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
		List<Integer> visibleColumns = new ArrayList<Integer>();

		visibleColumns.add(TermId.PLOT_NO.getId());
		visibleColumns.add(TermId.CROSS.getId());
		visibleColumns.add(TermId.GID.getId());

		return visibleColumns;
	}

	@Test
	public void testMeasurementVariableHasValue() {
		MeasurementData data = this.getMeasurementData();

		Assert.assertFalse("Expected that the measurement variable of the given measurementData has no value but didn't.",
				ExportImportStudyUtil.measurementVariableHasValue(data));

		data.setMeasurementVariable(this.getMeasurementVariableForCategoricalVariable());

		Assert.assertTrue("Expected that the measurement variable of the given measurementData has value but didn't.",
				ExportImportStudyUtil.measurementVariableHasValue(data));
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

	private List<ValueReference> getValueReferenceList() {
		List<ValueReference> possibleValues = new ArrayList<ValueReference>();

		for (int i = 0; i < 5; i++) {
			ValueReference possibleValue = new ValueReference(i, String.valueOf(i));
			possibleValues.add(possibleValue);
		}
		return possibleValues;
	}
}
