package com.efficio.fieldbook.web.nursery.controller;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;

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
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class DeleteNurseryControllerTest {
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
	private DeleteNurseryController deleteNurseryController;
	
	private GermplasmListTestDataInitializer germplasmListTestDataInitializer;
	
	private Locale locale;
	
	@Before
	public void setUp() {
		locale = new Locale("en", "US");;
		this.germplasmListTestDataInitializer = new GermplasmListTestDataInitializer();
		GermplasmList nurseryList = this.germplasmListTestDataInitializer.createGermplasmListWithType(1, GermplasmListType.NURSERY.name());
		GermplasmList trialList = this.germplasmListTestDataInitializer.createGermplasmListWithType(1, GermplasmListType.TRIAL.name());
		
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(PROJECT_ID, GermplasmListType.NURSERY)).thenReturn(Arrays.asList(nurseryList));
		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListsByProjectId(PROJECT_ID, GermplasmListType.TRIAL)).thenReturn(Arrays.asList(trialList));
	}
	
	@Test
	public void testSubmitDeleteNursery() throws UnpermittedDeletionException {
		Map<String, Object> result = this.deleteNurseryController.submitDelete(PROJECT_ID, "N", model, session, locale);
		Assert.assertEquals("The value should be 1", "1", result.get(DeleteNurseryController.IS_SUCCESS));
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).deleteStudy(PROJECT_ID, this.contextUtil.getCurrentUserLocalId());
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getGermplasmListsByProjectId(PROJECT_ID, GermplasmListType.NURSERY);
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getGermplasmListsByProjectId(PROJECT_ID, GermplasmListType.CHECK);
		Mockito.verify(this.germplasmListManager, Mockito.times(1)).deleteGermplasmList(Matchers.any(GermplasmList.class));
	}
	
	@Test
	public void testSubmitDeleteTrial() throws UnpermittedDeletionException {
		Map<String, Object> result = this.deleteNurseryController.submitDelete(PROJECT_ID, "T", model, session, locale);
		Assert.assertEquals("The value should be 1", "1", result.get(DeleteNurseryController.IS_SUCCESS));
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).deleteStudy(PROJECT_ID, this.contextUtil.getCurrentUserLocalId());
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getGermplasmListsByProjectId(PROJECT_ID, GermplasmListType.TRIAL);
		Mockito.verify(this.germplasmListManager, Mockito.times(1)).deleteGermplasmList(Matchers.any(GermplasmList.class));
	}
	
	@Test
	public void testSubmitDeleteWithUnpermittedDeletionException() throws UnpermittedDeletionException {
		Study study = Mockito.mock(Study.class);
		Mockito.when(study.getUser()).thenReturn(1);
		Mockito.when(this.fieldbookMiddlewareService.getStudy(PROJECT_ID)).thenReturn(study);
		Mockito.when(this.fieldbookMiddlewareService.getOwnerListName(1)).thenReturn("User Name");
		
		final String message = "UnpermittedDeletionException thrown";
		Mockito.when(this.messageSource.getMessage(Matchers.anyString(),Matchers.any(Object[].class), Matchers.eq(locale))).thenReturn(message);
		Mockito.doThrow(UnpermittedDeletionException.class).when(this.fieldbookMiddlewareService).deleteStudy(Matchers.anyInt(), Matchers.anyInt());
		
		Map<String, Object> result = this.deleteNurseryController.submitDelete(PROJECT_ID, "T", model, session, locale);
		Assert.assertEquals("The value should be 1", "0", result.get(DeleteNurseryController.IS_SUCCESS));
		Assert.assertEquals("The message should be " + message, message, result.get("message"));
	}
	
	@Test
	public void testSubmitDeleteGenericException() throws UnpermittedDeletionException {
		Mockito.doThrow(Exception.class).when(this.fieldbookMiddlewareService).deleteStudy(Matchers.anyInt(), Matchers.anyInt());
		
		Map<String, Object> result = this.deleteNurseryController.submitDelete(PROJECT_ID, "T", model, session, locale);
		Assert.assertEquals("The value should be 1", "0", result.get(DeleteNurseryController.IS_SUCCESS));
	}
}
