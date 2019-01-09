package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.pojos.Location;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	@Before
	public void setUp() throws Exception {
		Mockito.doReturn(LocationControllerTest.DUMMY_URL).when(this.fieldbookProperties).getProgramLocationsUrl();
		this.mole = Mockito.spy(this.controller);
	}

	@Test
	public void testGetProgramLocationsURL() {
		Assert.assertEquals(LocationControllerTest.DUMMY_URL, this.mole.getProgramLocationsURL());
	}

	@Test
	public void testGetLocations() {

		final List<Integer> locationIDs = new ArrayList<>();
		final List<Location> allLocationsList = new ArrayList<>();
		final List<Location> breedingLocationsList = new ArrayList<>();
		final List<Location> favoriteLocationsList = new ArrayList<>();

		Mockito.doReturn(allLocationsList).when(this.fieldbookMiddlewareService).getLocationsByProgramUUID(ArgumentMatchers.<String>isNull());
		Mockito.doReturn(locationIDs).when(this.fieldbookMiddlewareService).getFavoriteProjectLocationIds(ArgumentMatchers.<String>isNull());
		Mockito.doReturn(breedingLocationsList).when(this.fieldbookMiddlewareService)
				.getAllBreedingLocationsByProgramUUID(ArgumentMatchers.<String>isNull());
		Mockito.doReturn(favoriteLocationsList).when(this.fieldbookMiddlewareService).getFavoriteLocationByLocationIDs(locationIDs);
		final Map<String, Object> locations = this.mole.getLocations();

		Assert.assertNotNull(locations);
		Assert.assertEquals(LocationControllerTest.SUCCESS_STRING, locations.get(LocationControllerTest.SUCCESS_KEY));
		Assert.assertSame(breedingLocationsList, locations.get(LocationsController.ALL_BREEDING_LOCATIONS));
		Assert.assertSame(favoriteLocationsList, locations.get(LocationsController.FAVORITE_LOCATIONS));
		Assert.assertSame(allLocationsList, locations.get(LocationsController.ALL_LOCATIONS));

	}

}
