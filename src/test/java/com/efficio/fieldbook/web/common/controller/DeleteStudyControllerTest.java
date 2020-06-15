package com.efficio.fieldbook.web.common.controller;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.exceptions.UnpermittedDeletionException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
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
	private Model model;

	@Mock
	private HttpSession session;

	@InjectMocks
	private DeleteStudyController deleteStudyController;

	private Locale locale;

	@Before
	public void setUp() {
		this.locale = new Locale("en", "US");
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
	}

	@Test
	public void testSubmitDeleteWithUnpermittedDeletionException() throws UnpermittedDeletionException {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getProgramUUID()).thenReturn(RandomStringUtils.random(10));
		Mockito.doReturn(study).when(fieldbookMiddlewareService).getStudy(DeleteStudyControllerTest.PROJECT_ID);

		final String message = "UnpermittedDeletionException thrown";
		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.any(Object[].class),
				ArgumentMatchers.eq(this.locale))).thenReturn(message);
		Mockito.doThrow(UnpermittedDeletionException.class).when(this.fieldbookMiddlewareService)
				.deleteStudy(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt());

		final Map<String, Object> result = this.deleteStudyController
				.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 1", "0", result.get(DeleteStudyController.IS_SUCCESS));
		Assert.assertEquals("The message should be " + message, message, result.get("message"));
	}

	@Test
	public void testSubmitDeleteGenericException() {

		final Map<String, Object> result = this.deleteStudyController
				.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 1", "0", result.get(DeleteStudyController.IS_SUCCESS));
	}

	@Test
	public void testSubmitDeleteValidationStudyTemplate() {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getProgramUUID()).thenReturn(null);
		Mockito.doReturn(study).when(fieldbookMiddlewareService).getStudy(DeleteStudyControllerTest.PROJECT_ID);

		final String message = "Program templates cannot be deleted.";
		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.isNull(),
			ArgumentMatchers.eq(this.locale))).thenReturn(message);

		final Map<String, Object> result = this.deleteStudyController
			.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 0", "0", result.get(DeleteStudyController.IS_SUCCESS));
		Assert.assertEquals("Program templates cannot be deleted.", result.get("message"));
	}

	@Test
	public void testSubmitDeleteValidationNurseryTemplate() {
		final Study study = Mockito.mock(Study.class);
		Mockito.when(study.getProgramUUID()).thenReturn(null);
		Mockito.when(this.fieldbookMiddlewareService.getStudy(DeleteStudyControllerTest.PROJECT_ID))
			.thenReturn(study);
		final String message = "Program templates cannot be deleted.";
		Mockito.when(this.messageSource.getMessage(ArgumentMatchers.anyString(), ArgumentMatchers.isNull(),
			ArgumentMatchers.eq(this.locale))).thenReturn(message);

		final Map<String, Object> result = this.deleteStudyController
			.submitDelete(DeleteStudyControllerTest.PROJECT_ID, this.model, this.session, this.locale);
		Assert.assertEquals("The value should be 0", "0", result.get(DeleteStudyController.IS_SUCCESS));
		Assert.assertEquals("Program templates cannot be deleted.", result.get("message"));
	}
}
