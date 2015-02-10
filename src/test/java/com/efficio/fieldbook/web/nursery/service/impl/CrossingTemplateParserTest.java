package com.efficio.fieldbook.web.nursery.service.impl;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.nursery.bean.*;
import com.efficio.fieldbook.web.util.DateUtil;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CrossingTemplateParserTest {

	public static final int STUDY_ID = 1;
	public static final int OBSERVATION_HEADER_SIZE = 8;
	public static final String RANDOM_STRING = "0";
	private static final String STUDY_NAME = "testStudyName";
	private static final Integer GENDER_ENTRY_NO = 1;
	private final static int GERMPLASMLIST_ID = 0;
	@Mock
	private StudyDataManager studyDataManager;
	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;
	@Mock
	private FileService fileService;
	@Mock
	private ImportedCrossesList importedCrossesList;
	@Mock
	private Map<String, Integer> observationColumnMap;
	@InjectMocks
	private CrossingTemplateParser parser;
	private CrossingTemplateParser parserUnderTest;

	@Before
	public void beforeTest() throws Exception {

		FieldUtils.writeDeclaredField(parser, "importedCrossesList", importedCrossesList, true);

		when(observationColumnMap.get(anyString())).thenReturn(0);

		FieldUtils.writeDeclaredField(parser, "observationColumnMap", observationColumnMap, true);

		parserUnderTest = spy(parser);
	}

	@Test
	public void testParseFile() throws Exception {
		doReturn(mock(Workbook.class)).when(parserUnderTest).storeImportGermplasmWorkbook(any(
				MultipartFile.class));

		doNothing().when(parserUnderTest).parseCrossingListDetails();
		doNothing().when(parserUnderTest).parseConditions();
		doNothing().when(parserUnderTest).parseFactors();
		doNothing().when(parserUnderTest).parseConstants();
		doNothing().when(parserUnderTest).parseVariate();
		doNothing().when(parserUnderTest).parseObservationSheet();

		parserUnderTest.parseFile(mock(MultipartFile.class));

		verify(parserUnderTest, times(1)).parseDescriptionSheet();
		verify(parserUnderTest, times(1)).parseObservationSheet();
	}

	@Test
	public void testStoreImportGermplasmWorkbook() throws Exception {
		MultipartFile file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("ORIGINAL_FILENAME.xls");
		when(file.getInputStream()).thenReturn(mock(InputStream.class));

		when(fileService.saveTemporaryFile(any(InputStream.class)))
				.thenReturn("SERVER_FILE_NAME.xls");

		when(fileService.retrieveWorkbook("SERVER_FILE_NAME.xls")).thenReturn(mock(Workbook.class));

		Workbook wb = parserUnderTest.storeImportGermplasmWorkbook(file);

		String originalFileName = String
				.valueOf(FieldUtils.readField(parserUnderTest, "originalFilename", true));

		assertEquals("should retrieve original filename", "ORIGINAL_FILENAME.xls",
				originalFileName);
		assertNotNull("returns a workbook object", wb);

	}

	@Test
	public void testParseDescriptionSheet() throws Exception {
		doNothing().when(parserUnderTest).parseCrossingListDetails();
		doNothing().when(parserUnderTest).parseConditions();
		doNothing().when(parserUnderTest).parseFactors();
		doNothing().when(parserUnderTest).parseConstants();
		doNothing().when(parserUnderTest).parseVariate();

		parserUnderTest.parseDescriptionSheet();

		verify(parserUnderTest, times(1)).parseCrossingListDetails();
		verify(parserUnderTest, times(1)).parseConditions();
		verify(parserUnderTest, times(1)).parseFactors();
		verify(parserUnderTest, times(1)).parseConstants();
		verify(parserUnderTest, times(1)).parseVariate();
	}

	@Test
	public void testParseObservationSheet() throws Exception {
		// SETUP!
		doReturn(false).when(parserUnderTest).isObservationsHeaderInvalid();
		doReturn(OBSERVATION_HEADER_SIZE).when(parserUnderTest).sizeOfObservationHeader();

		int rowSize = 20;    // assume we have 20 obs rows
		for (int i = 1; i <= rowSize; i++) {
			doReturn(false).when(parserUnderTest)
					.isRowEmpty(CrossingTemplateParser.OBSERVATION_SHEET_NO, i, 8);
		}

		doReturn(true).when(parserUnderTest)
				.isRowEmpty(CrossingTemplateParser.OBSERVATION_SHEET_NO, rowSize + 1, 8);

		// assume any parsed string is integer, so we can avoid any number format exceptions
		doReturn(RANDOM_STRING).when(parserUnderTest)
				.getCellStringValue(eq(CrossingTemplateParser.OBSERVATION_SHEET_NO), anyInt(),
						anyInt());

		// assume valid obs row, unit test for both valid / invalid scenarios will be on separate test case
		doReturn(true).when(parserUnderTest)
				.isObservationRowValid(anyString(), anyString(), anyString(), anyString(),
						anyString(), anyString());

		doReturn(mock(ListDataProject.class)).when(parserUnderTest)
				.getCrossingListProjectData(anyString(), anyInt());
		doNothing().when(importedCrossesList).addImportedCrosses(any(ImportedCrosses.class));

		parserUnderTest.parseObservationSheet();

		// ASSERTIONS!
		verify(importedCrossesList, times(rowSize)).addImportedCrosses(any(ImportedCrosses.class));
	}

	@Test
	public void testParseObservationSheetObservationHeaderInvalid() throws Exception {
		doReturn(true).when(parserUnderTest).isObservationsHeaderInvalid();
		doNothing().when(parserUnderTest).addParseErrorMsg(CrossingTemplateParser.FILE_INVALID);

		parserUnderTest.parseObservationSheet();

		verify(parserUnderTest, times(1)).addParseErrorMsg(CrossingTemplateParser.FILE_INVALID);
	}

	@Test
	public void testParseCrossingListDetails() throws Exception {
		doReturn("listName").when(parserUnderTest)
				.getCellStringValue(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 0, 1);
		doReturn("listTitle").when(parserUnderTest)
				.getCellStringValue(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 1, 1);
		doReturn("List Date").when(parserUnderTest)
				.getCellStringValue(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 2, 0);
		doReturn("20010101").when(parserUnderTest).getCellStringValue(
				CrossingTemplateParser.DESCRIPTION_SHEET_NO, 2, 1);
		doReturn(CrossingTemplateParser.TEMPLATE_LIST_TYPE).when(parserUnderTest)
				.getCellStringValue(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 3, 1);
		doNothing().when(parserUnderTest).addParseErrorMsg(CrossingTemplateParser.FILE_INVALID);

		parserUnderTest.parseCrossingListDetails();

		ImportedCrossesList importedCrossesList1 = (ImportedCrossesList) FieldUtils
				.readField(parserUnderTest, "importedCrossesList",
						true);

		// no validation
		verify(parserUnderTest, never()).addParseErrorMsg(CrossingTemplateParser.FILE_INVALID);

		assertEquals("list name is set", "listName", importedCrossesList1.getName());
		assertEquals("list title/description is set", "listTitle", importedCrossesList1.getTitle());
		assertEquals("date is set", DateUtil.parseDate("20010101"), importedCrossesList1.getDate());
		assertEquals("list type is set (and valid)", CrossingTemplateParser.TEMPLATE_LIST_TYPE,
				importedCrossesList1.getType());

	}

	@Test
	public void testParseConditions() throws Exception {
		doReturn(false).when(parserUnderTest)
				.isConditionHeadersInvalid(CrossingTemplateParser.CONDITION_ROW_NO);
		doReturn(false).when(parserUnderTest).isRowEmpty(
				CrossingTemplateParser.DESCRIPTION_SHEET_NO,
				CrossingTemplateParser.CONDITION_ROW_NO + 1,
				CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);

		doReturn(true).when(parserUnderTest).isRowEmpty(CrossingTemplateParser.DESCRIPTION_SHEET_NO,
				CrossingTemplateParser.CONDITION_ROW_NO + 2,
				CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);
		doReturn(false).when(parserUnderTest)
				.isRowEmpty(CrossingTemplateParser.DESCRIPTION_SHEET_NO,
						CrossingTemplateParser.CONDITION_ROW_NO + 3,
						CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);

		doReturn(RANDOM_STRING).when(parserUnderTest).getCellStringValue(
				eq(CrossingTemplateParser.DESCRIPTION_SHEET_NO), anyInt(), anyInt());
		doNothing().when(importedCrossesList).addImportedCondition(any(ImportedCondition.class));

		parserUnderTest.parseConditions();

		verify(importedCrossesList, times(1)).addImportedCondition(any(ImportedCondition.class));
	}

	@Test
	public void testParseFactors() throws Exception {
		doReturn(false).when(parserUnderTest).isFactorHeadersInvalid(0);
		doReturn(false).when(parserUnderTest).isRowEmpty(
				CrossingTemplateParser.DESCRIPTION_SHEET_NO, 1,
				CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);

		doReturn(true).when(parserUnderTest)
				.isRowEmpty(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 2,
						CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);
		doReturn(false).when(parserUnderTest)
				.isRowEmpty(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 3,
						CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);

		doReturn(RANDOM_STRING).when(parserUnderTest).getCellStringValue(
				eq(CrossingTemplateParser.DESCRIPTION_SHEET_NO), anyInt(), anyInt());
		doNothing().when(importedCrossesList).addImportedFactor(any(ImportedFactor.class));

		parserUnderTest.parseFactors();

		verify(importedCrossesList, times(1)).addImportedFactor(any(ImportedFactor.class));

	}

	@Test
	public void testParseConstants() throws Exception {
		doReturn(false).when(parserUnderTest).isConstantsHeaderInvalid(0);
		doReturn(false).when(parserUnderTest).isRowEmpty(
				CrossingTemplateParser.DESCRIPTION_SHEET_NO, 1,
				CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);

		doReturn(true).when(parserUnderTest)
				.isRowEmpty(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 2,
						CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);
		doReturn(false).when(parserUnderTest)
				.isRowEmpty(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 3,
						CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);

		doReturn(RANDOM_STRING).when(parserUnderTest).getCellStringValue(
				eq(CrossingTemplateParser.DESCRIPTION_SHEET_NO), anyInt(), anyInt());
		doNothing().when(importedCrossesList).addImportedConstant(any(ImportedConstant.class));

		parserUnderTest.parseConstants();

		verify(importedCrossesList, times(1)).addImportedConstant(any(ImportedConstant.class));
	}

	@Test
	public void testParseVariate() throws Exception {
		doReturn(false).when(parserUnderTest).isVariateHeaderInvalid(0);
		doReturn(false).when(parserUnderTest).isRowEmpty(
				CrossingTemplateParser.DESCRIPTION_SHEET_NO, 1,
				CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);

		doReturn(true).when(parserUnderTest)
				.isRowEmpty(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 2,
						CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);
		doReturn(false).when(parserUnderTest)
				.isRowEmpty(CrossingTemplateParser.DESCRIPTION_SHEET_NO, 3,
						CrossingTemplateParser.DESCRIPTION_SHEET_COL_SIZE);

		doReturn(RANDOM_STRING).when(parserUnderTest).getCellStringValue(
				eq(CrossingTemplateParser.DESCRIPTION_SHEET_NO), anyInt(), anyInt());
		doNothing().when(importedCrossesList).addImportedVariate(any(ImportedVariate.class));

		parserUnderTest.parseVariate();

		verify(importedCrossesList, times(1)).addImportedVariate(any(ImportedVariate.class));

	}

	@Test
	public void testGetCrossingListProjectData() throws Exception {
		List<GermplasmList> germplasmList = new ArrayList<>();
		germplasmList.add(mock(GermplasmList.class));

		when(studyDataManager.getStudyIdByName(anyString())).thenReturn(STUDY_ID);
		when(studyDataManager.getStudyType(STUDY_ID)).thenReturn(StudyType.N);
		when(fieldbookMiddlewareService
				.getGermplasmListsByProjectId(STUDY_ID, GermplasmListType.NURSERY))
				.thenReturn(germplasmList);
		when(fieldbookMiddlewareService.getListDataProjectByListIdAndEntryNo(anyInt(), anyInt()))
				.thenReturn(mock(ListDataProject.class));

		ListDataProject results = parserUnderTest
				.getCrossingListProjectData(STUDY_NAME, GENDER_ENTRY_NO);

		verify(fieldbookMiddlewareService, times(1))
				.getListDataProjectByListIdAndEntryNo(GERMPLASMLIST_ID, GENDER_ENTRY_NO);

		assertNotNull("should return a listdataproj obj", results);

	}

	@Test(expected = MiddlewareQueryException.class)
	public void testGetCrossingListProjectDataNoStudyFound() throws Exception {
		when(studyDataManager.getStudyIdByName(anyString())).thenReturn(null);

		parserUnderTest.getCrossingListProjectData(STUDY_NAME, GENDER_ENTRY_NO);
	}

	@Test(expected = MiddlewareQueryException.class)
	public void testGetCrossingListProjectDataNoGermplasmListFound() throws Exception {
		when(studyDataManager.getStudyIdByName(anyString())).thenReturn(STUDY_ID);
		when(studyDataManager.getStudyType(STUDY_ID)).thenReturn(StudyType.N);
		when(fieldbookMiddlewareService
				.getGermplasmListsByProjectId(STUDY_ID, GermplasmListType.NURSERY))
				.thenReturn(new ArrayList<GermplasmList>());

		parserUnderTest.getCrossingListProjectData(STUDY_NAME, GENDER_ENTRY_NO);
	}
}