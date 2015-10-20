
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.ExcelCellStyleBuilder;
import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
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

	public static final String STUDY_NAME = "studyname";
	private static final int STUDY_ID = 1;
	public static final String TEST_FILENAME = "testFilename.xls";
	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private FileService fileService;

	@Mock
	private File templateFile;

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
		Mockito.when(this.fieldbookMiddlewareService
				.getGermplasmListsByProjectId(CrossingTemplateExcelExporterTest.STUDY_ID, GermplasmListType.NURSERY)).thenReturn(
				this.initializeCrossesList());

		Mockito.doReturn(1).when(this.fieldbookMiddlewareService).getMeasurementDatasetId(Matchers.anyInt(), Matchers.anyString());
		Mockito.doReturn(this.intializeExperiments()).when(this.studyDataManager)
				.getExperiments(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.any(VariableTypeList.class));
		Mockito.doReturn(this.workbook).when(this.fileService).retrieveWorkbookTemplate(TEST_FILENAME);
		Mockito.when(this.fieldbookMiddlewareService.getListDataProject(Matchers.anyInt())).thenReturn(new ArrayList<ListDataProject>());

		//to test
		final File exportFile = this.exporter.export(CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDY_NAME);
		Assert.assertEquals("uses same study name", "CrossingTemplate-" + CrossingTemplateExcelExporterTest.STUDY_NAME + ".xls",
				exportFile.getName());
	}

	@Test(expected = CrossingTemplateExportException.class)
	public void testExportException() throws Exception {
		Mockito.doThrow(new InvalidFormatException("forced exception")).when(this.fileService).retrieveWorkbookTemplate(TEST_FILENAME);
		this.exporter.export(CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDY_NAME);
	}

	@Test
	public void testWriteListDetailsSection() throws IOException {
		final Sheet sheet = this.workbook.getSheetAt(0);
		final GermplasmList list = new GermplasmList();
		list.setDate(20150506l);
		list.setType("LST");
		this.exporter.writeListDetailsSection(sheet, 1, list, new ExcelCellStyleBuilder((HSSFWorkbook) this.workbook));

		Assert.assertEquals(sheet.getRow(0).getCell(0).getStringCellValue(), "LIST NAME");
		Assert.assertEquals(sheet.getRow(0).getCell(1).getStringCellValue(), "");
		Assert.assertEquals(sheet.getRow(0).getCell(3).getStringCellValue(), "Enter a list name here, or add it when saving in the BMS");

		Assert.assertEquals(sheet.getRow(1).getCell(0).getStringCellValue(), "LIST DESCRIPTION");
		Assert.assertEquals(sheet.getRow(1).getCell(1).getStringCellValue(), "");
		Assert.assertEquals(sheet.getRow(1).getCell(3).getStringCellValue(),
				"Enter a list description here, or add it when saving in the BMS");

		Assert.assertEquals(sheet.getRow(2).getCell(0).getStringCellValue(), "LIST TYPE");
		Assert.assertEquals(sheet.getRow(2).getCell(1).getStringCellValue(), "LST");
		Assert.assertEquals(sheet.getRow(2).getCell(3).getStringCellValue(), "See valid list types on Codes sheet for more options");

		Assert.assertEquals(sheet.getRow(3).getCell(0).getStringCellValue(), "LIST DATE");
		Assert.assertEquals(sheet.getRow(3).getCell(1).getStringCellValue(), "20150506");
		Assert.assertEquals(sheet.getRow(3).getCell(3).getStringCellValue(), "Accepted formats: YYYYMMDD or YYYYMM or YYYY or blank");
	}

	@Test(expected = CrossingTemplateExportException.class)
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
			list.add(gplist);
		}
		return list;
	}

	private List<Experiment> intializeExperiments() {
		final List<Experiment> list = new ArrayList<>();
		final Experiment experiment = new Experiment();

		final VariableList factors = new VariableList();
		final Variable plotVariable = new Variable();
		plotVariable.setValue("1");
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(TermId.PLOT_NO.getId());
		final DMSVariableType variableType = new DMSVariableType("test", "test", standardVariable, 0);
		plotVariable.setVariableType(variableType);
		plotVariable.setValue("2");
		factors.add(plotVariable);

		experiment.setFactors(factors);
		list.add(experiment);

		return list;
	}
}
