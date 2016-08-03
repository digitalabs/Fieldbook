
package com.efficio.etl.web;

import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.etl.service.ETLService;
import com.efficio.etl.web.bean.FileUploadForm;
import com.efficio.etl.web.bean.UserSelection;
import com.efficio.etl.web.validators.FileUploadFormValidator;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@RunWith(JUnit4.class)
public class FileUploadControllerTest {

	private FileUploadController dut;
	private MultipartFile file;
	private ETLService etlService;
	private UserSelection userSelection;
	private FileUploadForm form;
	private BindingResult result;
	private Model model;
	private HttpSession session;

	private Workbook workbook;

	private final static String TEST_EXCEL_FILE_NAME = "something.xls";
	private final static String TEST_TEMP_FILE_NAME = "tempFile";

	@Before
	public void setup() {
		this.dut = new FileUploadController();
		this.file = Mockito.mock(MultipartFile.class);
		this.etlService = Mockito.mock(ETLService.class);
		this.userSelection = new UserSelection();
		this.workbook = Mockito.mock(Workbook.class);
		this.session = new MockHttpSession();

		this.form = new FileUploadForm();
		this.form.setFile(this.file);

		this.result = Mockito.mock(BindingResult.class);

		this.dut.setEtlService(this.etlService);
		this.dut.setUserSelection(this.userSelection);

		this.model = Mockito.mock(Model.class);
	}

	@After
	public void destroy() {
		this.file = null;
		this.etlService = null;
		this.workbook = null;
		this.result = null;
	}

	@Test
	public void testEmptyFileHandling() {

		this.form.setFile(null);

		// stub the hasErrors call to provide expected dut flow
		Mockito.when(this.result.hasErrors()).thenReturn(true);

		String navigationResult = this.dut.uploadFile(this.form, this.result, this.model);

		// verify if the expected methods in the mock object were called
		Mockito.verify(this.result).rejectValue("file", FileUploadFormValidator.FILE_NOT_FOUND_ERROR);

		System.out.println(navigationResult);

		Assert.assertEquals(this.dut.getContentName(), navigationResult);
	}

	@Test
	public void testNonExcelFileUpload() {
		this.form.setFile(this.file);

		Mockito.when(this.file.getOriginalFilename()).thenReturn("something.txt");
		Mockito.when(this.result.hasErrors()).thenReturn(true);

		String navigationResult = this.dut.uploadFile(this.form, this.result, this.model);

		// verify if the expected methods in the mock object were called
		Mockito.verify(this.result).rejectValue("file", FileUploadFormValidator.FILE_NOT_EXCEL_ERROR);

		Assert.assertEquals(this.dut.getContentName(), navigationResult);
	}
}
