
package com.efficio.fieldbook.web.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.efficio.fieldbook.web.experimentdesign.ExperimentDesignGenerator;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;

@RunWith(MockitoJUnitRunner.class)
public class ExpDesignUtilTest {

	private static final String ENTRY_NO = "ENTRY_NO";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String REP_NO = "REP_NO";

	private static final int TEST_STANDARD_VARIABLE_TERMID = 1;
	private static final int TEST_PROPERTY_TERMID = 1234;
	private static final int TEST_SCALE_TERMID = 4321;
	private static final int TEST_METHOD_TERMID = 3333;
	private static final int TEST_DATATYPE_TERMID = 4444;

	private static final String TEST_DATATYPE_DESCRIPTION = "TEST DATATYPE";
	private static final String TEST_METHOD_NAME = "TEST METHOD";
	private static final String TEST_SCALE_NAME = "TEST SCALE";
	private static final String TEST_PROPERTY_NAME = "TEST PROPERTY";
	private static final String TEST_VARIABLE_DESCRIPTION = "TEST DESCRIPTION";
	private static final String TEST_VARIABLE_NAME = "TEST VARIABLE";

	/** The Constant log. */
	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);

	@Mock
	private WorkbenchService workbenchService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private FieldbookService fieldbookService;

	@InjectMocks
	private ExperimentDesignGenerator experimentDesignGenerator;

	@Test
	public void testGenerateExperimentDesignMeasurements() {
		final Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, new StudyTypeDto(StudyTypeDto.TRIAL_NAME));

		final List<String> treatmentFactor = new ArrayList<String>();
		final List<String> levels = new ArrayList<String>();
		final MainDesign mainDesign =
				experimentDesignGenerator.createRandomizedCompleteBlockDesign("2", ExpDesignUtilTest.REP_NO, ExpDesignUtilTest.PLOT_NO,
						301, 201, treatmentFactor, levels, "");

		this.setMockValues(mainDesign);

		final List<ImportedGermplasm> germplasmList = this.createImportedGermplasms();

		final List<StandardVariable> requiredExpDesignVariable = this.createRequiredVariables();
		final Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>();
		final int environments = 3;
		final int environmentsToAdd = 1;

		try {
			final List<MeasurementRow> measurementRowList =
					experimentDesignGenerator.generateExperimentDesignMeasurements(environments, environmentsToAdd, workbook.getTrialVariables(),
							workbook.getFactors(), workbook.getNonTrialFactors(), workbook.getVariates(), null, requiredExpDesignVariable,
							germplasmList, mainDesign, ExpDesignUtilTest.ENTRY_NO,
							treatmentFactorValues, new HashMap<Integer, Integer>());

			Assert.assertTrue("Expected trial nos. from " + (environments - environmentsToAdd + 1) + "to " + environments
					+ " for all measurement rows but found a different trial no.",
					this.isTrialNoAssignedCorrect(measurementRowList, environments, environmentsToAdd));
		} catch (final BVDesignException e) {
			ExpDesignUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected mock values but called original BV Design generator.");
		}
	}

	@Test
	public void testConvertStandardVariableToMeasurementVariable() {

		final FieldbookService mockFieldbookService = Mockito.mock(FieldbookService.class);

		Mockito.when(mockFieldbookService.getAllPossibleValues(TEST_STANDARD_VARIABLE_TERMID)).thenReturn(new ArrayList<ValueReference>());

		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(TEST_STANDARD_VARIABLE_TERMID);
		standardVariable.setName(TEST_VARIABLE_NAME);
		standardVariable.setDescription(TEST_VARIABLE_DESCRIPTION);
		standardVariable.setProperty(new Term(TEST_PROPERTY_TERMID, TEST_PROPERTY_NAME, ""));
		standardVariable.setScale(new Term(TEST_SCALE_TERMID, TEST_SCALE_NAME, ""));
		standardVariable.setMethod(new Term(TEST_METHOD_TERMID, TEST_METHOD_NAME, ""));
		standardVariable.setDataType(new Term(TEST_DATATYPE_TERMID, TEST_DATATYPE_DESCRIPTION, ""));

		final MeasurementVariable measurementVariable =
				ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, mockFieldbookService);

		Assert.assertEquals(standardVariable.getId(), measurementVariable.getTermId());
		Assert.assertEquals(standardVariable.getName(), measurementVariable.getName());
		Assert.assertEquals(standardVariable.getDescription(), measurementVariable.getDescription());
		Assert.assertEquals(standardVariable.getProperty().getName(), measurementVariable.getProperty());
		Assert.assertEquals(standardVariable.getScale().getName(), measurementVariable.getScale());
		Assert.assertEquals(standardVariable.getMethod().getName(), measurementVariable.getMethod());
		Assert.assertEquals(standardVariable.getDataType().getName(), measurementVariable.getDataType());
		Assert.assertNull(measurementVariable.getRole());
		Assert.assertEquals("", measurementVariable.getLabel());
	}

	@Test
	public void testConvertStandardVariableToMeasurementVariableStandardVariableHasPhenotypicType() {

		final FieldbookService mockFieldbookService = Mockito.mock(FieldbookService.class);

		Mockito.when(mockFieldbookService.getAllPossibleValues(TEST_STANDARD_VARIABLE_TERMID)).thenReturn(new ArrayList<ValueReference>());

		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(TEST_STANDARD_VARIABLE_TERMID);
		standardVariable.setName(TEST_VARIABLE_NAME);
		standardVariable.setDescription(TEST_VARIABLE_DESCRIPTION);
		standardVariable.setProperty(new Term(TEST_PROPERTY_TERMID, TEST_PROPERTY_NAME, ""));
		standardVariable.setScale(new Term(TEST_SCALE_TERMID, TEST_SCALE_NAME, ""));
		standardVariable.setMethod(new Term(TEST_METHOD_TERMID, TEST_METHOD_NAME, ""));
		standardVariable.setDataType(new Term(TEST_DATATYPE_TERMID, TEST_DATATYPE_DESCRIPTION, ""));
		standardVariable.setPhenotypicType(PhenotypicType.TRIAL_ENVIRONMENT);

		final MeasurementVariable measurementVariable =
				ExpDesignUtil.convertStandardVariableToMeasurementVariable(standardVariable, Operation.ADD, mockFieldbookService);

		Assert.assertEquals(standardVariable.getId(), measurementVariable.getTermId());
		Assert.assertEquals(standardVariable.getName(), measurementVariable.getName());
		Assert.assertEquals(standardVariable.getDescription(), measurementVariable.getDescription());
		Assert.assertEquals(standardVariable.getProperty().getName(), measurementVariable.getProperty());
		Assert.assertEquals(standardVariable.getScale().getName(), measurementVariable.getScale());
		Assert.assertEquals(standardVariable.getMethod().getName(), measurementVariable.getMethod());
		Assert.assertEquals(standardVariable.getDataType().getName(), measurementVariable.getDataType());
		Assert.assertEquals(standardVariable.getPhenotypicType(), measurementVariable.getRole());
		Assert.assertEquals(PhenotypicType.TRIAL_ENVIRONMENT.getLabelList().get(0), measurementVariable.getLabel());
	}

	private void setMockValues(final MainDesign design) {

		try {
			Mockito.when(this.fieldbookService.getAllPossibleValues(TermId.REP_NO.getId())).thenReturn(null);
			Mockito.when(this.fieldbookService.getAllPossibleValues(TermId.PLOT_NO.getId())).thenReturn(null);
			Mockito.when(this.fieldbookService.runBVDesign(this.workbenchService, this.fieldbookProperties, design)).thenReturn(
					this.createBvOutput());

			Mockito.when(this.workbenchService.getToolWithName(AppConstants.TOOL_NAME_BREEDING_VIEW.getString())).thenReturn(new Tool());
		} catch (final MiddlewareException e) {
			ExpDesignUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected mock values but called original Middleware method.");
		} catch (final IOException e) {
			ExpDesignUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected mock values but called original method.");
		}
	}

	private BVDesignOutput createBvOutput() {
		final BVDesignOutput bvOutput = new BVDesignOutput(0);

		final List<String[]> entries = this.createEntries();
		bvOutput.setResults(entries);

		return bvOutput;
	}

	private List<String[]> createEntries() {
		final List<String[]> entries = new ArrayList<String[]>();
		final String[] headers = new String[] {ExpDesignUtilTest.PLOT_NO, ExpDesignUtilTest.REP_NO, ExpDesignUtilTest.ENTRY_NO};

		entries.add(headers);

		for (int i = 1; i <= 6; i++) {
			final String value = String.valueOf(i);
			final String[] data = new String[] {value, value, value};
			entries.add(data);
		}
		return entries;
	}

	private boolean isTrialNoAssignedCorrect(final List<MeasurementRow> measurementRowList, final int environments, final int environmentsToAdd) {
		for (final MeasurementRow row : measurementRowList) {
			for (final MeasurementData data : row.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()
						&& (Integer.parseInt(data.getValue()) > environments || Integer.parseInt(data.getValue()) < environments
								- environmentsToAdd + 1)) {
					return false;
				}
			}
		}
		return true;
	}

	private List<ImportedGermplasm> createImportedGermplasms() {
		final List<ImportedGermplasm> germplasms = new ArrayList<ImportedGermplasm>();

		germplasms.add(new ImportedGermplasm(1, "CLA35", "87395", "", "", "", ""));
		germplasms.add(new ImportedGermplasm(2, "CLA12", "134287", "", "", "", ""));
		germplasms.add(new ImportedGermplasm(3, "CLA13", "2342", "", "", "", ""));
		germplasms.add(new ImportedGermplasm(4, "CLA14", "452353", "", "", "", ""));
		germplasms.add(new ImportedGermplasm(5, "CLA15", "43323", "", "", "", ""));
		germplasms.add(new ImportedGermplasm(6, "CLA16", "3225", "", "", "", ""));

		return germplasms;
	}

	private List<StandardVariable> createRequiredVariables() {
		final List<StandardVariable> reqVariables = new ArrayList<StandardVariable>();

		reqVariables.add(this.createStandardVariable(TermId.REP_NO.getId(), ExpDesignUtilTest.REP_NO));
		reqVariables.add(this.createStandardVariable(TermId.PLOT_NO.getId(), ExpDesignUtilTest.PLOT_NO));

		return reqVariables;
	}

	private StandardVariable createStandardVariable(final int id, final String name) {
		final StandardVariable var = new StandardVariable();
		var.setId(id);
		var.setName(name);
		return var;
	}
}
