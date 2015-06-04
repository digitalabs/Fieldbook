
package com.efficio.fieldbook.web.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Tool;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;

public class ExpDesignUtilTest extends AbstractBaseIntegrationTest {

	private static final String ENTRY_NO = "ENTRY_NO";
	private static final String PLOT_NO = "PLOT_NO";
	private static final String REP_NO = "REP_NO";

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);

	private WorkbenchService workbenchService;

	private FieldbookProperties fieldbookProperties;

	private FieldbookService fieldbookService;

	@Test
	public void testGenerateExpDesignMeasurements() {
		Workbook workbook = WorkbookDataUtil.getTestWorkbook(10, StudyType.T);

		List<String> treatmentFactor = new ArrayList<String>();
		List<String> levels = new ArrayList<String>();
		MainDesign mainDesign =
				ExpDesignUtil.createRandomizedCompleteBlockDesign("2", ExpDesignUtilTest.REP_NO, ExpDesignUtilTest.PLOT_NO,
						treatmentFactor, levels, "");

		this.setMockValues(mainDesign);

		List<ImportedGermplasm> germplasmList = this.createImportedGermplasms();

		List<StandardVariable> requiredExpDesignVariable = this.createRequiredVariables();
		Map<String, List<String>> treatmentFactorValues = new HashMap<String, List<String>>();
		int environments = 3, environmentsToAdd = 1;

		try {
			List<MeasurementRow> measurementRowList =
					ExpDesignUtil.generateExpDesignMeasurements(environments, environmentsToAdd, workbook.getTrialVariables(),
							workbook.getFactors(), workbook.getNonTrialFactors(), workbook.getVariates(), null, requiredExpDesignVariable,
							germplasmList, mainDesign, this.workbenchService, this.fieldbookProperties, ExpDesignUtilTest.ENTRY_NO,
							treatmentFactorValues, this.fieldbookService);

			Assert.assertTrue("Expected trial nos. from " + (environments - environmentsToAdd + 1) + "to " + environments
					+ " for all measurement rows but found a different trial no.",
					this.isTrialNoAssignedCorrect(measurementRowList, environments, environmentsToAdd));
		} catch (BVDesignException e) {
			ExpDesignUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected mock values but called original BV Design generator.");
		}
	}

	private void setMockValues(MainDesign design) {
		this.fieldbookService = Mockito.mock(FieldbookService.class);
		this.workbenchService = Mockito.mock(WorkbenchService.class);
		this.fieldbookProperties = Mockito.mock(FieldbookProperties.class);

		try {
			Mockito.when(this.fieldbookService.getAllPossibleValues(TermId.REP_NO.getId())).thenReturn(null);
			Mockito.when(this.fieldbookService.getAllPossibleValues(TermId.PLOT_NO.getId())).thenReturn(null);
			Mockito.when(this.fieldbookService.runBVDesign(this.workbenchService, this.fieldbookProperties, design)).thenReturn(
					this.createBvOutput());

			Mockito.when(this.workbenchService.getToolWithName(AppConstants.TOOL_NAME_BREEDING_VIEW.getString())).thenReturn(new Tool());
		} catch (MiddlewareQueryException e) {
			ExpDesignUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected mock values but called original Middleware method.");
		} catch (IOException e) {
			ExpDesignUtilTest.LOG.error(e.getMessage(), e);
			Assert.fail("Expected mock values but called original method.");
		}
	}

	private BVDesignOutput createBvOutput() {
		BVDesignOutput bvOutput = new BVDesignOutput(0);

		List<String[]> entries = this.createEntries();
		bvOutput.setResults(entries);

		return bvOutput;
	}

	private List<String[]> createEntries() {
		List<String[]> entries = new ArrayList<String[]>();
		String[] headers = new String[] {ExpDesignUtilTest.PLOT_NO, ExpDesignUtilTest.REP_NO, ExpDesignUtilTest.ENTRY_NO};

		entries.add(headers);

		for (int i = 0; i < 6; i++) {
			String value = String.valueOf(i);
			String[] data = new String[] {value, value, value};
			entries.add(data);
		}
		return entries;
	}

	private boolean isTrialNoAssignedCorrect(List<MeasurementRow> measurementRowList, int environments, int environmentsToAdd) {
		for (MeasurementRow row : measurementRowList) {
			for (MeasurementData data : row.getDataList()) {
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
		List<ImportedGermplasm> germplasms = new ArrayList<ImportedGermplasm>();

		germplasms.add(new ImportedGermplasm(1, "CLA35", "87395", "", "", "", ""));
		germplasms.add(new ImportedGermplasm(2, "CLA12", "134287", "", "", "", ""));

		return germplasms;
	}

	private List<StandardVariable> createRequiredVariables() {
		List<StandardVariable> reqVariables = new ArrayList<StandardVariable>();

		reqVariables.add(this.createStandardVariable(TermId.REP_NO.getId(), ExpDesignUtilTest.REP_NO));
		reqVariables.add(this.createStandardVariable(TermId.PLOT_NO.getId(), ExpDesignUtilTest.PLOT_NO));

		return reqVariables;
	}

	private StandardVariable createStandardVariable(int id, String name) {
		StandardVariable var = new StandardVariable();
		var.setId(id);
		var.setName(name);
		return var;
	}
}
