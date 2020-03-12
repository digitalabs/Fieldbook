package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.service.CrossingService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.api.study.StudyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class CrossingTemplateParserTest {

	private static final String NO_PLOT_DATA_ERROR = "NO PLOT DATA ERROR";
	private static final Integer STUDY_ID = new Random().nextInt();
	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(20);
	private static final String STUDY_NAME = RandomStringUtils.randomAlphabetic(20);
	private static final String FEMALE_STUDY_NAME = RandomStringUtils.randomAlphabetic(20);
	private static final String MALE_STUDY_NAME = RandomStringUtils.randomAlphabetic(20);

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private MessageSource messageSource;

	@Mock
	private CrossingService crossingService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private StudyService studyService;

	@InjectMocks
	private CrossingTemplateParser templateParser;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(STUDY_ID).when(this.studyDataManager).getStudyIdByNameAndProgramUUID(STUDY_NAME, PROGRAM_UUID);
		Mockito.doReturn(NO_PLOT_DATA_ERROR).when(this.messageSource)
			.getMessage("no.list.data.for.plot", new Object[] {ArgumentMatchers.eq(STUDY_NAME), ArgumentMatchers.anyInt()},
				ArgumentMatchers.eq(LocaleContextHolder.getLocale()));
		Mockito.doReturn(CrossingTemplateParserTest.PROGRAM_UUID).when(this.contextUtil).getCurrentProgramUUID();
	}

	@Test
	public void testValidateObservationRow_FemalePlotNoIsValid() {

		final String femalePlotNo = "1";
		final String malePlotNo = "2";
		final int currentRow = 1;
		final String crossingDate = "20190101";

		try {
			this.templateParser.validateObservationRow(femalePlotNo, malePlotNo, currentRow, crossingDate);
		} catch (final FileParsingException e) {
			Assert.fail("Expected not to throw exception");
		}
	}

	@Test
	public void testValidateObservationRow_FemalePlotNoIsEmpty() {

		final String femalePlotNo = "";
		final String malePlotNo = "2";
		final int currentRow = 1;
		final String crossingDate = "20190101";

		try {
			this.templateParser.validateObservationRow(femalePlotNo, malePlotNo, currentRow, crossingDate);
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.messageSource).getMessage("error.import.crosses.observation.row.femalePlot", new Integer[] {1},
				LocaleContextHolder.getLocale());
		}
	}

	@Test
	public void testValidateObservationRow_FemalePlotNoIsNotNumeric() {

		final String femalePlotNo = "AAA";
		final String malePlotNo = "2";
		final int currentRow = 1;
		final String crossingDate = "20190101";

		try {
			this.templateParser.validateObservationRow(femalePlotNo, malePlotNo, currentRow, crossingDate);
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.messageSource).getMessage("error.import.crosses.observation.row.femalePlot", new Integer[] {1},
				LocaleContextHolder.getLocale());
		}
	}

	@Test
	public void testValidateObservationRow_MalePlotNoIsEmpty() {

		final String femalePlotNo = "1";
		final String malePlotNo = "";
		final int currentRow = 1;
		final String crossingDate = "20190101";

		try {
			this.templateParser.validateObservationRow(femalePlotNo, malePlotNo, currentRow, crossingDate);
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.messageSource).getMessage("error.import.crosses.observation.row.malePlot", new Integer[] {1},
				LocaleContextHolder.getLocale());
		}

	}

	@Test
	public void testValidateObservationRow_Valid() {

		final String femalePlotNo = "1";
		final String malePlotNo = "1";
		final int currentRow = 1;
		final String crossingDate = "20190101";

		try {
			this.templateParser.validateObservationRow(femalePlotNo, malePlotNo, currentRow, crossingDate);
		} catch (final FileParsingException e) {
			Assert.fail("Expected no to throw exception");
		}

	}

	@Test
	public void testValidateObservationRow_CrossingDateIsEmpty() {

		final String femalePlotNo = "1";
		final String malePlotNo = "1";
		final int currentRow = 1;
		final String crossingDate = "";

		try {
			this.templateParser.validateObservationRow(femalePlotNo, malePlotNo, currentRow, crossingDate);
		} catch (final FileParsingException e) {
			Assert.fail("Expected not to throw exception");
		}

	}

	@Test
	public void testValidateObservationRow_CrossingDateIsInvalid() {

		final String femalePlotNo = "1";
		final String malePlotNo = "1";
		final int currentRow = 1;
		final String crossingDate = "ABCDEFG";

		try {
			this.templateParser.validateObservationRow(femalePlotNo, malePlotNo, currentRow, crossingDate);
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.messageSource).getMessage("error.import.crosses.observation.row.crossing.date", new Integer[] {1},
				LocaleContextHolder.getLocale());
		}

	}

	@Test
	public void testConvertCommaSeparatedStringToList_CommaSeparatedValuesAreNumeric() {

		try {
			final List<Integer> plotNumbers = this.templateParser.convertCommaSeparatedStringToList("1, 2, 3", 1);
			Assert.assertEquals(Arrays.asList(1, 2, 3), plotNumbers);
		} catch (final FileParsingException e) {
			Assert.fail("Expected not to throw an exception");
		}

	}

	@Test
	public void testConvertCommaSeparatedStringToList_CommaSeparatedValuesAreNotNumeric() {

		try {
			this.templateParser.convertCommaSeparatedStringToList("A, B, C", 1);
			Assert.fail("Expected to throw an exception");
		} catch (final FileParsingException e) {
			Mockito.verify(this.messageSource).getMessage("error.import.crosses.observation.row.malePlot", new Integer[] {1},
				LocaleContextHolder.getLocale());
		}

	}

	@Test
	public void testConvertCommaSeparatedStringToList_CommaSeparatedValuesAreNumericButWithZeroValue() {

		try {
			this.templateParser.convertCommaSeparatedStringToList("0, 1, 2", 1);
			Assert.fail("Expected to throw an exception");
		} catch (final FileParsingException e) {
			Mockito.verify(this.messageSource).getMessage("error.import.crosses.observation.row.malePlot.must.be.greater.than.zero", null,
				LocaleContextHolder.getLocale());
		}

	}

	@Test
	public void testConvertCommaSeparatedStringToList_CommaSeparatedValuesAreNumericButWithValueLessThanZero() {

		try {
			this.templateParser.convertCommaSeparatedStringToList("-1, 1, 2", 1);
			Assert.fail("Expected to throw an exception");
		} catch (final FileParsingException e) {
			Mockito.verify(this.messageSource).getMessage("error.import.crosses.observation.row.malePlot.must.be.greater.than.zero", null,
				LocaleContextHolder.getLocale());
		}

	}


	@Test
	public void getPlotNoToStudyGermplasmDtoMapForStudyWithInvalidStudyName() {
		// setup mocks
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(null);
		final String errorMessage = "Study not found.";
		Mockito.when(this.messageSource.getMessage("no.such.study.exists", new String[] {CrossingTemplateParserTest.STUDY_NAME},
			LocaleContextHolder.getLocale())).thenReturn(errorMessage);

		// Expecting FileParsingException to be thrown for non-existent study name
		try {
			this.templateParser.getPlotNoToStudyGermplasmDtoMapForStudy(CrossingTemplateParserTest.STUDY_NAME, Collections.singleton(1));

			Assert.fail("Exception should have been thrown for non-existent study name but wasn't.");
		} catch (final FileParsingException e) {
			Assert.assertEquals(errorMessage, e.getMessage());
		}

	}

	@Test
	public void getPlotNoToStudyGermplasmDtoMapForStudyWithValidStudyAndPlotNumbers() {
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(1);
		Mockito.when(this.studyService.getStudyGermplasmListWithPlotInformation(ArgumentMatchers.anyInt(), ArgumentMatchers.anySetOf(Integer.class))).thenReturn(new ArrayList<>());

		try {
			final Set<Integer> plotNumbers = new HashSet<>(Arrays.asList(5, 4, 3, 2, 1));
			this.templateParser.getPlotNoToStudyGermplasmDtoMapForStudy(CrossingTemplateParserTest.STUDY_NAME, plotNumbers);

			final ArgumentCaptor<String> nurseryNameCaptor = ArgumentCaptor.forClass(String.class);
			final ArgumentCaptor<String> programUUIDCaptor = ArgumentCaptor.forClass(String.class);
			Mockito.verify(this.studyDataManager, Mockito.times(1)).getStudyIdByNameAndProgramUUID(nurseryNameCaptor.capture(),
				programUUIDCaptor.capture());
			Assert.assertEquals(CrossingTemplateParserTest.STUDY_NAME, nurseryNameCaptor.getValue());
			Assert.assertEquals(CrossingTemplateParserTest.PROGRAM_UUID, programUUIDCaptor.getValue());

			final  ArgumentCaptor<Set<Integer>> plotNosCaptor = ArgumentCaptor.forClass(Set.class);
			Mockito.verify(this.studyService, Mockito.times(1)).getStudyGermplasmListWithPlotInformation(ArgumentMatchers.anyInt(), plotNosCaptor.capture());
			Assert.assertEquals(plotNumbers, plotNosCaptor.getValue());

		} catch (final FileParsingException e) {
			Assert.fail("Exception should not have been thrown for valid study name.");
		}

	}

	@Test
	public void testLookupCrossParentsWithOneMaleStudy() {
		final ImportedCrossesList importCrossesList = this.createImportedCrossesList();
		this.templateParser.setImportedCrossesList(importCrossesList);
		final Set<Integer> femalePlotNumbers = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
		final Set<Integer> malePlotNumbers = new HashSet<>(Arrays.asList(5, 4, 3, 2, 1));
		final Map<String, Set<Integer>> maleNurseryMap = new HashMap<>();
		maleNurseryMap.put(CrossingTemplateParserTest.MALE_STUDY_NAME, malePlotNumbers);
		final Map<Integer, Triple<String, Integer, List<Integer>>> entryIdToCrossInfoMap = this.createEntryIdToCrossInfoMap();


		// setup mocks
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.FEMALE_STUDY_NAME,
			CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(1);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.MALE_STUDY_NAME,
			CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(2);
		Mockito.when(this.studyService.getStudyGermplasmListWithPlotInformation(1, femalePlotNumbers))
			.thenReturn(this.createImportedCrossParents(femalePlotNumbers, CrossingTemplateParserTest.FEMALE_STUDY_NAME));
		Mockito.when(this.studyService.getStudyGermplasmListWithPlotInformation(2, malePlotNumbers))
			.thenReturn(this.createImportedCrossParents(malePlotNumbers, CrossingTemplateParserTest.MALE_STUDY_NAME));
		Mockito.when(this.crossingService.getCross(ArgumentMatchers.any(Germplasm.class), ArgumentMatchers.any(ImportedCross.class),
			ArgumentMatchers.eq("/"))).thenReturn("cross");

		try {
			this.templateParser.lookupCrossParents(CrossingTemplateParserTest.FEMALE_STUDY_NAME, femalePlotNumbers, maleNurseryMap,
				entryIdToCrossInfoMap);

			// Verify Middleware call for looking up cross parents called twice
			// - once for female plots, once for male nursery with its male plots
			Mockito.verify(this.studyService, Mockito.times(2)).getStudyGermplasmListWithPlotInformation(
				ArgumentMatchers.anyInt(), ArgumentMatchers.anySetOf(Integer.class));

			// Verify that GID and Designation from parent crosses were set properly to crosses
			for (final ImportedCross cross : importCrossesList.getImportedCrosses()) {
				final Integer expectedFemaleGID = cross.getFemalePlotNo() + 100;
				final Integer expectedMaleGID = cross.getMalePlotNos().get(0) + 100;
				Assert.assertEquals(expectedFemaleGID, Integer.valueOf(cross.getFemaleGid()));
				Assert.assertEquals(CrossingTemplateParserTest.FEMALE_STUDY_NAME + ":" + cross.getFemalePlotNo(), cross.getFemaleDesignation());
				Assert.assertEquals(expectedMaleGID, cross.getMaleParents().get(0).getGid());
				Assert.assertEquals(CrossingTemplateParserTest.MALE_STUDY_NAME + ":" + cross.getMalePlotNos().get(0), cross.getMaleDesignationsAsString());
			}

		} catch (final FileParsingException e) {
			Assert.fail("Exception should not have been thrown for valid nursery name and plot numbers.");
		}

	}

	@Test
	public void testLookupCrossParentsWithInvalidFemalePlots() {
		final ImportedCrossesList importCrossesList = this.createImportedCrossesList();
		this.templateParser.setImportedCrossesList(importCrossesList);
		final Set<Integer> femalePlotNumbers = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
		final Set<Integer> malePlotNumbers = new HashSet<>(Arrays.asList(5, 4, 3, 2, 1));
		final Map<String, Set<Integer>> maleNurseryMap = new HashMap<>();
		maleNurseryMap.put(CrossingTemplateParserTest.MALE_STUDY_NAME, malePlotNumbers);
		final Map<Integer, Triple<String, Integer, List<Integer>>> entryIdToCrossInfoMap = this.createEntryIdToCrossInfoMap();


		// setup mocks
		final String errorMessage = "Invalid female plot.";
		final int invalidPlotNo = 4;
		Mockito.when(this.messageSource.getMessage("no.list.data.for.plot",
			new Object[] {CrossingTemplateParserTest.FEMALE_STUDY_NAME, invalidPlotNo}, LocaleContextHolder.getLocale()))
			.thenReturn(errorMessage);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.FEMALE_STUDY_NAME,
			CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(1);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.MALE_STUDY_NAME,
			CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(2);
		Mockito.when(this.studyService.getStudyGermplasmListWithPlotInformation(1, femalePlotNumbers))
			.thenReturn(this.createImportedCrossParents(new HashSet<>(Arrays.asList(1, 2, 3, 5)), CrossingTemplateParserTest.FEMALE_STUDY_NAME));
		Mockito.when(this.studyService.getStudyGermplasmListWithPlotInformation(2, malePlotNumbers))
			.thenReturn(this.createImportedCrossParents(malePlotNumbers, CrossingTemplateParserTest.MALE_STUDY_NAME));
		Mockito.when(this.crossingService.getCross(ArgumentMatchers.any(Germplasm.class), ArgumentMatchers.any(ImportedCross.class),
			ArgumentMatchers.eq("/"))).thenReturn("cross");

		try {
			this.templateParser.lookupCrossParents(CrossingTemplateParserTest.FEMALE_STUDY_NAME, femalePlotNumbers, maleNurseryMap,
				entryIdToCrossInfoMap);

			Assert.fail("Exception should have been thrown for non-existent female plot but wasn't.");

		} catch (final FileParsingException e) {
			Assert.assertEquals(errorMessage, e.getMessage());
		}

	}

	@Test
	public void testLookupCrossParentsWithInvalidMalePlots() {
		final ImportedCrossesList importCrossesList = this.createImportedCrossesList();
		this.templateParser.setImportedCrossesList(importCrossesList);
		final Set<Integer> femalePlotNumbers = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
		final Set<Integer> malePlotNumbers = new HashSet<>(Arrays.asList(5, 4, 3, 2, 1));
		final Map<String, Set<Integer>> maleNurseryMap = new HashMap<>();
		maleNurseryMap.put(CrossingTemplateParserTest.MALE_STUDY_NAME, malePlotNumbers);
		final Map<Integer, Triple<String, Integer, List<Integer>>> entryIdToCrossInfoMap = this.createEntryIdToCrossInfoMap();


		// setup mocks
		final String errorMessage = "Invalid male plot.";
		final int invalidPlotNo = 5;
		Mockito.when(this.messageSource.getMessage("no.list.data.for.plot",
			new Object[] {CrossingTemplateParserTest.MALE_STUDY_NAME, invalidPlotNo}, LocaleContextHolder.getLocale()))
			.thenReturn(errorMessage);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.FEMALE_STUDY_NAME,
			CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(1);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.MALE_STUDY_NAME,
			CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(2);
		Mockito.when(this.studyService.getStudyGermplasmListWithPlotInformation(1, femalePlotNumbers))
			.thenReturn(this.createImportedCrossParents(femalePlotNumbers, CrossingTemplateParserTest.FEMALE_STUDY_NAME));
		Mockito.when(this.studyService.getStudyGermplasmListWithPlotInformation(2, malePlotNumbers))
			.thenReturn(this.createImportedCrossParents(new HashSet<>(Arrays.asList(1, 2, 3, 4)), CrossingTemplateParserTest.MALE_STUDY_NAME));
		Mockito.when(this.crossingService.getCross(ArgumentMatchers.any(Germplasm.class), ArgumentMatchers.any(ImportedCross.class),
			ArgumentMatchers.eq("/"))).thenReturn("cross");

		try {
			this.templateParser.lookupCrossParents(CrossingTemplateParserTest.FEMALE_STUDY_NAME, femalePlotNumbers, maleNurseryMap,
				entryIdToCrossInfoMap);

			Assert.fail("Exception should have been thrown for non-existent male plot but wasn't.");

		} catch (final FileParsingException e) {
			Assert.assertEquals(errorMessage, e.getMessage());
		}

	}

	@Test
	public void testGetMaleParents() {
		final Map<String, Map<Integer, StudyGermplasmDto>> maleNurseriesPlotMap =  new HashMap<>();
		final List<StudyGermplasmDto> malePlotList =
			this.createImportedCrossParents(new HashSet<>(Collections.singletonList(21)), CrossingTemplateParserTest.MALE_STUDY_NAME);
		final Map<Integer, StudyGermplasmDto> malePlotMap =
			malePlotList.stream().collect(Collectors.toMap(e-> Integer.valueOf(e.getPosition()), e->e));
		maleNurseriesPlotMap.put(CrossingTemplateParserTest.MALE_STUDY_NAME, malePlotMap);
		final Triple<String, Integer, List<Integer>> crossInfo = new ImmutableTriple<>(CrossingTemplateParserTest.MALE_STUDY_NAME, 1, Collections.singletonList(21));
		try {
			final List<ImportedGermplasmParent> maleParents = this.templateParser.getMaleParents(maleNurseriesPlotMap, crossInfo);
			final ImportedGermplasmParent maleParent = maleParents.get(0);
			final StudyGermplasmDto crossParent = malePlotMap.get(21);
			Assert.assertEquals(crossParent.getPosition(), maleParent.getPlotNo().toString());
			Assert.assertEquals(crossParent.getGermplasmId(), maleParent.getGid());
			Assert.assertEquals(crossParent.getDesignation(), maleParent.getDesignation());
			Assert.assertEquals(CrossingTemplateParserTest.MALE_STUDY_NAME, maleParent.getStudyName());
		} catch (final FileParsingException e) {
			Assert.fail("Should not throw exception.");
		}
	}

	@Test
	public void testGetMaleParentsWithUnknownMaleParent() {
		final Map<String, Map<Integer, StudyGermplasmDto>> maleNurseriesPlotMap =  new HashMap<>();
		final Map<Integer, StudyGermplasmDto> malePlotMap = new HashMap<>();
		maleNurseriesPlotMap.put(CrossingTemplateParserTest.MALE_STUDY_NAME, malePlotMap);
		final Triple<String, Integer, List<Integer>> crossInfo = new ImmutableTriple<>(CrossingTemplateParserTest.MALE_STUDY_NAME, 1, Collections.singletonList(0));
		try {
			final List<ImportedGermplasmParent> maleParents = this.templateParser.getMaleParents(maleNurseriesPlotMap, crossInfo);
			final ImportedGermplasmParent unknownParent = maleParents.get(0);
			Assert.assertEquals("0", unknownParent.getPlotNo().toString());
			Assert.assertEquals("0", unknownParent.getGid().toString());
			Assert.assertEquals(Name.UNKNOWN, unknownParent.getDesignation());
			Assert.assertEquals(CrossingTemplateParserTest.MALE_STUDY_NAME, unknownParent.getStudyName());
		} catch (final FileParsingException e) {
			Assert.fail("Should not throw exception.");
		}
	}

	@Test
	public void testGetMaleParentsWithInvalidMalePlotNo() {
		final String errorMessage = "Invalid male plot.";
		final int invalidPlotNo = 5;
		Mockito.when(this.messageSource.getMessage("no.list.data.for.plot",
			new Object[] {CrossingTemplateParserTest.MALE_STUDY_NAME, invalidPlotNo}, LocaleContextHolder.getLocale()))
			.thenReturn(errorMessage);

		final Map<String, Map<Integer, StudyGermplasmDto>> maleNurseriesPlotMap =  new HashMap<>();
		final Map<Integer, StudyGermplasmDto> malePlotMap = new HashMap<>();
		maleNurseriesPlotMap.put(CrossingTemplateParserTest.MALE_STUDY_NAME, malePlotMap);
		final Triple<String, Integer, List<Integer>> crossInfo = new ImmutableTriple<>(CrossingTemplateParserTest.MALE_STUDY_NAME, 1, Collections.singletonList(invalidPlotNo));
		try {
			this.templateParser.getMaleParents(maleNurseriesPlotMap, crossInfo);
			Assert.fail("Should throw exception.");
		} catch (final FileParsingException e) {
			Assert.assertEquals(errorMessage, e.getMessage());
		}

	}

	private ImportedCrossesList createImportedCrossesList() {
		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCross> importedCrosses = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			final ImportedCross cross = new ImportedCross(i);
			importedCrosses.add(cross);
		}
		importedCrossesList.setImportedGermplasms(importedCrosses);
		return importedCrossesList;
	}

	private List<StudyGermplasmDto> createImportedCrossParents(final Set<Integer> plotNumbers, final String studyName) {
		final List<StudyGermplasmDto> list = new ArrayList<>();
		for (final Integer plotNo : plotNumbers) {
			final StudyGermplasmDto parent = new StudyGermplasmDto();
			parent.setGermplasmId(100 + plotNo);
			parent.setDesignation(studyName + ":" + plotNo);
			parent.setPosition(plotNo.toString());
			list.add(parent);
		}
		return list;
	}

	private Map<Integer, Triple<String, Integer, List<Integer>>> createEntryIdToCrossInfoMap() {
		final Map<Integer, Triple<String, Integer, List<Integer>>> entryIdToCrossInfoMap = new HashMap<>();
		for(int i=0; i<5; i++) {
			entryIdToCrossInfoMap.put(i+1, new ImmutableTriple<>(CrossingTemplateParserTest.MALE_STUDY_NAME, i+1, Collections.singletonList(i+1)));
		}
		return entryIdToCrossInfoMap;
	}

}
