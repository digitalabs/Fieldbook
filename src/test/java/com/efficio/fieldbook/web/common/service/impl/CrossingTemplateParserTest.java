package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;



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
		Mockito.doReturn(NO_PLOT_DATA_ERROR).when(this.messageSource).getMessage("no.list.data.for.plot", new Object[] {ArgumentMatchers.eq(STUDY_NAME), ArgumentMatchers.anyInt()},
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
		Mockito.doReturn(new ArrayList<>()).when(this.fieldbookMiddlewareService).getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY, Arrays.asList(0), "1");

		try {			
			this.templateParser.getListDataProject(STUDY_NAME, Arrays.asList(0), PROGRAM_UUID, false);
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.fieldbookMiddlewareService).getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY, Arrays.asList(0), "1");
			Mockito.verify(this.messageSource).getMessage("no.list.data.for.plot", new Object[] {STUDY_NAME, "0"},
					LocaleContextHolder.getLocale());
			
		}
	}
	
	@Test
	public void testGetListDataProjectForNonExistentPlotNumber() {
		final Integer plotNumber = new Random().nextInt();
		Mockito.doReturn(new ArrayList<>()).when(this.fieldbookMiddlewareService).getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY, Arrays.asList(plotNumber), "1");

		try {			
			this.templateParser.getListDataProject(STUDY_NAME,  Arrays.asList(plotNumber), PROGRAM_UUID, new Random().nextBoolean());
			Assert.fail("Expected to throw exception but didn't");
		} catch (final FileParsingException e) {
			Mockito.verify(this.fieldbookMiddlewareService).getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY,  Arrays.asList(plotNumber), "1");
			Mockito.verify(this.messageSource).getMessage("no.list.data.for.plot", new Object[] {STUDY_NAME, String.valueOf(plotNumber)},
					LocaleContextHolder.getLocale());
			
		}
	}
	
	@Test
	public void testGetListDataProjectForValidStudyPlot() throws FileParsingException {
		final Integer plotNumber = new Random().nextInt();
		final ListDataProject middlewareResult = new ListDataProject();
		middlewareResult.setListDataProjectId(new Random().nextInt());
		Mockito.doReturn(Arrays.asList(middlewareResult)).when(this.fieldbookMiddlewareService).getListDataProjectByStudy(STUDY_ID, GermplasmListType.STUDY,  Arrays.asList(plotNumber), "1");

		final ListDataProject result = this.templateParser.getListDataProject(STUDY_NAME,  Arrays.asList(plotNumber), PROGRAM_UUID, new Random().nextBoolean()).get(0);
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
	
	

}
