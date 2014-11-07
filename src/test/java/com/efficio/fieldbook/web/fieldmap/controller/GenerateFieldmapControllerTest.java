package com.efficio.fieldbook.web.fieldmap.controller;

import com.efficio.fieldbook.service.api.ExportExcelService;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;
import com.efficio.fieldbook.web.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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

		GenerateFieldmapController moleGenerateFieldmapCtrl = spy(generateFieldmapCtrl);
		when(userFieldmap.getBlockName()).thenReturn(BLOCK_NAME);

		when(exportExcelService.exportFieldMapToExcel(anyString(), eq(userFieldmap)))
				.thenReturn(mock(
						FileOutputStream.class));

		String fileName = moleGenerateFieldmapCtrl.makeSafeFileName(BLOCK_NAME);

		doNothing().when(moleGenerateFieldmapCtrl).writeXlsToOutputStream(response,
				new File(fileName));
		moleGenerateFieldmapCtrl.exportExcel(fieldmapForm, model, response);

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

		verify(response).setHeader(eq("Content-disposition"), argument.capture());
		verify(exportExcelService).exportFieldMapToExcel(fileName, userFieldmap);

		assertTrue("Content-disposition value has the filename argument",
				argument.getValue().contains("filename=\"" + fileName + "\""));
	}

	@Test
	public void makeSafeFileName() throws Exception {
		String out = generateFieldmapCtrl.makeSafeFileName(BLOCK_NAME);

		assertTrue("Contains the BLOCK_NAME without spaces",
				out.contains(BLOCK_NAME.replace(" ", "")));
		assertTrue("No spaces, ends with \"-<current_date>.xls\"",
				!out.contains(" ") && out.endsWith("-" + DateUtil.getCurrentDate() + ".xls"));

	}

}