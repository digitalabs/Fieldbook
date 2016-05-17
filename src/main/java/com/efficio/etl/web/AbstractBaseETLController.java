
package com.efficio.etl.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.generationcp.commons.util.SpringAppContextProvider;
import org.springframework.ui.Model;

import com.efficio.etl.web.bean.UserSelection;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public abstract class AbstractBaseETLController {

	public static final String USER_SELECTION_ID_SUFFIX = "TestUserSelection";

	public abstract String getContentName();

	public abstract UserSelection getUserSelection();

	@Resource
	private Properties configProperties;

	/**
	 * Base functionality for displaying the page.
	 *
	 * @param model
	 * @return
	 */

	public String show(Model model) {
		return this.show(model, true, null);

	}

	public String show(Model model, boolean requiresSelection) {
		return this.show(model, requiresSelection, null);
	}

	public String show(Model model, boolean requiresSelection, HttpServletRequest request) {
		if (!this.getContentName().equals("etl/fileUpload")
				&& requiresSelection
				&& (this.getUserSelection() == null || this.getUserSelection().getActualFileName() == null || this.getUserSelection()
						.getActualFileName().isEmpty())) {
			return "redirect:" + FileUploadController.URL;
		}

		if (request != null) {
			model.addAttribute("applicationBase", request.getContextPath());
		}

		return this.getContentName();
	}

	public String showTest(UserSelection userSelection, Model model) {
		UserSelection test = this.retrieveTestUserSelection();

		assert test != null;

		test.transferTo(userSelection);

		return this.show(model);
	}

	/**
	 * Retrieval of UserSelection object representing the state of the current page is first attempted from the Spring context configuration
	 * file. If no such object exists in the configuration file, it assumes that the developer has overridden the base
	 * constructTestUserSelection method
	 *
	 * @return
	 */
	public UserSelection retrieveTestUserSelection() {
		UserSelection returnValue = this.constructTestUserSelection();

		if (returnValue == null) {
			Object obj =
					SpringAppContextProvider.getApplicationContext().getBean(
							this.getContentName() + AbstractBaseETLController.USER_SELECTION_ID_SUFFIX);

			if (obj != null) {
				returnValue = (UserSelection) obj;
			}
		}

		return returnValue;
	}

	public UserSelection constructTestUserSelection() {
		return null;
	}

	public Map<String, Object> wrapFormResult(String redirectLocation, HttpServletRequest request) {
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("success", true);
		results.put("redirectUrl", request.getContextPath() + redirectLocation);

		return results;
	}

	public Map<String, Object> wrapFormResult(String message) {
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("success", false);
		results.put("message", message);

		return results;
	}

	public Map<String, Object> wrapFormResult(List<String> errorMessages) {
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("success", false);
		results.put("messages", errorMessages);

		return results;
	}

	public Map<String, Object> wrapPrimitive(Object object) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("value", object);
		result.put("success", object);
		return result;
	}
}
