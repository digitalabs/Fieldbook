package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.exception.CrossingTemplateExportException;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

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
		DUT = spy(exporter);
	}

	@Test
	public void testExport() throws Exception {
		when(fieldbookMiddlewareService.getGermplasmListsByProjectId(
				STUDY_ID,
				GermplasmListType.NURSERY)).thenReturn(initializeCrossesList());
		
		doReturn(1).when(fieldbookMiddlewareService).getMeasurementDatasetId(anyInt(), anyString());
		doReturn(mock(VariableTypeList.class)).when(DUT).createPlotVariableTypeList();
		doReturn(intializeExperiments()).when(studyDataManager).getExperiments(anyInt(), anyInt(), anyInt(), any(VariableTypeList.class));

		Workbook wb = mock(Workbook.class);
		when(wb.getSheetAt(1)).thenReturn(mock(Sheet.class));

		Map<String, CellStyle> style = mock(Map.class);
		doReturn(style).when(DUT).createStyles(wb);

		doReturn(wb).when(DUT).retrieveTemplate();
		doNothing().when(DUT).writeListDetailsSection(any(Map.class), any(Sheet.class), anyInt(),
				any(GermplasmList.class));

		when(fieldbookMiddlewareService.getListDataProject(anyInt()))
				.thenReturn(new ArrayList<ListDataProject>());

		ArgumentCaptor<String> studyNameCapture = ArgumentCaptor.forClass(String.class);

		File expectedExportFile = mock(File.class);

		doReturn(expectedExportFile).when(DUT).createExcelOutputFile(anyString(), eq(wb));

		File exportFile = DUT.export(STUDY_ID, STUDYNAME);

		verify(DUT, times(1)).createExcelOutputFile(studyNameCapture.capture(), eq(wb));

		assertEquals("Returns the expected export file", expectedExportFile, exportFile);

		assertEquals("uses same study name", STUDYNAME, studyNameCapture.getValue());
	}

	@Test(expected = CrossingTemplateExportException.class)
	public void testExportException() throws Exception {
		doThrow(new InvalidFormatException("forced exception")).when(DUT).retrieveTemplate();

		DUT.export(STUDY_ID, STUDYNAME);

	}

	@Test(expected = CrossingTemplateExportException.class)
	public void retrieveAndValidateIfHasGermplasmListExceptionHandling() throws Exception {
		when(fieldbookMiddlewareService.getGermplasmListsByProjectId(
				STUDY_ID,
				GermplasmListType.NURSERY)).thenReturn(Collections.EMPTY_LIST);

		DUT.retrieveAndValidateIfHasGermplasmList(STUDY_ID);
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
	
	private List<Experiment> intializeExperiments(){
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