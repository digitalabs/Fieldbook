package com.efficio.etl.web.controller.angular;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.bean.VariableDTO;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.google.common.base.Optional;
import junit.framework.Assert;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AngularMapOntologyControllerTest {

	private static final String PROGRAM_UUID = "55bd5dde-3a68-4dcd-bdda-d2301eff9e16";
	public static final String CONTEXT_PATH = "contextPath";
	public static final int CURRENT_IBDB_USER_ID = 1;
	public static final int NURSERY_TYPE_ID = 1;
	public static final int STUDY_TYPE_ID = 6;

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
	public void testProcessImporHeaderWithErrors() throws IOException, WorkbookParserException {
		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto(6,"","T"), "Sample Study", 1,
				false);

		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);

		// Add Variable with no header mapping
		final VariableDTO variableWithNoHeaderMapping = new VariableDTO();
		variableWithNoHeaderMapping.setId(null);

		// Add duplicate variables
		final VariableDTO variableDuplicate1 = new VariableDTO();
		variableDuplicate1.setHeaderName("SOME_VARIABLE");
		final VariableDTO variableDuplicate2 = new VariableDTO();
		variableDuplicate2.setHeaderName("SOME_VARIABLE");

		//
		Mockito.when(dataImportService.findMeasurementVariableByTermId(Matchers.eq(TermId.LOCATION_ID.getId()), Mockito.anyList())).thenReturn(Optional.<MeasurementVariable>absent());
		Mockito.when(dataImportService.findMeasurementVariableByTermId(Matchers.eq(TermId.TRIAL_LOCATION.getId()), Mockito.anyList())).thenReturn(Optional.of(new MeasurementVariable()));

		final VariableDTO[] variables = { variableWithNoHeaderMapping, variableDuplicate1, variableDuplicate2 } ;

		this.controller.processImport(variables);

		final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);


		Mockito.verify(etlService).mergeVariableData(variables, apacheWorkbook, this.userSelection);
		Mockito.verify(etlService).validateProjectOntology(workbook);

		Mockito.verify(etlService, Mockito.times(5)).convertMessageList(captor.capture());

		List<List> messages = captor.getAllValues();
		List<String> messageKeys = new ArrayList<>();
		for (List<Message> message : messages) {
			messageKeys.add(message.get(0).getMessageKey());
		}

		Assert.assertTrue(messageKeys.contains(AngularMapOntologyController.ERROR_HEADER_NO_MAPPING));
		Assert.assertTrue(messageKeys.contains(AngularMapOntologyController.ERROR_DUPLICATE_LOCAL_VARIABLE));
		Assert.assertTrue(messageKeys.contains(AngularMapOntologyController.ERROR_LOCATION_ID_DOESNT_EXISTS));

	}

	@Test
	public void testProcessImporHeaderSuccess() throws IOException, WorkbookParserException {
		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto(6,"","T"), "Sample Study", 1,
				false);

		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);

		VariableDTO variable1 = new VariableDTO();
		variable1.setId(101);
		variable1.setHeaderName("VARIABLE1");
		VariableDTO variable2 = new VariableDTO();
		variable2.setId(102);
		variable2.setHeaderName("VARIABLE2");

		Mockito.when(dataImportService.findMeasurementVariableByTermId(Matchers.eq(TermId.LOCATION_ID.getId()), Matchers.anyList())).thenReturn(Optional.of(new MeasurementVariable()));
		Mockito.when(dataImportService.findMeasurementVariableByTermId(Matchers.eq(TermId.TRIAL_LOCATION.getId()), Matchers.anyList())).thenReturn(Optional.of(new MeasurementVariable()));

		final VariableDTO[] variables = { variable1, variable2 } ;

		this.controller.processImport(variables);

		Mockito.verify(etlService).mergeVariableData(variables, apacheWorkbook, this.userSelection);
		Mockito.verify(etlService).validateProjectOntology(workbook);

		Mockito.verify(etlService, Mockito.times(0)).convertMessageList(Mockito.anyList());

	}

	@Test
	public void testConfirmImport() throws IOException, WorkbookParserException {
		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, new StudyTypeDto(6,"","T"), "Sample Study", 1,
				false);
		workbook.getStudyDetails().getStudyType().setId(1);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);

		final VariableDTO[] variables = new VariableDTO[] {};

		final Map<String, Object> result = this.controller.confirmImport(variables, this.session, this.request);

		Mockito.verify(this.userSelection).clearMeasurementVariables();
		Mockito.verify(this.etlService).mergeVariableData(variables, apacheWorkbook, this.userSelection);
		Mockito.verify(this.dataImportService).addLocationIDVariableInFactorsIfNotExists(workbook, PROGRAM_UUID);
		Mockito.verify(this.dataImportService).removeLocationNameVariableIfExists(workbook);
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

	@Test
	public void testCheckIfLocationIdVariableExistsTrial() throws IOException, WorkbookParserException {

		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(6, new StudyTypeDto(STUDY_TYPE_ID,"","T"), "Sample Study", 1,
				false);

		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(Matchers.eq(TermId.LOCATION_ID.getId()), Matchers.anyList())).thenReturn(Optional.<MeasurementVariable>absent());
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(Matchers.eq(TermId.TRIAL_LOCATION.getId()), Matchers.anyList())).thenReturn(Optional.of(new MeasurementVariable()));

		Assert.assertTrue(this.controller.checkIfLocationIdVariableExists(workbook));

		final ArgumentCaptor<List> captor1 = ArgumentCaptor.forClass(List.class);
		final ArgumentCaptor<List> captor2 = ArgumentCaptor.forClass(List.class);
		Mockito.verify(this.dataImportService).findMeasurementVariableByTermId(Matchers.eq(TermId.LOCATION_ID.getId()), captor1.capture());
		Mockito.verify(this.dataImportService).findMeasurementVariableByTermId(Matchers.eq(TermId.LOCATION_ID.getId()), captor2.capture());

		List<MeasurementVariable> measurementVariables = new ArrayList<>();

		measurementVariables.addAll(workbook.getConditions());
		measurementVariables.addAll(workbook.getFactors());

		Assert.assertEquals(measurementVariables, captor1.getValue());
		Assert.assertEquals(measurementVariables, captor2.getValue());

	}

	@Test
	public void testCheckIfLocationIdVariableExistsNursery() throws IOException, WorkbookParserException {

		final org.apache.poi.ss.usermodel.Workbook apacheWorkbook = Mockito
				.mock(org.apache.poi.ss.usermodel.Workbook.class);
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(6, new StudyTypeDto(NURSERY_TYPE_ID,"","N"), "Sample Study", 1,
				false);

		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(apacheWorkbook);
		Mockito.when(this.etlService.convertToWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(apacheWorkbook, CURRENT_IBDB_USER_ID)).thenReturn(workbook);
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(Matchers.eq(TermId.LOCATION_ID.getId()), Matchers.anyList())).thenReturn(Optional.<MeasurementVariable>absent());
		Mockito.when(this.dataImportService.findMeasurementVariableByTermId(Matchers.eq(TermId.TRIAL_LOCATION.getId()), Matchers.anyList())).thenReturn(Optional.of(new MeasurementVariable()));

		Assert.assertTrue(this.controller.checkIfLocationIdVariableExists(workbook));

		final ArgumentCaptor<List> captor1 = ArgumentCaptor.forClass(List.class);
		final ArgumentCaptor<List> captor2 = ArgumentCaptor.forClass(List.class);
		Mockito.verify(this.dataImportService).findMeasurementVariableByTermId(Matchers.eq(TermId.LOCATION_ID.getId()), captor1.capture());
		Mockito.verify(this.dataImportService).findMeasurementVariableByTermId(Matchers.eq(TermId.LOCATION_ID.getId()), captor2.capture());
		Assert.assertEquals(workbook.getConditions(), captor1.getValue());
		Assert.assertEquals(workbook.getConditions(), captor2.getValue());

	}

}
