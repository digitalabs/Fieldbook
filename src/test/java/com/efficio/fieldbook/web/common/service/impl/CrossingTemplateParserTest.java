package com.efficio.fieldbook.web.common.service.impl;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CrossingTemplateParserTest {

	private static final String NO_PLOT_DATA_ERROR = "NO PLOT DATA ERROR";
	private static final Integer STUDY_ID = new Random().nextInt();
	private static final String PROGRAM_UUID = RandomStringUtils.randomAlphabetic(20);
	private static final String STUDY_NAME = RandomStringUtils.randomAlphabetic(20);

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private MessageSource messageSource;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@InjectMocks
	private CrossingTemplateParser templateParser;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		Mockito.doReturn(STUDY_ID).when(this.studyDataManager).getStudyIdByNameAndProgramUUID(STUDY_NAME, PROGRAM_UUID);
		Mockito.doReturn(NO_PLOT_DATA_ERROR).when(this.messageSource)
			.getMessage("no.list.data.for.plot", new Object[] {ArgumentMatchers.eq(STUDY_NAME), ArgumentMatchers.anyInt()},
				ArgumentMatchers.eq(LocaleContextHolder.getLocale()));
	}

	@Test
	public void testGetListDataProjectForUnknownMaleParent() throws FileParsingException {
		final ListDataProject result = this.templateParser.getListDataProject(STUDY_NAME, Arrays.asList(0), PROGRAM_UUID, true).get(0);
		Assert.assertEquals(0, result.getGermplasmId().intValue());
		Assert.assertEquals(Name.UNKNOWN, result.getDesignation());
		Mockito.verify(this.fieldbookMiddlewareService).getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY, Arrays.asList(0), "1");
	}

	@Test
	public void testGetListDataProjectForUnknownFemaleParent() {
		Mockito.doReturn(new ArrayList<>()).when(this.fieldbookMiddlewareService)
			.getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY, Arrays.asList(0), "1");

		try {
			this.templateParser.getListDataProject(STUDY_NAME, Arrays.asList(0), PROGRAM_UUID, false);
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.fieldbookMiddlewareService)
				.getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY, Arrays.asList(0), "1");
			Mockito.verify(this.messageSource).getMessage("no.list.data.for.plot", new Object[] {STUDY_NAME, "0"},
				LocaleContextHolder.getLocale());

		}
	}

	@Test
	public void testGetListDataProjectForNonExistentPlotNumber() {
		final Integer plotNumber = new Random().nextInt();
		Mockito.doReturn(new ArrayList<>()).when(this.fieldbookMiddlewareService)
			.getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY, Arrays.asList(plotNumber), "1");

		try {
			this.templateParser.getListDataProject(STUDY_NAME, Arrays.asList(plotNumber), PROGRAM_UUID, new Random().nextBoolean());
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.fieldbookMiddlewareService)
				.getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY, Arrays.asList(plotNumber), "1");
			Mockito.verify(this.messageSource).getMessage("no.list.data.for.plot", new Object[] {STUDY_NAME, String.valueOf(plotNumber)},
				LocaleContextHolder.getLocale());

		}
	}

	@Test
	public void testGetListDataProjectForValidStudyPlot() throws FileParsingException {
		final Integer plotNumber = new Random().nextInt();
		final ListDataProject middlewareResult = new ListDataProject();
		middlewareResult.setListDataProjectId(new Random().nextInt());
		Mockito.doReturn(Arrays.asList(middlewareResult)).when(this.fieldbookMiddlewareService)
			.getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY, Arrays.asList(plotNumber), "1");

		final ListDataProject result =
			this.templateParser.getListDataProject(STUDY_NAME, Arrays.asList(plotNumber), PROGRAM_UUID, new Random().nextBoolean()).get(0);
		Assert.assertEquals(middlewareResult, result);

	}

	@Test
	public void testGetListDataProjectForInvalidStudy() {
		Mockito.doReturn(null).when(this.studyDataManager).getStudyIdByNameAndProgramUUID(STUDY_NAME, PROGRAM_UUID);

		try {
			this.templateParser.getListDataProject(STUDY_NAME, Arrays.asList(1), PROGRAM_UUID, new Random().nextBoolean());
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.messageSource).getMessage("no.such.study.exists", new Object[] {STUDY_NAME},
				LocaleContextHolder.getLocale());
			Mockito.verifyZeroInteractions(this.fieldbookMiddlewareService);

		}
	}

	@Test
	public void testGetListDataProjectForInvalidStudyWhenPlotIsZero() {
		Mockito.doReturn(null).when(this.studyDataManager).getStudyIdByNameAndProgramUUID(STUDY_NAME, PROGRAM_UUID);

		try {
			this.templateParser.getListDataProject(STUDY_NAME, Arrays.asList(0), PROGRAM_UUID, new Random().nextBoolean());
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.messageSource).getMessage("no.such.study.exists", new Object[] {STUDY_NAME},
				LocaleContextHolder.getLocale());
			Mockito.verifyZeroInteractions(this.fieldbookMiddlewareService);

		}
	}

	@Test
	public void testValidateObservationRow_FemalePlotNoIsValid() {

		String femalePlotNo = "1";
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

		String femalePlotNo = "";
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

		String femalePlotNo = "AAA";
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
	public void testValidateObservationRow_MalePlotNoIsValid() {

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
	public void testValidateObservationRow_CrossingDateIsValid() {

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

}
