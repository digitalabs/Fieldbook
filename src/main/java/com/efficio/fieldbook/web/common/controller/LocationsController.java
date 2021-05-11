package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import org.apache.commons.collections.ListUtils;
import org.generationcp.middleware.pojos.Location;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 2/2/2015 Time: 2:30 PM
 */

@Controller
@RequestMapping(LocationsController.URL)
public class LocationsController extends AbstractBaseFieldbookController {

	public static final String URL = "/locations";
	public static final String FAVORITE_LOCATIONS = "favoriteLocations";
	public static final String ALL_LOCATIONS = "allLocations";
	public static final String SUCCESS = "success";
	public static final String ALL_BREEDING_LOCATIONS = "allBreedingLocations";
	public static final String ALL_SEED_STORAGE_LOCATIONS = "allSeedStorageLocations";
	public static final String ALL_BREEDING_FAVORITES_LOCATIONS = "allBreedingFavoritesLocations";
	public static final String ALL_SEED_STORAGE_FAVORITES_LOCATIONS = "allSeedStorageFavoritesLocations";

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Override
	public String getContentName() {
		return null;
	}

	/**
	 * Gets the breeding methods.
	 *
	 * @return the breeding methods
	 */
	@ResponseBody
	@RequestMapping(value = "/getLocations", method = RequestMethod.GET)
	public Map<String, Object> getLocations() {
		final Map<String, Object> result = new HashMap<>();

		final String programUUID = this.contextUtil.getCurrentProgramUUID();

		final List<Location> faveLocations = this.fieldbookMiddlewareService
			.getFavoriteLocationByLocationIDs(this.fieldbookMiddlewareService.getFavoriteProjectLocationIds(programUUID));
		final List<Location> allLocations = this.fieldbookMiddlewareService.getLocationsByProgramUUID(programUUID);
		final List<Location> allBreedingLocations = this.fieldbookMiddlewareService.getAllBreedingLocationsByProgramUUID(programUUID);
		final List<Location> allSeedStorageLocations = this.fieldbookMiddlewareService.getAllSeedLocations();

		result.put(SUCCESS, "1");
		result.put(FAVORITE_LOCATIONS, faveLocations);
		result.put(ALL_LOCATIONS, allLocations);
		result.put(ALL_BREEDING_LOCATIONS, allBreedingLocations);
		result.put(ALL_SEED_STORAGE_LOCATIONS, allSeedStorageLocations);
		result.put(ALL_BREEDING_FAVORITES_LOCATIONS, ListUtils.intersection(allBreedingLocations, faveLocations));
		result.put(ALL_SEED_STORAGE_FAVORITES_LOCATIONS, ListUtils.intersection(allSeedStorageLocations, faveLocations));

		return result;
	}
}
