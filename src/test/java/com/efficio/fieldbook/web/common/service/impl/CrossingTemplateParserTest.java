
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.service.CrossingService;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class CrossingTemplateParserTest {

	private static final String FEMALE_NURSERY = "Nursery 001";

	private static final String MALE_NURSERY1 = "Nursery 002";

	private static final String MALE_NURSERY2 = "Nursery 003";

	private static final String PROGRAM_UUID = "qwerty-0987";

	@Mock
	private UserSelection studySelection;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private FieldbookService fieldbookService;

	@Mock
	private CrossingService crossingService;

	@Mock
	private MessageSource messageSource;

	@InjectMocks
	private CrossingTemplateParser crossingParser;

	@Captor
	private ArgumentCaptor<Set<Integer>> plotNosCaptor;

	@Test
	public void testGetPlotToListDataProjectMapForNurseryWithInvalidNurseryName() {
		// setup mocks
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(Matchers.anyString(), Matchers.anyString())).thenReturn(null);
		final String errorMessage = "Nursery not found.";
		Mockito.when(this.messageSource.getMessage("no.such.study.exists", new String[] {CrossingTemplateParserTest.FEMALE_NURSERY},
				LocaleContextHolder.getLocale())).thenReturn(errorMessage);

		// Expecting FileParsingException to be thrown for non-existent nursery name
		try {
			this.crossingParser.getPlotToListDataProjectMapForNursery(CrossingTemplateParserTest.FEMALE_NURSERY, Collections.singleton(1),
					CrossingTemplateParserTest.PROGRAM_UUID);

			Assert.fail("Exception should have been thrown for non-existent nursery name but wasn't.");
		} catch (final FileParsingException e) {
			Assert.assertEquals(errorMessage, e.getMessage());
		}

	}

	@Test
	public void testGetPlotToListDataProjectMapForNurseryWithValidNurseryAndPlotNumbers() {
		// setup mocks
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(Matchers.anyString(), Matchers.anyString())).thenReturn(1);
		Mockito.when(this.studyDataManager.getStudyType(1)).thenReturn(StudyType.N);
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(Matchers.anyInt(), Matchers.any(GermplasmListType.class),
				Matchers.anySetOf(Integer.class))).thenReturn(new HashMap<Integer, ListDataProject>());

		final Set<Integer> plotNumbers = new HashSet<>(Arrays.asList(1, 3, 5, 7, 9));
		try {
			this.crossingParser.getPlotToListDataProjectMapForNursery(CrossingTemplateParserTest.FEMALE_NURSERY, plotNumbers,
					CrossingTemplateParserTest.PROGRAM_UUID);

			// Verify Middleware calls
			// 1. Verify arguments for getting study ID of given nursery name
			final ArgumentCaptor<String> nurseryNameCaptor = ArgumentCaptor.forClass(String.class);
			final ArgumentCaptor<String> programUUIDCaptor = ArgumentCaptor.forClass(String.class);
			Mockito.verify(this.studyDataManager, Mockito.times(1)).getStudyIdByNameAndProgramUUID(nurseryNameCaptor.capture(),
					programUUIDCaptor.capture());
			Assert.assertEquals(CrossingTemplateParserTest.FEMALE_NURSERY, nurseryNameCaptor.getValue());
			Assert.assertEquals(CrossingTemplateParserTest.PROGRAM_UUID, programUUIDCaptor.getValue());

			// 2. Verify arguments for getting ListDataProject of nursery for specific plot #s
			final ArgumentCaptor<GermplasmListType> listTypeCaptor = ArgumentCaptor.forClass(GermplasmListType.class);
			Mockito.verify(this.fieldbookService, Mockito.times(1)).getListDataProjectByStudyTypeAndPlotNos(Matchers.anyInt(), listTypeCaptor.capture(),
					this.plotNosCaptor.capture());
			Assert.assertEquals(GermplasmListType.NURSERY, listTypeCaptor.getValue());
			Assert.assertEquals(plotNumbers, this.plotNosCaptor.getValue());

		} catch (final FileParsingException e) {
			Assert.fail("Exception should not have been thrown for valid nursery name.");
		}

	}

	@Test
	public void testLookupCrossParentsWithOneMaleNursery() {
		// setup test data. female nursery and 1 male nursery
		final ImportedCrossesList importCrossesList = this.createImportedCrossesList(5, false);
		this.crossingParser.setImportedCrossesList(importCrossesList);
		final Set<Integer> femalePlotNumbers = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
		final Set<Integer> malePlotNumbers = new HashSet<>(Arrays.asList(5, 4, 3, 2, 1));
		final Map<String, Set<Integer>> maleNurseryMap = new HashMap<>();
		maleNurseryMap.put(CrossingTemplateParserTest.MALE_NURSERY1, malePlotNumbers);

		// setup mocks
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.FEMALE_NURSERY,
				CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(1);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.MALE_NURSERY1,
				CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(2);
		Mockito.when(this.studyDataManager.getStudyType(Matchers.anyInt())).thenReturn(StudyType.N);
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(1, GermplasmListType.NURSERY, femalePlotNumbers))
				.thenReturn(this.createListDataProject(femalePlotNumbers, CrossingTemplateParserTest.FEMALE_NURSERY));
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(2, GermplasmListType.NURSERY, malePlotNumbers))
				.thenReturn(this.createListDataProject(malePlotNumbers, CrossingTemplateParserTest.MALE_NURSERY1));

		try {
			this.crossingParser.lookupCrossParents(CrossingTemplateParserTest.FEMALE_NURSERY, femalePlotNumbers, maleNurseryMap,
					CrossingTemplateParserTest.PROGRAM_UUID);

			// Verify Middleware call for looking up cross parents called twice
			// - once for female plots, once for male nursery with its male plots
			Mockito.verify(this.fieldbookService, Mockito.times(2)).getListDataProjectByStudyTypeAndPlotNos(Matchers.anyInt(),
					Matchers.any(GermplasmListType.class), Matchers.anySetOf(Integer.class));

			// Verify that GID and Designation from parent crosses were set properly to crosses
			for (final ImportedCrosses cross : importCrossesList.getImportedCrosses()) {
				final Integer expectedFemaleGID = Integer.valueOf(cross.getFemalePlotNo()) + 100;
				final Integer expectedMaleGID = Integer.valueOf(cross.getMalePlotNo()) + 100;
				Assert.assertEquals(expectedFemaleGID, Integer.valueOf(cross.getFemaleGid()));
				Assert.assertEquals(CrossingTemplateParserTest.FEMALE_NURSERY + ":" + cross.getFemalePlotNo(), cross.getFemaleDesig());
				Assert.assertEquals(expectedMaleGID, Integer.valueOf(cross.getMaleGid()));
				Assert.assertEquals(CrossingTemplateParserTest.MALE_NURSERY1 + ":" + cross.getMalePlotNo(), cross.getMaleDesig());
			}

		} catch (final FileParsingException e) {
			Assert.fail("Exception should not have been thrown for valid nursery name and plot numbers.");
		}

	}

	@Test
	public void testLookupCrossParentsWithMultipleMaleNurseries() {
		// setup test data. female nursery and 2 male nurseries
		final ImportedCrossesList importCrossesList = this.createImportedCrossesList(5, true);
		this.crossingParser.setImportedCrossesList(importCrossesList);

		final Set<Integer> femalePlotNumbers = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
		final Set<Integer> malePlotNumbers1 = new HashSet<>(Arrays.asList(5, 4));
		final Set<Integer> malePlotNumbers2 = new HashSet<>(Arrays.asList(3, 2, 1));
		final Map<String, Set<Integer>> maleNurseryMap = new HashMap<>();
		maleNurseryMap.put(CrossingTemplateParserTest.MALE_NURSERY1, malePlotNumbers1);
		maleNurseryMap.put(CrossingTemplateParserTest.MALE_NURSERY2, malePlotNumbers2);

		// setup mocks
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.FEMALE_NURSERY,
				CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(1);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.MALE_NURSERY1,
				CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(2);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.MALE_NURSERY2,
				CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(3);
		Mockito.when(this.studyDataManager.getStudyType(Matchers.anyInt())).thenReturn(StudyType.N);
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(1, GermplasmListType.NURSERY, femalePlotNumbers))
				.thenReturn(this.createListDataProject(femalePlotNumbers, CrossingTemplateParserTest.FEMALE_NURSERY));
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(2, GermplasmListType.NURSERY, malePlotNumbers1))
				.thenReturn(this.createListDataProject(malePlotNumbers1, CrossingTemplateParserTest.MALE_NURSERY1));
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(3, GermplasmListType.NURSERY, malePlotNumbers2))
				.thenReturn(this.createListDataProject(malePlotNumbers2, CrossingTemplateParserTest.MALE_NURSERY2));

		try {
			this.crossingParser.lookupCrossParents(CrossingTemplateParserTest.FEMALE_NURSERY, femalePlotNumbers, maleNurseryMap,
					CrossingTemplateParserTest.PROGRAM_UUID);

			// Verify Middleware call for looking up cross parents called three times
			// - once for female plots, twice for male nurseries with corresponding male plots
			Mockito.verify(this.fieldbookService, Mockito.times(3)).getListDataProjectByStudyTypeAndPlotNos(Matchers.anyInt(),
					Matchers.any(GermplasmListType.class), Matchers.anySetOf(Integer.class));

			// Verify that GID and Designation from parent crosses were set properly to crosses
			for (final ImportedCrosses cross : importCrossesList.getImportedCrosses()) {
				final Integer expectedFemaleGID = Integer.valueOf(cross.getFemalePlotNo()) + 100;
				final Integer expectedMaleGID = Integer.valueOf(cross.getMalePlotNo()) + 100;
				Assert.assertEquals(expectedFemaleGID, Integer.valueOf(cross.getFemaleGid()));
				Assert.assertEquals(CrossingTemplateParserTest.FEMALE_NURSERY + ":" + cross.getFemalePlotNo(), cross.getFemaleDesig());
				Assert.assertEquals(expectedMaleGID, Integer.valueOf(cross.getMaleGid()));
				Assert.assertEquals(cross.getMaleStudyName() + ":" + cross.getMalePlotNo(), cross.getMaleDesig());
			}

		} catch (final FileParsingException e) {
			Assert.fail("Exception should not have been thrown for valid nursery name and plot numbers. ");
		}
	}

	@Test
	public void testLookupCrossParentsWithInvalidFemalePlots() {
		// setup test data. female nursery and 1 male nursery
		final ImportedCrossesList importCrossesList = this.createImportedCrossesList(5, false);
		this.crossingParser.setImportedCrossesList(importCrossesList);
		final Set<Integer> femalePlotNumbers = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
		final Set<Integer> malePlotNumbers = new HashSet<>(Arrays.asList(5, 4, 3, 2, 1));
		final Map<String, Set<Integer>> maleNurseryMap = new HashMap<>();
		maleNurseryMap.put(CrossingTemplateParserTest.MALE_NURSERY1, malePlotNumbers);

		// Setup mocks
		// Expecting the female plot "4" to be non-existent. Only female plot 1, 2 and 3 have ListDataProject records returned from mock
		final String errorMessage = "Invalid female plot.";
		final Integer invalidPlotNo = 4;
		Mockito.when(this.messageSource.getMessage("no.list.data.for.plot",
				new Object[] {CrossingTemplateParserTest.FEMALE_NURSERY, invalidPlotNo}, LocaleContextHolder.getLocale()))
				.thenReturn(errorMessage);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.FEMALE_NURSERY,
				CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(1);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.MALE_NURSERY1,
				CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(2);
		Mockito.when(this.studyDataManager.getStudyType(Matchers.anyInt())).thenReturn(StudyType.N);
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(1, GermplasmListType.NURSERY, femalePlotNumbers))
				.thenReturn(this.createListDataProject(new HashSet<>(Arrays.asList(1, 2, 3)), CrossingTemplateParserTest.FEMALE_NURSERY));
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(2, GermplasmListType.NURSERY, malePlotNumbers))
				.thenReturn(this.createListDataProject(malePlotNumbers, CrossingTemplateParserTest.MALE_NURSERY1));

		try {
			this.crossingParser.lookupCrossParents(CrossingTemplateParserTest.FEMALE_NURSERY, femalePlotNumbers, maleNurseryMap,
					CrossingTemplateParserTest.PROGRAM_UUID);

			Assert.fail("Exception should have been thrown for non-existent female plot but wasn't.");

		} catch (final FileParsingException e) {
			Assert.assertEquals(errorMessage, e.getMessage());
		}

	}

	@Test
	public void testLookupCrossParentsWithInvalidMalePlots() {
		// setup test data. female nursery and 1 male nursery
		final ImportedCrossesList importCrossesList = this.createImportedCrossesList(5, false);
		this.crossingParser.setImportedCrossesList(importCrossesList);
		final Set<Integer> femalePlotNumbers = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
		final Set<Integer> malePlotNumbers = new HashSet<>(Arrays.asList(5, 4, 3, 2, 1));
		final Map<String, Set<Integer>> maleNurseryMap = new HashMap<>();
		maleNurseryMap.put(CrossingTemplateParserTest.MALE_NURSERY1, malePlotNumbers);

		// Setup mocks
		// Expecting the male plot "5" to be non-existent. Only male plots 1, 2, 3 and 4 have ListDataProject records returned from mock
		final String errorMessage = "Invalid Male plot.";
		final Integer invalidPlotNo = 5;
		Mockito.when(this.messageSource.getMessage("no.list.data.for.plot",
				new Object[] {CrossingTemplateParserTest.MALE_NURSERY1, invalidPlotNo}, LocaleContextHolder.getLocale()))
				.thenReturn(errorMessage);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.FEMALE_NURSERY,
				CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(1);
		Mockito.when(this.studyDataManager.getStudyIdByNameAndProgramUUID(CrossingTemplateParserTest.MALE_NURSERY1,
				CrossingTemplateParserTest.PROGRAM_UUID)).thenReturn(2);
		Mockito.when(this.studyDataManager.getStudyType(Matchers.anyInt())).thenReturn(StudyType.N);
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(1, GermplasmListType.NURSERY, femalePlotNumbers))
				.thenReturn(this.createListDataProject(femalePlotNumbers, CrossingTemplateParserTest.FEMALE_NURSERY));
		Mockito.when(this.fieldbookService.getListDataProjectByStudyTypeAndPlotNos(2, GermplasmListType.NURSERY, malePlotNumbers))
				.thenReturn(this.createListDataProject(new HashSet<>(Arrays.asList(1, 2, 3, 4)), CrossingTemplateParserTest.MALE_NURSERY1));

		try {
			this.crossingParser.lookupCrossParents(CrossingTemplateParserTest.FEMALE_NURSERY, femalePlotNumbers, maleNurseryMap,
					CrossingTemplateParserTest.PROGRAM_UUID);

			Assert.fail("Exception should have been thrown for non-existent male plot but wasn't.");

		} catch (final FileParsingException e) {
			Assert.assertEquals(errorMessage, e.getMessage());
		}

	}

	private ImportedCrossesList createImportedCrossesList(final Integer noOfCrosses, final Boolean hasMultipleMaleNursery) {
		final ImportedCrossesList importedCrossesList = new ImportedCrossesList();
		final List<ImportedCrosses> importedCrosses = new ArrayList<>();
		for (int i = 1; i <= noOfCrosses; i++) {
			final int malePlotNo = noOfCrosses - i + 1;
			String maleNursery = CrossingTemplateParserTest.MALE_NURSERY1;
			if (hasMultipleMaleNursery && i > noOfCrosses / 2) {
				maleNursery = CrossingTemplateParserTest.MALE_NURSERY2;
			}
			final ImportedCrosses cross = new ImportedCrosses(CrossingTemplateParserTest.FEMALE_NURSERY, maleNursery, String.valueOf(i),
					String.valueOf(malePlotNo), i);
			importedCrosses.add(cross);
		}
		importedCrossesList.setImportedGermplasms(importedCrosses);
		return importedCrossesList;
	}

	private Map<Integer, ListDataProject> createListDataProject(final Set<Integer> plotNumbers, final String nurseryName) {
		final Map<Integer, ListDataProject> listDataMap = new HashMap<>();
		for (final Integer plotNo : plotNumbers) {
			final ListDataProject listData = new ListDataProject();
			listData.setGermplasmId(100 + plotNo);
			listData.setDesignation(nurseryName + ":" + plotNo);
			listDataMap.put(plotNo, listData);
		}
		return listDataMap;
	}
}
