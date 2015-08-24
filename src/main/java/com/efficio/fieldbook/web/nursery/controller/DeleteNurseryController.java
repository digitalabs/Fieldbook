
package com.efficio.fieldbook.web.nursery.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.UnpermittedDeletionException;
import org.generationcp.middleware.service.api.FieldbookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;

@Controller
@RequestMapping(DeleteNurseryController.URL)
public class DeleteNurseryController extends AbstractBaseFieldbookController {

	private static final Logger LOG = LoggerFactory.getLogger(DeleteNurseryController.class);

	public static final String URL = "/NurseryManager/deleteNursery";

	public static final String STUDY_DELETE_NOT_PERMITTED = "study.delete.not.permitted";

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private MessageSource messageSource;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/{studyId}", method = RequestMethod.POST)
	public Map<String, Object> submitDelete(@PathVariable int studyId, Model model, HttpSession session,
			Locale locale) throws MiddlewareQueryException {
		Map<String, Object> results = new HashMap<String, Object>();

		try {
			this.fieldbookMiddlewareService.deleteStudy(studyId, this.contextUtil.getCurrentUserLocalId());
			results.put("isSuccess", "1");

		} catch (UnpermittedDeletionException ude) {
			Integer studyUserId = this.fieldbookMiddlewareService.getStudy(studyId).getUser();
			results.put("isSuccess", "0");
			results.put("message", this.messageSource.getMessage(DeleteNurseryController.STUDY_DELETE_NOT_PERMITTED,
					new String[] {this.fieldbookMiddlewareService.getOwnerListName(studyUserId)}, locale));
		} catch (Exception e) {
			DeleteNurseryController.LOG.error(e.getMessage(), e);
			results.put("isSuccess", "0");
		}

		return results;
	}
}
