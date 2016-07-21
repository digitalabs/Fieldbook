
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.ExcelCellStyleBuilder;
import org.generationcp.commons.service.FileService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.User;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;

@RunWith(MockitoJUnitRunner.class)
public class CrossingTemplateExcelExporterTest {

	private static final String STUDY_NAME = "studyname";
	private static final int STUDY_ID = 1;
	private static final String TEST_FILENAME = "testFilename.xls";
	private static final int CURRENT_USER_ID = 1;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private FileService fileService;

	@Mock
	private File templateFile;

	@Mock
	protected ContextUtil contextUtil;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	private CrossingTemplateExcelExporter exporter;

	private org.apache.poi.ss.usermodel.Workbook workbook;

	@Before
	public void setup() throws IOException, InvalidFormatException {
		MockitoAnnotations.initMocks(this);

		this.exporter.setTemplateFile(TEST_FILENAME);
		this.workbook = WorkbookFactory.create(this.getClass().getClassLoader().getResourceAsStream(TEST_FILENAME));
	}

	@After
	public void tearDown() {
		final File file = new File("CrossingTemplate-" + CrossingTemplateExcelExporterTest.STUDY_NAME + ".xls");
		file.deleteOnExit();
	}

	@Test
	public void testExport() throws Exception {
		Mockito.when(
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(CrossingTemplateExcelExporterTest.STUDY_ID,
						GermplasmListType.NURSERY)).thenReturn(this.initializeCrossesList());

		Mockito.doReturn(1).when(this.fieldbookMiddlewareService).getMeasurementDatasetId(Matchers.anyInt(), Matchers.anyString());
		Mockito.doReturn(this.intializeExperiments()).when(this.studyDataManager)
				.getExperiments(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.any(VariableTypeList.class));
		Mockito.doReturn(this.workbook).when(this.fileService).retrieveWorkbookTemplate(TEST_FILENAME);
		Mockito.when(this.fieldbookMiddlewareService.getListDataProject(Matchers.anyInt())).thenReturn(new ArrayList<ListDataProject>());
		Project projectMock = Mockito.mock(Project.class);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(projectMock);
		Mockito.when(this.workbenchDataManager.getUsersByProjectId(Matchers.anyLong())).thenReturn(new ArrayList<User>());

		// to test
		final File exportFile =
				this.exporter.export(CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDY_NAME,
						CrossingTemplateExcelExporterTest.CURRENT_USER_ID);
		Assert.assertEquals("uses same study name", "CrossingTemplate-" + CrossingTemplateExcelExporterTest.STUDY_NAME + ".xls",
				exportFile.getName());
	}

	@Test(expected = CrossingTemplateExportException.class)
	public void testExportException() throws Exception {
		Mockito.doThrow(new InvalidFormatException("forced exception")).when(this.fileService).retrieveWorkbookTemplate(TEST_FILENAME);
		this.exporter.export(CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDY_NAME,
				CrossingTemplateExcelExporterTest.CURRENT_USER_ID);
	}

	@Test
	public void testWriteListDetailsSection() throws IOException {
		final Sheet sheet = this.workbook.getSheetAt(0);
		final GermplasmList list = new GermplasmList();
		list.setDate(20150506L);
		list.setType("LST");
		this.exporter.writeListDetailsSection(sheet, 1, list, new ExcelCellStyleBuilder((HSSFWorkbook) this.workbook),
				CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDY_NAME);

		Assert.assertEquals(sheet.getRow(0).getCell(0).getStringCellValue(), "LIST NAME");
		Assert.assertEquals(sheet.getRow(0).getCell(1).getStringCellValue(), "");
		Assert.assertEquals(sheet.getRow(0).getCell(3).getStringCellValue(), "Enter a list name here, or add it when saving in the BMS");

		Assert.assertEquals(sheet.getRow(1).getCell(0).getStringCellValue(), "LIST DESCRIPTION");
		Assert.assertEquals(sheet.getRow(1).getCell(1).getStringCellValue(), "");
		Assert.assertEquals(sheet.getRow(1).getCell(3).getStringCellValue(),
				"Enter a list description here, or add it when saving in the BMS");

		Assert.assertEquals(sheet.getRow(2).getCell(0).getStringCellValue(), "LIST DATE");
		final Date todaysDate = new Date();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		final String todaysDateText = dateFormat.format(todaysDate);
		Assert.assertTrue(sheet.getRow(2).getCell(1).getNumericCellValue() == Long.parseLong(todaysDateText));
		Assert.assertEquals(sheet.getRow(2).getCell(3).getStringCellValue(), "Accepted formats: YYYYMMDD or YYYYMM or YYYY or blank");
	}

	@Test
	public void testUpdateCodesSection() throws IOException {

		Mockito.when(this.fieldbookMiddlewareService.getListDataProject(Matchers.anyInt())).thenReturn(new ArrayList<ListDataProject>());
		final Project projectMock = Mockito.mock(Project.class);
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(projectMock);

		// User

		final int userId = 8;

		final Person mockPerson = Mockito.mock(Person.class);
		Mockito.when(this.workbenchDataManager.getPersonById(Matchers.anyInt())).thenReturn(mockPerson);

		final ArrayList<User> users = new ArrayList<User>();
		final User mockUser = Mockito.mock(User.class);
		Mockito.when(mockUser.getUserid()).thenReturn(userId);
		users.add(mockUser);
		Mockito.when(this.workbenchDataManager.getUsersByProjectId(Matchers.anyLong())).thenReturn(users);

		// Methods

		final String mCode = "6";

		final List<Method> methods = new ArrayList<>();
		final Method mockMethod = Mockito.mock(Method.class);
		methods.add(mockMethod);
		Mockito.when(mockMethod.getMcode()).thenReturn(mCode);
		Mockito.when(this.germplasmDataManager.getMethodsByType(Matchers.anyString(), Matchers.anyString())).thenReturn(methods);

		final Sheet sheet = this.workbook.getSheetAt(2);
		this.exporter.updateCodesSection(sheet);

		Assert.assertEquals(String.valueOf(userId), sheet.getRow(1).getCell(2).getStringCellValue());
		Assert.assertEquals(mCode, sheet.getRow(2).getCell(2).getStringCellValue());
	}

	@Test(expected = CrossingTemplateExportException.class)
	@SuppressWarnings("unchecked")
	public void retrieveAndValidateIfHasGermplasmListExceptionHandling() throws Exception {
		Mockito.when(
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(CrossingTemplateExcelExporterTest.STUDY_ID,
						GermplasmListType.NURSERY)).thenReturn(Collections.EMPTY_LIST);

		this.exporter.retrieveAndValidateIfHasGermplasmList(CrossingTemplateExcelExporterTest.STUDY_ID);
	}

	private List<GermplasmList> initializeCrossesList() {
		final List<GermplasmList> list = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			final GermplasmList gplist = new GermplasmList();
			gplist.setId(i);
			gplist.setDate(20150506L);
			list.add(gplist);
		}

		return list;
	}

	private List<Experiment> intializeExperiments() {
		final List<Experiment> list = new ArrayList<>();
		final Experiment experiment = new Experiment();

		final VariableList factors = new VariableList();
		factors.add(createTestVariable(TermId.PLOT_NO.getId(), "1"));
		factors.add(createTestVariable(TermId.GID.getId(), "1"));
        factors.add(createTestVariable(TermId.DESIG.getId(), "ABC"));
        factors.add(createTestVariable(TermId.CROSS.getId(), "abc/def"));

		experiment.setFactors(factors);
		list.add(experiment);

		return list;
	}

    private Variable createTestVariable(final Integer termId, final String value) {
        final Variable testVariable = new Variable();
        testVariable.setValue(value);
        final StandardVariable standardVariable = new StandardVariable();
        standardVariable.setId(termId);
        final DMSVariableType variableType = new DMSVariableType("test", "test", standardVariable, 0);
        testVariable.setVariableType(variableType);

        return testVariable;
    }
}
