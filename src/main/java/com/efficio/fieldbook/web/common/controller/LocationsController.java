package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/2/2015
 * Time: 2:30 PM
 */

@Controller
@RequestMapping(LocationsController.URL)
public class LocationsController extends AbstractBaseFieldbookController {
	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsController.class);
		public static final String URL = "/locations";

		@Resource
		private FieldbookProperties fieldbookProperties;

		@Resource
		private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

		@Override public String getContentName() {
			return null;
		}

		@ResponseBody
		@RequestMapping(value = "/programLocationsURL", method = RequestMethod.GET)
		public String getProgramLocationsURL() {
			return fieldbookProperties.getProgramLocationsUrl();
		}

		@ResponseBody
		@RequestMapping(value = "/programID", method = RequestMethod.GET)
		public Long getCurrentProgramID(HttpServletRequest request) {
			ContextInfo contextInfo = (ContextInfo) WebUtils
							.getSessionAttribute(request,
									ContextConstants.SESSION_ATTR_CONTEXT_INFO);

			return contextInfo.getSelectedProjectId();
		}

		/**
		 * Gets the breeding methods.
		 *
		 * @return the breeding methods
		 */
		@ResponseBody
		@RequestMapping(value = "/getLocations", method = RequestMethod.GET)
		public Map<String, Object> getLocations() {
			Map<String, Object> result = new HashMap<>();

			try {
				List<Long> locationsIds = fieldbookMiddlewareService
						.getFavoriteProjectLocationIds();
				List<Location> faveLocations = fieldbookMiddlewareService
						.getFavoriteLocationByProjectId(locationsIds);
				List<Location> allBreedingLocations = fieldbookMiddlewareService
						.getAllBreedingLocations();
				List<Location> allSeedStorageLocations = fieldbookMiddlewareService
						.getAllSeedLocations();
				result.put("success", "1");
				result.put("favoriteLocations", faveLocations);
				result.put("allBreedingLocations", allBreedingLocations);
				result.put("allSeedStorageLocations", allSeedStorageLocations);
			} catch (MiddlewareQueryException e) {
				LOG.error(e.getMessage(), e);
				result.put("success", "-1");
			}

			return result;
		}
}
