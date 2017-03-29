
package com.efficio.etl.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.utils.test.WorkbookTestUtil;
import com.efficio.fieldbook.web.util.WorkbookUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.HTTPSessionUtil;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.operation.parser.WorkbookParser;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.validators.FileUploadFormValidator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@RunWith(MockitoJUnitRunner.class)
public class FileUploadControllerTest {

	private static final String PROGRAM_UUID = "55bd5dde-3a68-4dcd-bdda-d2301eff9e16";
	private static final String PROJECT_CODE_PREFIX = "AAGhs";
	@Mock
	private MultipartFile file;

	@Mock
	private ETLService etlService;

	@Mock
	private DataImportService dataImportService;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private HTTPSessionUtil httpSessionUtil;

	private FileUploadForm form;

	@Mock
	private BindingResult result;

	private Model model;
	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@InjectMocks
	private FileUploadController fileUploadController;

	@Before
	public void setup() {

		this.session = new MockHttpSession();
		this.request = new MockHttpServletRequest();
		this.response = new MockHttpServletResponse();

		this.form = new FileUploadForm();
		this.form.setFile(this.file);


		this.fileUploadController.setEtlService(this.etlService);
		this.fileUploadController.setUserSelection(this.userSelection);

		this.model = Mockito.mock(Model.class);

		final Project project = new Project();
		project.setCropType(new CropType("Maize"));
		project.getCropType().setPlotCodePrefix(PROJECT_CODE_PREFIX);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);
	}

	@Test
	public void testEmptyFileHandling() {

		this.form.setFile(null);

		// stub the hasErrors call to provide expected fileUploadController flow
		Mockito.when(this.result.hasErrors()).thenReturn(true);

		String navigationResult = this.fileUploadController.uploadFile(this.form, this.result, this.model);

		// verify if the expected methods in the mock object were called
		Mockito.verify(this.result).rejectValue("file", FileUploadFormValidator.FILE_NOT_FOUND_ERROR);

		System.out.println(navigationResult);

		Assert.assertEquals(this.fileUploadController.getContentName(), navigationResult);
	}

	@Test
	public void testNonExcelFileUpload() {
		this.form.setFile(this.file);

		Mockito.when(this.file.getOriginalFilename()).thenReturn("something.txt");
		Mockito.when(this.result.hasErrors()).thenReturn(true);

		String navigationResult = this.fileUploadController.uploadFile(this.form, this.result, this.model);

		// verify if the expected methods in the mock object were called
		Mockito.verify(this.result).rejectValue("file", FileUploadFormValidator.FILE_NOT_EXCEL_ERROR);

		Assert.assertEquals(this.fileUploadController.getContentName(), navigationResult);
	}

	@Test
	public void testStartProcessSuccessful() throws WorkbookParserException {

		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyType.T, "Sample Study", 1, false);

		Mockito.when(this.dataImportService.parseWorkbook(Mockito.any(File.class), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any(WorkbookParser.class))).thenReturn(workbook);

		final Map<String, String> returnMessage = this.fileUploadController.startProcess(0, this.session, this.request, this.response, this.model);

		Mockito.verify(this.fieldbookService).addMeasurementVariableToList(TermId.PLOT_ID.getId(), PhenotypicType.GERMPLASM , workbook.getFactors());
		Mockito.verify(this.fieldbookService).addMeasurementVariableToMeasurementRows(TermId.PLOT_ID.getId(), PhenotypicType.GERMPLASM , workbook.getObservations());
		Mockito.verify(this.dataImportService).saveDataset(workbook, PROGRAM_UUID, PROJECT_CODE_PREFIX);
		Mockito.verify(this.httpSessionUtil).clearSessionData(this.session, new String[] {HTTPSessionUtil.USER_SELECTION_SESSION_NAME});

		Assert.assertEquals(FileUploadController.STATUS_CODE_SUCCESSFUL, returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("Import is done.", returnMessage.get(FileUploadController.STATUS_MESSAGE));

	}

	@Test
	public void testStartProcessParserException() throws WorkbookParserException {

		final String errorMessage = "sample message";
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyType.T, "Sample Study", 1, false);

		Mockito.when(this.dataImportService.parseWorkbook(Mockito.any(File.class), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any(WorkbookParser.class))).thenThrow(new WorkbookParserException(errorMessage));

		final Map<String, String> returnMessage = this.fileUploadController.startProcess(0, this.session, this.request, this.response, this.model);

		Mockito.verify(this.fieldbookService, Mockito.times(0)).addMeasurementVariableToList(TermId.PLOT_ID.getId(), PhenotypicType.GERMPLASM , workbook.getFactors());
		Mockito.verify(this.fieldbookService, Mockito.times(0)).addMeasurementVariableToMeasurementRows(TermId.PLOT_ID.getId(), PhenotypicType.GERMPLASM , workbook.getObservations());
		Mockito.verify(this.dataImportService, Mockito.times(0)).saveDataset(workbook, PROGRAM_UUID, PROJECT_CODE_PREFIX);
		Mockito.verify(this.httpSessionUtil, Mockito.times(0)).clearSessionData(this.session, new String[] {HTTPSessionUtil.USER_SELECTION_SESSION_NAME});

		Assert.assertEquals(FileUploadController.STATUS_CODE_HAS_ERROR, returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals(errorMessage, returnMessage.get(FileUploadController.STATUS_MESSAGE));
		Assert.assertEquals(WorkbookParserException.class.getSimpleName(), returnMessage.get(FileUploadController.ERROR_TYPE));

	}

	@Test
	public void testStartProcessParserIOException() throws WorkbookParserException, IOException {

		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyType.T, "Sample Study", 1, false);

		Mockito.when(this.etlService.retrieveCurrentWorkbookAsFile(this.userSelection)).thenThrow(new IOException());

		final Map<String, String> returnMessage = this.fileUploadController.startProcess(0, this.session, this.request, this.response, this.model);

		Mockito.verify(this.fieldbookService, Mockito.times(0)).addMeasurementVariableToList(TermId.PLOT_ID.getId(), PhenotypicType.GERMPLASM , workbook.getFactors());
		Mockito.verify(this.fieldbookService, Mockito.times(0)).addMeasurementVariableToMeasurementRows(TermId.PLOT_ID.getId(), PhenotypicType.GERMPLASM , workbook.getObservations());
		Mockito.verify(this.dataImportService, Mockito.times(0)).saveDataset(workbook, PROGRAM_UUID, PROJECT_CODE_PREFIX);
		Mockito.verify(this.httpSessionUtil, Mockito.times(0)).clearSessionData(this.session, new String[] {HTTPSessionUtil.USER_SELECTION_SESSION_NAME});

		Assert.assertEquals(FileUploadController.STATUS_CODE_HAS_ERROR, returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("An error occurred while reading the file.", returnMessage.get(FileUploadController.STATUS_MESSAGE));
		Assert.assertEquals(IOException.class.getSimpleName(), returnMessage.get(FileUploadController.ERROR_TYPE));

	}
}
