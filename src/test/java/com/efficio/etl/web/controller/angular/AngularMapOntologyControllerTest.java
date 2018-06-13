package com.efficio.etl.web.controller.angular;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.efficio.fieldbook.service.api.WorkbenchService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.DataImportService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.bean.VariableDTO;
import com.efficio.fieldbook.service.api.FieldbookService;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class AngularMapOntologyControllerTest {

	private static final String PROGRAM_UUID = "55bd5dde-3a68-4dcd-bdda-d2301eff9e16";
	public static final String CONTEXT_PATH = "contextPath";
	public static final int CURRENT_IBDB_USER_ID = 1;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpSession session;

	@Mock
	private UserSelection userSelection;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private ETLService etlService;

	@Mock
	private DataImportService dataImportService;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private AngularMapOntologyController controller;

	@Mock
	protected WorkbenchService workbenchService;

	@Before
	public void setup() {

		Mockito.when(this.contextUtil.getCurrentProgramUUID())
				.thenReturn(AngularMapOntologyControllerTest.PROGRAM_UUID);
		Mockito.when(this.contextUtil.getCurrentIbdbUserId()).thenReturn(CURRENT_IBDB_USER_ID);
		Mockito.when(this.request.getContextPath()).thenReturn(AngularMapOntologyControllerTest.CONTEXT_PATH);
	}

	@Test
	public void testConfirmImport() throws IOException, WorkbookParserException {
		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto("T"), "Sample Study", 1,
				false);
		workbook.getStudyDetails().getStudyType().setId(1);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);

		final VariableDTO[] variables = new VariableDTO[] {};

		final Map<String, Object> result = this.controller.confirmImport(variables, this.session, this.request);

		Mockito.verify(this.userSelection).clearMeasurementVariables();
		Mockito.verify(this.etlService).mergeVariableData(variables, apacheWorkbook, this.userSelection);
		Mockito.verify(this.fieldbookService).addStudyUUIDConditionAndPlotIDFactorToWorkbook(workbook, false);
		Mockito.verify(this.etlService).saveProjectOntology(workbook, AngularMapOntologyControllerTest.PROGRAM_UUID);

		Mockito.verify(this.userSelection).setStudyId(workbook.getStudyDetails().getId());
		Mockito.verify(this.userSelection).setTrialDatasetId(workbook.getTrialDatasetId());
		Mockito.verify(this.userSelection).setMeasurementDatasetId(workbook.getMeasurementDatesetId());
		Mockito.verify(this.userSelection).setMeansDatasetId(workbook.getMeansDatasetId());

		Assert.assertEquals(true, result.get("success"));
		Assert.assertEquals(AngularMapOntologyControllerTest.CONTEXT_PATH + AngularOpenSheetController.URL,
				result.get("redirectUrl"));

	}

}
