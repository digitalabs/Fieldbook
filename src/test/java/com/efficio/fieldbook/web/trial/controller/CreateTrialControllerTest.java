
package com.efficio.fieldbook.web.trial.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.trial.bean.TabInfo;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;

public class CreateTrialControllerTest extends AbstractBaseIntegrationTest {

	@Autowired
	private CreateTrialController controller;

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Override
	@Before
	public void setUp() {
		this.fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		this.controller.setFieldbookMiddlewareService(this.fieldbookMiddlewareService);
		this.mockContextUtil();
	}

	@Test
	public void testUseExistingStudyWithError() throws Exception {
		Mockito.when(this.fieldbookMiddlewareService.getStudyDataSet(1))
				.thenThrow(new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"));

		final Map<String, Object> tabDetails = this.controller.getExistingTrialDetails(1);

		Assert.assertNotNull("Expecting error but did not get one", tabDetails.get("createTrialForm"));

		final CreateTrialForm form = (CreateTrialForm) tabDetails.get("createTrialForm");
		Assert.assertTrue("Expecting error but did not get one", form.isHasError());
	}

	@Test
	public void testUseExistingStudy() throws Exception {
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(true);
		WorkbookTestDataInitializer.setTrialObservations(workbook);
		Mockito.doReturn(workbook).when(this.fieldbookMiddlewareService).getStudyDataSet(1);
		this.mockStandardVariables(workbook.getAllVariables());

		// Verify that workbook has Analysis and/or Analysis Summary variables beforehand to check that they were later removed
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConditions()));
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConstants()));

		final Map<String, Object> tabDetails = this.controller.getExistingTrialDetails(1);
		boolean analysisVariableFound = false;
		for (final String tab : tabDetails.keySet()) {
			final Object tabDetail = tabDetails.get(tab);
			if (tabDetail instanceof TabInfo) {
				final TabInfo tabInfo = (TabInfo) tabDetail;
				final List<SettingDetail> detailList = this.getSettingDetails(tabInfo);
				if (detailList == null) {
					continue;
				}
				for (final SettingDetail settingDetail : detailList) {
					if (VariableType.getReservedVariableTypes().contains(settingDetail.getVariableType())) {
						analysisVariableFound = true;
						break;
					}
				}
			}
		}
		Assert.assertFalse("'Analysis' and 'Analysis Summary' variables should not be included.", analysisVariableFound);
	}

	private boolean hasAnalysisVariables(final List<MeasurementVariable> variables) {
		boolean analysisVariableFound = false;
		for (final MeasurementVariable variable : variables) {
			if (VariableType.getReservedVariableTypes().contains(variable.getVariableType())) {
				analysisVariableFound = true;
				break;
			}
		}
		return analysisVariableFound;
	}

	private void mockContextUtil() {
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		this.controller.setContextUtil(contextUtil);
		Mockito.doReturn(this.PROGRAM_UUID).when(contextUtil).getCurrentProgramUUID();
	}

	private void mockStandardVariables(final List<MeasurementVariable> allVariables) {
		for (final MeasurementVariable measurementVariable : allVariables) {
			Mockito.doReturn(this.createStandardVariable(measurementVariable.getTermId())).when(this.fieldbookMiddlewareService)
					.getStandardVariable(measurementVariable.getTermId(), this.PROGRAM_UUID);
		}
	}

	private StandardVariable createStandardVariable(final Integer id) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(id);
		return standardVariable;
	}

	@SuppressWarnings("unchecked")
	private List<SettingDetail> getSettingDetails(final TabInfo tabInfo) {
		final List<SettingDetail> detailList = new ArrayList<>();
		if (tabInfo.getSettings() != null && !tabInfo.getSettings().isEmpty()) {
			return tabInfo.getSettings();
		}
		if (tabInfo.getSettingMap() != null && !tabInfo.getSettingMap().isEmpty() && tabInfo.getSettingMap().values() != null
				&& !tabInfo.getSettingMap().values().isEmpty()) {
			if (tabInfo.getSettingMap().containsKey("managementDetails")) {
				detailList.addAll((List<SettingDetail>) tabInfo.getSettingMap().get("managementDetails"));
			} else if (tabInfo.getSettingMap().containsKey("trialConditionDetails")) {
				detailList.addAll((List<SettingDetail>) tabInfo.getSettingMap().get("trialConditionDetails"));
			} else if (tabInfo.getSettingMap().containsKey("details")) {
				detailList.addAll((List<SettingDetail>) tabInfo.getSettingMap().get("details"));
			} else if (tabInfo.getSettingMap().containsKey("treatmentLevelPairs")) {
				final Map<String, List<SettingDetail>> treatmentFactorPairs =
						(Map<String, List<SettingDetail>>) tabInfo.getSettingMap().get("details");
				for (final List<SettingDetail> settingDetails : treatmentFactorPairs.values()) {
					detailList.addAll(settingDetails);
				}
			}
		}

		return detailList;
	}

	@Test
	public void testRequiredExpDesignVar() {

		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.PLOT_NO.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.REP_NO.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.BLOCK_NO.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.ROW.getId()));
		Assert.assertTrue("Expected term to be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.COL.getId()));
		Assert.assertFalse("Expected term to NOT be in the required var list but did not found it.",
				this.controller.inRequiredExpDesignVar(TermId.LOCATION_ID.getId()));
	}
}
