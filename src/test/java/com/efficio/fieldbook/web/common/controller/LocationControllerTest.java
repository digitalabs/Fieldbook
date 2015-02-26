package com.efficio.fieldbook.web.common.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.efficio.fieldbook.web.util.FieldbookProperties;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/4/2015
 * Time: 3:07 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class LocationControllerTest {
	@Mock
	private FieldbookProperties fieldbookProperties;

	@Mock
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Mock
	private HttpServletRequest request;
	
	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private LocationsController controller;

	private LocationsController mole;

	public static final String DUMMY_URL = "myURL";
	public static final Long DUMMY_PROJECT_ID = (long) 2;
	public static final String SUCCESS_KEY = "success";
	public static final String SUCCESS_STRING = "1";
	public static final String FAILURE_STRING = "-1";

	@Before
	public void setUp() throws Exception {
		doReturn(DUMMY_URL).when(fieldbookProperties).getProgramLocationsUrl();
		mole = spy(controller);
		doReturn(DUMMY_PROJECT_ID).when(mole).getCurrentProgramID(request);
	}

	@Test
	public void testGetProgramLocationsURL() {
		assertEquals(DUMMY_URL, mole.getProgramLocationsURL());
	}

	@Test
		public void testGetLocations() {
			List<Long> locationIDs = new ArrayList<>();
			List<Location> breedingLocationsList = new ArrayList<>();
			List<Location> favoriteLocationsList = new ArrayList<>();

			try {
				doReturn(locationIDs).when(fieldbookMiddlewareService).getFavoriteProjectLocationIds(
						Mockito.anyString());
				doReturn(breedingLocationsList).when(fieldbookMiddlewareService).getAllBreedingLocations();
				doReturn(favoriteLocationsList).when(fieldbookMiddlewareService).getFavoriteLocationByProjectId(locationIDs);
				Map<String, Object> locations = mole.getLocations();

				assertNotNull(locations);
				assertEquals(SUCCESS_STRING, locations.get(SUCCESS_KEY));
				assertEquals(breedingLocationsList, locations.get("allBreedingLocations"));
				assertEquals(favoriteLocationsList, locations.get("favoriteLocations"));
			} catch (MiddlewareQueryException e) {
				fail(e.getMessage());
			}
		}

		@Test
		public void testGetLocationsException() {
			try {
				doThrow(MiddlewareQueryException.class).when(fieldbookMiddlewareService).getAllBreedingLocations();

				Map<String, Object> locations = mole.getLocations();

				assertNotNull(locations);
				assertEquals(FAILURE_STRING, locations.get(SUCCESS_KEY));

			} catch (MiddlewareQueryException e) {
				fail(e.getMessage());
			}
		}
}
