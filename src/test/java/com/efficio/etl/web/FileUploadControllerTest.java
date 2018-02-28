
package com.efficio.etl.web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.efficio.fieldbook.service.api.WorkbenchService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.HTTPSessionUtil;
import org.generationcp.middleware.data.initializer.MeasurementRowTestDataInitializer;
import org.generationcp.middleware.data.initializer.MeasurementVariableTestDataInitializer;
import org.generationcp.middleware.data.initializer.WorkbookTestDataInitializer;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.operation.parser.WorkbookParser;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.DataImportService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
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
import com.efficio.fieldbook.service.api.FieldbookService;

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

	@Mock
	protected WorkbenchService workbenchService;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	private Model model;
	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private MeasurementVariable plotIdMeasurementVariable;

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
		project.getCropType().setPlotCodePrefix(FileUploadControllerTest.PROJECT_CODE_PREFIX);
		project.setProjectId(Long.valueOf(1));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(project);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(FileUploadControllerTest.PROGRAM_UUID);

		this.plotIdMeasurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.PLOT_ID.getId(), TermId.PLOT_ID.name(), null);
		Mockito.when(this.fieldbookService.createMeasurementVariable(String.valueOf(TermId.PLOT_ID.getId()), "",
				Operation.ADD, PhenotypicType.GERMPLASM)).thenReturn(this.plotIdMeasurementVariable);

		Mockito.when(this.fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(
				Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any(PhenotypicType.class),
				Matchers.anyString())).thenReturn(null);

		Mockito.when(this.workbenchService.getCurrentIbdbUserId(Matchers.anyLong(), Matchers.anyInt())).thenReturn(1);
	}

	@Test
	public void testEmptyFileHandling() {

		this.form.setFile(null);

		// stub the hasErrors call to provide expected fileUploadController flow
		Mockito.when(this.result.hasErrors()).thenReturn(true);

		final String navigationResult = this.fileUploadController.uploadFile(this.form, this.result, this.model);

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

		final String navigationResult = this.fileUploadController.uploadFile(this.form, this.result, this.model);

		// verify if the expected methods in the mock object were called
		Mockito.verify(this.result).rejectValue("file", FileUploadFormValidator.FILE_NOT_EXCEL_ERROR);

		Assert.assertEquals(this.fileUploadController.getContentName(), navigationResult);
	}

	@Test
	public void testStartProcessSuccessful() throws WorkbookParserException {

		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyType.T, "Sample Study", 1,
				false);

		Mockito.when(this.dataImportService.parseWorkbook(Matchers.any(File.class), Matchers.anyString(),
				Matchers.anyBoolean(), Matchers.any(WorkbookParser.class), Matchers.anyInt())).thenReturn(workbook);

		final Map<String, String> returnMessage = this.fileUploadController.startProcess(0, this.session, this.request,
				this.response, this.model);

		Mockito.verify(this.fieldbookService).addStudyUUIDConditionAndPlotIDFactorToWorkbook(workbook, true);
		Mockito.verify(this.dataImportService).saveDataset(workbook, FileUploadControllerTest.PROGRAM_UUID,
				FileUploadControllerTest.PROJECT_CODE_PREFIX);
		Mockito.verify(this.httpSessionUtil).clearSessionData(this.session,
				new String[] { HTTPSessionUtil.USER_SELECTION_SESSION_NAME });

		Assert.assertEquals(FileUploadController.STATUS_CODE_SUCCESSFUL,
				returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("Import is done.", returnMessage.get(FileUploadController.STATUS_MESSAGE));

	}

	@Test
	public void testStartProcessParserException() throws WorkbookParserException {

		final String errorMessage = "sample message";
		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyType.T, "Sample Study", 1,
				false);

		Mockito.when(this.dataImportService.parseWorkbook(Matchers.any(File.class), Matchers.anyString(),
				Matchers.anyBoolean(), Matchers.any(WorkbookParser.class), Matchers.anyInt()))
				.thenThrow(new WorkbookParserException(errorMessage));

		Mockito.when(this.contextUtil.getCurrentIbdbUserId()).thenReturn(1);

		final Map<String, String> returnMessage = this.fileUploadController.startProcess(0, this.session, this.request,
				this.response, this.model);

		Mockito.verify(this.fieldbookService, Mockito.times(0))
				.addMeasurementVariableToList(this.plotIdMeasurementVariable, workbook.getFactors());
		Mockito.verify(this.fieldbookService, Mockito.times(0)).addStudyUUIDConditionAndPlotIDFactorToWorkbook(workbook,
				false);
		Mockito.verify(this.fieldbookService, Mockito.times(0))
				.addMeasurementVariableToMeasurementRows(this.plotIdMeasurementVariable, workbook.getObservations());
		Mockito.verify(this.dataImportService, Mockito.times(0)).saveDataset(workbook,
				FileUploadControllerTest.PROGRAM_UUID, FileUploadControllerTest.PROJECT_CODE_PREFIX);
		Mockito.verify(this.httpSessionUtil, Mockito.times(0)).clearSessionData(this.session,
				new String[] { HTTPSessionUtil.USER_SELECTION_SESSION_NAME });

		Assert.assertEquals(FileUploadController.STATUS_CODE_HAS_ERROR,
				returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals(errorMessage, returnMessage.get(FileUploadController.STATUS_MESSAGE));
		Assert.assertEquals(WorkbookParserException.class.getSimpleName(),
				returnMessage.get(FileUploadController.ERROR_TYPE));

	}

	@Test
	public void testStartProcessParserIOException() throws WorkbookParserException, IOException {

		final Workbook workbook = WorkbookTestDataInitializer.createTestWorkbook(1, StudyType.T, "Sample Study", 1,
				false);

		Mockito.when(this.etlService.retrieveCurrentWorkbookAsFile(this.userSelection)).thenThrow(new IOException());

		final Map<String, String> returnMessage = this.fileUploadController.startProcess(0, this.session, this.request,
				this.response, this.model);

		Mockito.verify(this.fieldbookService, Mockito.times(0))
				.addMeasurementVariableToList(this.plotIdMeasurementVariable, workbook.getFactors());
		Mockito.verify(this.fieldbookService, Mockito.times(0)).addStudyUUIDConditionAndPlotIDFactorToWorkbook(workbook,
				false);
		Mockito.verify(this.fieldbookService, Mockito.times(0))
				.addMeasurementVariableToMeasurementRows(this.plotIdMeasurementVariable, workbook.getObservations());
		Mockito.verify(this.dataImportService, Mockito.times(0)).saveDataset(workbook,
				FileUploadControllerTest.PROGRAM_UUID, FileUploadControllerTest.PROJECT_CODE_PREFIX);
		Mockito.verify(this.httpSessionUtil, Mockito.times(0)).clearSessionData(this.session,
				new String[] { HTTPSessionUtil.USER_SELECTION_SESSION_NAME });

		Assert.assertEquals(FileUploadController.STATUS_CODE_HAS_ERROR,
				returnMessage.get(FileUploadController.STATUS_CODE));
		Assert.assertEquals("An error occurred while reading the file.",
				returnMessage.get(FileUploadController.STATUS_MESSAGE));
		Assert.assertEquals(IOException.class.getSimpleName(), returnMessage.get(FileUploadController.ERROR_TYPE));

	}

	@Test
	public void testConvertEntryTypeNameToID() {
		final String value = "T";
		final MeasurementVariable measurementVariable = MeasurementVariableTestDataInitializer
				.createMeasurementVariable(TermId.ENTRY_TYPE.getId(), TermId.ENTRY_TYPE.name(), value);
		Mockito.when(this.fieldbookMiddlewareService.getMeasurementVariableByPropertyScaleMethodAndRole(
				Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.any(PhenotypicType.class),
				Matchers.anyString())).thenReturn(measurementVariable);
		final List<MeasurementRow> observations = MeasurementRowTestDataInitializer.createMeasurementRowList(
				TermId.ENTRY_TYPE.getId(), TermId.ENTRY_TYPE.name(), value, measurementVariable);
		final Map<String, Integer> availableEntryTypes = new HashMap<>();
		availableEntryTypes.put(value, TermId.ENTRY_TYPE.getId());
		final MeasurementData mdata = observations.get(0).getMeasurementData(TermId.ENTRY_TYPE.getId());
		this.fileUploadController.convertEntryTypeNameToID(FileUploadControllerTest.PROGRAM_UUID, observations,
				availableEntryTypes);

		Assert.assertEquals(String.valueOf(TermId.ENTRY_TYPE.getId()), mdata.getValue());
	}
}
