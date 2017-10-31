package com.efficio.etl.web.controller.angular;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
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
	private ContextUtil contextUtil;

	@InjectMocks
	private AngularMapOntologyController controller;

	@Before
	public void setup() {

		Mockito.when(this.contextUtil.getCurrentProgramUUID())
				.thenReturn(AngularMapOntologyControllerTest.PROGRAM_UUID);
		Mockito.when(this.request.getContextPath()).thenReturn(AngularMapOntologyControllerTest.CONTEXT_PATH);
	}

	@Test
	public void testConfirmImport() throws IOException {

		final MeasurementVariable plotIdMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.PLOT_ID.getId(), TermId.PLOT_ID.name(), null);
		Mockito.when(this.fieldbookService.createMeasurementVariable(String.valueOf(TermId.PLOT_ID.getId()), "",
				Operation.ADD, PhenotypicType.GERMPLASM)).thenReturn(plotIdMeasurementVariable);

		final MeasurementVariable userIdMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.STUDY_UID.getId(), TermId.STUDY_UID.name(), "");
		Mockito.when(this.fieldbookService.createMeasurementVariable(String.valueOf(TermId.STUDY_UID.getId()),
				String.valueOf(this.contextUtil.getCurrentUserLocalId()), Operation.ADD, PhenotypicType.STUDY))
				.thenReturn(userIdMeasurementVariable);

		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyType.T, "Sample Study", 1,
				false);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);

		final VariableDTO[] variables = new VariableDTO[] {};

		final Map<String, Object> result = this.controller.confirmImport(variables, this.session, this.request);

		Mockito.verify(this.userSelection).clearMeasurementVariables();
		Mockito.verify(this.etlService).mergeVariableData(variables, apacheWorkbook, this.userSelection);
		Mockito.verify(this.fieldbookService).addMeasurementVariableToList(plotIdMeasurementVariable,
				workbook.getFactors());
		Mockito.verify(this.fieldbookService).addMeasurementVariableToList(userIdMeasurementVariable,
				workbook.getConditions());
		Mockito.verify(this.etlService).saveProjectOntology(workbook, AngularMapOntologyControllerTest.PROGRAM_UUID);

		Mockito.verify(this.userSelection).setStudyId(workbook.getStudyDetails().getId());
		Mockito.verify(this.userSelection).setTrialDatasetId(workbook.getTrialDatasetId());
		Mockito.verify(this.userSelection).setMeasurementDatasetId(workbook.getMeasurementDatesetId());
		Mockito.verify(this.userSelection).setMeansDatasetId(workbook.getMeansDatasetId());

		Assert.assertEquals(true, plotIdMeasurementVariable.isFactor());
		Assert.assertEquals(true, result.get("success"));
		Assert.assertEquals(AngularMapOntologyControllerTest.CONTEXT_PATH + AngularOpenSheetController.URL,
				result.get("redirectUrl"));

	}

}
