
package com.efficio.fieldbook.web.common.controller;

import java.io.IOException;

import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.service.DataKaptureImportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.common.service.FieldroidImportStudyService;
import com.efficio.fieldbook.web.common.service.KsuExcelImportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.lowagie.text.pdf.codec.Base64.InputStream;

public class ImportStudyControllerTest {

	private static final int NO_OF_OBSERVATION = 20;
	private static final String SAMPLE_FILE_XLS = "SampleFile.xls";
	private static final String SAMPLE_FILE_XLSX = "SampleFile.xlsx";
	private static final String SAMPLE_FILE_CSV = "SampleFile.csv";
	@Mock
	private BindingResult result;
	@Mock
	private MultipartFile file;
	@Mock
	private FileService fileService;
	@Mock
	private FieldroidImportStudyService fieldroidImportStudyService;
	@Mock
	private ExcelImportStudyService excelImportStudyService;
	@Mock
	private DataKaptureImportStudyService dataKaptureImportStudyService;
	@Mock
	private KsuExcelImportStudyService ksuExcelImportStudyService;
	@Mock
	private InputStream inputStream;
	@Mock
	private OntologyService ontologyService;
	@Mock
	private FieldbookService fieldbookMiddlewareService;

	private Workbook workbook;

	@InjectMocks
	private final ImportStudyController _importStudyController = new ImportStudyController();

	private ImportStudyController importStudyController;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.importStudyController = Mockito.spy(this._importStudyController);
	}

	@Test
	public void testValidateImportFile_ForFieldroid_FileIsCSV() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_NURSERY_FIELDLOG_FIELDROID.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForFieldroid_FileIsNotCSV() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_NURSERY_FIELDLOG_FIELDROID.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(1)).rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForDataKapture_FileIsCSV() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_DATAKAPTURE.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForDataKapture_FileIsNotCSV() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_DATAKAPTURE.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(1)).rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForExcel_FileIsXLSX() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLSX);
		Integer importType = AppConstants.IMPORT_NURSERY_EXCEL.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForExcel_FileIsXLS() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_NURSERY_EXCEL.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForExcel_FileIsNotXLS() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_NURSERY_EXCEL.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(1)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForKsuExcel_FileIsXLS() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_KSU_EXCEL.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForKsuExcel_FileIsXLSX() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_XLSX);
		Integer importType = AppConstants.IMPORT_KSU_EXCEL.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(0)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testValidateImportFile_ForKsuExcel_FileIsNotXLS() {
		Mockito.when(this.file.getOriginalFilename()).thenReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_KSU_EXCEL.getInt();
		this.importStudyController.validateImportFile(this.file, this.result, importType);

		Mockito.verify(this.result, Mockito.times(1)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}

	@Test
	public void testImportWorkbookByType_ForFieldroid() throws IOException, WorkbookParserException {
		Mockito.doReturn(this.inputStream).when(this.file).getInputStream();
		Mockito.doReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV).when(this.fileService).saveTemporaryFile(this.inputStream);
		Mockito.doReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV).when(this.fileService)
				.getFilePath(ImportStudyControllerTest.SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_NURSERY_FIELDLOG_FIELDROID.getInt();

		Mockito.doNothing().when(this.importStudyController).validateImportFile(this.file, this.result, importType);

		this.workbook = WorkbookDataUtil.getTestWorkbook(ImportStudyControllerTest.NO_OF_OBSERVATION, StudyType.N);
		this.importStudyController.importWorkbookByType(this.file, this.result, this.workbook, importType);

		Mockito.verify(this.fieldroidImportStudyService, Mockito.times(1)).importWorkbook(this.workbook,
				ImportStudyControllerTest.SAMPLE_FILE_CSV, this.ontologyService, this.fieldbookMiddlewareService);
	}

	@Test
	public void testImportWorkbookByType_ForExcel() throws IOException, WorkbookParserException {
		Mockito.doReturn(this.inputStream).when(this.file).getInputStream();
		Mockito.doReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS).when(this.fileService).saveTemporaryFile(this.inputStream);
		Mockito.doReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS).when(this.fileService)
				.getFilePath(ImportStudyControllerTest.SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_NURSERY_EXCEL.getInt();

		Mockito.doNothing().when(this.importStudyController).validateImportFile(this.file, this.result, importType);

		this.workbook = WorkbookDataUtil.getTestWorkbook(ImportStudyControllerTest.NO_OF_OBSERVATION, StudyType.N);
		this.importStudyController.importWorkbookByType(this.file, this.result, this.workbook, importType);

		Mockito.verify(this.excelImportStudyService, Mockito.times(1)).importWorkbook(this.workbook,
				ImportStudyControllerTest.SAMPLE_FILE_XLS, this.ontologyService, this.fieldbookMiddlewareService);
	}

	@Test
	public void testImportWorkbookByType_ForDataKapture() throws IOException, WorkbookParserException {
		Mockito.doReturn(this.inputStream).when(this.file).getInputStream();
		Mockito.doReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV).when(this.fileService).saveTemporaryFile(this.inputStream);
		Mockito.doReturn(ImportStudyControllerTest.SAMPLE_FILE_CSV).when(this.fileService)
				.getFilePath(ImportStudyControllerTest.SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_DATAKAPTURE.getInt();

		Mockito.doNothing().when(this.importStudyController).validateImportFile(this.file, this.result, importType);

		this.workbook = WorkbookDataUtil.getTestWorkbook(ImportStudyControllerTest.NO_OF_OBSERVATION, StudyType.N);
		this.importStudyController.importWorkbookByType(this.file, this.result, this.workbook, importType);

		Mockito.verify(this.dataKaptureImportStudyService, Mockito.times(1)).importWorkbook(this.workbook,
				ImportStudyControllerTest.SAMPLE_FILE_CSV, this.ontologyService, this.fieldbookMiddlewareService);
	}

	@Test
	public void testImportWorkbookByType_ForKsuExcel() throws IOException, WorkbookParserException {
		Mockito.doReturn(this.inputStream).when(this.file).getInputStream();
		Mockito.doReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS).when(this.fileService).saveTemporaryFile(this.inputStream);
		Mockito.doReturn(ImportStudyControllerTest.SAMPLE_FILE_XLS).when(this.fileService)
				.getFilePath(ImportStudyControllerTest.SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_KSU_EXCEL.getInt();

		Mockito.doNothing().when(this.importStudyController).validateImportFile(this.file, this.result, importType);

		this.workbook = WorkbookDataUtil.getTestWorkbook(ImportStudyControllerTest.NO_OF_OBSERVATION, StudyType.N);
		this.importStudyController.importWorkbookByType(this.file, this.result, this.workbook, importType);

		Mockito.verify(this.ksuExcelImportStudyService, Mockito.times(1)).importWorkbook(this.workbook,
				ImportStudyControllerTest.SAMPLE_FILE_XLS, this.ontologyService, this.fieldbookMiddlewareService);
	}
}
