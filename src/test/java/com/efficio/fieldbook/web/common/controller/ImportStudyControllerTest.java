package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.service.DataKaptureImportStudyService;
import com.efficio.fieldbook.web.common.service.ExcelImportStudyService;
import com.efficio.fieldbook.web.common.service.FieldroidImportStudyService;
import com.efficio.fieldbook.web.common.service.KsuExcelImportStudyService;
import com.efficio.fieldbook.web.util.AppConstants;
import com.lowagie.text.pdf.codec.Base64.InputStream;
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
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.mockito.Mockito.*;

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
	private ImportStudyController _importStudyController = new ImportStudyController(); 
	
	private ImportStudyController importStudyController;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		importStudyController = spy(_importStudyController);
	}
	
	@Test
	public void testValidateImportFile_ForFieldroid_FileIsCSV(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_NURSERY_FIELDLOG_FIELDROID.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(0)).rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
	}
	
	@Test
	public void testValidateImportFile_ForFieldroid_FileIsNotCSV(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_NURSERY_FIELDLOG_FIELDROID.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(1)).rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
	}
	
	@Test
	public void testValidateImportFile_ForDataKapture_FileIsCSV(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_DATAKAPTURE.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(0)).rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
	}
	
	@Test
	public void testValidateImportFile_ForDataKapture_FileIsNotCSV(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_DATAKAPTURE.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(1)).rejectValue("file", AppConstants.FILE_NOT_CSV_ERROR.getString());
	}
	
	@Test
	public void testValidateImportFile_ForExcel_FileIsXLSX(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_XLSX);
		Integer importType = AppConstants.IMPORT_NURSERY_EXCEL.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(0)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}
	
	@Test
	public void testValidateImportFile_ForExcel_FileIsXLS(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_NURSERY_EXCEL.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(0)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}
	
	@Test
	public void testValidateImportFile_ForExcel_FileIsNotXLS(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_NURSERY_EXCEL.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(1)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}
	
	@Test
	public void testValidateImportFile_ForKsuExcel_FileIsXLS(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_KSU_EXCEL.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(0)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}
	
	@Test
	public void testValidateImportFile_ForKsuExcel_FileIsXLSX(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_XLSX);
		Integer importType = AppConstants.IMPORT_KSU_EXCEL.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(0)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}
	
	@Test
	public void testValidateImportFile_ForKsuExcel_FileIsNotXLS(){
		when(file.getOriginalFilename()).thenReturn(SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_KSU_EXCEL.getInt();
		importStudyController.validateImportFile(file, result, importType);
		
		verify(result,times(1)).rejectValue("file", AppConstants.FILE_NOT_EXCEL_ERROR.getString());
	}
	
	@Test
	public void testImportWorkbookByType_ForFieldroid() throws IOException, WorkbookParserException{
		doReturn(inputStream).when(file).getInputStream();
		doReturn(SAMPLE_FILE_CSV).when(fileService).saveTemporaryFile(inputStream);
		doReturn(SAMPLE_FILE_CSV).when(fileService).getFilePath(SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_NURSERY_FIELDLOG_FIELDROID.getInt();
		
		doNothing().when(importStudyController).validateImportFile(file, result, importType);
		
		workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATION, StudyType.N);
		importStudyController.importWorkbookByType(file, result, workbook, importType);
		
		verify(fieldroidImportStudyService,times(1)).importWorkbook(workbook, SAMPLE_FILE_CSV, ontologyService, fieldbookMiddlewareService);
	}
	
	@Test
	public void testImportWorkbookByType_ForExcel() throws IOException, WorkbookParserException{
		doReturn(inputStream).when(file).getInputStream();
		doReturn(SAMPLE_FILE_XLS).when(fileService).saveTemporaryFile(inputStream);
		doReturn(SAMPLE_FILE_XLS).when(fileService).getFilePath(SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_NURSERY_EXCEL.getInt();
		
		doNothing().when(importStudyController).validateImportFile(file, result, importType);
		
		workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATION, StudyType.N);
		importStudyController.importWorkbookByType(file, result, workbook, importType);
		
		verify(excelImportStudyService,times(1)).importWorkbook(workbook, SAMPLE_FILE_XLS, ontologyService, fieldbookMiddlewareService);
	}
	
	@Test
	public void testImportWorkbookByType_ForDataKapture() throws IOException, WorkbookParserException{
		doReturn(inputStream).when(file).getInputStream();
		doReturn(SAMPLE_FILE_CSV).when(fileService).saveTemporaryFile(inputStream);
		doReturn(SAMPLE_FILE_CSV).when(fileService).getFilePath(SAMPLE_FILE_CSV);
		Integer importType = AppConstants.IMPORT_DATAKAPTURE.getInt();
		
		doNothing().when(importStudyController).validateImportFile(file, result, importType);
		
		workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATION, StudyType.N);
		importStudyController.importWorkbookByType(file, result, workbook, importType);
		
		verify(dataKaptureImportStudyService,times(1)).importWorkbook(workbook, SAMPLE_FILE_CSV, ontologyService, fieldbookMiddlewareService);
	}
	
	@Test
	public void testImportWorkbookByType_ForKsuExcel() throws IOException, WorkbookParserException{
		doReturn(inputStream).when(file).getInputStream();
		doReturn(SAMPLE_FILE_XLS).when(fileService).saveTemporaryFile(inputStream);
		doReturn(SAMPLE_FILE_XLS).when(fileService).getFilePath(SAMPLE_FILE_XLS);
		Integer importType = AppConstants.IMPORT_KSU_EXCEL.getInt();
		
		doNothing().when(importStudyController).validateImportFile(file, result, importType);
		
		workbook = WorkbookDataUtil.getTestWorkbook(NO_OF_OBSERVATION, StudyType.N);
		importStudyController.importWorkbookByType(file, result, workbook, importType);
		
		verify(ksuExcelImportStudyService,times(1)).importWorkbook(workbook, SAMPLE_FILE_XLS, ontologyService, fieldbookMiddlewareService);
	}
}
