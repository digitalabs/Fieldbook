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

import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.common.form.AddOrRemoveTraitsForm;

public class ReviewStudyDetailsControllerTest extends AbstractBaseIntegrationTest {

	@Resource
	private ReviewStudyDetailsController reviewStudyDetailsController;

	@Test
	public void testShowReviewNurserySummaryWithError() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get(ReviewStudyDetailsController.URL + "/show/N/1"))
		.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.model().attributeExists("nurseryDetails"));
	}

	@Test
	public void testShowReviewTrialSummaryWithError() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get(ReviewStudyDetailsController.URL + "/show/T/1"))
		.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.model().attributeExists("trialDetails"));
	}

	@Test
	public void testAddErrorMessageToResultForNursery() throws Exception {
		final StudyDetails details = new StudyDetails();

		this.reviewStudyDetailsController.addErrorMessageToResult(details,
				new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"), true, 1);

		Assert.assertEquals("Expecting error message for nursery but got " + details.getErrorMessage() + " instead.",
				"This nursery is in a format that cannot be opened in the Nursery Manager. Please use the Study Browser if you"
						+ " wish to see the details of this nursery.", details.getErrorMessage());
	}

	@Test
	public void testAddErrorMessageToResultForTrial() throws Exception {
		final StudyDetails details = new StudyDetails();

		this.reviewStudyDetailsController.addErrorMessageToResult(details,
				new MiddlewareQueryException(ErrorCode.STUDY_FORMAT_INVALID.getCode(), "The term you entered is invalid"), false, 1);

		Assert.assertEquals("Expecting error message for nursery but got " + details.getErrorMessage() + " instead.",
				"This trial is in a format that cannot be opened in the Trial Manager. Please use the Study Browser if you"
						+ " wish to see the details of this trial.", details.getErrorMessage());
	}

	@Test
	public void testShowTrialSummaryEnvironmentsWithoutAnalysisVariables() {
		final int id = 1;
		final Workbook workbook = WorkbookTestDataInitializer.getTestWorkbook(true);
		final FieldbookService fieldbookMiddlewareService = Mockito.mock(FieldbookService.class);
		this.reviewStudyDetailsController.setFieldbookMiddlewareService(fieldbookMiddlewareService);
		Mockito.doReturn(workbook).when(fieldbookMiddlewareService).getStudyVariableSettings(id, false);
		this.mockStandardVariables(workbook.getAllVariables(), fieldbookMiddlewareService);
		this.mockContextUtil();
		final AddOrRemoveTraitsForm form = new AddOrRemoveTraitsForm();
		final Model model = new ExtendedModelMap();
		this.reviewStudyDetailsController.show(StudyType.T.toString(), id, form, model);
		final StudyDetails details = (StudyDetails) model.asMap().get("trialDetails");
		Assert.assertNotNull(details);
		final List<SettingDetail> conditionSettingDetails = details.getNurseryConditionDetails();
		boolean hasAnalysisVariable = false;
		for (final SettingDetail settingDetail : conditionSettingDetails) {
			if (settingDetail.getVariableType() == VariableType.ANALYSIS) {
				hasAnalysisVariable = true;
			}
		}
		Assert.assertFalse("Analysis variables should not be found under Trial Conditions of the Summary page.", hasAnalysisVariable);

	}

	private void mockContextUtil() {
		final ContextUtil contextUtil = Mockito.mock(ContextUtil.class);
		this.reviewStudyDetailsController.setContextUtil(contextUtil);
		Mockito.doReturn(this.PROGRAM_UUID).when(contextUtil).getCurrentProgramUUID();
	}

	private void mockStandardVariables(final List<MeasurementVariable> allVariables, final FieldbookService fieldbookMiddlewareService) {
		for (final MeasurementVariable measurementVariable : allVariables) {
			final StandardVariable stdVar =
					this.createStandardVariable(measurementVariable.getTermId(), measurementVariable.getProperty(),
							measurementVariable.getScale(), measurementVariable.getMethod(), measurementVariable.getRole());
			Mockito.doReturn(stdVar).when(fieldbookMiddlewareService)
					.getStandardVariable(measurementVariable.getTermId(), this.PROGRAM_UUID);
			Mockito.doReturn(stdVar.getId())
					.when(fieldbookMiddlewareService)
					.getStandardVariableIdByPropertyScaleMethodRole(measurementVariable.getProperty(), measurementVariable.getScale(),
							measurementVariable.getMethod(), measurementVariable.getRole());
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
