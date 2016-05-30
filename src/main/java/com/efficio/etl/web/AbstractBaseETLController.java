
package com.efficio.etl.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import com.efficio.etl.web.bean.UserSelection;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public abstract class AbstractBaseETLController {

	public abstract String getContentName();

	public abstract UserSelection getUserSelection();

	/**
	 * Base functionality for displaying the page.
	 *
	 * @param model model
	 * @return String
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

	public Map<String, Object> wrapFormResult(String redirectLocation, HttpServletRequest request) {
		Map<String, Object> results = new HashMap<>();
		results.put("success", true);
		results.put("redirectUrl", request.getContextPath() + redirectLocation);

		return results;
	}

	public Map<String, Object> wrapFormResult(List<String> errorMessages) {
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("success", false);
		results.put("messages", errorMessages);

		return results;
	}
}
