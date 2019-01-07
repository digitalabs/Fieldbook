
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Method;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.util.FieldbookProperties;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
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
	public static final Long DUMMY_PROJECT_ID = (long) 2;
	public static final String SUCCESS_KEY = "success";
	public static final String SUCCESS_STRING = "1";
	public static final String FAILURE_STRING = "-1";

	@InjectMocks
	private BreedingMethodController controller;

	private BreedingMethodController mole;

	@Before
	public void setUp() throws Exception {
		this.mole = Mockito.spy(this.controller);
		Mockito.doReturn(BreedingMethodControllerTest.DUMMY_URL).when(this.fieldbookProperties).getProgramBreedingMethodsUrl();
	}

	@Test
	public void testGetCurrentProgramID() {
		Assert.assertEquals(BreedingMethodControllerTest.DUMMY_URL, this.mole.getBreedingMethodProgramURL());
	}

	@Test
	public void testGetBreedingMethods() {
		List<Method> allMethodList = new ArrayList<>();
		List<Method> favoriteMethodList = new ArrayList<>();

		try {
			Mockito.doReturn(allMethodList).when(this.fieldbookMiddlewareService).getAllBreedingMethods(false);
			Map<String, Object> breedingMethods = this.mole.getBreedingMethods();

			Assert.assertNotNull(breedingMethods);
			Assert.assertEquals(BreedingMethodControllerTest.SUCCESS_STRING, breedingMethods.get(BreedingMethodControllerTest.SUCCESS_KEY));
			Assert.assertEquals(allMethodList, breedingMethods.get("allMethods"));
			Assert.assertEquals(favoriteMethodList, breedingMethods.get("favoriteMethods"));
		} catch (MiddlewareQueryException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetBreedingMethodsException() {
		try {
			Mockito.when(this.fieldbookMiddlewareService.getAllBreedingMethods(false)).thenThrow(MiddlewareQueryException.class);

			Map<String, Object> breedingMethods = this.mole.getBreedingMethods();

			Assert.assertNotNull(breedingMethods);
			Assert.assertEquals(BreedingMethodControllerTest.FAILURE_STRING, breedingMethods.get(BreedingMethodControllerTest.SUCCESS_KEY));

		} catch (MiddlewareQueryException e) {
			Assert.fail(e.getMessage());
		}
	}
}
