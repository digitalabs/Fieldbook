
package com.efficio.fieldbook.web.common.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.context.ContextConstants;
import org.generationcp.commons.context.ContextInfo;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import com.efficio.fieldbook.web.util.FieldbookProperties;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@Controller
@RequestMapping(BreedingMethodController.URL)
public class BreedingMethodController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(CrossingSettingsController.class);
	public static final String URL = "/breedingMethod";

	@Resource
	private FieldbookProperties fieldbookProperties;

	@Resource
	private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Resource
	private ContextUtil contextUtil;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/programMethodURL", method = RequestMethod.GET)
	public String getBreedingMethodProgramURL() {
		return this.fieldbookProperties.getProgramBreedingMethodsUrl();
	}

	@ResponseBody
	@RequestMapping(value = "/programID", method = RequestMethod.GET)
	public Long getCurrentProgramID(HttpServletRequest request) {
		ContextInfo contextInfo = (ContextInfo) WebUtils.getSessionAttribute(request, ContextConstants.SESSION_ATTR_CONTEXT_INFO);

		return contextInfo.getSelectedProjectId();
	}

	/**
	 * Gets the breeding methods.
	 *
	 * @return the breeding methods
	 */
	@ResponseBody
	@RequestMapping(value = "/getBreedingMethods", method = RequestMethod.GET)
	public Map<String, Object> getBreedingMethods() {
		Map<String, Object> result = new HashMap<>();

		try {
			List<Method> breedingMethods = this.fieldbookMiddlewareService.getAllBreedingMethods(false);
			List<Integer> methodIds = this.fieldbookMiddlewareService.getFavoriteProjectMethods(this.contextUtil.getCurrentProgramUUID());
			List<Method> favoriteMethods = this.fieldbookMiddlewareService.getFavoriteBreedingMethods(methodIds, false);
			List<Method> allNonGenerativeMethods = this.fieldbookMiddlewareService.getAllBreedingMethods(true);

			result.put("success", "1");
			result.put("allMethods", breedingMethods);
			result.put("favoriteMethods", favoriteMethods);
			result.put("allNonGenerativeMethods", allNonGenerativeMethods);
			result.put("favoriteNonGenerativeMethods", favoriteMethods);
		} catch (MiddlewareQueryException e) {
			BreedingMethodController.LOG.error(e.getMessage(), e);
			result.put("success", "-1");
			result.put("errorMessage", e.getMessage());
		}

		return result;
	}
}
