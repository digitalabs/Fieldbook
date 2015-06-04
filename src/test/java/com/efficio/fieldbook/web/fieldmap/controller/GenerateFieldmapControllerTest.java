
package com.efficio.fieldbook.web.fieldmap.controller;

import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.generationcp.commons.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.ExportExcelService;
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
	private HttpServletResponse response;

	@Mock
	private UserFieldmap userFieldmap;

	@Mock
	private ExportExcelService exportExcelService;

	@InjectMocks
	private GenerateFieldmapController generateFieldmapCtrl;

	@Test
	public void testExportExcel() throws Exception {

		GenerateFieldmapController moleGenerateFieldmapCtrl = Mockito.spy(this.generateFieldmapCtrl);
		Mockito.when(this.userFieldmap.getBlockName()).thenReturn(GenerateFieldmapControllerTest.BLOCK_NAME);

		Mockito.when(this.exportExcelService.exportFieldMapToExcel(Matchers.anyString(), Matchers.eq(this.userFieldmap))).thenReturn(
				Mockito.mock(FileOutputStream.class));

		String fileName = moleGenerateFieldmapCtrl.makeSafeFileName(GenerateFieldmapControllerTest.BLOCK_NAME);

		Mockito.doNothing().when(moleGenerateFieldmapCtrl).writeXlsToOutputStream(this.response, new File(fileName));
		moleGenerateFieldmapCtrl.exportExcel(this.fieldmapForm, this.model, this.response);

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

		Mockito.verify(this.response).setHeader(Matchers.eq("Content-disposition"), argument.capture());
		Mockito.verify(this.exportExcelService).exportFieldMapToExcel(fileName, this.userFieldmap);

		Assert.assertTrue("Content-disposition value has the filename argument",
				argument.getValue().contains("filename=\"" + fileName + "\""));
	}

	@Test
	public void makeSafeFileName() throws Exception {
		String out = this.generateFieldmapCtrl.makeSafeFileName(GenerateFieldmapControllerTest.BLOCK_NAME);

		Assert.assertTrue("Contains the BLOCK_NAME without spaces",
				out.contains(GenerateFieldmapControllerTest.BLOCK_NAME.replace(" ", "")));
		Assert.assertTrue("No spaces, ends with \"-<current_date>.xls\"",
				!out.contains(" ") && out.endsWith("-" + DateUtil.getCurrentDateAsStringValue() + ".xls"));

	}

}
