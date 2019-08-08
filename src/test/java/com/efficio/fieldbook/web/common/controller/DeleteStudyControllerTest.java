package com.efficio.fieldbook.web.common.controller;

import junit.framework.Assert;
import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.UnpermittedDeletionException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeleteStudyControllerTest {
	private static final int PROJECT_ID = 1;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private ContextUtil contextUtil;

	@Mock
	private MessageSource messageSource;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private Model model;

	@Mock
	private HttpSession session;

	@InjectMocks
	private DeleteStudyController deleteStudyController;

	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;

	private Locale locale;

	@Before
	public void setUp() {
		this.locale = new Locale("en", "US");
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
		final GermplasmList nurseryList = this.germplasmListTestDataInitializer.createGermplasmListWithType(1,
				GermplasmListType.STUDY.name());
		final GermplasmList germplasmListWithType = this.germplasmListTestDataInitializer.createGermplasmListWithType(1,
				GermplasmListType.STUDY.name());

		Mockito.when(this.fieldbookMiddlewareService
				.getGermplasmListsByProjectId(DeleteStudyControllerTest.PROJECT_ID, GermplasmListType.STUDY))
				.thenReturn(Arrays.asList(nurseryList));
		Mockito.when(this.fieldbookMiddlewareService
				.getGermplasmListsByProjectId(DeleteStudyControllerTest.PROJECT_ID, GermplasmListType.STUDY))
				.thenReturn(Arrays.asList(germplasmListWithType));
	}

	@Test
	public void testSubmitDeleteNursery() throws UnpermittedDeletionException {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getProgramUUID()).thenReturn(RandomStringUtils.random(10));
		Mockito.doReturn(study).when(fieldbookMiddlewareService).getStudy(DeleteStudyControllerTest.PROJECT_ID);
		final Map<String, Object> result = this.deleteStudyController
				.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 1", "1", result.get(DeleteStudyController.IS_SUCCESS));
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1))
				.deleteStudy(DeleteStudyControllerTest.PROJECT_ID, this.contextUtil.getCurrentWorkbenchUserId());
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1))
				.getGermplasmListsByProjectId(DeleteStudyControllerTest.PROJECT_ID, GermplasmListType.STUDY);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1))
				.getGermplasmListsByProjectId(DeleteStudyControllerTest.PROJECT_ID, GermplasmListType.CHECK);
		Mockito.verify(this.germplasmListManager, Mockito.times(1))
				.deleteGermplasmList(Matchers.any(GermplasmList.class));
	}

	@Test
	public void testSubmitDeleteTrial() throws UnpermittedDeletionException {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getProgramUUID()).thenReturn(RandomStringUtils.random(10));
		Mockito.doReturn(study).when(fieldbookMiddlewareService).getStudy(DeleteStudyControllerTest.PROJECT_ID);
		final Map<String, Object> result = this.deleteStudyController
				.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 1", "1", result.get(DeleteStudyController.IS_SUCCESS));
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1))
				.deleteStudy(DeleteStudyControllerTest.PROJECT_ID, this.contextUtil.getCurrentWorkbenchUserId());
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1))
				.getGermplasmListsByProjectId(DeleteStudyControllerTest.PROJECT_ID, GermplasmListType.STUDY);
		Mockito.verify(this.germplasmListManager, Mockito.times(1))
				.deleteGermplasmList(Matchers.any(GermplasmList.class));
	}

	@Test
	public void testSubmitDeleteWithUnpermittedDeletionException() throws UnpermittedDeletionException {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getProgramUUID()).thenReturn(RandomStringUtils.random(10));
		Mockito.doReturn(study).when(fieldbookMiddlewareService).getStudy(DeleteStudyControllerTest.PROJECT_ID);

		final String message = "UnpermittedDeletionException thrown";
		Mockito.when(this.messageSource.getMessage(Matchers.anyString(), Matchers.any(Object[].class),
				Matchers.eq(this.locale))).thenReturn(message);
		Mockito.doThrow(UnpermittedDeletionException.class).when(this.fieldbookMiddlewareService)
				.deleteStudy(Matchers.anyInt(), Matchers.anyInt());

		final Map<String, Object> result = this.deleteStudyController
				.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 1", "0", result.get(DeleteStudyController.IS_SUCCESS));
		Assert.assertEquals("The message should be " + message, message, result.get("message"));
	}

	@Test
	public void testSubmitDeleteGenericException() throws UnpermittedDeletionException {

		final Map<String, Object> result = this.deleteStudyController
				.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 1", "0", result.get(DeleteStudyController.IS_SUCCESS));
	}

	@Test
	public void testSubmitDeleteValidationStudyTemplate() throws UnpermittedDeletionException {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getProgramUUID()).thenReturn(null);
		Mockito.doReturn(study).when(fieldbookMiddlewareService).getStudy(DeleteStudyControllerTest.PROJECT_ID);

		final String message = "Program templates cannot be deleted.";
		Mockito.when(this.messageSource.getMessage(Matchers.anyString(), ArgumentMatchers.<Object[]>isNull(),
			Matchers.eq(this.locale))).thenReturn(message);

		final Map<String, Object> result = this.deleteStudyController
			.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 0", "0", result.get(DeleteStudyController.IS_SUCCESS));
		Assert.assertEquals("Program templates cannot be deleted.", result.get("message"));
	}

	@Test
	public void testSubmitDeleteValidationNurseryTemplate() throws UnpermittedDeletionException {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getProgramUUID()).thenReturn(null);
		Mockito.when(this.fieldbookMiddlewareService.getStudy(DeleteStudyControllerTest.PROJECT_ID))
			.thenReturn(study);
		final String message = "Program templates cannot be deleted.";
		Mockito.when(this.messageSource.getMessage(Matchers.anyString(), ArgumentMatchers.<Object[]>isNull(),
			Matchers.eq(this.locale))).thenReturn(message);

		final Map<String, Object> result = this.deleteStudyController
			.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 0", "0", result.get(DeleteStudyController.IS_SUCCESS));
		Assert.assertEquals("Program templates cannot be deleted.", result.get("message"));
	}
}
