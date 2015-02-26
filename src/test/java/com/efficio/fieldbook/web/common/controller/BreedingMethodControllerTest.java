package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.util.FieldbookProperties;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */

@RunWith(MockitoJUnitRunner.class)
public class BreedingMethodControllerTest {

	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private HttpServletRequest request;
	
	@Mock
	private ContextUtil contextUtil;

	public static final String DUMMY_URL = "myURL";
	public static final Long DUMMY_PROJECT_ID = (long)2;
	public static final String SUCCESS_KEY = "success";
	public static final String SUCCESS_STRING = "1";
	public static final String FAILURE_STRING = "-1";


	@InjectMocks
	private BreedingMethodController controller;

	private BreedingMethodController mole;

	@Before
	public void setUp() throws Exception {
		mole = spy(controller);
		doReturn(DUMMY_URL).when(fieldbookProperties).getProgramBreedingMethodsUrl();
		doReturn(DUMMY_PROJECT_ID).when(mole).getCurrentProgramID(request);
	}

	@Test
	public void testGetCurrentProgramID() {
		assertEquals(DUMMY_URL, mole.getBreedingMethodProgramURL());
	}

	@Test
	public void testGetBreedingMethods() {
		List<Method> allMethodList = new ArrayList<>();
		List<Method> favoriteMethodList = new ArrayList<>();

		try {
			doReturn(allMethodList).when(fieldbookMiddlewareService).getAllBreedingMethods(false);
			doReturn(favoriteMethodList).when(fieldbookMiddlewareService).getFavoriteProjectMethods(Mockito.anyString());
			Map<String, Object> breedingMethods = mole.getBreedingMethods();

			assertNotNull(breedingMethods);
			assertEquals(SUCCESS_STRING, breedingMethods.get(SUCCESS_KEY));
			assertEquals(allMethodList, breedingMethods.get("allMethods"));
			assertEquals(favoriteMethodList, breedingMethods.get("favoriteMethods"));
		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetBreedingMethodsException() {
		try {
			when(fieldbookMiddlewareService.getAllBreedingMethods(false)).thenThrow(MiddlewareQueryException.class);

			Map<String, Object> breedingMethods = mole.getBreedingMethods();

			assertNotNull(breedingMethods);
			assertEquals(FAILURE_STRING, breedingMethods.get(SUCCESS_KEY));

		} catch (MiddlewareQueryException e) {
			fail(e.getMessage());
		}
	}
}
