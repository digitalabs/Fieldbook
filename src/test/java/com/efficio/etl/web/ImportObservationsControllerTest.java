package com.efficio.etl.web;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.util.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class ImportObservationsControllerTest {

	private static final String PROGRAM_UUID = "55bd5dde-3a68-4dcd-bdda-d2301eff9e16";
	private static final String PROJECT_CODE_PREFIX = "AAGhs";
	public static final int CURRENT_IBDB_USER_ID = 1;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private ETLService etlService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private DataImportService dataImportService;

	@InjectMocks
	ImportObservationsController importObservationsController;
	private HttpSession session;
	private HttpServletRequest request;
	private FileUploadForm uploadForm;
	private Model model;

	@Before
	public void setUp() {
		this.session = new MockHttpSession();
		this.request = new MockHttpServletRequest();
		this.uploadForm = Mockito.mock(FileUploadForm.class);
		this.model = Mockito.mock(Model.class);

		final Project project = new Project();
		project.setCropType(new CropType("Maize"));
		project.getCropType().setPlotCodePrefix(ImportObservationsControllerTest.PROJECT_CODE_PREFIX);
		project.setProjectId(Long.valueOf(123));
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(ImportObservationsControllerTest.PROGRAM_UUID);
		Mockito.when(this.contextUtil.getCurrentIbdbUserId()).thenReturn(CURRENT_IBDB_USER_ID);
	}

	@Test
	public void testProcessImportWithNoErrors() throws IOException, WorkbookParserException {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook importData =
				Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		Mockito.when(this.etlService.createWorkbookFromUserSelection(Matchers.eq(this.userSelection), Matchers.anyBoolean()))
				.thenReturn(importData);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(workbook, CURRENT_IBDB_USER_ID)).thenReturn(importData);

		final String returnValue =
				this.importObservationsController.processImport(this.uploadForm, 1, this.model, this.session, this.request);
		Assert.assertEquals("redirect:/etl/fileUpload", returnValue);

		Mockito.verify(this.contextUtil).getCurrentProgramUUID();
		Mockito.verify(this.etlService).createWorkbookFromUserSelection(Matchers.eq(this.userSelection), Matchers.anyBoolean());
		Mockito.verify(this.dataImportService).parseWorkbookDescriptionSheet(workbook, CURRENT_IBDB_USER_ID);
		Mockito.verify(this.dataImportService).assignLocationIdVariableToEnvironmentDetailSection(importData);
		Mockito.verify(this.etlService).saveProjectData(importData, ImportObservationsControllerTest.PROGRAM_UUID);
		Mockito.verify(this.dataImportService).removeLocationNameVariableIfExists(importData);
	}

	@Test
	public void testProcessImportWithErrors() throws IOException, WorkbookParserException {
		final Workbook workbook = Mockito.mock(Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook importData =
				Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		Mockito.when(this.etlService.createWorkbookFromUserSelection(Matchers.eq(this.userSelection), Matchers.anyBoolean()))
				.thenReturn(importData);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);
		Mockito.when(this.etlService.convertMessageList(ArgumentMatchers.<List<Message>>any())).thenReturn(Arrays.asList("error"));

		final String returnValue =
				this.importObservationsController.processImport(this.uploadForm, 1, this.model, this.session, this.request);
		Assert.assertEquals("etl/validateProjectData", returnValue);
		Mockito.verify(this.contextUtil).getCurrentProgramUUID();
		Mockito.verify(this.etlService).createWorkbookFromUserSelection(Matchers.eq(this.userSelection), Matchers.anyBoolean());
		Mockito.verify(this.dataImportService, Mockito.times(0)).removeLocationNameVariableIfExists(importData);
		Mockito.verify(this.dataImportService, Mockito.times(0)).assignLocationIdVariableToEnvironmentDetailSection(importData);
	}

	@Test
	public void testConfirmImport() throws WorkbookParserException, IOException {
		final org.generationcp.middleware.domain.etl.Workbook importData =
				Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		final org.generationcp.middleware.domain.etl.Workbook referenceWorkbook =
				Mockito.mock(org.generationcp.middleware.domain.etl.Workbook.class);
		final Workbook workbook = Mockito.mock(Workbook.class);
		Mockito.when(this.dataImportService.parseWorkbookDescriptionSheet(workbook, CURRENT_IBDB_USER_ID)).thenReturn(referenceWorkbook);
		Mockito.when(this.etlService.retrieveCurrentWorkbook(this.userSelection)).thenReturn(workbook);

		final String returnValue =
				this.importObservationsController.confirmImport(this.model, importData, ImportObservationsControllerTest.PROGRAM_UUID);

		Assert.assertEquals("redirect:/etl/fileUpload", returnValue);
		Mockito.verify(this.dataImportService).addLocationIDVariableIfNotExists(importData, importData.getFactors(), PROGRAM_UUID);
		Mockito.verify(this.dataImportService).assignLocationIdVariableToEnvironmentDetailSection(importData);
		Mockito.verify(this.dataImportService).removeLocationNameVariableIfExists(importData);
		Mockito.verify(this.dataImportService).parseWorkbookDescriptionSheet(workbook, CURRENT_IBDB_USER_ID);
		Mockito.verify(this.etlService).saveProjectData(importData, ImportObservationsControllerTest.PROGRAM_UUID);
	}
}
