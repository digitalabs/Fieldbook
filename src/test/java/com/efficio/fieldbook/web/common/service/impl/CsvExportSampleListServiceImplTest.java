package com.efficio.fieldbook.web.common.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.util.SampleListUtilTest;

public class CsvExportSampleListServiceImplTest {
	
	private static String FILENAME = "Trial 33-Sample List Name";

	@Mock
	private ContextUtil contextUtil;

	private static final String CSV_EXT = ".csv";

	private CsvExportSampleListServiceImpl csvExportSampleListService;


	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();
	
	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);

		// Need to spy so writing of actual CSV file won't be performed
		this.csvExportSampleListService = Mockito.spy(new CsvExportSampleListServiceImpl());
		this.csvExportSampleListService.setContextUtil(this.contextUtil);
		Mockito.doReturn(Mockito.mock(File.class)).when(this.csvExportSampleListService)
			.generateCSVFile(Matchers.any(List.class), Matchers.any(List.class), Matchers.anyString());
		
		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
	}

	@Test
	public void testCSVSampleListExport() throws IOException {
		final List<SampleDetailsDTO> sampleDetailsDTOs = SampleListUtilTest.initSampleDetailsDTOs();
		final List<String> visibleColumns = SampleListUtilTest.getVisibleColumns();
		final FileExportInfo exportInfo =
				this.csvExportSampleListService.export(sampleDetailsDTOs, CsvExportSampleListServiceImplTest.FILENAME, visibleColumns);
		assertThat(CsvExportSampleListServiceImplTest.FILENAME + CSV_EXT, equalTo(exportInfo.getDownloadFileName()));
		final File outputFilePath = this.getOutputFilePath();
		assertThat(outputFilePath.getAbsolutePath(), equalTo(exportInfo.getFilePath()));
	}
	
	private File getOutputFilePath() {
		final String outputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		File outputFile = null;
		for (final File file : outputDirectoryFile.listFiles()) {
			if (file.getName().startsWith(CsvExportSampleListServiceImplTest.FILENAME)) {
				outputFile = file;
			}
		}
		return outputFile;
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
