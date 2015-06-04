
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
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
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 2/4/2015 Time: 3:07 PM
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
		Mockito.doReturn(LocationControllerTest.DUMMY_URL).when(this.fieldbookProperties).getProgramLocationsUrl();
		this.mole = Mockito.spy(this.controller);
		Mockito.doReturn(LocationControllerTest.DUMMY_PROJECT_ID).when(this.mole).getCurrentProgramID(this.request);
	}

	@Test
	public void testGetProgramLocationsURL() {
		Assert.assertEquals(LocationControllerTest.DUMMY_URL, this.mole.getProgramLocationsURL());
	}

	@Test
	public void testGetLocations() {
		List<Long> locationIDs = new ArrayList<>();
		List<Location> breedingLocationsList = new ArrayList<>();
		List<Location> favoriteLocationsList = new ArrayList<>();

		try {
			Mockito.doReturn(locationIDs).when(this.fieldbookMiddlewareService).getFavoriteProjectLocationIds(Matchers.anyString());
			Mockito.doReturn(breedingLocationsList).when(this.fieldbookMiddlewareService).getAllBreedingLocations();
			Mockito.doReturn(favoriteLocationsList).when(this.fieldbookMiddlewareService).getFavoriteLocationByProjectId(locationIDs);
			Map<String, Object> locations = this.mole.getLocations();

			Assert.assertNotNull(locations);
			Assert.assertEquals(LocationControllerTest.SUCCESS_STRING, locations.get(LocationControllerTest.SUCCESS_KEY));
			Assert.assertEquals(breedingLocationsList, locations.get("allBreedingLocations"));
			Assert.assertEquals(favoriteLocationsList, locations.get("favoriteLocations"));
		} catch (MiddlewareQueryException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetLocationsException() {
		try {
			Mockito.doThrow(MiddlewareQueryException.class).when(this.fieldbookMiddlewareService).getAllBreedingLocations();

			Map<String, Object> locations = this.mole.getLocations();

			Assert.assertNotNull(locations);
			Assert.assertEquals(LocationControllerTest.FAILURE_STRING, locations.get(LocationControllerTest.SUCCESS_KEY));

		} catch (MiddlewareQueryException e) {
			Assert.fail(e.getMessage());
		}
	}
}
