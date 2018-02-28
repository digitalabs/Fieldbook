
package com.efficio.fieldbook.web.fieldmap.controller;

import java.io.FileOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.ExportFieldmapService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.util.FileExportInfo;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;

@RunWith(MockitoJUnitRunner.class)
public class GenerateFieldmapControllerTest {

	public static final String BLOCK_NAME = "block semi-colon;";
	@Mock
	private FieldmapForm fieldmapForm;

	@Mock
	private Model model;

	@Mock
	private HttpServletRequest request;

	@Mock
	private UserFieldmap userFieldmap;

	@Mock
	private ExportFieldmapService exportExcelService;

	@InjectMocks
	private GenerateFieldmapController generateFieldmapCtrlToTest;

	@Test
	public void testExportExcel() throws Exception {
		/* Setup controller */
		Mockito.when(this.userFieldmap.getBlockName()).thenReturn(GenerateFieldmapControllerTest.BLOCK_NAME);

		Mockito.when(this.exportExcelService.exportFieldMapToExcel(Matchers.anyString(), Matchers.eq(this.userFieldmap))).thenReturn(
				Mockito.mock(FileOutputStream.class));

		// We dont care which ever browser we use, so we return anything for user-agent
		Mockito.when(this.request.getHeader("User-Agent")).thenReturn("RANDOM_BROWSER");

		FileExportInfo fileExportInfo = generateFieldmapCtrlToTest.makeSafeFileName(GenerateFieldmapControllerTest.BLOCK_NAME);

		/* Call method to test, collect the output */
		ResponseEntity<FileSystemResource> output = generateFieldmapCtrlToTest.exportExcel(this.request);

		// Verify that we performed the export operation
		Mockito.verify(this.exportExcelService).exportFieldMapToExcel(fileExportInfo.getFilePath(), this.userFieldmap);

		// Verify that the export is success
		Assert.assertEquals("Request to controller should be success", HttpStatus.OK , output.getStatusCode());
	}

	@Test(expected=FieldbookException.class)
	public void testExportExcelAssumeFailure() throws Exception {
		/* Setup controller */
		Mockito.when(this.userFieldmap.getBlockName()).thenReturn(GenerateFieldmapControllerTest.BLOCK_NAME);

		Mockito.when(this.exportExcelService.exportFieldMapToExcel(Matchers.anyString(), Matchers.eq(this.userFieldmap))).thenThrow(
				new FieldbookException("Something went wrong with writing the excel file"));

		/*
		 * Call method to test, collect the output
		 * We now expect the controller to throw an exception
		 */
		ResponseEntity<FileSystemResource> output = generateFieldmapCtrlToTest.exportExcel(this.request);

	}

	@Test
	public void makeSafeFileName() throws Exception {
		FileExportInfo exportInfo = this.generateFieldmapCtrlToTest.makeSafeFileName(GenerateFieldmapControllerTest.BLOCK_NAME);

		Assert.assertTrue("Contains the BLOCK_NAME without spaces",
				exportInfo.getDownloadFileName().contains(GenerateFieldmapControllerTest.BLOCK_NAME.replace(" ", "")));
		Assert.assertTrue("No spaces, ends with \"-<current_date>.xls\"",
				!exportInfo.getDownloadFileName().contains(" ") && exportInfo.getDownloadFileName().endsWith("-" + DateUtil.getCurrentDateAsStringValue() + ".xls"));

	}

}
