/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import junit.framework.Assert;
import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.annotation.Resource;
import java.util.List;

public class ReviewStudyDetailsControllerTest extends AbstractBaseIntegrationTest {

	@Resource
	private ReviewStudyDetailsController reviewStudyDetailsController;

	@Ignore("This test fails intermittently in CI. Needs re-evaluated for its value and be rewritten altogether.")
	@Test
	public void testShowReviewNurserySummaryWithError() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get(ReviewStudyDetailsController.URL + "/show/N/1"))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.model().attributeExists("nurseryDetails"));
	}

	// FIXME BMS-2360
	// @Test
	public void testShowReviewStudySummaryWithError() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get(ReviewStudyDetailsController.URL + "/show/T/1"))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.model().attributeExists("trialDetails"));
	}

	@Test
	public void testAddErrorMessageToResultForNursery() throws Exception {
		final StudyDetails details = new StudyDetails();

		this.reviewStudyDetailsController.addErrorMessageToResult(details,
			new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"), 1);

		Assert.assertEquals("Expecting error message for nursery but got " + details.getErrorMessage() + " instead.",
				"This study is in a format that cannot be opened in the Study Manager. Please use the Study Browser if you"
						+ " wish to see the details of this study.",
				details.getErrorMessage());
	}

	@Test
	public void testAddErrorMessageToResultForStudy() throws Exception {
		final StudyDetails details = new StudyDetails();

		this.reviewStudyDetailsController.addErrorMessageToResult(details,
			new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"), 1);

		Assert.assertEquals("Expecting error message for nursery but got " + details.getErrorMessage() + " instead.",
				"This study is in a format that cannot be opened in the Study Manager. Please use the Study Browser if you"
						+ " wish to see the details of this study.",
				details.getErrorMessage());
	}

	@Test
	public void testShowStudySummaryEnvironmentsWithoutAnalysisVariables() {
		final int id = 1;
		final CreateTrialForm form = new CreateTrialForm();
		final Model model = new ExtendedModelMap();

		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(true);
		final FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		final com.efficio.fieldbook.service.api.FieldbookService fieldbookService =
				Mockito.mock(com.efficio.fieldbook.service.api.FieldbookService.class);
		this.reviewStudyDetailsController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		this.reviewStudyDetailsController.setFieldbookService(fieldbookService);
		Mockito.doReturn(workbook).when(fieldbookMiddlewareService).getStudyVariableSettings(id);
		this.mockStandardVariables(workbook.getAllVariables(), fieldbookMiddlewareService, fieldbookService);
		this.mockContextUtil();

		// Verify that workbook has Analysis and/or Analysis Summary variables beforehand to check that they were later removed
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConditions()));
		Assert.assertTrue(this.hasAnalysisVariables(workbook.getConstants()));

		this.reviewStudyDetailsController.show(id, form, model);

		final StudyDetails details = (StudyDetails) model.asMap().get("trialDetails");
		Assert.assertNotNull(details);
		final List<SettingDetail> conditionSettingDetails = details.getStudyConditionDetails();
		boolean hasAnalysisVariable = false;
		for (final SettingDetail settingDetail : conditionSettingDetails) {
			if (VariableType.getReservedVariableTypes().contains(settingDetail.getVariableType())) {
				hasAnalysisVariable = true;
				break;
			}
		}
		Assert.assertFalse("'Analysis' and 'Analysis Summary' variables should not be found under Study Conditions of the Summary page.",
				hasAnalysisVariable);
		Mockito.verify(fieldbookService).getPersonByUserId(NumberUtils.toInt(workbook.getStudyDetails().getCreatedBy()));

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
		this.reviewStudyDetailsController.setContextUtil(contextUtil);
		Mockito.doReturn(this.PROGRAM_UUID).when(contextUtil).getCurrentProgramUUID();
	}

	private void mockStandardVariables(final List<MeasurementVariable> allVariables, final FieldbookService fieldbookMiddlewareService,
			final com.efficio.fieldbook.service.api.FieldbookService fieldbookService) {
		for (final MeasurementVariable measurementVariable : allVariables) {
			final StandardVariable stdVar = this.createStandardVariable(measurementVariable.getTermId(), measurementVariable.getProperty(),
					measurementVariable.getScale(), measurementVariable.getMethod(), measurementVariable.getRole());
			Mockito.doReturn(stdVar).when(fieldbookMiddlewareService).getStandardVariable(measurementVariable.getTermId(),
					this.PROGRAM_UUID);
			Mockito.doReturn(stdVar.getId()).when(fieldbookMiddlewareService).getStandardVariableIdByPropertyScaleMethodRole(
					measurementVariable.getProperty(), measurementVariable.getScale(), measurementVariable.getMethod(),
					measurementVariable.getRole());
			Mockito.when(fieldbookService.getValue(Matchers.anyInt(), Matchers.anyString(), Matchers.anyBoolean())).thenReturn("");
		}
	}

	private StandardVariable createStandardVariable(final Integer id, final String property, final String scale, final String method,
			final PhenotypicType role) {
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(id);
		standardVariable.setProperty(this.createTerm(property));
		standardVariable.setScale(this.createTerm(scale));
		standardVariable.setMethod(this.createTerm(method));
		standardVariable.setPhenotypicType(role);
		standardVariable.setDataType(this.createTerm("N"));
		return standardVariable;
	}

	private Term createTerm(final String name) {
		final Term term = new Term();
		term.setId(1);
		term.setName(name);
		return term;
	}
}
