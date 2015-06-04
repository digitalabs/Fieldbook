
package com.efficio.fieldbook.web.common.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

	@Before
	public void setup() {
		this.DUT = Mockito.spy(this.exporter);
	}

	@Test
	public void testExport() throws Exception {
		Mockito.when(
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(CrossingTemplateExcelExporterTest.STUDY_ID,
						GermplasmListType.NURSERY)).thenReturn(this.initializeCrossesList());

		Mockito.doReturn(1).when(this.fieldbookMiddlewareService).getMeasurementDatasetId(Matchers.anyInt(), Matchers.anyString());
		Mockito.doReturn(Mockito.mock(VariableTypeList.class)).when(this.DUT).createPlotVariableTypeList();
		Mockito.doReturn(this.intializeExperiments()).when(this.studyDataManager)
				.getExperiments(Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt(), Matchers.any(VariableTypeList.class));

		Workbook wb = Mockito.mock(Workbook.class);
		Mockito.when(wb.getSheetAt(1)).thenReturn(Mockito.mock(Sheet.class));

		Map<String, CellStyle> style = Mockito.mock(Map.class);
		Mockito.doReturn(style).when(this.DUT).createStyles(wb);

		Mockito.doReturn(wb).when(this.DUT).retrieveTemplate();
		Mockito.doNothing()
				.when(this.DUT)
				.writeListDetailsSection(Matchers.any(Map.class), Matchers.any(Sheet.class), Matchers.anyInt(),
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

	@Test(expected = CrossingTemplateExportException.class)
	public void retrieveAndValidateIfHasGermplasmListExceptionHandling() throws Exception {
		Mockito.when(
				this.fieldbookMiddlewareService.getGermplasmListsByProjectId(CrossingTemplateExcelExporterTest.STUDY_ID,
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
