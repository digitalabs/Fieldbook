
package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.AbstractBaseFieldbookController;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.UnpermittedDeletionException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping(DeleteStudyController.URL)
public class DeleteStudyController extends AbstractBaseFieldbookController {

	public static final String IS_SUCCESS = "isSuccess";

	private static final Logger LOG = LoggerFactory.getLogger(DeleteStudyController.class);

	public static final String URL = "/StudyManager/deleteStudy";

	public static final String STUDY_DELETE_NOT_PERMITTED = "study.delete.not.permitted";

	public static final String TEMPLATE_DELETE_NOT_PERMITTED = "study.template.delete.not.permitted";

	@Resource
	private FieldbookService fieldbookMiddlewareService;

	@Resource
	private MessageSource messageSource;

	@Resource
	private GermplasmListManager germplasmListManager;

	@Override
	public String getContentName() {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/{studyId}", method = RequestMethod.POST)
	public Map<String, Object> submitDelete(@PathVariable final int studyId,
			final Model model, final HttpSession session, final Locale locale) {
		final Map<String, Object> results = new HashMap<>();
		final List<GermplasmList> germplasmLists;
		final Study study;

		try {
			// Validation to don't delete a template programs
			study = this.fieldbookMiddlewareService.getStudy(studyId);
			if (null == study.getProgramUUID()) {
				results.put(DeleteStudyController.IS_SUCCESS, "0");
				results.put("message", this.messageSource.getMessage(DeleteStudyController.TEMPLATE_DELETE_NOT_PERMITTED, null, locale));
				return results;
			}

			this.fieldbookMiddlewareService.deleteStudy(studyId, this.contextUtil.getCurrentWorkbenchUserId());
			results.put(DeleteStudyController.IS_SUCCESS, "1");

		} catch (final UnpermittedDeletionException ude) {
			DeleteStudyController.LOG.error(ude.getMessage(), ude);
			final Integer studyUserId = this.fieldbookMiddlewareService.getStudy(studyId).getUser();
			results.put(DeleteStudyController.IS_SUCCESS, "0");
			results.put("message", this.messageSource.getMessage(DeleteStudyController.STUDY_DELETE_NOT_PERMITTED,
					new String[] { this.fieldbookMiddlewareService.getOwnerListName(studyUserId) }, locale));
		} catch (final Exception e) {
			DeleteStudyController.LOG.error(e.getMessage(), e);
			results.put(DeleteStudyController.IS_SUCCESS, "0");
		}

		return results;
	}

	private void deleteGermplasmList(final List<GermplasmList> germplasmLists) {
		if (germplasmLists != null && !germplasmLists.isEmpty()) {
			this.germplasmListManager.deleteGermplasmList(germplasmLists.get(0));
		}
	}
}
