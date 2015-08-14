
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;
import org.olap4j.impl.ArrayMap;

@RunWith(MockitoJUnitRunner.class)
public class CrossingTemplateExcelExporterTest {

	public static final String STUDYNAME = "studyname";
	private static final int STUDY_ID = 1;
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

	@InjectMocks
	private CrossingTemplateExcelExporter DUT;

	private org.apache.poi.ss.usermodel.Workbook workbook;

	@Before
	public void setup() throws IOException, InvalidFormatException {
		this.DUT = Mockito.spy(this.exporter);
		InputStream inp = this.getClass().getClassLoader().getResourceAsStream("testFilename.xls");

		this.workbook = WorkbookFactory.create(inp);
	}

	@Test
	public void testExport() throws Exception {
		Mockito.when(this.fieldbookMiddlewareService
				.getGermplasmListsByProjectId(CrossingTemplateExcelExporterTest.STUDY_ID, GermplasmListType.NURSERY)).thenReturn(
				this.initializeCrossesList());

		Mockito.doReturn(1).when(this.fieldbookMiddlewareService).getMeasurementDatasetId(Matchers.anyInt(), Matchers.anyString());
		Mockito.doReturn(this.intializeExperiments()).when(this.studyDataManager).getExperiments(Matchers.anyInt(), Matchers.anyInt(),
				Matchers.anyInt(), Matchers.any(VariableTypeList.class));

		Workbook wb = Mockito.mock(Workbook.class);
		Mockito.when(wb.getSheetAt(1)).thenReturn(Mockito.mock(Sheet.class));

		Map<String, CellStyle> style = Mockito.mock(Map.class);
		Mockito.doReturn(style).when(this.DUT).createStyles(wb);

		Mockito.doReturn(wb).when(this.DUT).retrieveTemplate();
		Mockito.doReturn(4).when(this.DUT).writeListDetailsSection(Matchers.any(Map.class), Matchers.any(Sheet.class), Matchers.anyInt(),
				Matchers.any(GermplasmList.class));

		Mockito.when(this.fieldbookMiddlewareService.getListDataProject(Matchers.anyInt())).thenReturn(new ArrayList<ListDataProject>());

		ArgumentCaptor<String> studyNameCapture = ArgumentCaptor.forClass(String.class);

		File expectedExportFile = Mockito.mock(File.class);

		Mockito.doReturn(expectedExportFile).when(this.DUT).createExcelOutputFile(Matchers.anyString(), Matchers.eq(wb));

		File exportFile = this.DUT.export(CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDYNAME);

		Mockito.verify(this.DUT, Mockito.times(1)).createExcelOutputFile(studyNameCapture.capture(), Matchers.eq(wb));

		Assert.assertEquals("Returns the expected export file", expectedExportFile, exportFile);

		Assert.assertEquals("uses same study name", CrossingTemplateExcelExporterTest.STUDYNAME, studyNameCapture.getValue());
	}

	@Test(expected = CrossingTemplateExportException.class)
	public void testExportException() throws Exception {
		Mockito.doThrow(new InvalidFormatException("forced exception")).when(this.DUT).retrieveTemplate();

		this.DUT.export(CrossingTemplateExcelExporterTest.STUDY_ID, CrossingTemplateExcelExporterTest.STUDYNAME);

	}

	@Test
	public void testWriteListDetailsSection() throws IOException {
		Map<String, CellStyle> styles = exporter.createStyles(workbook);
		Sheet sheet = workbook.getSheetAt(0);
		GermplasmList list = new GermplasmList();
		list.setDate(20150506l);
		list.setType("LST");
		exporter.writeListDetailsSection(styles, sheet, 1, list);

		Assert.assertEquals(sheet.getRow(0).getCell(0).getStringCellValue(), "LIST NAME");
		Assert.assertEquals(sheet.getRow(0).getCell(1).getStringCellValue(), "");
		Assert.assertEquals(sheet.getRow(0).getCell(3).getStringCellValue(), "Enter a list name here, or add it when saving in the BMS");

		Assert.assertEquals(sheet.getRow(1).getCell(0).getStringCellValue(), "LIST DESCRIPTION");
		Assert.assertEquals(sheet.getRow(1).getCell(1).getStringCellValue(), "");
		Assert.assertEquals(sheet.getRow(1).getCell(3).getStringCellValue(), "Enter a list description here, or add it when saving in the BMS");

		Assert.assertEquals(sheet.getRow(2).getCell(0).getStringCellValue(), "LIST TYPE");
		Assert.assertEquals(sheet.getRow(2).getCell(1).getStringCellValue(), "LST");
		Assert.assertEquals(sheet.getRow(2).getCell(3).getStringCellValue(), "See valid list types on Codes sheet for more options");

		Assert.assertEquals(sheet.getRow(3).getCell(0).getStringCellValue(), "LIST DATE");
		Assert.assertEquals(sheet.getRow(3).getCell(1).getStringCellValue(), "20150506");
		Assert.assertEquals(sheet.getRow(3).getCell(3).getStringCellValue(), "Accepted formats: YYYYMMDD or YYYYMM or YYYY or blank");
	}

	@Test(expected = CrossingTemplateExportException.class)
	public void retrieveAndValidateIfHasGermplasmListExceptionHandling() throws Exception {
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(CrossingTemplateExcelExporterTest.STUDY_ID,
				GermplasmListType.NURSERY)).thenReturn(Collections.EMPTY_LIST);

		this.DUT.retrieveAndValidateIfHasGermplasmList(CrossingTemplateExcelExporterTest.STUDY_ID);
	}

	private List<GermplasmList> initializeCrossesList() {
		List<GermplasmList> list = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			GermplasmList gplist = new GermplasmList();
			gplist.setId(i);
			list.add(gplist);
		}
		return list;
	}

	private List<Experiment> intializeExperiments() {
		List<Experiment> list = new ArrayList<>();
		Experiment experiment = new Experiment();

		VariableList factors = new VariableList();
		Variable plotVariable = new Variable();
		plotVariable.setValue("1");
		factors.add(plotVariable);

		experiment.setFactors(factors);

		return list;
	}
}
