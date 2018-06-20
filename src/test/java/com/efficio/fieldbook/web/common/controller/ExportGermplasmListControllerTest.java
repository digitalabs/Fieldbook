
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.utils.test.WorkbookDataUtil;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ExportGermplasmListForm;
import com.efficio.fieldbook.web.common.service.ExportGermplasmListService;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.commons.exceptions.GermplasmListExporterException;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.FileUtils;
import org.generationcp.commons.util.InstallationDirectoryUtil;
import org.generationcp.middleware.data.initializer.ProjectTestDataInitializer;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.workbench.ToolName;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ExportGermplasmListControllerTest {

	private static final int LIST_ID = 105;
	private static final long LIST_DATE = 20141112L;
	private static final String SAMPLE_NOTES = "Sample Notes";
	private static final String LST = "LST";
	private static final String SAMPLE_DESCRIPTION = "Sample description";
	private static final String SAMPLE_LIST = "Sample List";
	private static final String LIST_NAME = "LIST NAME";
	private static final int CURRENT_USER_ID = 1;
	private static final int LIST_STATUS = 1;
	// Columns
	private static final String ENTRY_NO = "entryNo";
	private static final String GID = "gid";
	private static final String ENTRY_CODE = "entryCode";
	private static final String DESIGNATION = "desig";
	private static final String PARENTAGE = "parentage";
	private static final String SEED_SOURCE = "seedSource";
	private static final String CHECK = "check";
	private static final String ENTRY_NUMBER_STORAGE = "entryNoStorage";

	private static final String NUMERIC_VARIABLE = "NUMERIC VARIABLE";
	private static final String TEST_METHOD = "TEST METHOD";
	private static final String TEST_SCALE = "TEST SCALE";
	private static final String TEST_PROPERTY = "TEST PROPERTY";
	private static final String TEST_DESCRIPTION = "TEST DESCRIPTION";

	private static final int EXCEL_TYPE = 1;
	private static final int CSV_TYPE = 2;

	@Mock
	private HttpServletResponse response;

	@Mock
	private HttpServletRequest req;

	@Mock
	private UserSelection userSelection;

	@Mock
	private OntologyService ontologyService;

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private ExportGermplasmListService exportGermplasmListService;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private ExportGermplasmListController exportGermplasmListController;

	private final InstallationDirectoryUtil installationDirectoryUtil = new InstallationDirectoryUtil();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_NO.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_NO.getId(), ExportGermplasmListControllerTest.ENTRY_NO));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.DESIG.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.DESIG.getId(), ExportGermplasmListControllerTest.DESIGNATION));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.GID.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.GID.getId(), ExportGermplasmListControllerTest.GID));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.CROSS.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.CROSS.getId(), ExportGermplasmListControllerTest.PARENTAGE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.SEED_SOURCE.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.SEED_SOURCE.getId(), ExportGermplasmListControllerTest.SEED_SOURCE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.ENTRY_CODE.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_CODE.getId(), ExportGermplasmListControllerTest.ENTRY_CODE));
		Mockito.when(
				this.ontologyService.getStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.ENTRY_NUMBER_STORAGE.getId(),
						ExportGermplasmListControllerTest.ENTRY_NUMBER_STORAGE));
		Mockito.when(this.ontologyService.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID()))
				.thenReturn(this.createStandardVariable(TermId.CHECK.getId(), ExportGermplasmListControllerTest.CHECK));
		Mockito.doReturn(this.getPlotLevelList()).when(this.userSelection).getPlotsLevelList();

		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(Matchers.anyInt())).thenReturn(this.getGermplasmList());

		Mockito.when(this.userSelection.getImportedGermplasmMainInfo()).thenReturn(Mockito.mock(ImportedGermplasmMainInfo.class));
		Mockito.when(this.userSelection.getImportedGermplasmMainInfo().getListId()).thenReturn(LIST_ID);
		Mockito.when(this.userSelection.getImportedGermplasmMainInfo().getListName())
				.thenReturn(ExportGermplasmListControllerTest.LIST_NAME);

		Mockito.doReturn(ProjectTestDataInitializer.createProject()).when(this.contextUtil).getProjectInContext();
	}

	@Test
	public void testExportGermplasmListExcelForStudy() throws JsonParseException, JsonMappingException, IOException {

		final ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");

		try {
			final String output =
				this.exportGermplasmListController.exportGermplasmList(form, ExportGermplasmListControllerTest.EXCEL_TYPE, this.response);

			//  Verify temporary file is created in proper directory and response object is properly set
			final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
			//final ArgumentCaptor<Boolean> isNurseryCaptor = ArgumentCaptor.forClass(Boolean.class);
			final ArgumentCaptor<Integer> listIdCaptor = ArgumentCaptor.forClass(Integer.class);
			Mockito.verify(this.exportGermplasmListService, Mockito.times(1)).exportGermplasmListXLS(filenameCaptor.capture(),
					listIdCaptor.capture(), Matchers.any(Map.class));
			//Assert.assertFalse(isNurseryCaptor.getValue());
			Assert.assertEquals(LIST_ID, listIdCaptor.getValue().intValue());

			final File outputFile = this.getOutputFilePath();
			Assert.assertNotNull(outputFile);
			Assert.assertEquals(outputFile.getAbsolutePath(), filenameCaptor.getValue());
			final Map<String, Object> result = new ObjectMapper().readValue(output, Map.class);
			Assert.assertEquals(SAMPLE_LIST + ".xls", result.get(ExportGermplasmListController.FILENAME));
			Assert.assertEquals(outputFile.getAbsolutePath(), result.get(ExportGermplasmListController.OUTPUT_FILENAME));
			Mockito.verify(this.response).setContentType(FileUtils.MIME_MS_EXCEL);

		} catch (final GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	private File getOutputFilePath() {
		final String outputDirectoryPath = this.installationDirectoryUtil.getOutputDirectoryForProjectAndTool(this.contextUtil.getProjectInContext(), ToolName.FIELDBOOK_WEB);
		final File outputDirectoryFile = new File(outputDirectoryPath);
		Assert.assertTrue(outputDirectoryFile.exists());
		File outputFile = null;
		for (final File file : outputDirectoryFile.listFiles()) {
			if (file.getName().startsWith(SAMPLE_LIST)) {
				outputFile = file;
			}
		}
		return outputFile;
	}

	@Test
	public void testExportGermplasmListCSVForStudy() throws JsonParseException, JsonMappingException, IOException {

		final ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");

		try {
			final String output =
				this.exportGermplasmListController.exportGermplasmList(form, ExportGermplasmListControllerTest.CSV_TYPE, this.response);

			//  Verify temporary file is created in proper directory and response object is properly set
			final ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
			Mockito.verify(this.exportGermplasmListService, Mockito.times(1)).exportGermplasmListCSV(filenameCaptor.capture(),
					Matchers.any(Map.class));
			final File outputFile = this.getOutputFilePath();
			Assert.assertNotNull(outputFile);
			Assert.assertEquals(outputFile.getAbsolutePath(), filenameCaptor.getValue());
			final Map<String, Object> result = new ObjectMapper().readValue(output, Map.class);
			Assert.assertEquals(SAMPLE_LIST + ".csv", result.get(ExportGermplasmListController.FILENAME));
			Assert.assertEquals(outputFile.getAbsolutePath(), result.get(ExportGermplasmListController.OUTPUT_FILENAME));
			Mockito.verify(this.response).setContentType(FileUtils.MIME_CSV);
		} catch (final GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void testExportGermplasmListCSVForStudy_NoSelectedGermplasmList() {

		try {
			Mockito.doReturn(null).when(this.fieldbookMiddlewareService).getGermplasmListById(Matchers.anyInt());
		} catch (final MiddlewareQueryException e1) {
			Assert.fail();
		}
		Mockito.when(this.userSelection.getImportedGermplasmMainInfo()).thenReturn(null);

		final ExportGermplasmListForm form = new ExportGermplasmListForm();
		form.setGermplasmListVisibleColumns("0");

		try {
			this.exportGermplasmListController.exportGermplasmList(form, ExportGermplasmListControllerTest.CSV_TYPE, this.response);
			Mockito.verify(this.exportGermplasmListService, Mockito.times(0)).exportGermplasmListCSV(Matchers.anyString(),
					Matchers.any(Map.class));
		} catch (final GermplasmListExporterException e) {
			Assert.fail();
		}

	}

	@Test
	public void test_getVisibleColumnsMapStudy() {

		final String[] termIds = new String[] {String.valueOf(TermId.CHECK.getId())};
		final Map<String, Boolean> result = this.exportGermplasmListController.getVisibleColumnsMap(termIds);

		Assert.assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));

		Assert.assertTrue(result.get(String.valueOf(TermId.CHECK.getId())));

		Assert.assertFalse(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		Assert.assertFalse(result.get(String.valueOf(TermId.CROSS.getId())));
		Assert.assertFalse(result.get(String.valueOf(TermId.SEED_SOURCE.getId())));
		Assert.assertFalse(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));

	}

	@Test
	public void test_getVisibleColumnsMapNurseryStudy() {

		final String[] termIds = new String[] {"0"};
		Mockito.when(this.userSelection.getPlotsLevelList()).thenReturn(WorkbookDataUtil.getPlotLevelList());

		final Map<String, Boolean> result = this.exportGermplasmListController.getVisibleColumnsMap(termIds);

		Assert.assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.CROSS.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.SEED_SOURCE.getId())));

		Assert.assertNull(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));
		Assert.assertNull(result.get(String.valueOf(TermId.CHECK.getId())));
	}

	@Test
	public void test_getVisibleColumnsMapWithNoVisibleColumns() {

		final String[] termIds = new String[] {"0"};

		final Map<String, Boolean> result = this.exportGermplasmListController.getVisibleColumnsMap(termIds);

		Assert.assertTrue(result.get(String.valueOf(TermId.GID.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.DESIG.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NO.getId())));

		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_CODE.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.CROSS.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.SEED_SOURCE.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId())));
		Assert.assertTrue(result.get(String.valueOf(TermId.CHECK.getId())));
	}

	private List<SettingDetail> getPlotLevelList() {
		final List<SettingDetail> plotLevelList = new ArrayList<>();

		for (final Entry<String, Boolean> entry : this.getVisibleColumnMap().entrySet()) {
			plotLevelList.add(this.generateSettingDetail(Integer.valueOf(entry.getKey())));
		}

		return plotLevelList;

	}

	private Map<String, Boolean> getVisibleColumnMap() {
		final Map<String, Boolean> visibleColumnMap = new HashMap<String, Boolean>();

		visibleColumnMap.put(String.valueOf(TermId.GID.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.CROSS.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NO.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.DESIG.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.SEED_SOURCE.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_CODE.getId()), true);
		visibleColumnMap.put(String.valueOf(TermId.ENTRY_NUMBER_STORAGE.getId()), false);
		visibleColumnMap.put(String.valueOf(TermId.CHECK.getId()), false);

		return visibleColumnMap;

	}

	private SettingDetail generateSettingDetail(final Integer termId) {
		final SettingDetail settingDetail = new SettingDetail();
		settingDetail.setHidden(false);
		final SettingVariable var = new SettingVariable();
		var.setCvTermId(termId);
		settingDetail.setVariable(var);

		final StandardVariable stdVar;
		try {
			stdVar = this.ontologyService.getStandardVariable(termId, this.contextUtil.getCurrentProgramUUID());

			settingDetail.getVariable().setName(stdVar.getName());
			settingDetail.getVariable().setDescription(stdVar.getDescription());
			settingDetail.getVariable().setProperty(stdVar.getProperty().getName());
			settingDetail.getVariable().setScale(stdVar.getScale().getName());
			settingDetail.getVariable().setMethod(stdVar.getMethod().getName());
			settingDetail.getVariable().setDataType(stdVar.getDataType().getName());

		} catch (final MiddlewareException e) {
			// do nothing
		}

		return settingDetail;
	}

	private StandardVariable createStandardVariable(final int id, final String name) {
		final StandardVariable stdVar = new StandardVariable();
		stdVar.setId(id);
		stdVar.setName(name);
		stdVar.setDescription(ExportGermplasmListControllerTest.TEST_DESCRIPTION);

		final Term prop = new Term();
		prop.setName(ExportGermplasmListControllerTest.TEST_PROPERTY);
		stdVar.setProperty(prop);

		final Term scale = new Term();
		scale.setName(ExportGermplasmListControllerTest.TEST_SCALE);
		stdVar.setScale(scale);

		final Term method = new Term();
		method.setName(ExportGermplasmListControllerTest.TEST_METHOD);
		stdVar.setMethod(method);

		final Term dataType = new Term();
		dataType.setName(ExportGermplasmListControllerTest.NUMERIC_VARIABLE);
		stdVar.setDataType(dataType);

		return stdVar;
	}

	private GermplasmList getGermplasmList() {
		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setName(ExportGermplasmListControllerTest.SAMPLE_LIST);
		germplasmList.setUserId(ExportGermplasmListControllerTest.CURRENT_USER_ID);
		germplasmList.setDescription(ExportGermplasmListControllerTest.SAMPLE_DESCRIPTION);
		germplasmList.setType(ExportGermplasmListControllerTest.LST);
		germplasmList.setDate(ExportGermplasmListControllerTest.LIST_DATE);
		germplasmList.setNotes(ExportGermplasmListControllerTest.SAMPLE_NOTES);
		germplasmList.setStatus(ExportGermplasmListControllerTest.LIST_STATUS);

		return germplasmList;
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
