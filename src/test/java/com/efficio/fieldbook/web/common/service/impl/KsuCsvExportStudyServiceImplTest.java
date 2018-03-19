package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.commons.pojo.FileExportInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.util.AppConstants;

public class KsuCsvExportStudyServiceImplTest {
	private static final String CSV_EXT = ".csv";

	private static final String ZIP_EXT = ".zip";
	private static String STUDY_NAME = "Test Study";
	private static String ZIP_FILEPATH = "./someDirectory/output/TestFileName.zip";
	
	@Mock
	protected ContextUtil contextUtil;
	
	@Mock
	private OntologyService ontologyService;
	
	private KsuCsvExportStudyServiceImpl ksuCsvExportStudyService;
	private InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();
	
	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);

		// Need to spy so that actual writing of CSV / ZIP file won't be performed
		this.ksuCsvExportStudyService = Mockito.spy(new KsuCsvExportStudyServiceImpl());
		this.ksuCsvExportStudyService.setOntologyService(this.ontologyService);
		this.ksuCsvExportStudyService.setContextUtil(this.contextUtil);

		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
		Mockito.doNothing().when(this.ksuCsvExportStudyService).writeOutputFile(Matchers.anyString(), Matchers.anyList(), Matchers.anyString());
		Mockito.doReturn(ZIP_FILEPATH).when(this.ksuCsvExportStudyService).createZipFile(Matchers.anyString(), Matchers.anyListOf(String.class));
		Mockito.doNothing().when(this.ksuCsvExportStudyService).writeTraitsFile(Matchers.any(Workbook.class), Matchers.anyString());
	}
	
	@Test
	public void testDoExport() throws IOException {
		final int numberOfInstances = 3;
		final Workbook workbook = WorkbookDataUtil.getTestWorkbookForTrial(20, numberOfInstances);
		workbook.setExportArrangedObservations(workbook.getObservations());
		final List<Integer> instances = WorkbookDataUtil.getTrialInstances(workbook);

		final FileExportInfo exportInfo =
				this.ksuCsvExportStudyService.export(workbook, KsuCsvExportStudyServiceImplTest.STUDY_NAME, instances);

		final List<File> outputDirectories = this.getTempOutputDirectoriesGenerated();
		// Expecting 1 CSV file per trial instance plus Trait file
		Assert.assertEquals(numberOfInstances + 1, outputDirectories.size());
		final ArgumentCaptor<String> studyFilenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.ksuCsvExportStudyService, Mockito.times(numberOfInstances)).writeOutputFile(Matchers.anyString(), Matchers.anyList(), studyFilenameCaptor.capture());
		final ArgumentCaptor<String> traitFilenameCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.ksuCsvExportStudyService).writeTraitsFile(Matchers.any(Workbook.class), traitFilenameCaptor.capture());
		final List<String> studyFilePaths = studyFilenameCaptor.getAllValues();
		Assert.assertEquals(numberOfInstances, studyFilePaths.size());
		final String traitFilePath = traitFilenameCaptor.getValue();
		Assert.assertTrue(traitFilePath.endsWith(AppConstants.EXPORT_KSU_TRAITS_SUFFIX.getString()));
		Assert.assertTrue(outputDirectories.contains(new File(traitFilePath).getParentFile()));
		
		final List<String> allFilePaths = new ArrayList<>(studyFilePaths);
		allFilePaths.add(traitFilePath);
		Mockito.verify(this.ksuCsvExportStudyService).createZipFile(KsuCsvExportStudyServiceImplTest.STUDY_NAME, allFilePaths);
		Assert.assertEquals(numberOfInstances + 1, allFilePaths.size());
		for (final String path : studyFilePaths) {
			final File outputFile = new File(path);
			Assert.assertTrue(outputFile.getAbsolutePath().endsWith(CSV_EXT));
			Assert.assertTrue(outputDirectories.contains(outputFile.getParentFile()));
		}
		Assert.assertEquals(KsuCsvExportStudyServiceImplTest.STUDY_NAME + ZIP_EXT, exportInfo.getDownloadFileName());
		Assert.assertEquals(ZIP_FILEPATH, exportInfo.getFilePath());
	}
	
	@Test
	public void testGetFileExtension() {
		Assert.assertEquals(AppConstants.EXPORT_CSV_SUFFIX.getString(), this.ksuCsvExportStudyService.getFileExtension());
	}
	
	private List<File> getTempOutputDirectoriesGenerated() {
		final String genericOutputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final String toolDirectory = genericOutputDirectoryPath.substring(0, genericOutputDirectoryPath.indexOf(InstallationDirectoryUtil.OUTPUT));
		File toolDirectoryFile = new File(toolDirectory);
		Assert.assertTrue(toolDirectoryFile.exists());
		List<File> outputDirectoryFiles = new ArrayList<>();
		for (final File file : toolDirectoryFile.listFiles()) {
			if (file.getName().startsWith("output") && file.getName() != InstallationDirectoryUtil.OUTPUT && file.isDirectory()) {
				outputDirectoryFiles.add(file);
			}
		}
		return outputDirectoryFiles;
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
