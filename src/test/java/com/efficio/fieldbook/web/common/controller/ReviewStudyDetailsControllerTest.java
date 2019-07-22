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

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.security.SecurityUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.Role;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.user.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.StudyDetails;
import com.efficio.fieldbook.web.trial.form.CreateTrialForm;
import com.google.common.collect.Lists;


public class ReviewStudyDetailsControllerTest extends AbstractBaseIntegrationTest {

	@Resource
	private ReviewStudyDetailsController reviewStudyDetailsController;

	@Mock
	private FieldbookService fieldbookMWService;

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private UserService userService;

	@Mock
	private ContextUtil contextUtil;

	private Workbook workbook;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		this.workbook = WorkbookTestDataInitializer.getTestWorkbook(true);
		this.reviewStudyDetailsController.setFieldbookMiddlewareService(this.fieldbookMWService);
		this.reviewStudyDetailsController.setFieldbookService(this.fieldbookService);
		this.reviewStudyDetailsController.setUserService(this.userService);
		Mockito.doReturn(this.workbook).when(this.fieldbookMWService).getStudyVariableSettings(1);
		this.mockStandardVariables(this.workbook.getAllVariables(), this.fieldbookMWService, this.fieldbookService);

		this.reviewStudyDetailsController.setContextUtil(this.contextUtil);
		Mockito.doReturn(this.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();

		final GermplasmList germplasmList = new GermplasmList(1);
		Mockito.when(this.fieldbookMWService.getGermplasmListsByProjectId(1, GermplasmListType.STUDY)).thenReturn(Arrays.asList(germplasmList));
		Mockito.when(this.fieldbookService.getGermplasmListChecksSize(germplasmList.getId())).thenReturn(2L);
	}

	@Test
	public void testAddErrorMessageToResultForStudy() {
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

		// Verify that this.workbook has Analysis and/or Analysis Summary variables beforehand to check that they were later removed
		Assert.assertTrue(this.hasAnalysisVariables(this.workbook.getConditions()));
		Assert.assertTrue(this.hasAnalysisVariables(this.workbook.getConstants()));

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
		Mockito.verify(this.userService).getPersonName(NumberUtils.toInt(this.workbook.getStudyDetails().getCreatedBy()));
	}

	@Test
	public void testShowStudySummary() {
		final int id = 1;
		final CreateTrialForm form = new CreateTrialForm();
		final Model model = new ExtendedModelMap();

		this.reviewStudyDetailsController.show(id, form, model);

		final StudyDetails details = (StudyDetails) model.asMap().get("trialDetails");
		Assert.assertNotNull(details);
		final Boolean isSuperAdmin =  (Boolean) model.asMap().get("isSuperAdmin");
		Assert.assertNotNull(isSuperAdmin);
		Assert.assertEquals(2L, model.asMap().get("numberOfChecks"));
	}

	@Test
	public void testShowStudySummaryViewTemplateWithNullCreatedBy() {
		final int id = 1;
		final CreateTrialForm form = new CreateTrialForm();
		final Model model = new ExtendedModelMap();

		this.workbook.getStudyDetails().setCreatedBy(null);
		this.reviewStudyDetailsController.show(id, form, model);

		Mockito.verify(this.userService).getPersonName(0);

	}

	@Test
	public void testSetIsSuperAdminAttributeForNonSuperAdminUser() {
		final Model model = new ExtendedModelMap();

		SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority(SecurityUtil.ROLE_PREFIX + Role.ADMIN);
		UsernamePasswordAuthenticationToken loggedInUser = new UsernamePasswordAuthenticationToken("", "", Lists.newArrayList(roleAuthority));
		SecurityContextHolder.getContext().setAuthentication(loggedInUser);
		this.reviewStudyDetailsController.setIsSuperAdminAttribute(model);
		Assert.assertFalse((Boolean)model.asMap().get("isSuperAdmin"));
	}

	@Test
	public void testSetIsSuperAdminAttributeForSuperAdminUser() {
		final Model model = new ExtendedModelMap();

		SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority(SecurityUtil.ROLE_PREFIX + Role.SUPERADMIN);
		UsernamePasswordAuthenticationToken loggedInUser = new UsernamePasswordAuthenticationToken("", "", Lists.newArrayList(roleAuthority));
		SecurityContextHolder.getContext().setAuthentication(loggedInUser);
		this.reviewStudyDetailsController.setIsSuperAdminAttribute(model);
		Assert.assertTrue((Boolean)model.asMap().get("isSuperAdmin"));
	}

	@Test
	public void getNumberOfChecks() {
		final long numberOfChecks = this.reviewStudyDetailsController.getNumberOfChecks(1);
		Assert.assertEquals(2L, numberOfChecks);

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
			Mockito.when(fieldbookService.getValue(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean())).thenReturn("");
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
