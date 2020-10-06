
package com.efficio.fieldbook.web.fieldmap.controller;

import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.efficio.fieldbook.service.api.ExportFieldmapService;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.utils.test.LabelPrintingDataUtil;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.fieldmap.form.FieldmapForm;

public class GenerateFieldmapControllerTest {

	private static final String XLS_EXT = ".xls";
	public static final String LOCATION_NAME = "location semi-colon;";
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

	@Mock
	protected ContextUtil contextUtil;

	@InjectMocks
	private GenerateFieldmapController generateFieldmapCtrlToTest;

	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
		Mockito.when(this.userFieldmap.getLocationName()).thenReturn(GenerateFieldmapControllerTest.LOCATION_NAME);
	}

	@Test
	public void testExportExcel() throws Exception {
		Mockito.when(this.exportExcelService.exportFieldMapToExcel(Matchers.anyString(), Matchers.eq(this.userFieldmap))).thenReturn(
				Mockito.mock(FileOutputStream.class));

		// We dont care which ever browser we use, so we return anything for user-agent
		Mockito.when(this.request.getHeader("User-Agent")).thenReturn("RANDOM_BROWSER");

		/* Call method to test, collect the output */
		final ResponseEntity<FileSystemResource> output = this.generateFieldmapCtrlToTest.exportExcel(this.request);

		// Verify that we performed the export operation
		final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.exportExcelService).exportFieldMapToExcel(filenameCaptor.capture(), Matchers.eq(this.userFieldmap));
		final String outputFilepath = filenameCaptor.getValue();
		final String outputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		final File outputFile = new File(outputFilepath);
		Assert.assertEquals(outputDirectoryFile, outputFile.getParentFile());
		Assert.assertTrue(outputFile.getName().startsWith(this.getExpectedFilenamePrefix()));
		Assert.assertTrue(outputFile.getName().endsWith(XLS_EXT));

		// Verify that the export is success
		Assert.assertEquals("Request to controller should be success", HttpStatus.OK , output.getStatusCode());
	}

	private String getExpectedFilenamePrefix() {
		return GenerateFieldmapControllerTest.LOCATION_NAME.replace(" ", "") + "-" + DateUtil.getCurrentDateAsStringValue();
	}

	@Test(expected=FieldbookException.class)
	public void testExportExcelAssumeFailure() throws Exception {

		Mockito.when(this.exportExcelService.exportFieldMapToExcel(Matchers.anyString(), Matchers.eq(this.userFieldmap))).thenThrow(
				new FieldbookException("Something went wrong with writing the excel file"));

		/*
		 * Call method to test, expect the controller to throw an exception
		 */
		this.generateFieldmapCtrlToTest.exportExcel(this.request);

	}

	@Test
	public void testMakeSafeFileName() throws Exception {
		final FileExportInfo exportInfo = this.generateFieldmapCtrlToTest.makeSafeFileName(GenerateFieldmapControllerTest.LOCATION_NAME);

		Assert.assertTrue("Contains the BLOCK_NAME without spaces",
				exportInfo.getDownloadFileName().contains(GenerateFieldmapControllerTest.LOCATION_NAME.replace(" ", "")));
		Assert.assertEquals("No spaces, ends with \"-<current_date>.xls\"", this.getExpectedFilenamePrefix() + XLS_EXT, exportInfo.getDownloadFileName());

		final String outputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		final File outputFile = new File(exportInfo.getFilePath());
		Assert.assertEquals(outputDirectoryFile, outputFile.getParentFile());
		Assert.assertTrue(outputFile.getName().startsWith(this.getExpectedFilenamePrefix()));
		Assert.assertTrue(outputFile.getName().endsWith(XLS_EXT));
	}

	@After
	public void cleanup() {
		this.deleteTestInstallationDirectory();
	}

	private void deleteTestInstallationDirectory() {
		// Delete test installation directory and its contents as part of cleanup
		final File testInstallationDirectory = new File(InstallationDirectoryUtil.WORKSPACE_DIR);
		this.installationDirectoryUtil.recursiveFileDelete(testInstallationDirectory);
	}

}
